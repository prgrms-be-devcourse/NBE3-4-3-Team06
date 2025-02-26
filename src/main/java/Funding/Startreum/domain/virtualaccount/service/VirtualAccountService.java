package funding.startreum.domain.virtualaccount.service;


import funding.startreum.domain.funding.entity.Funding;
import funding.startreum.domain.funding.service.FundingService;
import funding.startreum.domain.project.entity.Project;
import funding.startreum.domain.project.repository.ProjectRepository;
import funding.startreum.domain.project.service.ProjectService;
import funding.startreum.domain.transaction.entity.Transaction;
import funding.startreum.domain.transaction.repository.TransactionRepository;
import funding.startreum.domain.transaction.service.TransactionService;
import funding.startreum.domain.users.User;
import funding.startreum.domain.users.UserRepository;
import funding.startreum.domain.virtualaccount.dto.VirtualAccountDtos;
import funding.startreum.domain.virtualaccount.dto.request.AccountPaymentRequest;
import funding.startreum.domain.virtualaccount.dto.request.AccountRequest;
import funding.startreum.domain.virtualaccount.dto.response.AccountPaymentResponse;
import funding.startreum.domain.virtualaccount.dto.response.AccountRefundResponse;
import funding.startreum.domain.virtualaccount.dto.response.AccountResponse;
import funding.startreum.domain.virtualaccount.entity.VirtualAccount;
import funding.startreum.domain.virtualaccount.exception.AccountNotFoundException;
import funding.startreum.domain.virtualaccount.exception.NotEnoughBalanceException;
import funding.startreum.domain.transaction.transaction.TransactionNotFoundException;
import funding.startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static funding.startreum.domain.transaction.entity.Transaction.TransactionType.REFUND;
import static funding.startreum.domain.transaction.entity.Transaction.TransactionType.REMITTANCE;
import static funding.startreum.domain.virtualaccount.dto.response.AccountPaymentResponse.mapToAccountPaymentResponse;
import static funding.startreum.domain.virtualaccount.dto.response.AccountRefundResponse.mapToAccountRefundResponse;
import static funding.startreum.domain.virtualaccount.dto.response.AccountResponse.mapToAccountResponse;

// TODO 도메인 분리 작업 필요
@Service
@RequiredArgsConstructor
public class VirtualAccountService {

    private final VirtualAccountRepository virtualAccountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    private final FundingService fundingService;
    private final TransactionService transactionService;
    private final ProjectService projectService;


    /**
     * 사용자의 계좌 정보를 가져와 DTO로 반환
     */
    public VirtualAccountDtos findByName(String name) {
        User user = userRepository.findByName(name).orElse(null);
        if (user == null) {
            return new VirtualAccountDtos(false); // 계좌 없음 응답
        }

        VirtualAccount account = virtualAccountRepository.findByUser_UserId(user.getUserId()).orElse(null);
        return (account != null) ? VirtualAccountDtos.fromEntity(account) : new VirtualAccountDtos(false);
    }

    /**
     * 계좌 생성
     */
    public VirtualAccountDtos createAccount(String name) {
        User user = userRepository.findByName(name).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다: " + name));

        // 이미 계좌가 있는지 확인
        if (virtualAccountRepository.findByUser_UserId(user.getUserId()).isPresent()) {
            throw new IllegalStateException("이미 계좌가 존재합니다.");
        }

        VirtualAccount newAccount = new VirtualAccount();
        newAccount.setUser(user);
        newAccount.setBalance(BigDecimal.ZERO); // 초기 잔액 0원
        newAccount.setFundingBlock(false); // 기본적으로 펀딩 차단 없음
        newAccount.setCreatedAt(LocalDateTime.now());
        newAccount.setUpdatedAt(LocalDateTime.now());

        virtualAccountRepository.save(newAccount);
        return VirtualAccountDtos.fromEntity(newAccount);
    }

    /**
     * 계좌를 충전합니다. (계좌 ID 기반)
     *
     * @param accountId 조회할 계좌 ID
     * @param request   잔액 정보가 담겨진 DTO
     * @return 충전 후 갱신된 계좌 정보 DTO
     */
    @Transactional
    public AccountPaymentResponse chargeByAccountId(int accountId, AccountRequest request) {
        VirtualAccount account = getAccount(accountId);
        return chargeAccount(account, request);
    }

