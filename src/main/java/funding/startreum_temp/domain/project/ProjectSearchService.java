package funding.startreum.domain.project;


import funding.startreum.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 프로젝트 검색을 담당하는 서비스 클래스.
 * - 프로젝트 제목 또는 설명을 기반으로 검색 가능.
 * - 프로젝트 상태(ONGOING, SUCCESS, FAILED) 필터링 가능.
 * - 승인된(`APPROVE`) 프로젝트만 조회 가능.
 */
@Service
public class ProjectSearchService {

    private final ProjectSearchRepository projectSearchRepository;

    public ProjectSearchService(ProjectSearchRepository projectSearchRepository) {
        this.projectSearchRepository = projectSearchRepository;
    }

    /**
     * 검색 조건에 따라 프로젝트 조회
     */
    public List<ProjectSearchDto> searchProjects(String query, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Project> projectPage;

        // ✅ 검색어가 없을 경우 전체 승인된 프로젝트 조회
        if (query == null || query.trim().isEmpty()) {
            return getAllApprovedProjects(page, size);
        }

        // ✅ 검색어가 있을 경우 상태 필터링 적용
        if (status != null && !status.isEmpty()) {
            try {
                Project.Status projectStatus = Project.Status.valueOf(status.toUpperCase());
                projectPage = projectSearchRepository.searchByKeywordAndStatus(query, projectStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("잘못된 상태값입니다. 허용되는 값: ONGOING, SUCCESS, FAILED");
            }
        } else {
            projectPage = projectSearchRepository.searchByKeyword(query, pageable);
        }

        return projectPage.map(ProjectSearchDto::from).toList();
    }

    /**
     * 전체 승인된 프로젝트 조회
     */
    public List<ProjectSearchDto> getAllApprovedProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return projectSearchRepository.findAllApproved(pageable).map(ProjectSearchDto::from).toList();
    }
}