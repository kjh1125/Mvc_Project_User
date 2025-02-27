package kr.bit.controller.login;

import kr.bit.service.LoginService;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
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

@Controller
@CrossOrigin("*")
@PropertySource("classpath:application.properties")
public class KakaoLoginController {

    @Autowired
    private UserService userService;

    @Value("${kakao.client.id}")
    private String kakaoClientId;

    @Autowired
    private LoginService loginService;

    @RequestMapping(value = "/kakaoLogin", method = RequestMethod.GET)
    public String loginKakao(@RequestParam(value = "code") String code,RedirectAttributes redirectAttributes, HttpServletResponse response, HttpServletRequest request) {
        String accessToken = getAccessToken(code);
        if (accessToken != null) {
            return getKakaoUserInfo(accessToken,redirectAttributes,response,request);
        }
        return "Error: Unable to retrieve access token.";
    }

    private String getAccessToken(String code) {
        String accessToken = null;
        try {
//            String redirectURI = "http://localhost:8080/kakaoLogin"; // Callback URL
            String redirectURI = "https://blindtime.kro.kr/kakaoLogin"; // Callback URL
            String apiURL = "https://kauth.kakao.com/oauth/token?"
                    + "grant_type=authorization_code"
                    + "&client_id=" + kakaoClientId
                    + "&redirect_uri=" + redirectURI
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
        Long kakaoId = null;
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
            kakaoId = userInfoJson.getLong("id");
            userId = userService.kakaoLogin(kakaoId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (userId != null) {

            loginService.processLogin(userId,request,response);

            return "redirect:/main";
        } else {
            redirectAttributes.addAttribute("kakaoId", kakaoId);
            return "redirect:/user/register";
        }
    }
}
