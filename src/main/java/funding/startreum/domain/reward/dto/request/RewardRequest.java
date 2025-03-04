package funding.startreum.domain.reward.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RewardRequest(
        @NotNull(message = "프로젝트 ID는 필수입니다.")
        Integer projectId,       // 프로젝트 ID

        @NotBlank(message = "리워드 설명은 필수입니다.")
        String description,     // 리워드 설명

        @NotNull(message = "리워드 금액은 필수입니다.")
        @Min(value = 1, message = "리워드 금액은 1 이상이어야 합니다.")
        BigDecimal amount       // 리워드 최소 기준 금액
) {
}