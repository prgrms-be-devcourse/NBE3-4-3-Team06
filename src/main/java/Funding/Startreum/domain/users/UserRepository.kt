package funding.startreum.domain.users

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface UserRepository : JpaRepository<User?, Int?> {
    fun findByEmail(email: String?): Optional<User?>? // 이메일 중복 확인, 검색

    @Query("SELECT u FROM User u WHERE LOWER(u.name) = LOWER(:name)")
    fun findByName(@Param("name") name: String?): Optional<User?>? // 이름 검색 (대소문자 무시)
}