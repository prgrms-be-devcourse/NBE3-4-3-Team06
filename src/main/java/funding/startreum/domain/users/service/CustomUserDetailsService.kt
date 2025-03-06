package funding.startreum.domain.users.service

import funding.startreum.domain.users.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service

class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        // 🔍 데이터베이스에서 사용자 조회 시도
        return userRepository.findByName(username)
            .map { user ->
                // ✅ 사용자 찾음
                val role = "ROLE_${user.role.name}" // Spring Security 권한 적용

                User.withUsername(user.name)
                    .password(user.password)
                    .authorities(role)
                    .build()
            }
            .orElseThrow {
                // ❌ 사용자 정보 조회 실패 (DB에 존재하지 않음)
                UsernameNotFoundException("사용자를 찾을 수 없습니다: $username")
            }
    }
}
