package kr.bit.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@RestController
@CrossOrigin("*")
@PropertySource("classpath:application.properties")
public class NaverLoginController {

    @Value("${naver.client.id}")
    private String naverClientId;

    @Value("${naver.client.pw}")
    private String naverClientSecret;

    @RequestMapping(value = "/naverLogin", method = RequestMethod.GET)
    public String loginNaver(@RequestParam(value = "code") String code, @RequestParam(value = "state") String state) {
        String accessToken = getAccessToken(code, state);
        if (accessToken != null) {
            return getNaverUserInfo(accessToken);
        }
        return "Error: Unable to retrieve access token.";
    }

    private String getAccessToken(String code, String state) {
        String accessToken = null;
        try {
            String redirectURI = "http://localhost:8080/testtest/naverLogin"; // Callback URL
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

    private String getNaverUserInfo(String accessToken) {
        String userInfo = null;
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
            String id = userInfoJson.getJSONObject("response").getString("id"); // 사용자 ID (이메일도 가능)
            userInfo = id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userInfo;
    }
}
