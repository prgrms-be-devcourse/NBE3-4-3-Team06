package funding.startreum.domain.users.controller

import com.fasterxml.jackson.databind.ObjectMapper
import funding.startreum.common.config.TestSecurityConfig
import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.users.dto.LoginRequest
import funding.startreum.domain.users.dto.SignupRequest
import funding.startreum.domain.users.dto.UserResponse
import funding.startreum.domain.users.entity.User
import funding.startreum.domain.users.repository.RefreshTokenRepository
import funding.startreum.domain.users.service.MyFundingService
import funding.startreum.domain.users.service.MyProjectService
import funding.startreum.domain.users.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class, MockitoExtension::class)
@WebMvcTest(UserController::class)
@Import(TestSecurityConfig::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var jwtUtil: JwtUtil  // ✅ @Mock -> @MockitoBean으로 변경

    @MockitoBean
    private lateinit var myFundingService: MyFundingService

    @MockitoBean
    private lateinit var myProjectService: MyProjectService

    @MockitoBean
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }



    @Test
    fun `회원가입 성공`() {
        val request = SignupRequest("testUser", "test@example.com", "password123", User.Role.valueOf("SPONSOR"))


        doNothing().`when`(userService).registerUser(request)

        mockMvc.perform(post("/api/users/registrar")
            .param("name", request.name)
            .param("email", request.email)
            .param("password", request.password)
            .param("role", request.role?.name ?: "USER")
        )
            .andExpect(status().isFound)
            .andExpect(header().string("Location", "/"))
    }



    @Test
    fun `로그인 성공`() {
        val request = LoginRequest("testUser", "password123")
        val userResponse = UserResponse(
            name = "testUser",
            email = "test@example.com",
            role = User.Role.SPONSOR, // ✅ 존재하는 값 사용
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val accessToken = "access-token"
        val refreshToken = "refresh-token"

        `when`(userService.authenticateUser(request.name, request.password)).thenReturn(userResponse)
        `when`(jwtUtil.generateAccessToken(userResponse.name, userResponse.email, userResponse.role?.name ?: "USER")).thenReturn(accessToken)
        `when`(jwtUtil.generateRefreshToken(userResponse.name)).thenReturn(refreshToken)

        mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value(accessToken))
            .andExpect(jsonPath("$.refreshToken").value(refreshToken))
    }

    @Test
    fun `로그아웃 성공`() {
        mockMvc.perform(post("/api/users/logout"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("success"))
    }

    @Test
    fun `사용자 프로필 조회 성공`() {
        val user = User("testUser", "test@example.com", "password123", User.Role.SPONSOR, LocalDateTime.now(), LocalDateTime.now())

        `when`(userService.getUserByName("testUser")).thenReturn(user) // ✅ User 반환

        mockMvc.perform(get("/api/users/profile/testUser"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.data.name").value(user.name)) // ✅ user.name 사용
    }
}
