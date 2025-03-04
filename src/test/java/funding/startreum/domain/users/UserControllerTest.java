package funding.startreum.domain.users;

import funding.startreum.common.util.JwtUtil;
import funding.startreum.domain.users.controller.UserController;
import funding.startreum.domain.users.dto.UserResponse;
import funding.startreum.domain.users.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)  // Mockito 확장 사용
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    @DisplayName("ID 중복 확인 API - 중복 아닐 때")
    void checkNameDuplicate_noDuplicate() throws Exception {
        String testName = "testUser";
        when(userService.isNameDuplicate(testName)).thenReturn(false);

        mockMvc.perform(get("/api/users/check-name")
                        .param("name", testName))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("회원가입 API - 성공")
    void registerUser_success() throws Exception {
        mockMvc.perform(post("/api/users/registrar")
                        .param("name", "newUser")
                        .param("email", "newuser@example.com")
                        .param("password", "password123")
                        .param("role", "SPONSOR"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/"));
    }

    @Test
    @DisplayName("로그인 API - 성공")
    void loginUser_success() throws Exception {
        // Mock UserResponse 객체 생성
        UserResponse mockUserResponse = new UserResponse("newUser", "newuser@example.com", User.Role.SPONSOR, null, null);

        // userService.authenticateUser()가 호출되면 mockUserResponse 반환
        when(userService.authenticateUser("newUser", "password123")).thenReturn(mockUserResponse);

        // jwtUtil.generateAccessToken()와 generateRefreshToken() 설정
        when(jwtUtil.generateAccessToken(anyString(), anyString(), anyString())).thenReturn("mockAccessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("mockRefreshToken");

        // refreshTokenRepository.deleteByUsername()는 void 반환 -> doNothing() 사용
        doNothing().when(refreshTokenRepository).deleteByUsername(anyString());

        // 실제 테스트 실행
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"newUser\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mockAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("mockRefreshToken"))
                .andExpect(jsonPath("$.userName").value("newUser"));
    }
}
