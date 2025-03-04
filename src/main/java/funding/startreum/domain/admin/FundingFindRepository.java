package funding.startreum.domain.admin;

import funding.startreum.domain.funding.entity.Funding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FundingFindRepository extends JpaRepository<Funding, Integer> {

    // 특정 프로젝트의 환등되지 않은 후원 내역 조회 (isDeleted = false)
    @Query("SELECT f FROM Funding f WHERE f.project.projectId = :projectId AND f.isDeleted = false")
    List<Funding> findActiveFundingsByProjectId(Integer projectId);

    // 특정 프로젝트의 환등된 후원 내역 조회 (isDeleted = true)
    @Query("SELECT f FROM Funding f WHERE f.project.projectId = :projectId AND f.isDeleted = true")
    List<Funding> findRefundedFundingsByProjectId(Integer projectId);

    // 특정 프로젝트의 모든 후원 내역 조회 (ud658등 유무여 상관없이)
    List<Funding> findByProject_ProjectId(Integer projectId);

    // 환등 시 후원 내역의 isDeleted 값을 true로 변경
    @Modifying
    @Transactional
    @Query("UPDATE Funding f SET f.isDeleted = true WHERE f.project.projectId = :projectId")
    void markFundingsAsRefunded(Integer projectId);
}