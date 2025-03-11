package funding.startreum.domain.users.controller

import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.users.dto.EmailUpdateRequest
import funding.startreum.domain.users.dto.SignupRequest
import funding.startreum.domain.users.dto.UserResponse
import funding.startreum.domain.users.entity.RefreshToken
import funding.startreum.domain.users.entity.User
import funding.startreum.domain.users.repository.RefreshTokenRepository
import funding.startreum.domain.users.service.MyFundingService
import funding.startreum.domain.users.service.MyProjectService
import funding.startreum.domain.users.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api/users")
 open class UserController(
    private val jwtUtil: JwtUtil,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val myFundingService: MyFundingService,
    private val myProjectService: MyProjectService,
    private val userService: UserService

) {


    // ✅ ID 중복 확인
    @GetMapping("/check-name")
    fun checkNameDuplicate(@RequestParam name: String): ResponseEntity<Boolean> {

        return ResponseEntity.ok(userService.isNameDuplicate(name))
    }

    // ✅ 이메일 중복 확인
    @GetMapping("/check-email")
    fun checkEmailDuplicate(@RequestParam email: String): ResponseEntity<Boolean> {

        return ResponseEntity.ok(userService.isEmailDuplicate(email))
    }

    // ✅ 회원가입 처리 (REST API)
    @PostMapping("/registrar")
     fun  registerUser(
        @RequestParam name: String,
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam role: User.Role
    ): ResponseEntity<Unit> {
        userService.registerUser(SignupRequest(name, email, password, role))
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, "/")
            .build()
    }

    // ✅ 로그아웃
    @PostMapping("/logout")
     fun  logout(): ResponseEntity<Map<String, String>> {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication == null || !authentication.isAuthenticated) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("status" to "error", "message" to "로그인 상태가 아닙니다."))
        } else {
            SecurityContextHolder.clearContext()
            ResponseEntity.ok(mapOf("status" to "success", "message" to "로그아웃 성공"))
        }
    }

    // ✅ 로그인 요청 데이터 클래스
    data class LoginRequest(val name: String, val password: String)

    // ✅ 로그인 API (JWT 발급)
    @PostMapping("/login")
     fun  loginUser(@RequestBody loginRequest: LoginRequest): ResponseEntity<Any> =
        try {
            val user = userService.authenticateUser(loginRequest.name, loginRequest.password)

            refreshTokenRepository.deleteByUsername(user.name)

            val accessToken = jwtUtil.generateAccessToken(user.name, user.email, user.role.name)
            val refreshToken = jwtUtil.generateRefreshToken(user.name)

            refreshTokenRepository.save(
                RefreshToken(
                    token = refreshToken,
                    username = user.name,
                    expiryDate = Date(System.currentTimeMillis() + jwtUtil.refreshTokenExpiration)
                )
            )

            ResponseEntity.ok(
                mapOf(
                    "accessToken" to accessToken,
                    "refreshToken" to refreshToken,
                    "userName" to user.name,
                    "role" to user.role.name
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to (e.message ?: "로그인 실패")))
        }

    // ✅ Access Token 갱신 (Refresh Token 사용)
    @PostMapping("/refresh")
     fun  refreshAccessToken(@RequestBody request: Map<String, String>): ResponseEntity<Any> {
        val refreshToken = request["refreshToken"] ?: return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(mapOf("error" to "유효하지 않은 Refresh Token"))

        if (!jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "유효하지 않은 Refresh Token"))
        }

        val name = jwtUtil.getNameFromToken(refreshToken)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다.")

        val storedToken = refreshTokenRepository.findByToken(refreshToken)?.orElse(null)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Refresh Token이 존재하지 않습니다. 다시 로그인하세요."))

        if (storedToken.expiryDate.before(Date())) {
            refreshTokenRepository.deleteByToken(refreshToken)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Refresh Token이 만료되었습니다. 다시 로그인하세요."))
        }

        val user = userService.getUserByName(name)
        return ResponseEntity.ok(mapOf("accessToken" to jwtUtil.generateAccessToken(user.name, user.email, user.role.name)))
    }

    // ✅ 사용자 프로필 조회 (본인 또는 관리자만 가능)
    @GetMapping("/profile/{name}")
    @PreAuthorize("#name == authentication.name or hasRole('ADMIN')")
     fun  getUserProfile(@PathVariable name: String): ResponseEntity<Any> {
        val targetUser = userService.getUserByName(name)



        // ✅ 수정 코드
        val userProfile = UserResponse(
            name = targetUser.name,
            email = targetUser.email,
            role = targetUser.role,
            createdAt = targetUser.createdAt,
            updatedAt = targetUser.updatedAt
        )

        return ResponseEntity.ok(mapOf("status" to "success", "data" to userProfile))
    }


    // ✅ 이메일 수정 API
    @PutMapping("/profile/modify/{name}")
    @PreAuthorize("#name == authentication.name or hasRole('ROLE_ADMIN')")
     fun  updateEmail(@PathVariable name: String, @Valid @RequestBody request: EmailUpdateRequest): ResponseEntity<Any> {
        userService.updateUserEmail(name, request.newEmail)
        return ResponseEntity.ok(mapOf("message" to "이메일이 성공적으로 변경되었습니다."))
    }

    // ✅ 로그인한 사용자의 후원 내역 조회
    @GetMapping("/fundings/{username}")
    @PreAuthorize("isAuthenticated()")
     fun  getFundingsByUsername(@PathVariable username: String): ResponseEntity<Any> {
        val user = userService.getUserByName(username)
        return ResponseEntity.ok(mapOf("status" to "success", "data" to myFundingService.getMyFundings(user.userId)))
    }

    // ✅ 로그인한 수혜자의 프로젝트 목록 조회
    @GetMapping("/projects/{username}")
    @PreAuthorize("hasRole('ROLE_BENEFICIARY') and #username == authentication.name")
     fun getMyProjects(@PathVariable username: String): ResponseEntity<Any> {
        val projects = myProjectService.getProjectsByUser(username)


        // ✅ 수정 코드
        return ResponseEntity.ok(mapOf("status" to "success", "data" to projects))
    }

}
