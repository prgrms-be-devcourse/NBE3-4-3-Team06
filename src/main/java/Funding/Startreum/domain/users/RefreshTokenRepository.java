package Funding.Startreum.domain.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token); // ✅ 토큰으로 찾기

    void deleteByToken(String token); // ✅ 토큰으로 삭제


    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.username = :username")      // ✅ JPQL로 직접 삭제 쿼리 실행

    void deleteByUsername(@Param("username") String username);


    // ✅ 만료된 Refresh Token 삭제 쿼리
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") Date now);

}

