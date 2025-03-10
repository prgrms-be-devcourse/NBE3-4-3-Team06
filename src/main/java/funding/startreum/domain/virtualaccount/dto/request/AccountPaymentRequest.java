package funding.startreum.domain.virtualaccount.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountPaymentRequest(
        @NotNull(message = "프로젝트 ID를 확인해주세요.")
        @Min(value = 1, message = "프로젝트 ID를 확인해주세요.")
        Integer projectId,

        @NotNull(message = "금액을 확인해주세요.")
        @Min(value = 1, message = "금액을 확인해주세요.")
        BigDecimal amount
) {
}

