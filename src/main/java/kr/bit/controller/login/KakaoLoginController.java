package kr.bit.controller.login;

import kr.bit.service.LoginService;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin("*")
@PropertySource("classpath:application.properties")
public class KakaoLoginController {

    @Autowired
    private UserService userService;

    @Value("${kakao.client.id}")
    private String kakaoClientId;


    @Autowired
    private Environment env;

    @Autowired
    private LoginService loginService;

    @GetMapping("/kakaoLoginRequest")
    @ResponseBody
    public Map<String, String> kakaoLoginRequest(HttpServletRequest request) {
        String state = loginService.generateCSRFToken(); // 🔹 CSRF 방어를 위한 state 값 생성
        request.getSession().setAttribute("oauthState", state); // 🔹 세션에 state 저장

        // 🔹 Kakao OAuth2 인증 요청 URL
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + env.getProperty("kakao.client.id")
                + "&redirect_uri=" + env.getProperty("redirectURI")+"kakaoLogin"
                + "&response_type=code"
                + "&state=" + state; // 🔹 CSRF 보호용 state 값 추가

        Map<String, String> response = new HashMap<>();
        response.put("url", kakaoAuthUrl);
        return response;
    }

    @RequestMapping(value = "/kakaoLogin", method = RequestMethod.GET)
    public String loginKakao(
            @RequestParam(value = "code") String code,
            @RequestParam(value = "state", required = false) String state, // ✅ state 값 추가
            RedirectAttributes redirectAttributes,
            HttpServletResponse response,
            HttpServletRequest request) {

        // ✅ 1️⃣ 세션에서 `state` 값 가져오기
        String sessionState = (String) request.getSession().getAttribute("oauthState");

        // ✅ 2️⃣ `state` 값이 일치하지 않으면 로그인 차단 (CSRF 공격 방어)
        if (sessionState == null || !sessionState.equals(state)) {
            return "Error: Invalid OAuth state. Possible CSRF attack.";
        }

        // ✅ 3️⃣ `state` 값 검증 후, 세션에서 제거 (재사용 방지)
        request.getSession().removeAttribute("oauthState");

        // ✅ 4️⃣ Kakao Access Token 요청
        String accessToken = getAccessToken(code);
        if (accessToken != null) {
            return getKakaoUserInfo(accessToken, redirectAttributes, response, request);
        }
        return "Error: Unable to retrieve access token.";
    }

    private String getAccessToken(String code) {
        String accessToken = null;
        try {
            String apiURL = "https://kauth.kakao.com/oauth/token?"
                    + "grant_type=authorization_code"
                    + "&client_id=" + env.getProperty("kakao.client.id")
                    + "&redirect_uri=" + env.getProperty("redirectURI")+"kakaoLogin"
                    + "&code=" + code;

            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuilder res = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                res.append(inputLine);
            }
            br.close();

            // JSON 파싱 후 access token 추출
            JSONObject jsonObject = new JSONObject(res.toString());
            accessToken = jsonObject.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accessToken;
    }

    private String getKakaoUserInfo(String accessToken, RedirectAttributes redirectAttributes, HttpServletResponse response, HttpServletRequest request) {
        Integer userId = null;
        String kakaoId = null;
        try {
            String header = "Bearer " + accessToken;
            String apiURL_userInfo = "https://kapi.kakao.com/v2/user/me";
            URL url_userInfo = new URL(apiURL_userInfo);
            HttpURLConnection con_userInfo = (HttpURLConnection) url_userInfo.openConnection();
            con_userInfo.setRequestMethod("GET");
            con_userInfo.setRequestProperty("Authorization", header);
            int responseCode_userInfo = con_userInfo.getResponseCode();

            BufferedReader br_userInfo;
            if (responseCode_userInfo == 200) {
                br_userInfo = new BufferedReader(new InputStreamReader(con_userInfo.getInputStream()));
            } else {
                br_userInfo = new BufferedReader(new InputStreamReader(con_userInfo.getErrorStream()));
            }

            String inputLine;
            StringBuilder res_userInfo = new StringBuilder();
            while ((inputLine = br_userInfo.readLine()) != null) {
                res_userInfo.append(inputLine);
            }
            br_userInfo.close();

            // 사용자 정보 JSON 파싱
            JSONObject userInfoJson = new JSONObject(res_userInfo.toString());
            kakaoId =  String.valueOf(userInfoJson.getLong("id"));
            System.out.println(kakaoId);
            userId = userService.kakaoLogin(kakaoId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (userId != null) {

            loginService.processLogin(userId,request,response);

            return "redirect:/main";
        } else {
            request.getSession().setAttribute("kakaoId", kakaoId);
            loginService.processRegister(request,response);
            return "redirect:/user/register";
        }
    }
}
