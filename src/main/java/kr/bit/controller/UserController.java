package kr.bit.controller;

import kr.bit.entity.Hobby;
import kr.bit.entity.Message;
import kr.bit.entity.User;
import kr.bit.bean.UserJoinBean;
import kr.bit.entity.UserProfile;
import kr.bit.service.LoginService;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
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
    @Autowired
    private LoginService loginService;

    @GetMapping("register")
    public String register(HttpServletRequest request, Model model) {
        List<Hobby> hobbyList = userService.getHobby();
        model.addAttribute("hobbyList", hobbyList);
        User user = new User();
        UserJoinBean userJoinBean = new UserJoinBean();
        userJoinBean.setUser(user);
        userJoinBean.setUserProfile(new UserProfile());
        userJoinBean.setHobbies("");
        model.addAttribute("userJoinBean", userJoinBean);

        return "user/register";
    }

    @PostMapping("register_pro")
    public String register_pro(@Valid @ModelAttribute("userJoinBean") UserJoinBean userJoinBean,
                               BindingResult result,Model model,
                               @RequestParam("imgFile") MultipartFile file, RedirectAttributes redirectAttributes,
                               HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        if(result.hasErrors()){
            List<Hobby> hobbyList = userService.getHobby();
            model.addAttribute("hobbyList", hobbyList);
            return "user/register";
        }
        User user = userJoinBean.getUser();
        UserProfile userProfile = userJoinBean.getUserProfile();
        String hobbies = userJoinBean.getHobbies();

        user.setGoogleId((String) request.getSession().getAttribute("googleId"));
        user.setKakaoId((String) request.getSession().getAttribute("kakaoId"));
        user.setNaverId((String) request.getSession().getAttribute("naverId"));

        try {
            // 디렉토리 경로 생성
            Path uploadPath = Paths.get(uploadDir, "user_photos");

            // 디렉토리가 존재하지 않으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = "";
            if(file.isEmpty()){
                fileName = "default.png";
            }
            else {
                fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadDir, "user_photos", fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }


            // 파일을 지정된 디렉토리에 저장

            // 파일 이름을 userProfile에 저장
            userProfile.setPhotoImageUrl(fileName);  // 업로드된 파일의 이름을 photoImageUrl로 저장

        } catch (IOException e) {
            return "redirect:/user/register";  // 업로드 실패 시 리디렉션
        }

        // 사용자와 프로필, 취미 정보를 DB에 저장
        userService.registerUserWithHobbies(user, userProfile, hobbies);

        loginService.processLogin(user.getUserId(), request,response);
        return "redirect:/main";
    }


    @PostMapping("/delete")
    public String deleteUser(HttpSession session, HttpServletResponse response){
        int userId = (int) session.getAttribute("user");
        userService.deleteUser(userId);
        session.invalidate();
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");  // 애플리케이션의 루트 경로로 설정
        cookie.setMaxAge(0);  // 쿠키를 즉시 만료시킴
        response.addCookie(cookie);  // 응답에 쿠키 추가

        return "redirect:/login";
    }

}