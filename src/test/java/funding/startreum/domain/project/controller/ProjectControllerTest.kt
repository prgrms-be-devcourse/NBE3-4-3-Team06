package funding.startreum.domain.project.controller

import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.project.dto.*
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.project.repository.ProjectRepository
import funding.startreum.domain.project.service.ProjectService
import funding.startreum.domain.users.entity.User
import funding.startreum.domain.users.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
open class ProjectControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jwtUtil: JwtUtil

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var projectRepository: ProjectRepository

    @MockitoBean
    lateinit var projectService: ProjectService

    private lateinit var token: String
    private var projectId: Int = 0

    @BeforeEach
    @Transactional
    open fun setUp() {
        // 1. 테스트용 사용자 생성
        val user = User().apply {
            email = "test1234567@test.com"
            password = "password123" // 비밀번호 암호화는 생략 (테스트 환경)
            role = User.Role.BENEFICIARY
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
            name = "testUser6"
        }
        userRepository.save(user)

        // 2. JWT 토큰 생성
        token = jwtUtil.generateAccessToken("testUser6", "test1234567@test.com", "BENEFICIARY")
        println("token: $token")

        // 3. 테스트용 프로젝트 생성
        val project = Project().apply {
            title = "기존 프로젝트 제목"
            description = "기존 프로젝트 설명"
            fundingGoal = BigDecimal(1000000)
            startDate = LocalDateTime.of(2025, 2, 1, 0, 0)
            endDate = LocalDateTime.of(2025, 3, 1, 0, 0)
            creator = user
            simpleDescription = "간단한 설명"
        }
        projectRepository.save(project)

        projectId = project.projectId!!
    }

    @Test
    @DisplayName("프로젝트 생성 테스트")
    @Transactional
    open fun testCreateProject() {
        // 프로젝트 생성 요청 DTO
        val requestDto = ProjectCreateRequestDto(
            "Test Project",
            "simple description",
            "Description of test project",
            BigDecimal(100000),
            "https://example.com/banner.jpg",
            LocalDateTime.now(),
            LocalDateTime.now().plusMonths(1)
        )

        // 프로젝트 생성 응답 DTO
        val responseDto = ProjectCreateResponseDto(8, "Test Project", LocalDateTime.now())

        // ProjectService의 createProject 메서드가 호출될 때 responseDto를 반환하도록 설정
        BDDMockito.given(projectService.createProject(any(), any()))
            .willReturn(responseDto)

        // 요청 보내기
        val result: ResultActions = mockMvc.perform(
            post("/api/beneficiary/create/projects")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Test Project",
                      "simpleDescription": "simple description",
                      "description": "Description of test project",
                      "fundingGoal": 100000,
                      "bannerUrl": "https://example.com/banner.jpg",
                      "startDate": "2025-01-01T00:00:00",
                      "endDate": "2025-02-01T00:00:00"
                    }
                """)
        )

        // 응답 검증
        result.andExpect(status().isCreated)
    }

    @Test
    @DisplayName("프로젝트 수정 성공 테스트")
    @Transactional
    open fun testModifyProject() {
        // 수정 요청 DTO 생성
        val requestDto = ProjectUpdateRequestDto(
            "수정된 프로젝트 제목",
            "수정된 프로젝트 설명",
            BigDecimal(1500000),
            LocalDateTime.of(2025, 2, 10, 0, 0),
            LocalDateTime.of(2025, 3, 10, 0, 0)
        )

        // ProjectUpdateResponseDto 모의 응답 설정
        val responseDto = ProjectUpdateResponseDto(
            projectId,
            requestDto.title,
            requestDto.description,
            requestDto.fundingGoal,
            requestDto.startDate,
            requestDto.endDate,
            LocalDateTime.now()
        )

        BDDMockito.given(projectService.modifyProject(any(), any(), any()))
            .willReturn(responseDto)

        // PUT 요청 수행
        val result: ResultActions = mockMvc.perform(
            put("/api/beneficiary/modify/$projectId")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "수정된 프로젝트 제목",
                      "description": "수정된 프로젝트 설명",
                      "fundingGoal": 1500000,
                      "startDate": "2025-02-10T00:00:00",
                      "endDate": "2025-03-10T00:00:00"
                    }
                """)
        )

        // 응답 검증
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.message").value("프로젝트 수정에 성공하였습니다."))
            .andExpect(jsonPath("$.data.projectId").value(projectId))
            .andExpect(jsonPath("$.data.title").value("수정된 프로젝트 제목"))
            .andExpect(jsonPath("$.data.description").value("수정된 프로젝트 설명"))
            .andExpect(jsonPath("$.data.fundingGoal").value(1500000))
            .andExpect(jsonPath("$.data.startDate").value("2025-02-10T00:00:00"))
            .andExpect(jsonPath("$.data.endDate").value("2025-03-10T00:00:00"))
    }

    @Test
    @DisplayName("프로젝트 삭제 성공 테스트")
    @Transactional
    open fun testDeleteProject() {
        // projectService.deleteProject 모의 응답 설정
        BDDMockito.doNothing().`when`(projectService).deleteProject(any(), any())

        // DELETE 요청 수행
        val result: ResultActions = mockMvc.perform(
            delete("/api/beneficiary/delete/$projectId")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )

        // 응답 검증: HTTP 204 상태 코드 및 응답 본문 없음
        result.andExpect(status().isNoContent)
    }

    @Test
    @DisplayName("프로젝트 승인 요청 성공 테스트")
    @Transactional
    open fun testRequestApprovalSuccess() {
        // Mocking ProjectService의 응답
        val mockResponse = ProjectApprovalResponseDto(
            statusCode = 200,
            status = "AWAITING_APPROVAL",
            message = "승인 요청에 성공하였습니다.",
            data = ProjectApprovalResponseDto.Data(
                projectId = 1,
                requestedAt = LocalDateTime.now()
            )
        )

        // ProjectService의 requestApprove 메서드가 호출될 때 mockResponse 반환
        BDDMockito.given(projectService.requestApprove(any(), any()))
            .willReturn(mockResponse)

        // POST 요청 수행
        val result: ResultActions = mockMvc.perform(
            post("/api/beneficiary/requestApprove/1")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        )

        // 응답 검증
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.status").value("AWAITING_APPROVAL"))
            .andExpect(jsonPath("$.message").value("승인 요청에 성공하였습니다."))
            .andExpect(jsonPath("$.data.projectId").value(1))
            .andExpect(jsonPath("$.data.requestedAt").exists())
    }
}
