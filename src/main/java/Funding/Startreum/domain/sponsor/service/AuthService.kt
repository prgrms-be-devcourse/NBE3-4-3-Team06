package funding.startreum.domain.sponsor.service

import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.sponsor.exception.InvalidTokenException
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
class AuthService {
    private val jwtUtil: JwtUtil? = null

    fun extractEmail(token: String): String {
        if (!token.startsWith("Bearer ")) {
            throw InvalidTokenException()
        }
        return jwtUtil!!.getEmailFromToken(token.replace("Bearer ", ""))
    }
}
