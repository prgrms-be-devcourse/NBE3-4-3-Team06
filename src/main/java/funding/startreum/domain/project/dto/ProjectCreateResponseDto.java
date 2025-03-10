package funding.startreum.domain.project.dto;

import java.time.LocalDateTime;

public record ProjectCreateResponseDto(
        Integer projectId,
        String title,
        LocalDateTime createdAt
) {}
