package funding.startreum.domain.project.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProjectCreateRequestDto(
        @NotNull String title,
        @NotNull String simpleDescription,
        String description,
        @NotNull BigDecimal fundingGoal,
        @NotNull String bannerUrl,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate
) {}
