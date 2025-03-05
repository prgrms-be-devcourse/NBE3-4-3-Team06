package funding.startreum.domain.virtualaccount.service;


import funding.startreum.domain.funding.entity.Funding;
import funding.startreum.domain.funding.service.FundingService;
import funding.startreum.domain.project.entity.Project;
import funding.startreum.domain.project.repository.ProjectRepository;
import funding.startreum.domain.transaction.entity.Transaction;
import funding.startreum.domain.transaction.service.TransactionService;
import funding.startreum.domain.virtualaccount.dto.response.AccountRefundResponse;
import funding.startreum.domain.virtualaccount.entity.VirtualAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static funding.startreum.domain.transaction.entity.Transaction.TransactionType.REFUND;
import static funding.startreum.domain.virtualaccount.dto.response.AccountRefundResponse.mapToAccountRefundResponse;

@Service
@RequiredArgsConstructor
public class AccountRefundService {
    private final TransactionService transactionService;
    private final AccountQueryService accountQueryService;
    private final FundingService fundingService;
    private final ProjectRepository projectRepository;

    /**
     * 환불을 진행하는 로직입니다.
     *
     * @param payerAccountId 환불 받을 사용자 계좌 ID (결제한 계좌)
     * @param transactionId  원 거래의 ID
     * @return AccountRefundResponse
     */
    @Transactional
    public AccountRefundResponse refund(int payerAccountId, int transactionId) {
        // 1) 원 거래 조회
        Transaction oldTransaction = transactionService.getTransaction(transactionId);

        // 2) 계좌 조회
        VirtualAccount payerAccount = accountQueryService.getAccount(payerAccountId);
        VirtualAccount projectAccount = accountQueryService.getReceiverAccountByTransactionId(transactionId);

        // 3) 환불 처리: 프로젝트 계좌에서 환불 금액 출금하여 결제자 계좌에 입금
        BigDecimal beforeMoney = payerAccount.getBalance();
        BigDecimal refundAmount = oldTransaction.getAmount();
        projectAccount.transferTo(refundAmount, payerAccount);

        // 4) 펀딩 취소 및 거래 내역 생성
        Funding funding = fundingService.cancelFunding(oldTransaction.getFunding().getFundingId());
        Transaction newTransaction = transactionService.createTransaction(funding, projectAccount, payerAccount, refundAmount, REFUND);

        // 5) 프로젝트의 현재 펀딩 금액 차감
        Project project = projectRepository.findProjectByTransactionId(transactionId);
        project.setCurrentFunding(project.getCurrentFunding().subtract(refundAmount));

        // 6) 응답 객체 반환
        return mapToAccountRefundResponse(payerAccount, newTransaction, transactionId, refundAmount, beforeMoney);
    }


}
