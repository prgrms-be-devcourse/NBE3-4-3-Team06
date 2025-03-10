package funding.startreum.domain.admin.repository

import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.project.entity.Project.ApprovalStatus
import funding.startreum.domain.project.entity.Project.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

interface ProjectAdminRepository : JpaRepository<Project, Int> {

    /**
     * 🔹 특정 프로젝트의 승인 상태(isApproved) 변경
     */
    @Modifying
    @Transactional
    @Query("UPDATE Project p SET p.isApproved = :isApproved WHERE p.projectId = :projectId")
    fun updateApprovalStatus(@Param("projectId") projectId: Int, @Param("isApproved") isApproved: ApprovalStatus): Int

    /**
     * 🔹 특정 프로젝트의 진행 상태(status) 변경
     */
    @Modifying
    @Transactional
    @Query("UPDATE Project p SET p.status = :status WHERE p.projectId = :projectId")
    fun updateProjectStatus(@Param("projectId") projectId: Int, @Param("status") status: Status): Int

    /**
     * 🔹 특정 프로젝트의 삭제 상태(isDeleted) 변경
     */
    @Modifying
    @Transactional
    @Query("UPDATE Project p SET p.isDeleted = :isDeleted WHERE p.projectId = :projectId")
    fun updateIsDeleted(@Param("projectId") projectId: Int, @Param("isDeleted") isDeleted: Boolean): Int

    /**
     * 🔹 승인 상태(isApproved)로 프로젝트 목록 조회 (관리자용)
     */
    fun findByIsApproved(approvalStatus: ApprovalStatus): List<Project>

    /**
     * 🔹 종료된데도 아직 성공 또는 실패 처리가 되지 않은 프로젝트 조회
     */
    fun findByEndDateBeforeAndStatusNotIn(endDate: LocalDateTime, statuses: List<Project.Status>): List<Project>


    /**
     * 🔹 특정 상태(Status)에 있고, 승인된(Approve) 프로젝트 조회
     * ✅ 자동 승인 거절을 위해 사용
     */
    fun findByStatusInAndIsApproved(statuses: List<Project.Status>, isApproved: ApprovalStatus): List<Project>

}
