package funding.startreum.common.config

import JwtAuthenticationFilter
import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.users.service.CustomUserDetailsService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
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
                    // ✅ ID 중복 확인 API는 누구나 접근 가능하도록 허용
                    .requestMatchers(HttpMethod.GET, "/api/users/check-name").permitAll()

                    // ✅ 프로젝트 생성 API는 수혜자(ROLE_BENEFICIARY)만 접근 가능하도록 설정
                    .requestMatchers(HttpMethod.POST, "/api/beneficiary/create/projects").hasRole("BENEFICIARY")
                    .requestMatchers(HttpMethod.GET, "/projects/new").permitAll()

                    // ✅ 검색 API를 인증 없이 허용
                    .requestMatchers(HttpMethod.GET, "/api/projects/search").permitAll()
                    .requestMatchers(HttpMethod.GET, "/projects/search").permitAll()

                    // ✅ 프로젝트 상세 API는 인증 없이 접근 가능
                    .requestMatchers(HttpMethod.GET, "/api/projects/{projectId}").permitAll()

                    // ✅ 프로젝트 상세 페이지(View)는 인증 없이 접근 가능
                    .requestMatchers(HttpMethod.GET, "/projects/{projectId}").permitAll()

                    // ✅ 인증 없이 접근 가능한 정적 리소스 및 공용 API
                    .requestMatchers("/", "/home", "/index.html").permitAll()
                    .requestMatchers("/favicon.ico", "/css/**", "/js/**", "/images/**", "/img/**").permitAll()
                    .requestMatchers(
                        "/api/users/signup", "/api/users/registrar", "/api/users/login",
                        "/api/users/check-email"
                    ).permitAll()

                    // ✅ 댓글 조회 허용 (로그인 없이 가능)
                    .requestMatchers(HttpMethod.GET, "/api/comment/**").permitAll()

                    // ✅ 리워드 조회는 누구나 접근 가능
                    .requestMatchers(HttpMethod.GET, "/api/reward/{projectId}").permitAll()

                    // ✅ HTML 페이지는 누구나 접근 가능 (관리자 뷰 페이지)
                    .requestMatchers("/admin").permitAll()
                    // ✅ 관리자 전용 API는 ROLE_ADMIN 필요
                    .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                    .requestMatchers("/admin/project").permitAll()

                    .requestMatchers("/profile/{name}").permitAll()  // ✅ 프로필 뷰는 인증 없이 접근 가능
                    .requestMatchers("/profile/modify/{name}").permitAll() // ✅ 프로필 수정 뷰도 인증 없이 접근 가능
                    .requestMatchers("/api/users/profile/{name}").authenticated()  // ✅ 프로필 API는 인증 필요
                    .requestMatchers("/api/users/profile/modify/{name}")
                    .access { authenticationSupplier, context ->
                        val authentication: Authentication = authenticationSupplier.get()
                        val pathUsername = context.variables["name"]

                        val isOwner = authentication.name == pathUsername
                        val isAdmin = authentication.authorities.any { it.authority == "ROLE_ADMIN" }

                        AuthorizationDecision(isOwner || isAdmin) // ✅ 본인 또는 관리자만 수정 가능
                    }

                    .requestMatchers("/profile/account/{name}").permitAll()  // ✅ HTML 페이지는 인증 없이 접근 가능
                    .requestMatchers(HttpMethod.GET, "/api/account/user/{name}").authenticated()  // ✅ 계좌 조회는 로그인 필요
                    .requestMatchers(HttpMethod.POST, "/api/account/user/{name}/create")
                    .access { authenticationSupplier, context ->
                        val authentication: Authentication = authenticationSupplier.get()
                        val requestURI = context.request.requestURI

                        // 호출된 사용자 이름 추출
                        val parts = requestURI.split("/")
                        val pathUsername = parts[parts.size - 2] // {name} 위치

                        AuthorizationDecision(authentication.name == pathUsername) // ✅ 본인만 계좌 생성 가능
                    }

                    // ✅ 모든 API 요청에 대해 JWT 인증 필터 적용
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java) // ✅ JWT 필터 추가
            .formLogin { it.disable() } // ✅ 기본 로그인 폼 비활성화 (Spring이 가로채지 않도록)
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

    // ✅ CORS 설정 추가 (필요한 경우 도메인 허용 가능)
    @Bean
    open fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*") // ✅ 모든 도메인 허용 (개발 환경에서만 적용)
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*") // ✅ 모든 헤더 허용
        configuration.exposedHeaders = listOf("Authorization") // ✅ 클라이언트가 Authorization 헤더 접근 가능하도록 설정

        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}