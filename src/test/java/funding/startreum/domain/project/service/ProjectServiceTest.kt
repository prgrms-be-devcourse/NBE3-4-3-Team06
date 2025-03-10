package funding.startreum.domain.project.service

import funding.startreum.domain.project.dto.*
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.project.repository.ProjectRepository
import funding.startreum.domain.users.entity.User
import funding.startreum.domain.users.repository.UserRepository
import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.reward.entity.Reward
import funding.startreum.domain.reward.repository.RewardRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ProjectServiceTest {

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var rewardRepository: RewardRepository

    @Mock
    private lateinit var jwtUtil: JwtUtil

    @InjectMocks
    private lateinit var projectService: ProjectService

    private val USER_NAME = "testUser"
    private lateinit var JWT_TOKEN: String
    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser = User().apply {
            name = USER_NAME
            email = "test@example.com"
            role = User.Role.BENEFICIARY
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        // JWT 토큰 mock 설정
        JWT_TOKEN = "mockJwtToken"

        // 불필요한 mock 설정 제거
        Mockito.lenient().`when`(jwtUtil.getEmailFromToken(Mockito.anyString())).thenReturn("test@example.com")
    }


    @Test
    fun getProjectByProjectId() {
        // Given
        val projectId = 1
        val mockProject = Project().apply {
            this.projectId = projectId
            title = "테스트 프로젝트"
            description = "테스트 프로젝트 설명"
            fundingGoal = BigDecimal(1000)
            startDate = LocalDateTime.now()
            endDate = LocalDateTime.now().plusDays(10)
        }

        Mockito.`when`(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject))

        // When
        val project = projectService.getProject(projectId)

        // Then
        assertNotNull(project)
        assertEquals(projectId, project.projectId)
        assertEquals("테스트 프로젝트", project.title)
    }

    @Test
    fun createProject() {
        // Given: Project 생성 요청 DTO
        val requestDto = ProjectCreateRequestDto(
            "Project Title",
            "Short Description",
            "Full Description",
            BigDecimal.valueOf(1000),
            "bannerUrl",
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(10)
        )

        // User 정보를 mock 처리 (실제 유저 객체 생성 없이)
        val mockUser = User().apply {
            name = "testUser"
            email = "test@example.com"
            role = User.Role.BENEFICIARY
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        // 유저가 이메일로 조회될 때 mockUser를 반환하도록 설정
        Mockito.`when`(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(mockUser))

        // Project 객체 설정 (유저는 mock으로 처리된 mockUser를 사용)
        val project = Project().apply {
            title = "Project Title"
            creator = mockUser
            simpleDescription = "Short Description"
            description = "Full Description"
            fundingGoal = BigDecimal.valueOf(1000)
            bannerUrl = "bannerUrl"
            startDate = LocalDateTime.now()
            endDate = LocalDateTime.now().plusDays(10)
            status = Project.Status.ONGOING
            isApproved = Project.ApprovalStatus.AWAITING_APPROVAL
            createdAt = LocalDateTime.now()
            isDeleted = false
        }

        // ProjectRepository에서 save 메서드를 mock으로 설정하여 프로젝트를 저장
        Mockito.`when`(projectRepository.save(Mockito.any(Project::class.java))).thenReturn(project)

        // Reward 객체 설정 (리워드가 저장되는 부분도 mock으로 처리)
        val reward = Reward(
            project = project,
            description = project.simpleDescription,
            amount = BigDecimal.valueOf(10000)
        )
        Mockito.`when`(rewardRepository.save(Mockito.any(Reward::class.java))).thenReturn(reward)

        // When: 프로젝트 생성 호출
        val response = projectService.createProject(requestDto, "test@example.com")

        // Then: 응답 검증
        assertNotNull(response)
        assertEquals("Project Title", response.title)
        assertNotNull(response.createdAt)
    }

    @Test
    fun modifyProject() {
        val projectId = 1
        val requestDto = ProjectUpdateRequestDto(
            "Updated Title",
            "Updated Description",
            BigDecimal.valueOf(2000),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(10)
        )

        val mockUser = User().apply {
            name = "testUser"
            email = "test@example.com"
            role = User.Role.BENEFICIARY
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        val existingProject = Project().apply {
            this.projectId = projectId
            creator = mockUser
            title = "Original Title"
            description = "Original Description"
        }

        // JWT 토큰에서 이메일을 반환하도록 Mock 설정
        Mockito.`when`(jwtUtil.getEmailFromToken(Mockito.anyString())).thenReturn("test@example.com")

        // 유저 조회 Mock 설정
        Mockito.`when`(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser))

        // 프로젝트 조회 Mock 설정
        Mockito.`when`(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject))

        // When: 프로젝트 수정 호출
        val response = projectService.modifyProject(projectId, requestDto, "Bearer mockToken") // ✅ Bearer 추가

        // Then: 응답 검증
        assertNotNull(response)
        assertEquals("Updated Title", response.title)
        assertEquals("Updated Description", response.description)
        assertEquals(BigDecimal.valueOf(2000), response.fundingGoal)
    }


    @Test
    fun deleteProject() {
        val projectId = 1

        val mockUser = User().apply {
            name = "testUser"
            email = "test@example.com"
            role = User.Role.BENEFICIARY
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        val mockProject = Project().apply {
            this.projectId = projectId
            creator = mockUser
        }

        // JWT 토큰에서 이메일을 반환하도록 Mock 설정
        Mockito.`when`(jwtUtil.getEmailFromToken(Mockito.anyString())).thenReturn("test@example.com")

        // 유저 조회 Mock 설정
        Mockito.`when`(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser))

        // ✅ 프로젝트 조회 Mock 설정 추가 (프로젝트가 존재하도록 설정)
        Mockito.`when`(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject))

        // When: 프로젝트 삭제 호출
        projectService.deleteProject(projectId, "Bearer mockToken")

        // Then: delete() 메서드가 한 번 호출되었는지 검증
        Mockito.verify(projectRepository, Mockito.times(1)).delete(mockProject)
    }



    @Test
    fun requestApprove() {
        val projectId = 1
        val project = Project().apply {
            this.projectId = projectId
            creator = testUser
            isApproved = Project.ApprovalStatus.AWAITING_APPROVAL
        }

        Mockito.`when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))

        val response = projectService.requestApprove(projectId, JWT_TOKEN)

        assertNotNull(response)
        assertEquals("AWAITING_APPROVAL", response.status)
    }
}
