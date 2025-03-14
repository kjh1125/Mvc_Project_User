package kr.bit.config;

import kr.bit.interceptor.SessionInterceptor;
import kr.bit.mapper.UserMapper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.ServletContext;
import javax.servlet.SessionCookieConfig;

@Configuration
@EnableWebMvc
@EnableScheduling
@ComponentScan(basePackages = {"kr.bit.controller", "kr.bit.service", "kr.bit.dao", "kr.bit.mapper", "kr.bit.security", "kr.bit.dto","kr.bit.entity","kr.bit.handler", "kr.bit.interceptor"})
public class ServletConfig implements WebMvcConfigurer {

    @Autowired
    private SessionInterceptor sessionInterceptor;

    @Value("${upload.path}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // static 리소스 처리
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");  // 모든 정적 리소스를 포함하도록 설정
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:///"+uploadDir);
    }


    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("/WEB-INF/views/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCacheable(false);  // 캐시 비활성화
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        return templateEngine;
    }

    @Bean
    public ThymeleafViewResolver thymeleafViewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setCharacterEncoding("UTF-8"); // UTF-8 인코딩 추가
        return viewResolver;
    }

    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSize(20971520);  // 최대 업로드 크기 20MB
        resolver.setMaxUploadSizePerFile(41943040); // 한번에 업로드 가능한 최대 크기 40MB
        resolver.setMaxInMemorySize(20971520); // 메모리 임계값
        return resolver;
    }

    @Bean
    public JedisPool jedisPool(){
        return new JedisPool("223.130.151.175", 6379);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/**")    // /chat 하위 경로 모두 검사
                .excludePathPatterns("/", "/manifest.json", "/uploads/**/**", "/login", "/googleLogin", "/googleLoginRequest", "/kakaoLoginRequest", "/naverLoginRequest", "/kakaoLogin", "/naverLogin", "/login", "/js/**", "/css/**", "/images/**", "/auth/check", "/api/public/**","/user/register", "/user/register_pro");  // 로그인, 회원가입은 제외
    }
}
