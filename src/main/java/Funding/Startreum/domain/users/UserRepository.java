package Funding.Startreum.domain.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email); // 이메일 중복 확인, 검색

    @Query("SELECT u FROM User u WHERE LOWER(u.name) = LOWER(:name)")  // ✅ 대소문자 무시
    Optional<User> findByName(@Param("name") String name); // 이름 검색 (대소문자 무시)


}