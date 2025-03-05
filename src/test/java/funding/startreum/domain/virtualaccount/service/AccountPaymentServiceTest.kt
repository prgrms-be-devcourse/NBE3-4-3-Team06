package funding.startreum.domain.virtualaccount.service

import funding.startreum.domain.funding.entity.Funding
import funding.startreum.domain.funding.service.FundingService
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.project.service.ProjectService
import funding.startreum.domain.transaction.entity.Transaction
import funding.startreum.domain.transaction.entity.Transaction.TransactionType
import funding.startreum.domain.transaction.service.TransactionService
import funding.startreum.domain.virtualaccount.dto.request.AccountPaymentRequest
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
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
internal class AccountPaymentServiceTest {

    @Mock
    lateinit var transactionService: TransactionService

    @Mock
    lateinit var projectService: ProjectService

    @Mock
    lateinit var fundingService: FundingService

    @Mock
    lateinit var accountQueryService: AccountQueryService

    @InjectMocks
    lateinit var accountPaymentService: AccountPaymentService

    lateinit var now: LocalDateTime
    lateinit var testTransaction: Transaction

    @BeforeEach
    fun setUp() {
        now = LocalDateTime.now()
        testTransaction = Transaction().apply {
            transactionId = 999
            transactionDate = now
        }
    }

    private fun createProject(projectId: Int): Project =
        Project().apply {
            this.projectId = projectId
            currentFunding = BigDecimal.ZERO
        }

    private fun createVirtualAccount(accountId: Int, balance: BigDecimal): VirtualAccount =
        VirtualAccount().apply {
            this.accountId = accountId
            this.balance = balance
        }

    @Nested
    @DisplayName("paymentByAccountId() 테스트")
    inner class PaymentByAccountIdTests {
        @Test
        @DisplayName("정상 결제 시, 계좌 잔액/프로젝트 펀딩/거래내역 검증")
        fun testPaymentByAccountId() {
            // Given
            val accountId = 1
            val projectId = 100
            val username = "payer"
            val paymentAmount = BigDecimal.valueOf(50)
            val request = AccountPaymentRequest(projectId, paymentAmount)

            // 생성 객체
            val project = createProject(projectId)
            val payerAccount = createVirtualAccount(accountId, BigDecimal.valueOf(200))
            val projectAccount = createVirtualAccount(2, BigDecimal.valueOf(100))
            val funding = Funding().apply { fundingId = 10 }

            // Stubbing
            given(projectService.getProject(projectId)).willReturn(project)
            given(accountQueryService.getAccountByAccountId(accountId)).willReturn(payerAccount)
            given(accountQueryService.getAccountByProjectId(projectId)).willReturn(projectAccount)
            given(fundingService.createFunding(project, username, paymentAmount)).willReturn(funding)
            given(
                transactionService.createTransaction(
                    funding,
                    payerAccount,
                    projectAccount,
                    paymentAmount,
                    TransactionType.REMITTANCE
                )
            ).willReturn(testTransaction)

            // When
            val response = accountPaymentService.paymentByAccountId(accountId, request, username)

            // Then
            // 잔액 업데이트: 결제 후 결제자 200 - 50 = 150, 수혜자 100 + 50 = 150
            assertThat(payerAccount.balance).isEqualTo(BigDecimal.valueOf(150))
            assertThat(projectAccount.balance).isEqualTo(BigDecimal.valueOf(150))
            // 프로젝트의 currentFunding: 기존 0 + 50 = 50
            assertThat(project.currentFunding).isEqualTo(paymentAmount)
            // 거래 응답 검증
            assertThat(response.transactionId).isEqualTo(testTransaction.transactionId)
            assertThat(response.accountId).isEqualTo(accountId)
            assertThat(response.beforeMoney).isEqualTo(BigDecimal.valueOf(200))
            assertThat(response.chargeAmount).isEqualTo(paymentAmount)
            assertThat(response.afterMoney).isEqualTo(payerAccount.balance)
            assertThat(response.transactionDate).isEqualTo(now)
        }
    }

    @Nested
    @DisplayName("paymentByUsername() 테스트")
    inner class PaymentByUsernameTests {
        @Test
        @DisplayName("정상 결제 시, 계좌 잔액/프로젝트 펀딩/거래내역 검증")
        fun testPaymentByUsername() {
            // Given
            val projectId = 200
            val username = "payerUser"
            val paymentAmount = BigDecimal.valueOf(80)
            val request = AccountPaymentRequest(projectId, paymentAmount)

            // 생성 객체
            val project = createProject(projectId)
            val payerAccount = createVirtualAccount(3, BigDecimal.valueOf(300))
            val projectAccount = createVirtualAccount(4, BigDecimal.valueOf(50))
            val funding = Funding().apply { fundingId = 20 }

            // Stubbing
            given(projectService.getProject(projectId)).willReturn(project)
            given(accountQueryService.getAccountByUsername(username)).willReturn(payerAccount)
            given(accountQueryService.getAccountByProjectId(projectId)).willReturn(projectAccount)
            given(fundingService.createFunding(project, username, paymentAmount)).willReturn(funding)
            given(
                transactionService.createTransaction(
                    funding,
                    payerAccount,
                    projectAccount,
                    paymentAmount,
                    TransactionType.REMITTANCE
                )
            ).willReturn(testTransaction)

            // When
            val response = accountPaymentService.paymentByUsername(request, username)

            // Then
            // 잔액 업데이트: 결제 후 결제자 300 - 80 = 220, 수혜자 50 + 80 = 130
            assertThat(payerAccount.balance).isEqualTo(BigDecimal.valueOf(220))
            assertThat(projectAccount.balance).isEqualTo(BigDecimal.valueOf(130))
            // 프로젝트의 currentFunding: 기존 0 + 80 = 80
            assertThat(project.currentFunding).isEqualTo(paymentAmount)
            // 거래 응답 검증
            assertThat(response.transactionId).isEqualTo(testTransaction.transactionId)
            assertThat(response.accountId).isEqualTo(payerAccount.accountId)
            assertThat(response.beforeMoney).isEqualTo(BigDecimal.valueOf(300))
            assertThat(response.chargeAmount).isEqualTo(paymentAmount)
            assertThat(response.afterMoney).isEqualTo(BigDecimal.valueOf(220))
            assertThat(response.transactionDate).isEqualTo(now)
        }
    }
}