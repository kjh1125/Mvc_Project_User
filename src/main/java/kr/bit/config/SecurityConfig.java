package kr.bit.config;

import kr.bit.security.JwtGenerator;
import kr.bit.security.JwtTokenFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private JwtGenerator jwtGenerator;

    public SecurityConfig(JwtGenerator jwtGenerator) {
        this.jwtGenerator = jwtGenerator;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(new JwtTokenFilter(jwtGenerator), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/login", "/register").permitAll()  // 로그인, 회원가입은 누구나 접근 가능
                .anyRequest().authenticated();  // 그 외 모든 요청은 인증 필요
    }
}
