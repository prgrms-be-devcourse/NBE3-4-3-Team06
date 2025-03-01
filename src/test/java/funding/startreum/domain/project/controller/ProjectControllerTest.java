package funding.startreum.domain.project.controller;

import funding.startreum.common.util.JwtUtil;
import funding.startreum.domain.project.dto.*;
import funding.startreum.domain.project.entity.Project;
import funding.startreum.domain.project.repository.ProjectRepository;
import funding.startreum.domain.project.service.ProjectService;
import funding.startreum.domain.users.User;
import funding.startreum.domain.users.UserRepository;  // UserRepository import 추가
import funding.startreum.domain.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired  // UserRepository 주입
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @MockitoBean
    private ProjectService projectService;

    private String token;
    private Integer projectId;

    @BeforeEach
    void setUp() {
        // ✅ 1. 테스트용 사용자 생성
        User user = new User();
        user.setEmail("test1234567@test.com");
        user.setPassword("password123"); // 비밀번호 암호화는 생략 (테스트 환경)
        user.setRole(User.Role.BENEFICIARY);
        user.setCreatedAt(LocalDateTime.now());  // createdAt 설정
        user.setUpdatedAt(LocalDateTime.now());  // updatedAt 설정
        user.setName("testUser6");
        userRepository.save(user);

        // ✅ 2. JWT 토큰 생성
        token = jwtUtil.generateAccessToken("testUser6", "test1234567@test.com", "BENEFICIARY");
        System.out.println("token: " + token);

        // ✅ 3. 테스트용 프로젝트 생성
        Project project = new Project();
        project.setTitle("기존 프로젝트 제목");
        project.setDescription("기존 프로젝트 설명");
        project.setFundingGoal(new BigDecimal(1000000));
        project.setStartDate(LocalDateTime.of(2025, 2, 1, 0, 0));
        project.setEndDate(LocalDateTime.of(2025, 3, 1, 0, 0));
        project.setCreator(user);
        project.setSimpleDescription("간단한 설명");
        projectRepository.save(project);
        System.out.println(project.getCreator().getUserId());

        projectId = project.getProjectId();
    }

    @Test
    @DisplayName("프로젝트 생성 테스트")
    void testCreateProject() throws Exception {
        // 프로젝트 생성 요청 DTO
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto(
                "Test Project", // title
                "simple description",
                "Description of test project", // description
                new BigDecimal(100000), // fundingGoal
                "https://example.com/banner.jpg", // bannerUrl
                LocalDateTime.now(), // startDate
                LocalDateTime.now().plusMonths(1) // endDate
        );

        // 프로젝트 생성 응답 DTO
        ProjectCreateResponseDto responseDto = new ProjectCreateResponseDto(8, "Test Project", LocalDateTime.now());

        // ProjectService의 createProject 메서드가 호출될 때 responseDto를 반환하도록 설정
        BDDMockito.given(projectService.createProject(any(ProjectCreateRequestDto.class), any(String.class)))
                .willReturn(responseDto);

        // 요청 보내기
        ResultActions result = mockMvc.perform(post("/api/beneficiary/create/projects")
                .header("Authorization", "Bearer " + token)  // Authorization 헤더에 Bearer 토큰 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test Project\",\"simpleDescription\":\"simple description\",\"description\":\"Description of test project\",\"fundingGoal\":100000,\"bannerUrl\":\"https://example.com/banner.jpg\",\"startDate\":\"2025-01-01T00:00:00\",\"endDate\":\"2025-02-01T00:00:00\"}"));

        // 응답 검증
        result.andExpect(status().isCreated()); // 응답 코드가 201 Created이어야 함
    }
    @Test
    @DisplayName("프로젝트 수정 성공 테스트")
    void testModifyProject() throws Exception {
        // ✅ 4. 수정 요청 DTO 생성
        ProjectUpdateRequestDto requestDto = new ProjectUpdateRequestDto(
                "수정된 프로젝트 제목",
                "수정된 프로젝트 설명",
                new BigDecimal(1500000),
                LocalDateTime.of(2025, 2, 10, 0, 0),
                LocalDateTime.of(2025, 3, 10, 0, 0)
        );

        // ✅ 5. ProjectUpdateResponseDto 모의 응답 설정
        ProjectUpdateResponseDto responseDto = new ProjectUpdateResponseDto(
                projectId,
                requestDto.title(),
                requestDto.description(),
                requestDto.fundingGoal(),
                requestDto.startDate(),
                requestDto.endDate(),
                LocalDateTime.now() // 수정된 시간
        );

        BDDMockito.given(projectService.modifyProject(any(Integer.class), any(ProjectUpdateRequestDto.class), any(String.class)))
                .willReturn(responseDto);

        // ✅ 6. PUT 요청 수행
        ResultActions result = mockMvc.perform(put("/api/beneficiary/modify/" + projectId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "수정된 프로젝트 제목",
                          "description": "수정된 프로젝트 설명",
                          "fundingGoal": 1500000,
                          "startDate": "2025-02-10T00:00:00",
                          "endDate": "2025-03-10T00:00:00"
                        }
                        """));
        // ✅ 7. 응답 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("프로젝트 수정에 성공하였습니다."))
                .andExpect(jsonPath("$.data.projectId").value(projectId))
                .andExpect(jsonPath("$.data.title").value("수정된 프로젝트 제목"))
                .andExpect(jsonPath("$.data.description").value("수정된 프로젝트 설명"))
                .andExpect(jsonPath("$.data.fundingGoal").value(1500000))
                .andExpect(jsonPath("$.data.startDate").value("2025-02-10T00:00:00"))
                .andExpect(jsonPath("$.data.endDate").value("2025-03-10T00:00:00"));
    }
    @Test
    @DisplayName("프로젝트 삭제 성공 테스트")
    void testDeleteProject() throws Exception {
        // ✅ 4. projectService.deleteProject 모의 응답 설정
        BDDMockito.doNothing().when(projectService).deleteProject(any(Integer.class), any(String.class));

        // ✅ 5. DELETE 요청 수행
        ResultActions result = mockMvc.perform(delete("/api/beneficiary/delete/" + projectId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));

        // ✅ 6. 응답 검증: HTTP 204 상태 코드 및 응답 본문 없음
        result.andExpect(status().isNoContent());  // 204 상태 코드
    }

    @Test
    @DisplayName("프로젝트 승인 요청 성공 테스트")
    void testRequestApprovalSuccess() throws Exception {
        // Mocking ProjectService의 응답
        ProjectApprovalResponseDto mockResponse = new ProjectApprovalResponseDto(
                200,
                "AWAITING_APPROVAL",
                "승인 요청에 성공하였습니다.",
                new ProjectApprovalResponseDto.Data(
                        1,  // 예시 projectId
                        LocalDateTime.now()
                )
        );

        // ProjectService의 requestApprove 메서드가 호출될 때 mockResponse 반환
        BDDMockito.given(projectService.requestApprove(anyInt(), anyString()))
                .willReturn(mockResponse);

        // MockMvc를 사용하여 POST 요청 수행
        ResultActions result = mockMvc.perform(post("/api/beneficiary/requestApprove/" + 1)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));

        // 응답 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.status").value("AWAITING_APPROVAL"))
                .andExpect(jsonPath("$.message").value("승인 요청에 성공하였습니다."))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.requestedAt").exists());
    }
}
