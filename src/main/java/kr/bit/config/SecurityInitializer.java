package kr.bit.config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {
    //웹앱 보안 초기화 작업 -> 시큐리티 필터가 자동으로 등록됨
    //상속만하면 시큐리티 활성화됨
    // 스프링 시큐리티를 내부적으로 동작시킨다
}
