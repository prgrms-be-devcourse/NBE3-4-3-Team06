package funding.startreum.domain.virtualaccount.service;

import funding.startreum.domain.funding.entity.Funding;
import funding.startreum.domain.funding.service.FundingService;
import funding.startreum.domain.project.entity.Project;
import funding.startreum.domain.project.service.ProjectService;
import funding.startreum.domain.transaction.entity.Transaction;
import funding.startreum.domain.transaction.service.TransactionService;
import funding.startreum.domain.virtualaccount.dto.request.AccountPaymentRequest;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountPaymentServiceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private ProjectService projectService;

    @Mock
    private FundingService fundingService;

    @Mock
    private AccountQueryService accountQueryService;

    @InjectMocks
    private AccountPaymentService accountPaymentService;

    // 공통으로 쓸 객체들
    private LocalDateTime now;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        // 공통적으로 쓸 날짜나 Transaction
        now = LocalDateTime.now();

        testTransaction = new Transaction();
        testTransaction.setTransactionId(999);
        testTransaction.setTransactionDate(now);
    }

    private Project createProject(int projectId) {
        Project project = new Project();
        project.setProjectId(projectId);
        project.setCurrentFunding(BigDecimal.ZERO);
        return project;
    }

    private VirtualAccount createVirtualAccount(int accountId, BigDecimal balance) {
        VirtualAccount account = new VirtualAccount();
        account.setAccountId(accountId);
        account.setBalance(balance);
        return account;
    }

    @Nested
    @DisplayName("paymentByAccountId() 테스트")
    class PaymentByAccountIdTests {

        @Test
        @DisplayName("정상 결제 시 계좌 잔액/프로젝트 펀딩/거래내역을 확인한다.")
        void testPaymentByAccountId() {
            // given
            int accountId = 1;
            int projectId = 100;
            String username = "payer";
            BigDecimal paymentAmount = BigDecimal.valueOf(50);
            AccountPaymentRequest request = new AccountPaymentRequest(projectId, paymentAmount);

            // Project
            Project project = createProject(projectId);
            when(projectService.getProject(projectId)).thenReturn(project);

            // 결제자 계좌: 200원
            VirtualAccount payerAccount = createVirtualAccount(accountId, BigDecimal.valueOf(200));
            when(accountQueryService.getAccount(accountId)).thenReturn(payerAccount);

            // 수혜자 계좌: 100원
            VirtualAccount projectAccount = createVirtualAccount(2, BigDecimal.valueOf(100));
            when(accountQueryService.getAccountByProjectId(projectId)).thenReturn(projectAccount);

            // 펀딩 Mock
            Funding funding = new Funding();
            funding.setFundingId(10);
            when(fundingService.createFunding(eq(project), eq(username), eq(paymentAmount)))
                    .thenReturn(funding);

            // 거래 Mock
            when(transactionService.createTransaction(
                    eq(funding),
                    eq(payerAccount),
                    eq(projectAccount),
                    eq(paymentAmount),
                    eq(REMITTANCE)))
                    .thenReturn(testTransaction);

            // when
            AccountPaymentResponse response = accountPaymentService.paymentByAccountId(accountId, request, username);

            // then
            // (1) 잔액 검증
            assertThat(payerAccount.getBalance()).as("결제자 계좌 잔액").isEqualTo(BigDecimal.valueOf(150));
            assertThat(projectAccount.getBalance()).as("결제자 계좌 잔액").isEqualTo(BigDecimal.valueOf(150));
            // (2) 프로젝트 currentFunding
            assertThat(project.getCurrentFunding()).as("프로젝트 펀딩 합계").isEqualTo(paymentAmount);
            // (3) 응답 객체 검증
            assertThat(response.getTransactionId()).isEqualTo(testTransaction.getTransactionId());
            assertThat(response.getAccountId()).isEqualTo(accountId);
            assertThat(response.getBeforeMoney()).isEqualTo(BigDecimal.valueOf(200));
            assertThat(response.getChargeAmount()).isEqualTo(paymentAmount);
            assertThat(response.getAfterMoney()).isEqualTo(payerAccount.getBalance());
            assertThat(response.getTransactionDate()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("paymentByUsername() 테스트")
    class PaymentByUsernameTests {

        @Test
        @DisplayName("정상 결제 시 계좌 잔액/프로젝트 펀딩/거래내역을 확인한다.")
        void testPaymentByUsername() {
            // given
            int projectId = 200;
            String username = "payerUser";
            BigDecimal paymentAmount = BigDecimal.valueOf(80);
            AccountPaymentRequest request = new AccountPaymentRequest(projectId, paymentAmount);

            // Project
            Project project = createProject(projectId);
            when(projectService.getProject(projectId)).thenReturn(project);

            // 결제자 계좌: 300원
            VirtualAccount payerAccount = createVirtualAccount(3, BigDecimal.valueOf(300));
            when(accountQueryService.getAccount(username)).thenReturn(payerAccount);

            // 프로젝트 수혜자 계좌: 50원
            VirtualAccount projectAccount = createVirtualAccount(4, BigDecimal.valueOf(50));
            when(accountQueryService.getAccountByProjectId(projectId)).thenReturn(projectAccount);

            // 펀딩 Mock
            Funding funding = new Funding();
            funding.setFundingId(20);
            when(fundingService.createFunding(eq(project), eq(username), eq(paymentAmount)))
                    .thenReturn(funding);

            // 거래 Mock
            when(transactionService.createTransaction(
                    eq(funding),
                    eq(payerAccount),
                    eq(projectAccount),
                    eq(paymentAmount),
                    eq(REMITTANCE)))
                    .thenReturn(testTransaction);

            // when
            AccountPaymentResponse response = accountPaymentService.paymentByUsername(request, username);

            // then
            // (1) 잔액 검증
            assertThat(payerAccount.getBalance()).as("결제자 계좌 잔액").isEqualTo(BigDecimal.valueOf(220));
            assertThat(projectAccount.getBalance()).as("결제자 계좌 잔액").isEqualTo(BigDecimal.valueOf(130));
            // (2) 프로젝트 currentFunding
            assertThat(project.getCurrentFunding()).as("프로젝트 펀딩 합계").isEqualTo(paymentAmount);
            // (3) 응답 객체 검증
            assertThat(response.getTransactionId()).isEqualTo(testTransaction.getTransactionId());
            assertThat(response.getAccountId()).isEqualTo(payerAccount.getAccountId());
            assertThat(response.getBeforeMoney()).isEqualTo(BigDecimal.valueOf(300));
            assertThat(response.getChargeAmount()).isEqualTo(paymentAmount);
            assertThat(response.getAfterMoney()).isEqualTo(BigDecimal.valueOf(220));
            assertThat(response.getTransactionDate()).isEqualTo(now);
        }
    }
}