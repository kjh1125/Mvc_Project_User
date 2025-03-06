package kr.bit.service;

import kr.bit.dao.ChatDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

@Service
public class LoginService {
    @Autowired
    private UserService userService;

    @Autowired
    private ChatRestoreService chatRestoreService;

    public void clearRegisterRole(HttpServletRequest request, HttpServletResponse response) {
        // ✅ 현재 인증 정보 가져오기
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

        if (currentAuth != null && currentAuth.isAuthenticated()) {
            // ✅ `anonymousUser`로 설정 (로그인되지 않은 상태로 초기화)
            Authentication anonymousAuth = new UsernamePasswordAuthenticationToken("anonymousUser", null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
        }

        // ✅ 세션에서 회원가입 상태 제거
        request.getSession().removeAttribute("registering");

        System.out.println("회원가입 상태 해제됨: ROLE_REGISTER 제거됨");
    }


    public void processRegister(HttpServletRequest request, HttpServletResponse response) {
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = currentAuth.getPrincipal();

        // ✅ 2️⃣ `ROLE_REGISTER` 역할 부여 (회원가입 진행 중)
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_REGISTER"));
        Authentication newAuth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        // ✅ 3️⃣ 세션에도 회원가입 상태 저장
        request.getSession().setAttribute("registering", true);

        System.out.println("회원가입 상태로 변경됨: ROLE_REGISTER");
    }

    public void processLogin(int userId, HttpServletRequest request, HttpServletResponse response) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        // ✅ Spring Security 인증 객체 생성 (사용자 ID와 권한 추가)
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
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

        // ✅ CSRF 토큰 생성 및 부여
        String csrfToken = generateCSRFToken();
        session.setAttribute("csrfToken", csrfToken);

        // ✅ CSRF 토큰을 쿠키에 저장 (클라이언트가 AJAX 요청 시 사용할 수 있도록)
        Cookie csrfCookie = new Cookie("XSRF-TOKEN", csrfToken);
        csrfCookie.setPath("/");
        csrfCookie.setHttpOnly(false); // ✅ JavaScript에서 읽을 수 있도록 false 설정
        csrfCookie.setSecure(true);
        response.addCookie(csrfCookie);


        chatRestoreService.restoreChatMessages(userId);
    }

    public String generateCSRFToken() {
        return java.util.UUID.randomUUID().toString();
    }
}
