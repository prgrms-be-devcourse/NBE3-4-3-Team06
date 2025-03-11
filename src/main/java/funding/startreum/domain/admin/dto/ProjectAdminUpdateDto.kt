package funding.startreum.domain.admin.dto

import funding.startreum.domain.project.entity.Project

data class ProjectAdminUpdateDto(
    var isApproved: Project.ApprovalStatus? = null, // 승인 상태 (승인, 대기중, 거부)
    var status: Project.Status? = null, // 프로젝트 진행 상태 (진행중, 성공, 실패)
    var isDeleted: Boolean? = null // 프로젝트 삭제 여부 (false: 미삭제, true: 삭제)
)
