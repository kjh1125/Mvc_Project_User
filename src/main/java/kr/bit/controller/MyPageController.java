package kr.bit.controller;

import kr.bit.dao.UserDao;
import kr.bit.entity.Hobby;
import kr.bit.entity.Point;
import kr.bit.entity.UserHobby;
import kr.bit.entity.UserProfile;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/myPage")
public class MyPageController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

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

    @GetMapping("/info")
    public String info(HttpSession session, Model model) {
        int userId = (int) session.getAttribute("user");
        String nickname = userService.getNickname(userId);
        UserProfile userProfile = userService.getProfile(userId);
        List<String> userHobbyList = userService.getUserHobbies(userId);
        List<Hobby> hobbyList = userService.getHobby();
        model.addAttribute("hobbyList", hobbyList);
        model.addAttribute("nickname", nickname);
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("userHobbyList", userHobbyList);
        return "myPage/info";
    }


    @GetMapping("/glass")
    public String glass(HttpSession session, Model model) {
        int userId = (int) session.getAttribute("user");
        Point point = userService.getPoint(userId);
        model.addAttribute("point", point);
        return "myPage/glass";
    }


    @GetMapping("/firewood")
    public String firewood(HttpSession session, Model model) {
        int userId = (int) session.getAttribute("user");
        Point point = userService.getPoint(userId);
        model.addAttribute("point", point);
        return "myPage/firewood";
    }

    @GetMapping("/supports")
    public String supports(){
        return "myPage/supports";
    }

    @GetMapping("/guide")
    public String guide(){
        return "myPage/guide";
    }

    @GetMapping("/map")
    public String map() {return "myPage/map";}


    @GetMapping("/terms")
    public String terms() {return "myPage/terms";}

}
