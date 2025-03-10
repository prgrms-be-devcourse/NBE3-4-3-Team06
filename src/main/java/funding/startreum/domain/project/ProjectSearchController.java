package funding.startreum.domain.project;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 프로젝트 검색 API 컨트롤러.
 * - 검색어가 없을 경우 전체 승인된 프로젝트 반환.
 * - 검색어가 있을 경우 제목 또는 설명에서 검색.
 * - 상태 필터링 가능.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectSearchController {

    private final ProjectSearchService projectSearchService;

    public ProjectSearchController(ProjectSearchService projectSearchService) {
        this.projectSearchService = projectSearchService;
    }

    /**
     * 🔹 검색 API
     * - `/api/projects/search` : 전체 승인된 프로젝트 조회
     * - `/api/projects/search/{query}` : 특정 검색어에 맞는 프로젝트 조회
     */
    @GetMapping("/search")
    public Map<String, Object> searchProjects(
            @RequestParam(required = false) String query, // ✅ @RequestParam 사용
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (query != null) {
            query = query.replaceAll("[^a-zA-Z0-9가-힣]", "").trim();
        }

        List<ProjectSearchDto> projects = (query == null || query.isBlank())
                ? projectSearchService.getAllApprovedProjects(page, size)
                : projectSearchService.searchProjects(query, status, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "프로젝트 검색 성공");
        response.put("totalResults", projects.size());
        response.put("data", projects);
        return response;
    }

}