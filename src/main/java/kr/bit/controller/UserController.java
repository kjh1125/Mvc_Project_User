package kr.bit.controller;

import kr.bit.entity.Hobby;
import kr.bit.entity.Message;
import kr.bit.entity.User;
import kr.bit.bean.UserJoinBean;
import kr.bit.entity.UserProfile;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

    @Value("${upload.path}")
    private String uploadDir; // 이미지 저장 경로를 application.properties로부터 가져옵니다.

    @Autowired
    private UserService userService;

    @GetMapping("register")
    public String register(
            @RequestParam(value = "googleId", required = false) String googleId,
            @RequestParam(value = "kakaoId", required = false) String kakaoId,
            @RequestParam(value = "naverId", required = false) String naverId,
            Model model) {
        List<Hobby> hobbyList = userService.getHobby();
        model.addAttribute("hobbyList", hobbyList);
        model.addAttribute("googleId", googleId);
        model.addAttribute("kakaoId", kakaoId);
        model.addAttribute("naverId", naverId);
        
        User user = new User();
        user.setGoogleId(googleId);
        user.setKakaoId(kakaoId);
        user.setNaverId(naverId);
        UserJoinBean userJoinBean = new UserJoinBean();
        userJoinBean.setUser(user);
        userJoinBean.setUserProfile(new UserProfile());
        userJoinBean.setHobbies("");
        model.addAttribute("userJoinBean", userJoinBean);

        return "user/register";
    }

    @PostMapping("register_pro")
    public String register_pro(@ModelAttribute("userJoinBean") UserJoinBean userJoinBean,
                               @RequestParam("imgFile") MultipartFile file, RedirectAttributes redirectAttributes,
                               HttpSession session, HttpServletResponse response) {
        User user = userJoinBean.getUser();
        UserProfile userProfile = userJoinBean.getUserProfile();
        String hobbies = userJoinBean.getHobbies();
        if (user.getGoogleId() != null && user.getGoogleId().isEmpty()) {
            user.setGoogleId(null);
        }
        if (user.getKakaoId() != null && user.getKakaoId().isEmpty()) {
            user.setKakaoId(null);
        }
        if (user.getNaverId() != null && user.getNaverId().isEmpty()) {
            user.setNaverId(null);
        }

        try {
            // 디렉토리 경로 생성
            Path uploadPath = Paths.get(uploadDir, "user_photos");

            // 디렉토리가 존재하지 않으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일 저장 경로 설정 (예: /path/to/upload/directory)
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir, "user_photos", fileName);

            // 파일을 지정된 디렉토리에 저장
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // 파일 이름을 userProfile에 저장
            userProfile.setPhotoImageUrl(fileName);  // 업로드된 파일의 이름을 photoImageUrl로 저장

        } catch (IOException e) {
            return "redirect:/user/register";  // 업로드 실패 시 리디렉션
        }

        // 사용자와 프로필, 취미 정보를 DB에 저장
        userService.registerUserWithHobbies(user, userProfile, hobbies);

        // 인증 정보를 SecurityContext에 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUserId(), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 세션에 사용자 ID 저장
        session.setAttribute("user", user.getUserId());
        session.setAttribute("gender",userProfile.getGender());

        // 세션 ID를 쿠키로 설정
        Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());  // 세션 ID를 쿠키에 담습니다
        sessionCookie.setPath("/");  // 전체 사이트에 대해 쿠키가 유효하도록 설정
        sessionCookie.setHttpOnly(true);  // 클라이언트 JS에서 쿠키 접근 불가
        sessionCookie.setSecure(true);  // HTTPS에서만 쿠키 전송
        sessionCookie.setMaxAge(60 * 60);  // 쿠키의 유효기간 설정 (예: 1시간)
        response.addCookie(sessionCookie);  // 응답으로 쿠키를 클라이언트에 추가

        return "redirect:/main";
    }

    @PostMapping("/logout")
    public String logOut(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");  // 애플리케이션의 루트 경로로 설정
        cookie.setMaxAge(0);  // 쿠키를 즉시 만료시킴
        response.addCookie(cookie);  // 응답에 쿠키 추가


        return "redirect:/login";
    }


}