package kr.bit.controller.login;

import kr.bit.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
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

@Controller
@CrossOrigin("*")
@PropertySource("classpath:application.properties")
public class GoogleLoginController {

    @Autowired
    private UserService userService;

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.pw}")
    private String googleClientPw;

    @GetMapping("/googleLogin")
    public String loginGoogle(@RequestParam(value = "code") String authCode, RedirectAttributes redirectAttributes, HttpServletResponse response, HttpServletRequest request) {
        String token = getGoogleAccessToken(authCode);
        if (token != null) {
            return getGoogleUserInfo(token, redirectAttributes, response, request);  // 수정된 부분
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

    private String getGoogleUserInfo(String token, RedirectAttributes redirectAttributes, HttpServletResponse response, HttpServletRequest request) {
        Integer userId;
        String googleId;
        BufferedReader br = null;
        try {
            String apiURL = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
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
            // 인증 정보를 SecurityContext에 설정
            Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            HttpSession session = request.getSession();
            session.setAttribute("user", userId);  // 세션에 사용자 ID 저장
            // 세션 ID를 쿠키로 설정
            Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());  // 세션 ID를 쿠키에 담습니다
            sessionCookie.setPath("/");  // 전체 사이트에 대해 쿠키가 유효하도록 설정
            sessionCookie.setHttpOnly(true);  // 클라이언트 JS에서 쿠키 접근 불가
            sessionCookie.setSecure(true);  // HTTPS에서만 쿠키 전송
            sessionCookie.setMaxAge(60 * 60);  // 쿠키의 유효기간 설정 (예: 1시간)
            response.addCookie(sessionCookie);  // 응답으로 쿠키를 클라이언트에 추가

            return "redirect:/main";
        } else {
            redirectAttributes.addAttribute("googleId", googleId);
            return "redirect:/user/register";
        }
    }
}
