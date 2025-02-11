package kr.bit.controller;

import kr.bit.beans.*;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

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
    public String register_pro(@ModelAttribute("userJoinBean") UserJoinBean userJoinBean) {
        User user = userJoinBean.getUser();
        UserProfile userProfile = userJoinBean.getUserProfile();
        String hobbies = userJoinBean.getHobbies();

        userService.registerUserWithHobbies(user, userProfile, hobbies);

        return "main";
    }
}