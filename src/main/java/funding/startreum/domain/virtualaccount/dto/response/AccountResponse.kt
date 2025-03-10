package funding.startreum.domain.virtualaccount.dto.response

import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import java.math.BigDecimal
import java.time.LocalDateTime

data class AccountResponse(
    val accountId: Int,  // 계좌 ID
    val balance: BigDecimal,  // 잔액
    val createdAt: LocalDateTime // 생성일자
) {
    companion object {
        @JvmStatic
        fun mapToAccountResponse(account: VirtualAccount): AccountResponse {
            return AccountResponse(
                account.accountId!!,
                account.balance,
                account.createdAt
            )
        }
    }
}

