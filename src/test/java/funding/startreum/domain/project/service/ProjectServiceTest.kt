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

        // email로 사용자 조회
        Mockito.`when`(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(testUser))
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
            LocalDateTime.now().plusDays(20)
        )

        val existingProject = Project().apply {
            this.projectId = projectId
            creator = testUser // creator 설정
            title = "Original Title"
            description = "Original Description"
        }

        // 프로젝트 조회 시, Optional.of(existingProject) 반환 설정
        Mockito.`when`(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject))

        // 사용자가 이메일로 조회될 때 testUser를 반환하도록 설정
        Mockito.`when`(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(testUser))

        // ProjectRepository에서 save 메서드를 mock으로 설정하여 프로젝트를 저장
        Mockito.`when`(projectRepository.save(Mockito.any(Project::class.java))).thenReturn(existingProject)

        val response = projectService.modifyProject(projectId, requestDto, JWT_TOKEN)

        // Then: 수정된 프로젝트 응답 검증
        assertNotNull(response)
        assertEquals("Updated Title", response.title)
        assertEquals("Updated Description", response.description)
        assertEquals(BigDecimal.valueOf(2000), response.fundingGoal)
    }

    @Test
    fun deleteProject() {
        val projectId = 1
        val project = Project().apply {
            this.projectId = projectId
            creator = testUser
        }

        // Mocking repository behavior
        Mockito.`when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))

        // When: 프로젝트 삭제 호출
        projectService.deleteProject(projectId, JWT_TOKEN)

        // Then: delete() 메서드가 호출되었는지 검증
        Mockito.verify(projectRepository).delete(project)
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
