package kr.bit.controller;

import kr.bit.entity.UserProfile;
import kr.bit.service.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class RestUserController {

    private final UserService userService;

    public RestUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/changeProfile/{profileImageId}")
    public String changeProfile(@PathVariable("profileImageId") int profileImageId, HttpSession session) {
        int userId = (int)session.getAttribute("user");
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
        userProfile.setProfileImageId(profileImageId);
        userService.updateProfileImage(userProfile);

        return "success";
    }
}
