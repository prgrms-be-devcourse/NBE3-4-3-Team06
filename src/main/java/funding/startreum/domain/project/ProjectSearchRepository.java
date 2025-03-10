package funding.startreum.domain.project;


import funding.startreum.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 프로젝트 검색을 위한 JPA Repository 인터페이스.
 * - 승인된 프로젝트만 검색하도록 설정 (`isApproved = 'APPROVE'`)
 */
public interface ProjectSearchRepository extends JpaRepository<Project, Integer> {

    /**
     * 특정 상태를 가진 프로젝트 중 제목 또는 설명에 특정 키워드가 포함된 프로젝트 검색 (승인된 프로젝트만).
     *
     * @param keyword  검색할 키워드 (제목 또는 설명)
     * @param status   프로젝트 상태 (ONGOING, SUCCESS, FAILED)
     * @param pageable 페이지네이션 정보
     * @return 검색된 프로젝트 목록 (페이지 형식)
     */
    @Query("SELECT p FROM Project p WHERE (p.title LIKE %:keyword% OR p.description LIKE %:keyword%) " +
            "AND p.status = :status AND p.isApproved = 'APPROVE'")
    Page<Project> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") Project.Status status, Pageable pageable);

    /**
     * 제목 또는 설명에 특정 키워드가 포함된 승인된 프로젝트 검색 (상태 필터링 없음).
     *
     * @param keyword  검색할 키워드 (제목 또는 설명)
     * @param pageable 페이지네이션 정보
     * @return 검색된 프로젝트 목록 (페이지 형식)
     */
    @Query("SELECT p FROM Project p WHERE (p.title LIKE %:keyword% OR p.description LIKE %:keyword%) " +
            "AND p.isApproved = 'APPROVE'")
    Page<Project> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 검색어 없이 모든 승인된 프로젝트 조회
     *
     * @param pageable 페이지네이션 정보
     * @return 승인된 전체 프로젝트 목록 (페이지 형식)
     */
    @Query("SELECT p FROM Project p WHERE p.isApproved = 'APPROVE'")
    Page<Project> findAllApproved(Pageable pageable);
}