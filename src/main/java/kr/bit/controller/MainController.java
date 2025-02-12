package kr.bit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class MainController {

    @GetMapping("/main")
    public String main(HttpServletRequest request) {
        HttpSession session = request.getSession(false);  // false는 세션이 없으면 새로 생성하지 않음
        if (session != null && session.getAttribute("user") != null) {
            return "main";  // main.html 페이지로 이동
        } else {
            return "redirect:/login";  // login.html 페이지로 리다이렉트
        }
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        HttpSession session = request.getSession(false);  // false는 세션이 없으면 새로 생성하지 않음
        if (session != null && session.getAttribute("user") != null) {
            return "redirect:/main";  // main.html 페이지로 이동
        } else {
            return "login";
        }
    }
}

