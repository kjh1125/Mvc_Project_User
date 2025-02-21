package kr.bit.controller;

import kr.bit.entity.Point;
import kr.bit.entity.UserProfile;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/myPage")
public class MyPageController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public String myPage(HttpSession session, Model model) {
        int userId = (int) session.getAttribute("user");
        int profileImage = userService.getProfileImage(userId);
        String nickname = userService.getNickname(userId);
        Point point = userService.getPoint(userId);
        model.addAttribute("point", point);
        model.addAttribute("nickname", nickname);
        model.addAttribute("profileImage", profileImage);
        return "myPage/page";
    }

    @GetMapping("/faq")
    public String faq(HttpSession session, Model model) {
        return "myPage/faq";
    }

    @GetMapping("/charge")
    public String charge(HttpSession session, Model model) {
        int userId = (int) session.getAttribute("user");
        Point point = userService.getPoint(userId);
        model.addAttribute("point", point.getPoints());
        return "myPage/charge";
    }
}
