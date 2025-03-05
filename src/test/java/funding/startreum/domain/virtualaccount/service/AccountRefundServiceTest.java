package funding.startreum.domain.virtualaccount.service;

import funding.startreum.domain.funding.entity.Funding;
import funding.startreum.domain.funding.service.FundingService;
import funding.startreum.domain.project.entity.Project;
import funding.startreum.domain.project.repository.ProjectRepository;
import funding.startreum.domain.transaction.entity.Transaction;
import funding.startreum.domain.transaction.service.TransactionService;
import funding.startreum.domain.virtualaccount.dto.response.AccountRefundResponse;
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

import static funding.startreum.domain.transaction.entity.Transaction.TransactionType.REFUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountRefundServiceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountQueryService accountQueryService;

    @Mock
    private FundingService fundingService;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private AccountRefundService accountRefundService;

    @Nested
    @DisplayName("refund() 테스트")
    class RefundTests {
        @Test
        void testRefund() {
            // Given: 테스트 데이터 및 목(mock) 설정
            int payerAccountId = 1;
            int originalTransactionId = 10;
            BigDecimal refundAmount = BigDecimal.valueOf(50);

            Transaction oldTransaction = createOldTransaction(originalTransactionId, refundAmount, 100);
            when(transactionService.getTransaction(originalTransactionId)).thenReturn(oldTransaction);

            VirtualAccount payerAccount = createVirtualAccount(payerAccountId, BigDecimal.valueOf(100));
            when(accountQueryService.getAccountByAccountId(payerAccountId)).thenReturn(payerAccount);

            VirtualAccount projectAccount = spy(createVirtualAccount(2, BigDecimal.valueOf(200)));
            when(accountQueryService.getReceiverAccountByTransactionId(originalTransactionId)).thenReturn(projectAccount);

            // projectAccount의 transferTo 메서드가 호출될 때 실제 잔액 변경이 일어나도록 모의
            doAnswer(invocation -> {
                payerAccount.setBalance(payerAccount.getBalance().add(refundAmount));
                projectAccount.setBalance(projectAccount.getBalance().subtract(refundAmount));
                return null;
            }).when(projectAccount).transferTo(eq(refundAmount), eq(payerAccount));

            Funding canceledFunding = new Funding();
            canceledFunding.setFundingId(101);
            when(fundingService.cancelFunding(oldTransaction.getFunding().getFundingId()))
                    .thenReturn(canceledFunding);

            LocalDateTime now = LocalDateTime.now();
            Transaction refundTransaction = createRefundTransaction(20, now);
            when(transactionService.createTransaction(
                    eq(canceledFunding),
                    eq(projectAccount),
                    eq(payerAccount),
                    eq(refundAmount),
                    eq(REFUND)
            )).thenReturn(refundTransaction);

            Project project = new Project();
            project.setCurrentFunding(BigDecimal.valueOf(80));
            when(projectRepository.findProjectByTransactionId(originalTransactionId)).thenReturn(project);

            BigDecimal beforeBalance = payerAccount.getBalance();

            // When: 환불 실행
            AccountRefundResponse response = accountRefundService.refund(payerAccountId, originalTransactionId);

            // Then: 환불 후 결과 검증
            assertEquals(beforeBalance.add(refundAmount), payerAccount.getBalance(),
                    "환불 후 결제자 계좌 잔액이 갱신되어야 합니다.");
            assertEquals(BigDecimal.valueOf(80).subtract(refundAmount),
                    project.getCurrentFunding(),
                    "프로젝트 currentFunding이 환불 금액만큼 차감되어야 합니다.");
            assertEquals(refundTransaction.getTransactionId(), response.getRefundTransactionId());
            assertEquals(originalTransactionId, response.getOriginalTransactionId());
            assertEquals(payerAccountId, response.getAccountId());
            assertEquals(beforeBalance, response.getBeforeMoney());
            assertEquals(refundAmount, response.getRefundAmount());
            assertEquals(payerAccount.getBalance(), response.getAfterMoney());
            assertEquals(now, response.getTransactionDate());
        }
    }

    // 테스트 데이터 생성 헬퍼 메서드
    private Transaction createOldTransaction(int transactionId, BigDecimal amount, int fundingId) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setAmount(amount);
        Funding funding = new Funding();
        funding.setFundingId(fundingId);
        transaction.setFunding(funding);
        return transaction;
    }

    private VirtualAccount createVirtualAccount(int accountId, BigDecimal balance) {
        VirtualAccount account = new VirtualAccount();
        account.setAccountId(accountId);
        account.setBalance(balance);
        return account;
    }

    private Transaction createRefundTransaction(int transactionId, LocalDateTime transactionDate) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setTransactionDate(transactionDate);
        return transaction;
    }
}
