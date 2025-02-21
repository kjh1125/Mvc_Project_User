package kr.bit.controller;

import kr.bit.entity.Point;
import kr.bit.entity.UserProfile;
import kr.bit.service.UserService;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/charge")
    public String charge(@RequestParam("points") int points,
            HttpSession session) {
        int userId = (int)session.getAttribute("user");
        Point point = new Point();
        point.setPoints(points);
        point.setUserId(userId);
        userService.setPoints(point);
        return "success";
    }
}
