package funding.startreum.domain.project;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 프로젝트 상세 정보를 제공하는 REST 컨트롤러.
 * - 클라이언트가 `/api/projects/{projectId}`를 호출하면 JSON 데이터를 반환.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectDetailController {

    private final ProjectDetailService projectDetailService;

    public ProjectDetailController(ProjectDetailService projectDetailService) {
        this.projectDetailService = projectDetailService;
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailDto> getProjectDetail(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectDetailService.getProjectDetail(projectId));
    }
}