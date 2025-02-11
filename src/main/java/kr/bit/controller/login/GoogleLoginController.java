package kr.bit.controller.login;

import kr.bit.security.JwtGenerator;
import kr.bit.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Controller
@CrossOrigin("*")
@PropertySource("classpath:application.properties")
public class GoogleLoginController {

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private UserService userService;

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.pw}")
    private String googleClientPw;

    @GetMapping(value = "/googleLogin")
    public String loginGoogle(@RequestParam(value = "code") String authCode, RedirectAttributes redirectAttributes) {
        String token = getGoogleAccessToken(authCode);
        if (token != null) {
            return getGoogleUserInfo(token, redirectAttributes);  // 수정된 부분
        }
        return "Error: Unable to retrieve access token.";
    }

    private String getGoogleAccessToken(String authCode) {
        String accessToken = null;
        try {
            String redirectURI = "http://localhost:8080/blindtime/googleLogin"; // Callback URL
            String apiURL = "https://oauth2.googleapis.com/token";

            // 요청 파라미터 구성
            String data = "grant_type=authorization_code"
                    + "&client_id=" + googleClientId
                    + "&client_secret=" + googleClientPw
                    + "&code=" + authCode
                    + "&redirect_uri=" + redirectURI;

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
            accessToken = jsonResponse.getString("id_token");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accessToken;
    }

    private String getGoogleUserInfo(String token, RedirectAttributes redirectAttributes) {
        String userId = null;
        String googleId = null;
        try {
            String apiURL = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

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

            // JSON 파싱 후 사용자 정보 추출
            JSONObject jsonResponse = new JSONObject(response.toString());
            googleId = jsonResponse.getString("email");
            userId = userService.googleLogin(googleId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (userId != null) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(userId,null);

            // JWT 토큰 생성
            String jwtToken = jwtGenerator.generateToken(authentication);  // JWT 토큰 발급

            // JWT 토큰을 세션이나 클라이언트로 전달 (여기서는 세션에 저장)
// JWT 토큰을 URL 파라미터로 전달
            redirectAttributes.addAttribute("token", jwtToken);
            return "redirect:/save";

        }
        else{
            redirectAttributes.addAttribute("googleId", googleId);
            return "redirect:/user/register";
        }
    }
}
