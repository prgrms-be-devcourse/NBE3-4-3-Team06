package funding.startreum.domain.users.dto

import funding.startreum.domain.project.entity.Project
import java.math.BigDecimal
import java.time.LocalDateTime

data class MyProjectDTO(
    val title: String,           // 프로젝트 타이틀
    val fundingGoal: BigDecimal, // 목표 금액
    val createdAt: LocalDateTime, // 생성일
    val status: Project.Status,  // 프로젝트 상태
    val isApproved: Project.ApprovalStatus // 승인 여부
) {
    companion object {
        // 엔티티로부터 DTO 변환 메서드
        fun from(project: Project): MyProjectDTO {
            return MyProjectDTO(
                title = project.title,
                fundingGoal = project.fundingGoal,
                createdAt = project.createdAt,
                status = project.status,
                isApproved = project.isApproved
            )
        }
    }
}
