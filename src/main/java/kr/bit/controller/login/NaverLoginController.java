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
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Controller
@CrossOrigin("*")
@PropertySource("classpath:application.properties")
public class NaverLoginController {

    @Autowired
    private UserService userService;

    @Value("${naver.client.id}")
    private String naverClientId;

    @Value("${naver.client.pw}")
    private String naverClientSecret;

    @RequestMapping(value = "/naverLogin", method = RequestMethod.GET)
    public String loginNaver(@RequestParam(value = "code") String code, @RequestParam(value = "state") String state, RedirectAttributes redirectAttributes, HttpServletResponse response, HttpServletRequest request) {
        String accessToken = getAccessToken(code, state);
        if (accessToken != null) {
            return getNaverUserInfo(accessToken,redirectAttributes,response,request);
        }
        return "Error: Unable to retrieve access token.";
    }

    private String getAccessToken(String code, String state) {
        String accessToken = null;
        try {
//            String redirectURI = "http://localhost:8080/naverLogin"; // Callback URL
            String redirectURI = "https://blindtime.kro.kr/naverLogin"; // Callback URL
            String apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code"
                    + "&client_id=" + naverClientId
                    + "&client_secret=" + naverClientSecret
                    + "&redirect_uri=" + redirectURI
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
            // 인증 정보를 SecurityContext에 설정
            Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String gender = userService.getGender(userId);
            HttpSession session = request.getSession();
            session.setAttribute("user", userId);
            session.setAttribute("gender", gender);

            // 세션 ID를 쿠키로 설정
            Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());  // 세션 ID를 쿠키에 담습니다
            sessionCookie.setPath("/");  // 전체 사이트에 대해 쿠키가 유효하도록 설정
            sessionCookie.setHttpOnly(true);  // 클라이언트 JS에서 쿠키 접근 불가
            sessionCookie.setSecure(true);  // HTTPS에서만 쿠키 전송
            sessionCookie.setMaxAge(60 * 60);  // 쿠키의 유효기간 설정 (예: 1시간)
            response.addCookie(sessionCookie);  // 응답으로 쿠키를 클라이언트에 추가

            return "redirect:/main";
        } else {
            redirectAttributes.addAttribute("naverId", naverId);
            return "redirect:/user/register";
        }
    }
}
