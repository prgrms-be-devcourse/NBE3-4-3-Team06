package funding.startreum.domain.admin;

import funding.startreum.domain.project.entity.Project;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/projects")
public class AdminProjectApiController {

    private final funding.startreum.domain.admin.ProjectAdminRepository projectAdminRepository;
    private final funding.startreum.domain.admin.ProjectAdminService projectAdminService;

    public AdminProjectApiController(funding.startreum.domain.admin.ProjectAdminRepository projectAdminRepository, funding.startreum.domain.admin.ProjectAdminService projectAdminService) {
        this.projectAdminRepository = projectAdminRepository;
        this.projectAdminService = projectAdminService;
    }


    /**
     * 🔹 프로젝트 목록 조회 (is_approved 상태 필터링 가능)
     */
    @GetMapping
    public ResponseEntity<List<funding.startreum.domain.admin.ProjectAdminSearchDto>> getProjectsByApprovalStatus(
            @RequestParam(required = false) String status,
            Authentication authentication
    ) {
        // 🔍 현재 로그인한 사용자의 권한 확인
        if (authentication == null || authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .noneMatch(role -> role.equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(null);
        }

        List<Project> projects;

        // 상태 필터링 적용
        if (status != null && !status.isBlank()) {
            try {
                Project.ApprovalStatus approvalStatus = Project.ApprovalStatus.valueOf(status.toUpperCase());
                projects = projectAdminRepository.findByIsApproved(approvalStatus);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            projects = projectAdminRepository.findAll(); // 전체 조회
        }

        // ✅ 관리자용 DTO로 변환하여 반환
        List<funding.startreum.domain.admin.ProjectAdminSearchDto> projectDtos = projects.stream()
                .map(funding.startreum.domain.admin.ProjectAdminSearchDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(projectDtos);
    }

    /**
     * 🔹 프로젝트 승인 및 진행 상태 변경 API
     */
    @PatchMapping("/{projectId}/update")
    public ResponseEntity<String> updateProjectStatus(
            @PathVariable Integer projectId,
            @RequestBody funding.startreum.domain.admin.ProjectAdminUpdateDto updateDto,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .noneMatch(role -> role.equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body("❌ 권한 없음");
        }

        // Repository 호출 대신 Service 호출로 변경
        projectAdminService.updateProject(projectId, updateDto);

        return ResponseEntity.ok("✅ 프로젝트 상태가 변경되었습니다.");
    }
}