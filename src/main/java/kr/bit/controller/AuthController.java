package kr.bit.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @GetMapping("/auth/check")
    public Map<String, Object> checkAuthentication(HttpServletRequest request) {
        System.out.println("여기아닌가");
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(60*60);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ) {
            return new HashMap<>() {{
                put("authenticated", false);
                put("message", "사용자가 로그인하지 않았습니다.");
            }};
        }

        return new HashMap<>() {{
            put("authenticated", true);
            put("userId", authentication.getPrincipal());
            put("authorities", authentication.getAuthorities());
        }};
    }
}
