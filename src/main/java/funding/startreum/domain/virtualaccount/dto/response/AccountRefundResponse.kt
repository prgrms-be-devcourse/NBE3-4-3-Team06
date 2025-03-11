package funding.startreum.domain.virtualaccount.dto.response

import funding.startreum.domain.transaction.entity.Transaction
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import java.math.BigDecimal
import java.time.LocalDateTime

data class AccountRefundResponse(
    val refundTransactionId: Int,  // 환불 거래 내역 ID
    val originalTransactionId: Int,  // 거래되었던 거래 내역 ID
    val accountId: Int,  // 계좌 ID
    val beforeMoney: BigDecimal,  // 환불 전 금액
    val refundAmount: BigDecimal,  // 환불 금액
    val afterMoney: BigDecimal,  // 환불 후 금액
    val transactionDate: LocalDateTime // 거래 일자
) {
    companion object {
        @JvmStatic
        fun mapToAccountRefundResponse(
            account: VirtualAccount,
            refundTransaction: Transaction,
            originalTransactionId: Int,
            refundAmount: BigDecimal,
            beforeMoney: BigDecimal
        ): AccountRefundResponse {
            return AccountRefundResponse(
                refundTransaction.transactionId!!,
                originalTransactionId,
                account.accountId!!,
                beforeMoney,
                refundAmount,
                account.balance,
                refundTransaction.transactionDate
            )
        }
    }
}

