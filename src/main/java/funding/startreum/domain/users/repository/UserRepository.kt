package funding.startreum.domain.users.repository

import funding.startreum.domain.users.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
@Repository
interface UserRepository : JpaRepository<User, Int> { // ✅ Users 엔티티로 변경
    fun findByEmail(email: String): Optional<User> // ✅ Nullable 제거

    @Query("SELECT u FROM User u WHERE LOWER(u.name) = LOWER(:name)") // ✅ funding.startreum.domain.users.entity.User → Users 변경
    fun findByName(@Param("name") name: String): Optional<User> // ✅ Nullable 제거


    // ✅ 추가: ID 중복 확인
    fun existsByName(name: String): Boolean

    // ✅ 추가: 이메일 중복 확인
    fun existsByEmail(email: String): Boolean
}
