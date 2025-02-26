package funding.startreum.domain.project.dto;


import funding.startreum.domain.project.entity.Project;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProjectUpdateResponseDto (
        Integer projectId,
        String title,
        String description,
        BigDecimal fundingGoal,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime updatedAt
){
    // ✅ 엔티티 → DTO 변환 메서드 추가
    public static ProjectUpdateResponseDto from(Project project) {
        return new ProjectUpdateResponseDto(
                project.getProjectId(),
                project.getTitle(),
                project.getDescription(),
                project.getFundingGoal(),
                project.getStartDate(),
                project.getEndDate(),
                project.getUpdatedAt()
        );
    }
}
