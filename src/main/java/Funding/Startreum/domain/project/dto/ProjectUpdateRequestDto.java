package Funding.Startreum.domain.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProjectUpdateRequestDto(
   String title,
   String description,
   BigDecimal fundingGoal,
   LocalDateTime startDate,
   LocalDateTime endDate
) {}
