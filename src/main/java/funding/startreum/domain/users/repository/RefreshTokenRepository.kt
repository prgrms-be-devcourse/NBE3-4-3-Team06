package funding.startreum.domain.users.repository

import funding.startreum.domain.users.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken?, Long?> {
    fun findByToken(token: String?): Optional<RefreshToken?>? // ✅ 토큰으로 찾기

    fun deleteByToken(token: String?) // ✅ 토큰으로 삭제


    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.username = :username")
    fun deleteByUsername(@Param("username") username: String?)


    // ✅ 만료된 Refresh Token 삭제 쿼리
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    fun deleteExpiredTokens(@Param("now") now: Date?): Int
}

