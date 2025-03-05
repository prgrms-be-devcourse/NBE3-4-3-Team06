package funding.startreum.domain.virtualaccount.dto.response;

import funding.startreum.domain.transaction.entity.Transaction;
import funding.startreum.domain.virtualaccount.entity.VirtualAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountRefundResponse(
        int refundTransactionId,        // 환불 거래 내역 ID
        int originalTransactionId,      // 거래되었던 거래 내역 ID
        int accountId,                  // 계좌 ID
        BigDecimal beforeMoney,         // 환불 전 금액
        BigDecimal refundAmount,        // 환불 금액
        BigDecimal afterMoney,          // 환불 후 금액
        LocalDateTime transactionDate   // 거래 일자
) {

    public static AccountRefundResponse mapToAccountRefundResponse(
            VirtualAccount account,
            Transaction refundTransaction,
            Integer originalTransactionId,
            BigDecimal refundAmount,
            BigDecimal beforeMoney
    ) {
        return new AccountRefundResponse(
                refundTransaction.getTransactionId(),
                originalTransactionId,
                account.getAccountId(),
                beforeMoney,
                refundAmount,
                account.getBalance(),
                refundTransaction.getTransactionDate()
        );
    }

}

