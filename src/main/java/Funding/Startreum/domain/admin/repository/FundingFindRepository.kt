package funding.startreum.domain.admin.repository

import funding.startreum.domain.funding.entity.Funding
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface FundingFindRepository : JpaRepository<Funding, Int> {

    // 특정 프로젝트의 환등되지 않은 후원 내역 조회 (isDeleted = false)
    @Query("SELECT f FROM Funding f WHERE f.project.projectId = :projectId AND f.isDeleted = false")
    fun findActiveFundingsByProjectId(projectId: Int): List<Funding>

    // 특정 프로젝트의 환등된 후원 내역 조회 (isDeleted = true)
    @Query("SELECT f FROM Funding f WHERE f.project.projectId = :projectId AND f.isDeleted = true")
    fun findRefundedFundingsByProjectId(projectId: Int): List<Funding>

    // 특정 프로젝트의 모든 후원 내역 조회 (isDeleted 유무 상관없이)
    fun findByProject_ProjectId(projectId: Int): List<Funding>

    // 환등 시 후원 내역의 isDeleted 값을 true로 변경
    @Modifying
    @Transactional
    @Query("UPDATE Funding f SET f.isDeleted = true WHERE f.project.projectId = :projectId")
    fun markFundingsAsRefunded(projectId: Int)
}
