package kr.bit.controller;

import kr.bit.entity.Event;
import kr.bit.service.EventService;
import kr.bit.service.LoginService;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private EventService eventService;

    @Autowired
    private LoginService loginService;

    @GetMapping("/main")
    public String main(HttpServletRequest request, Model model) {
        // ✅ 현재 로그인 상태 확인 (Spring Security 사용)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // ✅ 로그인 여부 확인 (anonymousUser 또는 인증되지 않은 상태인지 검사)
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";  // ✅ 로그인되지 않은 경우, 로그인 페이지로 이동
        }

        // ✅ 로그인된 경우, 이벤트 리스트 가져오기
        List<Event> eventList = eventService.getCurrentEvents();
        model.addAttribute("eventList", eventList);
        return "main";  // ✅ main.html 페이지로 이동
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response) {
        // ✅ 현재 로그인 상태 확인 (Spring Security 사용)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // ✅ 로그인되어 있으면 `/main`으로 리다이렉트
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/main";
        }

        // ✅ 현재 사용자의 권한 가져오기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // ✅ `ROLE_REGISTER`가 있는 경우에만 실행
        if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_REGISTER"))) {
            loginService.clearRegisterRole(request, response);
        }

        return "login";
    }
}

