package funding.startreum.domain.project.repository;

import funding.startreum.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    List<Project> findByIsApproved(Project.ApprovalStatus approvalStatus);

    /**
     * 프로젝트 검색(거래 내역 ID기반)
     * @param transactionId 거래 내역 ID
     * @return 프로젝트
     */
    @Query("SELECT f.project FROM Transaction t JOIN t.funding f WHERE t.transactionId = :transactionId")
    Project findProjectByTransactionId(@Param("transactionId") Integer transactionId);
}
