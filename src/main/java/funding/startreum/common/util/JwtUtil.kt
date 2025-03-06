package funding.startreum.common.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*

@Component
class JwtUtil {

    private val logger: Logger = LoggerFactory.getLogger(JwtUtil::class.java) // 🔹 SLF4J Logger 선언

    companion object {
        private const val SECRET_KEY = "ThisIsASecretKeyForJwtTokenForTestingPurposeOnly" // 환경 변수로 관리 권장
        private const val ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 30L // 30분
        private const val REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7 // 7일
    }

    val refreshTokenExpiration: Long
        get() = REFRESH_TOKEN_EXPIRATION // ✅ 프로퍼티로 선언

    private val key: Key = Keys.hmacShaKeyFor(SECRET_KEY.toByteArray(StandardCharsets.UTF_8))

    

    fun generateAccessToken(name: String, email: String, role: String): String {
        val formattedRole = if (role.startsWith("ROLE_")) role else "ROLE_$role"

        return Jwts.builder()
            .setSubject(name)
            .claim("email", email)
            .claim("role", formattedRole)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact().also {
                logger.info("✅ Access Token 생성됨 (name=$name, email=$email, role=$formattedRole)")
            }
    }

    fun generateRefreshToken(name: String): String {
        return Jwts.builder()
            .setSubject(name)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact().also {
                logger.info("✅ Refresh Token 생성됨 (name=$name)")
            }
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body

            val tokenUsername = claims.subject
            val isValid = tokenUsername == userDetails.username
            logger.info("✅ Token 검증 완료 (username=$tokenUsername, isValid=$isValid)")
            isValid
        } catch (e: Exception) {
            logger.error("❌ Token 검증 오류: ${e.message}")
            false
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            logger.info("✅ Refresh Token 검증 성공")
            true
        } catch (e: Exception) {
            logger.error("❌ Refresh Token 검증 실패: ${e.message}")
            false
        }
    }

    fun getNameFromToken(token: String): String? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
                .subject
                ?.lowercase().also {
                    logger.info("📌 Token에서 추출한 사용자 이름: $it")
                }
        } catch (e: Exception) {
            logger.error("❌ Token에서 사용자 이름 추출 실패: ${e.message}")
            null
        }
    }

    fun getEmailFromToken(token: String): String {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
                .get("email", String::class.java) ?: throw IllegalArgumentException("Token에서 이메일을 찾을 수 없습니다.")
        } catch (e: Exception) {
            logger.error("❌ Token에서 이메일 추출 실패: ${e.message}")
            throw IllegalArgumentException("유효하지 않은 토큰입니다.") // 예외 던지기
        }
    }


    fun getRoleFromToken(token: String): String? {
        return try {
            val role = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
                .get("role", String::class.java)

            val formattedRole = if (role != null && role.startsWith("ROLE_")) role else "ROLE_$role"
            logger.info("📌 Token에서 추출한 역할: $formattedRole")

            formattedRole
        } catch (e: Exception) {
            logger.error("❌ Token에서 역할 추출 실패: ${e.message}")
            null
        }
    }
}