    /**
     * 계좌를 충전합니다. (username 기반)
     *
     * @param request 잔액 정보가 담겨진 DTO
     * @return 충전 후 갱신된 계좌 정보 DTO
     */
    @Transactional
    public AccountPaymentResponse chargeByUsername(String username, AccountRequest request) {
        VirtualAccount account = getAccount(username);
        return chargeAccount(account, request);
    }

    /**
     * 계좌를 충전합니다.
     *
     * @param account 충전할 VirtualAccount 엔티티
     * @param request 잔액 정보가 담긴 DTO
     * @return 충전 후 갱신된 계좌 정보 DTO
     */
    private AccountPaymentResponse chargeAccount(VirtualAccount account, AccountRequest request) {
        // 1. 잔액 업데이트
        BigDecimal beforeMoney = account.getBalance();
        account.setBalance(account.getBalance().add(request.amount()));

        // 2. 거래 내역 생성 (여기서 첫번째 파라미터는 외부 전달용 ID로, null로 처리)
        Transaction transaction = transactionService.createTransaction(null, account, account, request.amount(), REMITTANCE);

        // 3. 응답 객체 생성 및 반환
        return mapToAccountPaymentResponse(account, transaction, beforeMoney, request.amount());
    }

    /**
     * 계좌를 조회합니다. (계좌 ID 기반)
     *
     * @param accountId 조회할 계좌 ID
     * @return 조회한 계좌를 반환합니다.
     */
    public VirtualAccount getAccount(int accountId) {
        return virtualAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    /**
     * 계좌를 조회합니다. (username 기반)
     *
     * @param username 조회할 유저 ID
     * @return 조회한 계좌를 반환합니다.
     */
    public VirtualAccount getAccount(String username) {
        return virtualAccountRepository.findByUser_Name(username)
                .orElseThrow(() -> new AccountNotFoundException(username));
    }

    /**
     * 계좌를 조회합니다. (계좌 ID 기반)
     *
     * @param accountId 조회할 계좌 ID
     * @return 조회한 계좌의 정보 DTO를 반환합니다.
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountInfo(int accountId) {
        return mapToAccountResponse(getAccount(accountId));
    }

    /**
     * 계좌를 조회합니다. (username 기반)
     *
     * @param username 현재 로그인한 유저의 이름
     * @return 조회한 계좌의 정보 DTO를 반환합니다.
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountInfo(String username) {
        return mapToAccountResponse(getAccount(username));
    }

    /**
     * 결제를 진행하는 로직입니다. (계좌 ID 기반)
     *
     * @param accountId 결제할 사용자의 계좌 ID
     * @param request   프로젝트 ID와 결제 금액이 담긴 DTO
     * @param username  결제할 사용자 이름
     * @return 결제 후 갱신된 계좌 정보 DTO
     */
    @Transactional
    public AccountPaymentResponse payment(int accountId, AccountPaymentRequest request, String username) {
        // 1. 프로젝트 조회
        Project project = projectService.getProject(request.projectId());

        // 2. 계좌 조회
        VirtualAccount payerAccount = getAccount(accountId);
        VirtualAccount projectAccount = virtualAccountRepository.findBeneficiaryAccountByProjectId(request.projectId())
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // 3. 공통 결제 처리 로직 호출
        return processPayment(project, payerAccount, projectAccount, request, username);
    }

    /**
     * 결제를 진행하는 로직입니다. (username 기반)
     *
     * @param request  프로젝트 ID와 결제 금액이 담긴 DTO
     * @param username 결제할 사용자 이름
     * @return 결제 후 갱신된 계좌 정보 DTO
     */
    @Transactional
    public AccountPaymentResponse payment(AccountPaymentRequest request, String username) {
        // 1. 프로젝트 조회
        Project project = projectService.getProject(request.projectId());

        // 2. 계좌 조회
        VirtualAccount payerAccount = getAccount(username);
        VirtualAccount projectAccount = virtualAccountRepository.findBeneficiaryAccountByProjectId(request.projectId())
                .orElseThrow(() -> new AccountNotFoundException(request.projectId()));

        // 3. 공통 결제 처리 로직 호출
        return processPayment(project, payerAccount, projectAccount, request, username);
    }

    /**
     * 공통 결제 처리 로직입니다.
     *
     * @param project        결제 대상 프로젝트
     * @param payerAccount   결제자 계좌
     * @param projectAccount 프로젝트 계좌 (수혜자 계좌)
     * @param request        결제 정보 DTO
     * @param username       결제자 사용자 이름
     * @return 결제 후 갱신된 계좌 정보 DTO
     */
    private AccountPaymentResponse processPayment(Project project, VirtualAccount payerAccount,
                                                  VirtualAccount projectAccount, AccountPaymentRequest request, String username) {
        // 1. 계좌 잔액 업데이트 (결제자 계좌에서 차감, 프로젝트 계좌에 추가)
        BigDecimal payerBalanceBefore = payerAccount.getBalance();
        BigDecimal paymentAmount = request.amount();
        processAccountPayment(paymentAmount, payerAccount, projectAccount);

        // 2. 프로젝트 목표액 업데이트
        project.setCurrentFunding(project.getCurrentFunding().add(paymentAmount));

        // 3. 펀딩 및 거래 내역 저장
        Funding funding = fundingService.createFunding(project, username, paymentAmount);
        Transaction transaction = transactionService.createTransaction(funding, payerAccount, projectAccount, paymentAmount, REMITTANCE);

        // 4. 응답 객체 생성 및 반환
        return mapToAccountPaymentResponse(payerAccount, transaction, payerBalanceBefore, paymentAmount);
    }


    /**
     * 환불을 진행하는 로직입니다.
     *
     * @param payerAccountId 환불 받을 사용자 계좌 ID (결제한 계좌)
     * @param transactionId  원 거래의 ID
     * @return 환불 완료 후 갱신된 계좌 정보 DTO
     */
    @Transactional
    public AccountRefundResponse refund(int payerAccountId, int transactionId) {
        // 1. 원 거래 내역 조회
        Transaction oldTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        // 2. 계좌 조회: 환불은 수혜자 계좌에서 결제자(환불 대상) 계좌로 자금 이동
        VirtualAccount payerAccount = getAccount(payerAccountId);
        VirtualAccount projectAccount = virtualAccountRepository.findReceiverAccountByTransactionId(transactionId)
                .orElseThrow(() -> new AccountNotFoundException(transactionId));

        // 3. 환불 로직 - 계좌 잔액 업데이트 (projectAccount에서 환불 금액 차감, payerAccount에 추가)
        BigDecimal beforeMoney = payerAccount.getBalance();
        BigDecimal refundAmount = oldTransaction.getAmount();
        processAccountPayment(refundAmount, projectAccount, payerAccount);

        // 4. 펀딩 취소 및 환불 거래 내역 저장, 프로젝트 차감
        Funding funding = fundingService.cancelFunding(oldTransaction.getFunding().getFundingId());
        Transaction newTransaction = transactionService.createTransaction(funding, projectAccount, payerAccount, refundAmount, REFUND);
        Project project = projectRepository.findProjectByTransactionId(transactionId);
        project.setCurrentFunding(project.getCurrentFunding().subtract(refundAmount));

        // 5. 응답 객체 생성 및 반환 (환불 후 결제자 계좌 정보를 기준)
        return mapToAccountRefundResponse(payerAccount, newTransaction, transactionId, refundAmount, beforeMoney);
    }

    /**
     * 결제 또는 환불 시 계좌 잔액 업데이트를 진행합니다.
     *
     * @param amount        거래 금액
     * @param sourceAccount 출금(또는 환불 출금) 계좌
     * @param targetAccount 입금(또는 환불 입금) 계좌
     * @throws RuntimeException 잔액이 부족할 경우 예외 발생
     */
    private void processAccountPayment(BigDecimal amount, VirtualAccount sourceAccount, VirtualAccount targetAccount) {
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new NotEnoughBalanceException(sourceAccount.getBalance());
        }
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        targetAccount.setBalance(targetAccount.getBalance().add(amount));
    }

}