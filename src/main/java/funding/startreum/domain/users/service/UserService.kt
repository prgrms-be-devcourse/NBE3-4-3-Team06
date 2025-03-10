package funding.startreum.domain.users.service

import funding.startreum.domain.users.dto.SignupRequest
import funding.startreum.domain.users.dto.UserResponse
import funding.startreum.domain.users.entity.User
import funding.startreum.domain.users.repository.UserRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
 class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,

)  {
    // Refresh Token 저장소 (임시 Map 사용 → DB 또는 Redis로 변경 가능)
    private val refreshTokenStorage = mutableMapOf<String, String>()

    // 허용된 역할 목록
    private val allowedRoles = setOf(User.Role.BENEFICIARY, User.Role.SPONSOR, User.Role.ADMIN)

    /**
     * 회원가입
     */
    fun registerUser(signupRequest: SignupRequest) {
        // 입력 값 검증
        validateSignupRequest(signupRequest)

        // 비밀번호 암호화
        val encryptedPassword = passwordEncoder.encode(signupRequest.password)

        // 사용자 엔티티 생성 (Nullable 값 방지)
        val user = User(

            name = signupRequest.name ?: "", // ✅ null 방지
            email = signupRequest.email ?: "", // ✅ null 방지
            password = encryptedPassword,
            role = signupRequest.role ?: User.Role.SPONSOR, // ✅ 기본값 설정
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // 데이터베이스에 저장
        userRepository.save(user)
    }

    /**
     * 입력 값 검증
     */
    private fun validateSignupRequest(signupRequest: SignupRequest) {
        if (isEmailDuplicate(signupRequest.email)) {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다.")
        }

        if (isNameDuplicate(signupRequest.name)) {
            throw IllegalArgumentException("이미 사용 중인 이름(ID)입니다.")
        }

        if (!allowedRoles.contains(signupRequest.role)) {
            throw IllegalArgumentException("허용되지 않은 역할(Role)입니다.")
        }
    }

    /**
     * 이름(ID) 중복 확인
     */
    fun isNameDuplicate(name: String): Boolean {
        println("🔍 Checking name duplication for: $name")
        val result = userRepository.existsByName(name)
        println("✅ Result: $result")
        return result
    }


    /**
     * 이메일 중복 확인
     */
    fun isEmailDuplicate(email: String): Boolean {
        println("🔍 Checking email duplication for: $email")
        val result = userRepository.existsByEmail(email)
        println("✅ Result: $result")
        return result
    }

    /**
     * 사용자 인증 (name 기반)
     */
    fun authenticateUser(name: String, password: String): UserResponse {
        val user = userRepository.findByName(name)
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }

        if (!passwordEncoder.matches(password, user.password)) {
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다.")
        }

        return UserResponse(
            name = user.name,
            email = user.email,
            role = user.role,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }

    /**
     * Refresh Token 저장 (name 기반)
     */
    fun saveRefreshToken(name: String, refreshToken: String) {
        refreshTokenStorage[name] = refreshToken
    }

    /**
     * 저장된 Refresh Token 조회
     */
    fun getRefreshToken(name: String): String? {
        return refreshTokenStorage[name]
    }

    /**
     * Refresh Token 검증
     */
    fun isRefreshTokenValid(name: String, refreshToken: String): Boolean {
        return refreshTokenStorage[name] == refreshToken
    }

    /**
     * 사용자 정보 조회 (Refresh 토큰 재발급 시 사용)
     */
    fun getUserByName(name: String): User {
        return userRepository.findByName(name)
            .orElseThrow { IllegalArgumentException("해당 이름의 사용자를 찾을 수 없습니다.") }
    }

    /**
     * 사용자 마이페이지 조회
     */
    fun getUserProfile(name: String, loggedInUsername: String): UserResponse {
        val loggedInUser = userRepository.findByName(loggedInUsername)
            .orElseThrow { IllegalArgumentException("현재 로그인한 사용자를 찾을 수 없습니다.") }

        val targetUser = userRepository.findByName(name)
            .orElseThrow { IllegalArgumentException("해당 사용자를 찾을 수 없습니다.") }

        if (!loggedInUser.name.equals(targetUser.name, ignoreCase = true)
            && loggedInUser.role != User.Role.ADMIN) {
            throw AccessDeniedException("권한이 없습니다.")
        }

        return UserResponse(
            name = targetUser.name,
            email = targetUser.email,
            role = targetUser.role,
            createdAt = targetUser.createdAt,
            updatedAt = targetUser.updatedAt
        )
    }

    /**
     * 이메일 업데이트 (PUT 요청)
     */
    fun updateUserEmail(name: String, newEmail: String) {
        val user = userRepository.findByName(name)
            .orElseThrow { IllegalArgumentException("해당 사용자를 찾을 수 없습니다.") }

        if (userRepository.findByEmail(newEmail).isPresent) {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다.")
        }

        user.email = newEmail
        user.updatedAt = LocalDateTime.now()
        userRepository.save(user)
    }
}
