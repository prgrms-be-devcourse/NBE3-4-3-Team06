package funding.startreum.domain.users;

import funding.startreum.domain.users.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RefreshTokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // ✅ 매일 밤 12시(자정)에 실행 (크론 표현식: "0 0 0 * * ?")
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokens() {
       // System.out.println("🔹 만료된 Refresh Token 정리 시작...");
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(new Date());
        //System.out.println("✅ 삭제된 만료 토큰 수: " + deletedCount);
    }
}