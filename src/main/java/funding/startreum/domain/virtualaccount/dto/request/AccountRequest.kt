package funding.startreum.domain.virtualaccount.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class AccountRequest(
    @field:NotNull(message = "금액을 확인해주세요.")
    @field:Min(value = 1, message = "금액을 확인해주세요.")
    val amount: BigDecimal
)

