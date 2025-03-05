package funding.startreum.domain.project.service

import funding.startreum.domain.project.dto.*
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.project.repository.ProjectRepository
import funding.startreum.domain.users.entity.User
import funding.startreum.domain.users.repository.UserRepository
import funding.startreum.common.util.JwtUtil
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

        // JWT 토큰 생성
        JWT_TOKEN = jwtUtil.generateAccessToken(testUser.name, testUser.email, testUser.role.name)

        // email로 사용자 조회
        Mockito.`when`(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(testUser))
    }

    @Test
    fun createProject() {
        val requestDto = ProjectCreateRequestDto(
            "Project Title",
            "Short Description",
            "Full Description",
            BigDecimal.valueOf(1000),
            "bannerUrl",
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(10)
        )

        val project = Project().apply {
            title = "Project Title"
            creator = testUser
            simpleDescription = "Short Description"
            description = "Full Description"
            fundingGoal = BigDecimal.valueOf(1000)
            bannerUrl = "bannerUrl"
            startDate = LocalDateTime.now()
            endDate = LocalDateTime.now().plusDays(10)
        }

        Mockito.`when`(projectRepository.save(Mockito.any(Project::class.java))).thenReturn(project)

        val response = projectService.createProject(requestDto, USER_NAME)

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
            creator = testUser
            title = "Original Title"
        }

        Mockito.`when`(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject))
        Mockito.`when`(projectRepository.save(Mockito.any(Project::class.java))).thenReturn(existingProject)

        val response = projectService.modifyProject(projectId, requestDto, JWT_TOKEN)

        assertNotNull(response)
        assertEquals("Updated Title", response.title)
    }

    @Test
    fun deleteProject() {
        val projectId = 1
        val project = Project().apply {
            this.projectId = projectId
            creator = testUser
        }

        Mockito.`when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))

        projectService.deleteProject(projectId, JWT_TOKEN)

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
