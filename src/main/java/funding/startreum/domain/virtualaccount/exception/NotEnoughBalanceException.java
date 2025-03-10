package funding.startreum.domain.virtualaccount.exception;

import java.math.BigDecimal;

public class NotEnoughBalanceException extends RuntimeException {

    public NotEnoughBalanceException(BigDecimal currentBalance) {
        super("잔액이 부족합니다. 현재 잔액:" + currentBalance);
    }

}
