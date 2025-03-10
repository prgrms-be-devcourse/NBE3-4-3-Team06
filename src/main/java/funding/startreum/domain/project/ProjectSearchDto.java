package funding.startreum.domain.project;

import funding.startreum.domain.project.entity.Project;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 프로젝트 검색 결과를 반환하는 DTO.
 * record를 사용하여 불변 객체로 설계.
 */
public record ProjectSearchDto(
        Integer projectId,      // 프로젝트 고유 ID
        String title,           // 프로젝트 제목
        String simpleDescription, // 간단한 설명
        String bannerUrl,       // 배너 이미지 URL
        String description,     // 프로젝트 설명
        BigDecimal fundingGoal, // 목표 금액
        BigDecimal currentFunding, // 현재 펀딩 금액
        LocalDateTime startDate,   // 시작 날짜
        LocalDateTime endDate,     // 종료 날짜
        Project.Status status,      // 프로젝트 상태 (ONGOING, SUCCESS, FAILED)
        Long daysLeft  // 남은 일수 추가
) {
    /**
     * Project 엔티티를 ProjectSearchDto로 변환하는 정적 메서드.
     * @param project 변환할 Project 엔티티
     * @return 변환된 ProjectSearchDto 객체
     */
    public static ProjectSearchDto from(Project project) {
        long daysLeft = Duration.between(LocalDateTime.now(), project.getEndDate()).toDays();
        return new ProjectSearchDto(
                project.getProjectId(),
                project.getTitle(),
                project.getSimpleDescription(),
                project.getBannerUrl(),
                project.getDescription(),
                project.getFundingGoal(),
                project.getCurrentFunding(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus(),
                daysLeft
        );
    }
}