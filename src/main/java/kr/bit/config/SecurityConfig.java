package kr.bit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        http.addFilterBefore(filter, CsrfFilter.class);

        http
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).ignoringAntMatchers("/user/register_pro")
                .and()
                .authorizeRequests()
                .antMatchers("/", "/manifest.json", "/uploads/**/**", "/login", "/googleLogin", "/googleLoginRequest", "/kakaoLoginRequest", "/naverLoginRequest", "/kakaoLogin", "/naverLogin", "/login", "/js/**", "/css/**", "/images/**", "/auth/check", "/api/public/**").permitAll() // ✅ 누구나 접근 가능
                .antMatchers("/user/register", "/user/register_pro").hasRole("REGISTER") // ✅ `ROLE_REGISTER`가 있어야 회원가입 가능
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/login"); // ✅ 인증되지 않은 사용자는 회원가입 페이지로 이동
                })
                .and()
                .logout()
                .logoutSuccessUrl("/") // ✅ 로그아웃 후 메인 페이지로 이동
                .invalidateHttpSession(true) // ✅ 세션 삭제
                .deleteCookies("JSESSIONID", "XSRF-TOKEN") // ✅ 쿠키 삭제
                .clearAuthentication(true); // ✅ 인증 정보 제거
    }

}
