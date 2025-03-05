package funding.startreum.domain.admin

import funding.startreum.domain.project.entity.Project
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 관리자용 프로젝트 조회 DTO.
 */
data class ProjectAdminSearchDto(
    val projectId: Int?,  // ✅ nullable로 변경하여 null 가능성 허용
    val title: String,
    val description: String,
    val fundingGoal: BigDecimal,
    val currentFunding: BigDecimal,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val status: Project.Status,
    val isApproved: Project.ApprovalStatus
)  {
    companion object {
        /**
         * Project 엔티티를 ProjectAdminSearchDto로 변환하는 정적 메서드.
         * @param project 변환할 Project 엔티티
         * @return 변환된 ProjectAdminSearchDto 객체
         */
        fun from(project: Project): ProjectAdminSearchDto {
            return ProjectAdminSearchDto(
                projectId = project.projectId, // nullable 허용
                title = project.title,
                description = project.description,
                fundingGoal = project.fundingGoal,
                currentFunding = project.currentFunding,
                startDate = project.startDate,
                endDate = project.endDate,
                status = project.status,
                isApproved = project.isApproved
            )
        }
    }
}