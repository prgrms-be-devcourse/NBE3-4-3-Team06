package funding.startreum.common.config

import JwtAuthenticationFilter
import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.users.service.CustomUserDetailsService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // ✅ Spring Security 메소드 보안 활성화
open class SecurityConfig(
    private val customUserDetailsService: CustomUserDetailsService
) {

    // ✅ 비밀번호 암호화 설정
    @Bean
    open fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    open fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        return JwtAuthenticationFilter(JwtUtil(), customUserDetailsService)
    }

    // ✅ Spring Security 필터 체인 설정
    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) } // ✅ CORS 설정 추가
            .csrf { it.disable() } // ✅ CSRF 비활성화 (REST API 방식)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // ✅ 세션 비활성화 (JWT 사용)
            .authorizeHttpRequests {
                it
                    // ✅ 누구나 접근 가능 (Next.js에서 처리)
                    .requestMatchers("/", "/home", "/index.html").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll() // ✅ 로그인 엔드포인트 허용
                    .requestMatchers(HttpMethod.POST, "/api/users/logout").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users/signup").permitAll() // ✅ 회원가입  register
                    .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/check-name").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/check-email").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/projects/search").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/projects/{projectId}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/comment/**").permitAll()

                    // ✅ 인증 필요한 API
                    .requestMatchers("/api/users/profile/{name}").authenticated()

                    // ✅ 관리자 전용 API
                    .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                    .requestMatchers(HttpMethod.POST, "/api/account/payment").permitAll()

                    // ✅ 기타 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java) // ✅ JWT 필터 추가
            .formLogin { it.disable() } // ✅ 기본 로그인 폼 비활성화
            .logout {
                it.logoutUrl("/api/users/logout") // ✅ 로그아웃 URL
                    .logoutSuccessHandler { _, response, _ ->
                        response.contentType = "application/json"
                        response.characterEncoding = "UTF-8"
                        response.status = HttpServletResponse.SC_OK
                        val jsonResponse = """{"status": "success", "message": "로그아웃 성공"}"""
                        response.writer.write(jsonResponse)
                        response.writer.flush()
                    }
                    .permitAll()
            }

        return http.build()
    }

    // ✅ CORS 설정 (Next.js와의 연동을 위해 도메인 제한)
    @Bean
    open fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:3000") // ✅ Next.js 개발 서버 허용
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.exposedHeaders = listOf("Authorization")
        configuration.allowCredentials = true // ✅ JWT 사용을 위해 쿠키 허용

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}