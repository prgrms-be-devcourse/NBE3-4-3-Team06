package funding.startreum.domain.users.service

import funding.startreum.domain.users.repository.RefreshTokenRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
class RefreshTokenCleanupService(private val refreshTokenRepository: RefreshTokenRepository) {
    // ✅ 매일 밤 12시(자정)에 실행 (크론 표현식: "0 0 0 * * ?")
    @Scheduled(cron = "0 0 0 * * ?")
    fun cleanupExpiredTokens() {
        // System.out.println("🔹 만료된 Refresh Token 정리 시작...");
        val deletedCount = refreshTokenRepository.deleteExpiredTokens(Date())
        //System.out.println("✅ 삭제된 만료 토큰 수: " + deletedCount);
    }
}