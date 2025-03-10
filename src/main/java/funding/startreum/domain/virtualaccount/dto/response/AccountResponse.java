package funding.startreum.domain.virtualaccount.dto.response;

import funding.startreum.domain.virtualaccount.entity.VirtualAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        int accountId,              // 계좌 ID
        BigDecimal balance,         // 잔액
        LocalDateTime createdAt     // 생성일자
) {
    /**
     * 현재 계좌를 조회합니다.
     * @param account 조회할 계좌
     * @return 변환된 DTO
     */
    public static AccountResponse mapToAccountResponse(VirtualAccount account) {
        return new AccountResponse(
                account.getAccountId(),
                account.getBalance(),
                account.getCreatedAt()
        );
    }
}

