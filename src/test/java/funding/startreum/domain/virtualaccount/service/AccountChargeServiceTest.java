package funding.startreum.domain.virtualaccount.service;

import funding.startreum.domain.transaction.entity.Transaction;
import funding.startreum.domain.transaction.service.TransactionService;
import funding.startreum.domain.virtualaccount.dto.request.AccountRequest;
import funding.startreum.domain.virtualaccount.dto.response.AccountPaymentResponse;
import funding.startreum.domain.virtualaccount.entity.VirtualAccount;
import org.junit.jupiter.api.BeforeEach;
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
import static org.assertj.core.api.Assertions.assertThat; // AssertJ
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountChargeServiceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountQueryService accountQueryService;

    @InjectMocks
    private AccountChargeService accountChargeService;

    private VirtualAccount testAccount;
    private Transaction testTransaction;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        testAccount = new VirtualAccount();
        testAccount.setAccountId(999);
        testAccount.setBalance(BigDecimal.valueOf(100));

        now = LocalDateTime.now();

        testTransaction = new Transaction();
        testTransaction.setTransactionId(9999);
        testTransaction.setTransactionDate(now);
    }

    @Nested
    @DisplayName("chargeByAccountId() 테스트")
    class ChargeByAccountIdTests {

        @Test
        @DisplayName("정상 충전 시, 잔액 증가 및 Transaction 정보 확인")
        void testChargeByAccountId() {
            // given
            int accountId = 1;
            BigDecimal chargeAmount = BigDecimal.valueOf(50);
            BigDecimal initialBalance = testAccount.getBalance();
            AccountRequest request = new AccountRequest(chargeAmount);

            // accountId에 해당하는 가짜 계좌 반환
            given(accountQueryService.getAccount(accountId)).willReturn(testAccount);

            // transactionService.createTransaction(...) 호출 시 testTransaction 반환
            given(transactionService.createTransaction(isNull(), eq(testAccount), eq(testAccount),
                    eq(chargeAmount), eq(REMITTANCE)))
                    .willReturn(testTransaction);

            // when
            AccountPaymentResponse response = accountChargeService.chargeByAccountId(accountId, request);

            // then
            // 잔액 증가 확인
            assertThat(testAccount.getBalance()).isEqualTo(initialBalance.add(chargeAmount));

            // 응답 객체 검증
            assertThat(response.getTransactionId()).isEqualTo(testTransaction.getTransactionId());
            assertThat(response.getAccountId()).isEqualTo(testAccount.getAccountId());
            assertThat(response.getBeforeMoney()).isEqualTo(initialBalance);
            assertThat(response.getChargeAmount()).isEqualTo(chargeAmount);
            assertThat(response.getAfterMoney()).isEqualTo(testAccount.getBalance());
            assertThat(response.getTransactionDate()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("chargeByUsername() 테스트")
    class ChargeByUsernameTests {
        @Test
        @DisplayName("유저네임으로 충전 시, 잔액 증가 및 Transaction 정보 확인")
        void testChargeByUsername() {
            // given
            String username = "testUser";
            BigDecimal chargeAmount = BigDecimal.valueOf(30);
            BigDecimal initialBalance = testAccount.getBalance();
            AccountRequest request = new AccountRequest(chargeAmount);

            // username에 해당하는 가짜 계좌 반환
            given(accountQueryService.getAccount(username)).willReturn(testAccount);

            // transactionService.createTransaction(...) 호출 시 testTransaction 반환
            given(transactionService.createTransaction(isNull(), eq(testAccount), eq(testAccount),
                    eq(chargeAmount), eq(REMITTANCE)))
                    .willReturn(testTransaction);

            // when
            AccountPaymentResponse response = accountChargeService.chargeByUsername(username, request);

            // then
            // 잔액 증가 확인
            assertThat(testAccount.getBalance()).isEqualTo(initialBalance.add(chargeAmount));

            // 응답 객체 검증
            assertThat(response.getTransactionId()).isEqualTo(testTransaction.getTransactionId());
            assertThat(response.getAccountId()).isEqualTo(testAccount.getAccountId());
            assertThat(response.getBeforeMoney()).isEqualTo(initialBalance);
            assertThat(response.getChargeAmount()).isEqualTo(chargeAmount);
            assertThat(response.getAfterMoney()).isEqualTo(testAccount.getBalance());
            assertThat(response.getTransactionDate()).isEqualTo(now);
        }
    }
}
