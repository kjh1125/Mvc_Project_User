package kr.bit.controller;

import kr.bit.beans.*;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class UserController {


    @Value("${upload.path}")
    private String uploadDir; // 이미지 저장 경로를 application.properties로부터 가져옵니다.

    @Autowired
    private UserService userService;

    @GetMapping("user/register")
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

    @PostMapping("user/register_pro")
    public String register_pro(@ModelAttribute("userJoinBean") UserJoinBean userJoinBean,
                               @RequestParam("imgFile") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        User user = userJoinBean.getUser();
        UserProfile userProfile = userJoinBean.getUserProfile();
        String hobbies = userJoinBean.getHobbies();

        System.out.println(user);
        System.out.println(userProfile);
        System.out.println(hobbies);
        System.out.println(file);
        System.out.println(uploadDir);


        try {
            // 파일 저장 경로 설정 (예: /path/to/upload/directory)
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            System.out.println(fileName);
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

        // 사용자 ID를 리디렉션 URL에 추가
        redirectAttributes.addAttribute("userId", user.getUserId());
        return "redirect:/user/photo";  // 사진 업로드 페이지로 리디렉션
    }

    }