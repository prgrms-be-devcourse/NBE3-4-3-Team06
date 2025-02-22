package Funding.Startreum.domain.project.service;

import Funding.Startreum.domain.project.dto.*;
import Funding.Startreum.domain.project.entity.Project;
import Funding.Startreum.domain.project.repository.ProjectRepository;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import Funding.Startreum.common.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ProjectService projectService;

    private static final String USER_NAME = "testUser";
    private String JWT_TOKEN;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName(USER_NAME);
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.BENEFICIARY);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // JWT 토큰 생성
        JWT_TOKEN = jwtUtil.generateAccessToken(testUser.getName(), testUser.getEmail(), testUser.getRole().name());


        // email로 사용자 조회
        Mockito.when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(testUser));
    }

    @Test
    void createProject() {
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto(
                "Project Title",
                "Short Description",
                "Full Description",
                BigDecimal.valueOf(1000),
                "bannerUrl",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(10)
        );

        Project project = new Project();
        project.setTitle("Project Title");
        project.setCreator(testUser);
        project.setSimpleDescription("Short Description");
        project.setDescription("Full Description");
        project.setFundingGoal(BigDecimal.valueOf(1000));
        project.setBannerUrl("bannerUrl");
        project.setStartDate(LocalDateTime.now());
        project.setEndDate(LocalDateTime.now().plusDays(10));

        Mockito.when(projectRepository.save(Mockito.any(Project.class))).thenReturn(project);

        ProjectCreateResponseDto response = projectService.createProject(requestDto, USER_NAME);

        assertNotNull(response);
        assertEquals("Project Title", response.title());
        assertNotNull(response.createdAt());
    }

    @Test
    void modifyProject() {
        Integer projectId = 1;
        ProjectUpdateRequestDto requestDto = new ProjectUpdateRequestDto(
                "Updated Title",
                "Updated Description",
                BigDecimal.valueOf(2000),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(20)
        );

        Project existingProject = new Project();
        existingProject.setProjectId(projectId);
        existingProject.setCreator(testUser);
        existingProject.setTitle("Original Title");

        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        Mockito.when(projectRepository.save(Mockito.any(Project.class))).thenReturn(existingProject);

        ProjectUpdateResponseDto response = projectService.modifyProject(projectId, requestDto, JWT_TOKEN);

        assertNotNull(response);
        assertEquals("Updated Title", response.title());
    }

    @Test
    void deleteProject() {
        Integer projectId = 1;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setCreator(testUser);

        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        projectService.deleteProject(projectId, JWT_TOKEN);

        Mockito.verify(projectRepository).delete(project);
    }

    @Test
    void requestApprove() {
        Integer projectId = 1;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setCreator(testUser);
        project.setIsApproved(Project.ApprovalStatus.AWAITING_APPROVAL);

        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        ProjectApprovalResponseDto response = projectService.requestApprove(projectId, JWT_TOKEN);

        assertNotNull(response);
        assertEquals("AWAITING_APPROVAL", response.status());
    }
}
