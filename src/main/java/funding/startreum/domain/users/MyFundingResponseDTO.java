package funding.startreum.domain.users;

import funding.startreum.domain.project.entity.Project;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MyFundingResponseDTO {
    private String projectTitle;
    private Project.Status projectStatus;  // Enum 타입으로 변경
    private BigDecimal fundingAmount;
    private LocalDateTime fundedAt;
    private String transactionStatus;

    public MyFundingResponseDTO(String projectTitle, Project.Status projectStatus, BigDecimal fundingAmount, LocalDateTime fundedAt, String transactionStatus) {
        this.projectTitle = projectTitle;
        this.projectStatus = projectStatus;
        this.fundingAmount = fundingAmount;
        this.fundedAt = fundedAt;
        this.transactionStatus = transactionStatus;
    }
}