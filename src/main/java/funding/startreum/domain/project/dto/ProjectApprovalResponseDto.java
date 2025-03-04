package funding.startreum.domain.project.dto;

import java.time.LocalDateTime;

public record ProjectApprovalResponseDto(
        Integer statusCode,
        String status,
        String message,
        Data data
) {
    public record Data(
            Integer projectId,
            LocalDateTime requestedAt
    ){}
}
