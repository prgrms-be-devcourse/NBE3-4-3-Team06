package funding.startreum.domain.virtualaccount.exception

import java.math.BigDecimal

class NotEnoughBalanceException(currentBalance: BigDecimal) :
    RuntimeException("잔액이 부족합니다. 현재 잔액: $currentBalance")
