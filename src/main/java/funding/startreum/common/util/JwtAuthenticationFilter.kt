
import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.users.service.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component

import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
open class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: CustomUserDetailsService // ✅ 명확한 타입 지정
) : OncePerRequestFilter() {

    private val logger: Logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestURI = request.requestURI
        logger.debug("🔍 Incoming request: $requestURI")

        // ✅ 인증이 필요 없는 엔드포인트 (정확한 비교)
        val excludedPaths = setOf(
            "/api/users/signup",
            "/api/users/registrar",
            "/api/users/login",
            "/api/users/check-name",
            "/api/users/check-email",
            "/favicon.ico",
            "/error"
        )

        // ✅ 정규식을 사용하여 특정 패턴 허용 (CSS, JS, 이미지 등 정적 리소스)
        val staticResourcePattern = Regex("^/(css|js|images|img)/.*")

        // ✅ 인증 없이 접근 가능한 엔드포인트 예외 처리
        if (requestURI in excludedPaths || staticResourcePattern.matches(requestURI)) {
            logger.debug("⏩ Skipping authentication for: $requestURI")
            filterChain.doFilter(request, response)
            return
        }

        // ✅ Authorization 헤더 확인
        val header = request.getHeader("Authorization")
        if (header == null || !header.startsWith("Bearer ")) {
            logger.debug("🚫 No JWT token found in request headers for URI: $requestURI")
            filterChain.doFilter(request, response)
            return
        }

        val token = header.replace("Bearer ", "")

        // ✅ JWT에서 사용자명 추출
        val username: String? = try {
            jwtUtil.getNameFromToken(token)?.trim()
        } catch (e: Exception) {
            logger.warn("❌ JWT token extraction failed: ${e.message}", e)
            filterChain.doFilter(request, response)
            return
        }

        if (username == null) {
            logger.warn("⚠️ Invalid JWT token, username is null for URI: $requestURI")
            filterChain.doFilter(request, response)
            return
        }

        // ✅ 이미 인증된 사용자인 경우 필터 통과
        if (SecurityContextHolder.getContext().authentication != null) {
            logger.debug("✅ User $username is already authenticated.")
            filterChain.doFilter(request, response)
            return
        }

        try {
            // ✅ 데이터베이스에서 사용자 정보 조회
            val userDetails: UserDetails = userDetailsService.loadUserByUsername(username)

            // ✅ JWT 유효성 검증 및 SecurityContext 설정
            if (jwtUtil.validateToken(token, userDetails)) {
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
                logger.debug("✅ User $username authenticated successfully for URI: $requestURI")
            } else {
                logger.warn("⚠️ JWT validation failed for user: $username")
            }

        } catch (e: Exception) {
            logger.error("❌ User not found in database: $username", e)
        }

        filterChain.doFilter(request, response)
    }
}
