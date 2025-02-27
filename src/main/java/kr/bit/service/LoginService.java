package kr.bit.service;

import kr.bit.dao.ChatDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Service
public class LoginService {
    @Autowired
    private UserService userService;

    @Autowired
    private ChatRestoreService chatRestoreService;

    public void processLogin(int userId, HttpServletRequest request, HttpServletResponse response) {
        // Spring Security 인증 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 성별 정보 가져오기
        String gender = userService.getGender(userId);

        // 세션 설정
        HttpSession session = request.getSession();
        session.setAttribute("user", userId);
        session.setAttribute("gender", gender);

        // 세션 ID를 쿠키로 설정
        Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
        sessionCookie.setPath("/");
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(true);
        sessionCookie.setMaxAge(60 * 60); // 1시간
        response.addCookie(sessionCookie);

        chatRestoreService.restoreChatMessages(userId);
    }
}
