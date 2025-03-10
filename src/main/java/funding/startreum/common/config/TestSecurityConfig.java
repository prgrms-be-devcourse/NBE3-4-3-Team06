package funding.startreum.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration  // ✅ 테스트 환경에서만 사용하므로 @Configuration 추가
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // ✅ CSRF 비활성화
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()  // ✅ 모든 요청 허용
                );
        return http.build();
    }
}
