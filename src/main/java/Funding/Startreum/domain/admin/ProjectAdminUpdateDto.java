package Funding.Startreum.domain.admin;

import Funding.Startreum.domain.project.entity.Project;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectAdminUpdateDto {
    private Project.ApprovalStatus isApproved; // 승인 상태 (승인, 대기중, 거부)
    private Project.Status status; // 프로젝트 진행 상태 (진행중, 성공, 실패)
    private Boolean isDeleted;  // 프로젝트 삭제 여부(false: 미삭제, true: 삭제, default: false)
}