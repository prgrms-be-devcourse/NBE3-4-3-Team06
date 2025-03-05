package funding.startreum.domain.virtualaccount.dto.response

import funding.startreum.domain.transaction.entity.Transaction
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import java.math.BigDecimal
import java.time.LocalDateTime

data class AccountPaymentResponse(
    val transactionId: Int,       // 진행된 거래 ID
    val accountId: Int,           // 계좌 ID
    val beforeMoney: BigDecimal,  // 계산 전 금액
    val chargeAmount: BigDecimal, // 적용 금액
    val afterMoney: BigDecimal,   // 계산 후 금액
    val transactionDate: LocalDateTime // 거래 일자
) {
    companion object {
        @JvmStatic
        fun mapToAccountPaymentResponse(
            account: VirtualAccount,
            transaction: Transaction,
            beforeMoney: BigDecimal,
            chargeAmount: BigDecimal
        ): AccountPaymentResponse {
            return AccountPaymentResponse(
                transactionId = transaction.transactionId!!,
                accountId = account.accountId!!,
                beforeMoney = beforeMoney,
                chargeAmount = chargeAmount,
                afterMoney = account.balance,
                transactionDate = transaction.transactionDate
            )
        }
    }
}

