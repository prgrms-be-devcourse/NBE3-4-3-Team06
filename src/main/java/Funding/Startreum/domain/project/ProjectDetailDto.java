package funding.startreum.domain.project;

import funding.startreum.domain.project.entity.Project;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * 프로젝트 상세 정보를 위한 DTO (record 클래스 사용)
 */
public record ProjectDetailDto(
        Integer projectId,
        String title,
        String bannerUrl,
        String description,
        BigDecimal fundingGoal,
        BigDecimal currentFunding,
        String status,
        String startDate,
        String endDate,
        String  creatorName,
        String simpleDescription // 간단한 설명

) {
    public static ProjectDetailDto from(Project project) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return new ProjectDetailDto(
                project.getProjectId(),
                project.getTitle(),
                project.getBannerUrl(),
                project.getDescription(),
                project.getFundingGoal(),
                project.getCurrentFunding(),
                convertStatusToKorean(project.getStatus().name()),
                project.getStartDate().format(formatter),
                project.getEndDate().format(formatter),
                project.getCreator().getName(),
                project.getSimpleDescription()
        );
    }

    private static String convertStatusToKorean(String status) {
        return switch (status) {
            case "ONGOING" -> "진행중";
            case "SUCCESS" -> "성공";
            case "FAILED" -> "실패";
            default -> "알 수 없음";
        };
    }
}
