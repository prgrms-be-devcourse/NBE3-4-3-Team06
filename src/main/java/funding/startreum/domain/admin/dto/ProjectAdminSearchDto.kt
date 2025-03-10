package funding.startreum.domain.admin.dto

import funding.startreum.domain.project.entity.Project
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 관리자용 프로젝트 조회 DTO.
 */
data class ProjectAdminSearchDto(
    val projectId: Int?,
    val title: String,
    val description: String,
    val fundingGoal: BigDecimal,
    val currentFunding: BigDecimal,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val status: String,
    val isApproved: String  // ✅ isApproved 추가
) {
    companion object {
        /**
         * Project 엔티티를 ProjectAdminSearchDto로 변환하는 정적 메서드.
         * @param project 변환할 Project 엔티티
         * @return 변환된 ProjectAdminSearchDto 객체
         */
        fun from(project: Project): ProjectAdminSearchDto {
            return ProjectAdminSearchDto(
                projectId = project.projectId,
                title = project.title,
                description = project.description,
                fundingGoal = project.fundingGoal,
                currentFunding = project.currentFunding,
                startDate = project.startDate,
                endDate = project.endDate,
                status = project.status?.name ?: "UNKNOWN",
                isApproved = project.isApproved?.name ?: "UNDEFINED"  // ✅ 추가: null 방지
            )
        }
    }
}
