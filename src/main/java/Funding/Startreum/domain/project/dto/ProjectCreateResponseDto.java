package Funding.Startreum.domain.project.dto;

import java.time.LocalDateTime;

public record ProjectCreateResponseDto(
        Integer projectId,
        String title,
        LocalDateTime createdAt
) {}
