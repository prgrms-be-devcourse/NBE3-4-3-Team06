package funding.startreum.domain.virtualaccount.service

import funding.startreum.domain.transaction.entity.Transaction
import funding.startreum.domain.transaction.entity.Transaction.TransactionType
import funding.startreum.domain.transaction.service.TransactionService
import funding.startreum.domain.virtualaccount.dto.request.AccountRequest
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import funding.startreum.domain.virtualaccount.service.AccountChargeService
import funding.startreum.domain.virtualaccount.service.AccountQueryService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
internal class AccountChargeServiceTest {

    @Mock
    lateinit var transactionService: TransactionService

    @Mock
    lateinit var accountQueryService: AccountQueryService

    @InjectMocks
    lateinit var accountChargeService: AccountChargeService

    lateinit var testAccount: VirtualAccount
    lateinit var testTransaction: Transaction
    lateinit var now: LocalDateTime

    @BeforeEach
    fun setUp() {
        testAccount = VirtualAccount().apply {
            accountId = 999
            balance = BigDecimal.valueOf(100)
        }
        now = LocalDateTime.now()
        testTransaction = Transaction().apply {
            transactionId = 9999
            transactionDate = now
        }
    }

    @Nested
    @DisplayName("chargeByAccountId() 테스트")
    inner class ChargeByAccountIdTests {
        @Test
        @DisplayName("정상 충전 시, 잔액 증가 및 Transaction 정보 확인")
        fun testChargeByAccountId() {
            // Given
            val accountId = 1
            val chargeAmount = BigDecimal.valueOf(50)
            val initialBalance = testAccount.balance
            val request = AccountRequest(chargeAmount)

            // 계좌 조회 시 가짜 계좌 반환
            given(accountQueryService.getAccountByAccountId(accountId)).willReturn(testAccount)

            given(
                transactionService.createTransaction(
                    null,
                    testAccount,
                    testAccount,
                    chargeAmount,
                    TransactionType.REMITTANCE
                )
            ).willReturn(testTransaction)

            // When
            val response = accountChargeService.chargeByAccountId(accountId, request)

            // Then
            // 잔액 증가 확인
            assertThat(testAccount.balance).isEqualTo(initialBalance.add(chargeAmount))
            // 응답 객체 검증
            assertThat(response.transactionId).isEqualTo(testTransaction.transactionId)
            assertThat(response.accountId).isEqualTo(testAccount.accountId)
            assertThat(response.beforeMoney).isEqualTo(initialBalance)
            assertThat(response.chargeAmount).isEqualTo(chargeAmount)
            assertThat(response.afterMoney).isEqualTo(testAccount.balance)
            assertThat(response.transactionDate).isEqualTo(now)
        }
    }

    @Nested
    @DisplayName("chargeByUsername() 테스트")
    inner class ChargeByUsernameTests {
        @Test
        @DisplayName("유저네임으로 충전 시, 잔액 증가 및 Transaction 정보 확인")
        fun testChargeByUsername() {
            // Given
            val username = "testUser"
            val chargeAmount = BigDecimal.valueOf(30)
            val initialBalance = testAccount.balance
            val request = AccountRequest(chargeAmount)

            // username 기반 가짜 계좌 반환
            given(accountQueryService.getAccountByUsername(username)).willReturn(testAccount)

            // transactionService.createTransaction(...) 호출 시 testTransaction 반환
            given(
                transactionService.createTransaction(
                    null,
                    testAccount,
                    testAccount,
                    chargeAmount,
                    TransactionType.REMITTANCE
                )
            ).willReturn(testTransaction)

            // When
            val response = accountChargeService.chargeByUsername(username, request)

            // Then
            // 잔액 증가 확인
            assertThat(testAccount.balance).isEqualTo(initialBalance.add(chargeAmount))
            // 응답 객체 검증
            assertThat(response.transactionId).isEqualTo(testTransaction.transactionId)
            assertThat(response.accountId).isEqualTo(testAccount.accountId)
            assertThat(response.beforeMoney).isEqualTo(initialBalance)
            assertThat(response.chargeAmount).isEqualTo(chargeAmount)
            assertThat(response.afterMoney).isEqualTo(testAccount.balance)
            assertThat(response.transactionDate).isEqualTo(now)
        }
    }
}