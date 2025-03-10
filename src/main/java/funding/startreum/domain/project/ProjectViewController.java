package funding.startreum.domain.project;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 프로젝트 검색 페이지를 반환하는 컨트롤러.
 * - 데이터는 REST API를 통해 로드됨.
 */
@Controller
public class ProjectViewController {

    @GetMapping("/projects/search")
    public String showSearchPage() {
        return "projects/search"; // templates/projects/search.html 뷰 반환

    }

    @GetMapping("/projects/{projectId}")
    public String showProjectDetailPage(@PathVariable Integer projectId) {
        return "projects/project-detail"; // templates/projects/project-detail.html 뷰 반환
    }

}