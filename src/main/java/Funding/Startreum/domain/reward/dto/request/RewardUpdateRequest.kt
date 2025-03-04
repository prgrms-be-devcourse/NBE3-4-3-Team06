package funding.startreum.domain.reward.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class RewardUpdateRequest(
    @field:NotBlank(message = "리워드 설명은 필수입니다.")
    val description: String, // 리워드 설명

    @field:NotNull(message = "리워드 금액은 필수입니다.")
    @field:Min(value = 1, message = "리워드 금액은 1 이상이어야 합니다.")
    val amount: BigDecimal // 리워드 금액
)
