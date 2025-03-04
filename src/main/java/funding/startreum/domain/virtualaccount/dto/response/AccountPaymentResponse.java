package funding.startreum.domain.virtualaccount.dto.response;

import funding.startreum.domain.transaction.entity.Transaction;
import funding.startreum.domain.virtualaccount.entity.VirtualAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountPaymentResponse(
        int transactionId,              // 진행된 거래 ID
        int accountId,                  // 계좌 ID
        BigDecimal beforeMoney,         // 계산 전 금액
        BigDecimal chargeAmount,        // 적용 금액
        BigDecimal afterMoney,          // 계산 후 금액
        LocalDateTime transactionDate   // 거래 일자
) {

    public static AccountPaymentResponse mapToAccountPaymentResponse(
            VirtualAccount account,
            Transaction transaction,
            BigDecimal beforeMoney,
            BigDecimal chargeAmount
    ) {
        return new AccountPaymentResponse(
                transaction.getTransactionId(),
                account.getAccountId(),
                beforeMoney,
                chargeAmount,
                account.getBalance(),
                transaction.getTransactionDate()
        );
    }

}

