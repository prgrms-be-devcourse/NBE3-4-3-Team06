package funding.startreum.domain.virtualaccount.service

import funding.startreum.domain.funding.entity.Funding
import funding.startreum.domain.funding.service.FundingService
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.project.repository.ProjectRepository
import funding.startreum.domain.transaction.entity.Transaction
import funding.startreum.domain.transaction.entity.Transaction.TransactionType
import funding.startreum.domain.transaction.service.TransactionService
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import funding.startreum.domain.virtualaccount.service.AccountQueryService
import funding.startreum.domain.virtualaccount.service.AccountRefundService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDateTime
import org.mockito.kotlin.any

@ExtendWith(MockitoExtension::class)
internal class AccountRefundServiceTest {

    @Mock
    lateinit var transactionService: TransactionService

    @Mock
    lateinit var accountQueryService: AccountQueryService

    @Mock
    lateinit var fundingService: FundingService

    @Mock
    lateinit var projectRepository: ProjectRepository

    @InjectMocks
    lateinit var accountRefundService: AccountRefundService

    @Nested
    @DisplayName("refund() 테스트")
    inner class RefundTests {

        @Test
        fun testRefund() {
            // Given: 테스트 데이터 및 목(mock) 설정
            val payerAccountId = 1
            val originalTransactionId = 10
            val refundAmount = BigDecimal.valueOf(50)

            val oldTransaction = createOldTransaction(originalTransactionId, refundAmount, 100)
            `when`(transactionService.getTransaction(originalTransactionId)).thenReturn(oldTransaction)

            val payerAccount = createVirtualAccount(payerAccountId, BigDecimal.valueOf(100))
            `when`(accountQueryService.getAccountByAccountId(payerAccountId)).thenReturn(payerAccount)

            val projectAccount = spy(createVirtualAccount(2, BigDecimal.valueOf(200)))
            `when`(accountQueryService.getReceiverAccountByTransactionId(originalTransactionId))
                .thenReturn(projectAccount)

            // projectAccount의 transferTo 메서드 호출 시 실제 잔액 변경 모의
            doAnswer {
                payerAccount.balance = payerAccount.balance.add(refundAmount)
                projectAccount.balance = projectAccount.balance.subtract(refundAmount)
                null
            }.`when`(projectAccount).transferTo(any(), any())


            val canceledFunding = Funding().apply { fundingId = 101 }
            `when`(fundingService.cancelFunding(oldTransaction.funding!!.fundingId!!))
                .thenReturn(canceledFunding)

            val now = LocalDateTime.now()
            val refundTransaction = createRefundTransaction(20, now)
            `when`(
                transactionService.createTransaction(
                    canceledFunding,
                    projectAccount,
                    payerAccount,
                    refundAmount,
                    TransactionType.REFUND
                )
            ).thenReturn(refundTransaction)

            val project = Project().apply { currentFunding = BigDecimal.valueOf(80) }
            `when`(projectRepository.findProjectByTransactionId(originalTransactionId)).thenReturn(project)

            val beforeBalance = payerAccount.balance

            // When: 환불 실행
            val response = accountRefundService.refund(payerAccountId, originalTransactionId)

            // Then: 환불 후 결과 검증
            assertEquals(
                beforeBalance.add(refundAmount), payerAccount.balance,
                "환불 후 결제자 계좌 잔액이 갱신되어야 합니다."
            )
            assertEquals(
                BigDecimal.valueOf(80).subtract(refundAmount), project.currentFunding,
                "프로젝트 currentFunding이 환불 금액만큼 차감되어야 합니다."
            )
            assertEquals(refundTransaction.transactionId, response.refundTransactionId)
            assertEquals(originalTransactionId, response.originalTransactionId)
            assertEquals(payerAccountId, response.accountId)
            assertEquals(beforeBalance, response.beforeMoney)
            assertEquals(refundAmount, response.refundAmount)
            assertEquals(payerAccount.balance, response.afterMoney)
            assertEquals(now, response.transactionDate)
        }
    }

    // 테스트 데이터 생성 헬퍼 메서드
    private fun createOldTransaction(transactionId: Int, amount: BigDecimal, fundingId: Int): Transaction =
        Transaction().apply {
            this.transactionId = transactionId
            this.amount = amount
            this.funding = Funding().apply { this.fundingId = fundingId }
        }

    private fun createVirtualAccount(accountId: Int, balance: BigDecimal): VirtualAccount =
        VirtualAccount().apply {
            this.accountId = accountId
            this.balance = balance
        }

    private fun createRefundTransaction(transactionId: Int, transactionDate: LocalDateTime): Transaction =
        Transaction().apply {
            this.transactionId = transactionId
            this.transactionDate = transactionDate
        }
}