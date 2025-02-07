package kr.bit.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
@CrossOrigin("*")
@PropertySource("classpath:application.properties")
public class GoogleLoginController {

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.pw}")
    private String googleClientPw;

    @GetMapping(value = "/googleLogin")
    public String loginGoogle(@RequestParam(value = "code") String authCode) {
        String jwtToken = getGoogleAccessToken(authCode);
        if (jwtToken != null) {
            return getGoogleUserInfo(jwtToken);
        }
        return "Error: Unable to retrieve access token.";
    }

    private String getGoogleAccessToken(String authCode) {
        String accessToken = null;
        try {
            String redirectURI = "http://localhost:8080/testtest/googleLogin"; // Callback URL
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

    private String getGoogleUserInfo(String jwtToken) {
        String userInfo = null;
        try {
            String apiURL = "https://oauth2.googleapis.com/tokeninfo?id_token=" + jwtToken;
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
            String email = jsonResponse.getString("email");
            userInfo = email; // 이메일 정보 리턴
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userInfo;
    }
}
