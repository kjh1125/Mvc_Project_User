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
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin("*")
@PropertySource("classpath:application.properties")
public class NaverLoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private Environment env;

    @GetMapping("/naverLoginRequest")
    @ResponseBody
    public Map<String, String> naverLoginRequest(HttpServletRequest request) {
        String state = loginService.generateCSRFToken(); // 🔹 CSRF 방어를 위한 state 값 생성
        request.getSession().setAttribute("oauthState", state); // 🔹 세션에 state 저장

        // 🔹 Naver OAuth2 인증 요청 URL
        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + env.getProperty("naver.client.id")
                + "&redirect_uri=" + env.getProperty("redirectURI") + "naverLogin"
                + "&state=" + state; // 🔹 CSRF 보호용 state 값 추가

        Map<String, String> response = new HashMap<>();
        response.put("url", naverAuthUrl);
        return response;
    }


    @RequestMapping(value = "/naverLogin", method = RequestMethod.GET)
    public String loginNaver(@RequestParam(value = "code") String code, @RequestParam(value = "state", required = false) String state, RedirectAttributes redirectAttributes, HttpServletResponse response, HttpServletRequest request) {

        String accessToken = getAccessToken(code, state);
        // ✅ 1️⃣ 세션에서 `state` 값 가져오기
        String sessionState = (String) request.getSession().getAttribute("oauthState");
        // ✅ 2️⃣ `state` 값이 일치하지 않으면 로그인 차단 (CSRF 공격 방어)
        if (sessionState == null || !sessionState.equals(state)) {
            return "Error: Invalid OAuth state. Possible CSRF attack.";
        }

        if (accessToken != null) {
            return getNaverUserInfo(accessToken,redirectAttributes,response,request);
        }
        return "Error: Unable to retrieve access token.";
    }
    private String getAccessToken(String code, String state) {
        String accessToken = null;
        try {
            String redirectURI = "http://localhost:8080/naverLogin"; // Callback URL
//            String redirectURI = "https://blindtime.kro.kr/naverLogin"; // Callback URL
            String apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code"
                    + "&client_id=" + env.getProperty("naver.client.id")
                    + "&client_secret=" + env.getProperty("naver.client.pw")
                    + "&redirect_uri=" + env.getProperty("redirectURI")+"naverLogin"
                    + "&code=" + code
                    + "&state=" + state;

            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();

            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuilder res = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                res.append(inputLine);
            }
            br.close();

            // JSON 파싱 후 access_token 추출
            JSONObject jsonObject = new JSONObject(res.toString());
            accessToken = jsonObject.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accessToken;
    }

    private String getNaverUserInfo(String accessToken, RedirectAttributes redirectAttributes, HttpServletResponse response, HttpServletRequest request) {
        Integer userId = null;
        String naverId = null;
        try {
            String header = "Bearer " + accessToken;
            String apiURL_userInfo = "https://openapi.naver.com/v1/nid/me";
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

            // JSON 파싱 후 사용자 정보 추출
            JSONObject userInfoJson = new JSONObject(res_userInfo.toString());
            naverId = userInfoJson.getJSONObject("response").getString("id"); // 사용자 ID (이메일도 가능)
            userId= userService.naverLogin(naverId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (userId != null) {

            loginService.processLogin(userId,request,response);

            return "redirect:/main";
        } else {
            request.getSession().setAttribute("naverId", naverId);
            loginService.processRegister(request,response);
            return "redirect:/user/register";
        }
    }
}
