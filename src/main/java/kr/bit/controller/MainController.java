package kr.bit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    @GetMapping("/save")
    public String save(@RequestParam(value = "token", required = false) String jwtToken, Model model) {
        if (jwtToken != null) {
            model.addAttribute("jwtToken", jwtToken);
            return "save";  // save.html 페이지로 이동
        }
        return "redirect:/login";  // 토큰이 없으면 로그인 페이지로 리디렉션
    }

    @GetMapping("/main")
    public String main() {
        return "main";  // main.html 페이지로 이동
    }

    @GetMapping("/login")
    public String login() {
        return "login";  // login.html 페이지로 이동
    }
}

