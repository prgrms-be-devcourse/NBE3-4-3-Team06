package Funding.Startreum.domain.admin;

import Funding.Startreum.domain.project.entity.Project;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 관리자용 프로젝트 조회 DTO.
 */
public record ProjectAdminSearchDto(
        Integer projectId,       // 프로젝트 고유 ID
        String title,            // 프로젝트 제목
        String description,      // 프로젝트 설명
        BigDecimal fundingGoal,  // 목표 금액
        BigDecimal currentFunding, // 현재 펀딩 금액
        LocalDateTime startDate,    // 시작 날짜
        LocalDateTime endDate,      // 종료 날짜
        Project.Status status,      // 프로젝트 상태 (ONGOING, SUCCESS, FAILED)
        Project.ApprovalStatus isApproved // ✅ 승인 상태 추가
) {
    /**
     * Project 엔티티를 ProjectAdminSearchDto로 변환하는 정적 메서드.
     * @param project 변환할 Project 엔티티
     * @return 변환된 ProjectAdminSearchDto 객체
     */
    public static ProjectAdminSearchDto from(Project project) {
        return new ProjectAdminSearchDto(
                project.getProjectId(),
                project.getTitle(),
                project.getDescription(),
                project.getFundingGoal(),
                project.getCurrentFunding(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus(),
                project.getIsApproved() // ✅ 승인 상태 포함
        );
    }
}
