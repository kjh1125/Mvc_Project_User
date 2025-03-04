package kr.bit.controller.login;

import kr.bit.service.LoginService;
import kr.bit.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin("*")
@PropertySource("classpath:application.properties")
public class GoogleLoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private Environment env;



    @GetMapping("/googleLoginRequest")
    @ResponseBody
    public Map<String, String> googleLoginRequest(HttpServletRequest request) {
        String state = loginService.generateCSRFToken();
        request.getSession().setAttribute("oauthState", state); // 세션에 저장

        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + env.getProperty("google.client.id")
                + "&redirect_uri=" + env.getProperty("redirectURI")+"googleLogin"
                + "&response_type=code"
                + "&scope=email%20profile%20openid"
                + "&access_type=offline"
                + "&state=" + state; // CSRF 보호용 state 값 추가

        Map<String, String> response = new HashMap<>();
        response.put("url", googleAuthUrl);
        return response;
    }

    @GetMapping("/googleLogin")
    public String loginGoogle(
            @RequestParam(value = "code") String authCode,
            @RequestParam(value = "state", required = false) String state,
            RedirectAttributes redirectAttributes,
            HttpServletResponse response,
            HttpServletRequest request) {
        // 1️⃣ 세션에서 `state` 값 가져오기
        String sessionState = (String) request.getSession().getAttribute("oauthState");

        // 2️⃣ `state` 값이 일치하지 않으면 로그인 차단 (CSRF 공격 방어)
        if (sessionState == null || !sessionState.equals(state)) {
            return "Error: Invalid OAuth state. Possible CSRF attack.";
        }

        // 3️⃣ `state` 값 검증 후, 세션에서 제거 (재사용 방지)
        request.getSession().removeAttribute("oauthState");

        // 4️⃣ Google Access Token 요청
        String token = getGoogleAccessToken(authCode);
        if (token != null) {
            return getGoogleUserInfo(token, redirectAttributes, response, request);
        }
        return "Error: Unable to retrieve access token.";
    }


    private String getGoogleAccessToken(String authCode) {
        String accessToken = null;
        try {
            String apiURL = "https://oauth2.googleapis.com/token";

            // 요청 파라미터 구성
            String data = "grant_type=authorization_code"
                    + "&client_id=" + env.getProperty("google.client.id")
                    + "&client_secret=" + env.getProperty("google.client.pw")
                    + "&code=" + authCode
                    + "&redirect_uri=" + env.getProperty("redirectURI")+"googleLogin";

            // URL 연결 설정
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);  // POST 본문에 데이터를 포함시킬 수 있도록 설정
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // Content-Type 설정
            con.setRequestProperty("Content-Length", String.valueOf(data.length())); // Content-Length 설정

            // POST 데이터를 전송
            con.getOutputStream().write(data.getBytes("UTF-8"));

            // 응답 받기
            BufferedReader br;
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            // JSON 파싱 후 id_token 추출
            JSONObject jsonResponse = new JSONObject(response.toString());
            accessToken = jsonResponse.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accessToken;
    }

    private String getGoogleUserInfo(String token, RedirectAttributes redirectAttributes, HttpServletResponse response, HttpServletRequest request) {
        Integer userId;
        String googleId;
        BufferedReader br = null;
        try {
            String apiURL = "https://oauth2.googleapis.com/tokeninfo?access_token=" + token;
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            // 응답 받기
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuilder responseContent = new StringBuilder();  // 변수명 변경
            while ((inputLine = br.readLine()) != null) {
                responseContent.append(inputLine);
            }

            // 응답을 읽은 후 BufferedReader 닫기
            br.close();

            // JSON 파싱 후 사용자 정보 추출
            JSONObject jsonResponse = new JSONObject(responseContent.toString());
            googleId = jsonResponse.getString("email");
            userId = userService.googleLogin(googleId);
        } catch (Exception e) {
            e.printStackTrace();  // 예외 로그 출력
            return "Error: Unable to process Google user information.";
        } finally {
            // 리소스 닫기
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();  // 예외 로그 출력
                }
            }
        }

        if (userId != null) {

            loginService.processLogin(userId,request,response);

            return "redirect:/main";
        } else {
            request.getSession().setAttribute("googleId", googleId);
            loginService.processRegister(request,response);
            return "redirect:/user/register";
        }
    }
}
