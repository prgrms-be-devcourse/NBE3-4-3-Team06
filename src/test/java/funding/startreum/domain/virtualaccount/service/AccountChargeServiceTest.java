package funding.startreum.domain.virtualaccount.service;

import funding.startreum.domain.transaction.entity.Transaction;
import funding.startreum.domain.transaction.service.TransactionService;
import funding.startreum.domain.virtualaccount.dto.request.AccountRequest;
import funding.startreum.domain.virtualaccount.dto.response.AccountPaymentResponse;
import funding.startreum.domain.virtualaccount.entity.VirtualAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static funding.startreum.domain.transaction.entity.Transaction.TransactionType.REMITTANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountChargeServiceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountQueryService accountQueryService;

    @InjectMocks
    private AccountChargeService accountChargeService;

    @Nested
    @DisplayName("chargeByAccountId() 테스트")
    class ChargeByAccountIdTests {

        @Test
        void testChargeByAccountId() {
            int accountId = 1;
            BigDecimal initialBalance = BigDecimal.valueOf(100);
            BigDecimal chargeAmount = BigDecimal.valueOf(50);
            AccountRequest request = new AccountRequest(chargeAmount);

            // 테스트용 VirtualAccount 생성
            VirtualAccount account = new VirtualAccount();
            account.setAccountId(accountId);
            account.setBalance(initialBalance);

            when(accountQueryService.getAccount(accountId)).thenReturn(account);

            // 거래 생성 모의 (외부 전달용 ID null)
            Transaction transaction = new Transaction();
            transaction.setTransactionId(1);
            LocalDateTime now = LocalDateTime.now();
            transaction.setTransactionDate(now);
            when(transactionService.createTransaction(isNull(), eq(account), eq(account), eq(chargeAmount), eq(REMITTANCE)))
                    .thenReturn(transaction);

            AccountPaymentResponse response = accountChargeService.chargeByAccountId(accountId, request);

            // 충전 후 잔액 및 응답 검증
            assertEquals(initialBalance.add(chargeAmount), account.getBalance());
            assertEquals(transaction.getTransactionId(), response.getTransactionId());
            assertEquals(accountId, response.getAccountId());
            assertEquals(initialBalance, response.getBeforeMoney());
            assertEquals(chargeAmount, response.getChargeAmount());
            assertEquals(account.getBalance(), response.getAfterMoney());
            assertEquals(now, response.getTransactionDate());
        }
    }

    @Nested
    @DisplayName("chargeByUsername() 테스트")
    class ChargeByUsernameTests {
        @Test
        void testChargeByUsername() {
            String username = "testUser";
            BigDecimal initialBalance = BigDecimal.valueOf(200);
            BigDecimal chargeAmount = BigDecimal.valueOf(30);
            AccountRequest request = new AccountRequest(chargeAmount);

            // 테스트용 VirtualAccount 생성
            VirtualAccount account = new VirtualAccount();
            account.setAccountId(2);
            account.setBalance(initialBalance);

            when(accountQueryService.getAccount(username)).thenReturn(account);

            Transaction transaction = new Transaction();
            transaction.setTransactionId(2);
            LocalDateTime now = LocalDateTime.now();
            transaction.setTransactionDate(now);
            when(transactionService.createTransaction(isNull(), eq(account), eq(account), eq(chargeAmount), eq(REMITTANCE)))
                    .thenReturn(transaction);

            AccountPaymentResponse response = accountChargeService.chargeByUsername(username, request);

            // 충전 후 잔액 및 응답 검증
            assertEquals(initialBalance.add(chargeAmount), account.getBalance());
            assertEquals(transaction.getTransactionId(), response.getTransactionId());
            assertEquals(account.getAccountId(), response.getAccountId());
            assertEquals(initialBalance, response.getBeforeMoney());
            assertEquals(chargeAmount, response.getChargeAmount());
            assertEquals(account.getBalance(), response.getAfterMoney());
            assertEquals(now, response.getTransactionDate());
        }
    }
}
