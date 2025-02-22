package Funding.Startreum.domain.users;

import Funding.Startreum.domain.project.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MyProjectDTO {

    private String title;           // 프로젝트 타이틀
    private BigDecimal fundingGoal; // 목표 금액
    private LocalDateTime createdAt; // 생성일
    private Project.Status status;  // 프로젝트 상태
    private Project.ApprovalStatus isApproved; // 승인 여부

    // 엔티티로부터 DTO 변환 메서드
    public static MyProjectDTO from(Project project) {
        return new MyProjectDTO(
                project.getTitle(),
                project.getFundingGoal(),
                project.getCreatedAt(),
                project.getStatus(),
                project.getIsApproved()
        );
    }
}