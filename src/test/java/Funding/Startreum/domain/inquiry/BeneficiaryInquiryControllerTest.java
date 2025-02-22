package Funding.Startreum.domain.inquiry;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BeneficiaryInquiryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        // ✅ 1. 테스트용 사용자 생성
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password123");
        user.setRole(User.Role.BENEFICIARY);
        user.setCreatedAt(LocalDateTime.now());  // createdAt 설정
        user.setUpdatedAt(LocalDateTime.now());  // updatedAt 설정
        user.setName("testUser");
        userRepository.save(user);

        // ✅ 2. JWT 토큰 생성
        token = jwtUtil.generateAccessToken("testUser", "test@test.com", "BENEFICIARY");
        System.out.println("token: " + token);

    }

    @Test
    void testCreateInquiry_Success() throws Exception {
        // 🔹 문의 생성 요청 JSON 데이터
        String requestBody = """
                {
                  "title": "테스트 문의 제목",
                  "content": "테스트 문의 내용입니다."
                }
                """;

        // 🔹 요청 실행
        mockMvc.perform(post("/api/beneficiary/inquiries")
                        .header("Authorization", "Bearer " + token) // 발급한 액세스 토큰 사용
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated()) // 201 응답 확인
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("문의 생성 성공."))
                .andExpect(jsonPath("$.data.title").value("테스트 문의 제목"))
                .andExpect(jsonPath("$.data.content").value("테스트 문의 내용입니다."))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
}