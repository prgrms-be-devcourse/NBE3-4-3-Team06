package funding.startreum.domain.virtualaccount.service

import funding.startreum.domain.funding.service.FundingService
import funding.startreum.domain.project.repository.ProjectRepository
import funding.startreum.domain.transaction.entity.Transaction.TransactionType
import funding.startreum.domain.transaction.service.TransactionService
import funding.startreum.domain.virtualaccount.dto.response.AccountRefundResponse
import funding.startreum.domain.virtualaccount.dto.response.AccountRefundResponse.Companion.mapToAccountRefundResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
open class AccountRefundService(
    private val transactionService: TransactionService,
    private val accountQueryService: AccountQueryService,
    private val fundingService: FundingService,
    private val projectRepository: ProjectRepository,
) {

    /**
     * 환불을 진행하는 로직입니다.
     *
     * @param payerAccountId 환불 받을 사용자 계좌 ID (결제한 계좌)
     * @param transactionId  원 거래의 ID
     * @return AccountRefundResponse
     */
    @Transactional
    open fun refund(payerAccountId: Int, transactionId: Int): AccountRefundResponse {
        // 1) 원 거래 조회
        val oldTransaction = transactionService.getTransaction(transactionId)

        // 2) 계좌 조회
        val payerAccount = accountQueryService.getAccountByAccountId(payerAccountId)
        val projectAccount = accountQueryService.getReceiverAccountByTransactionId(transactionId)

        // 3) 환불 처리: 프로젝트 계좌에서 환불 금액 출금하여 결제자 계좌에 입금
        val beforeMoney = payerAccount.balance
        val refundAmount = oldTransaction.amount
        projectAccount.transferTo(refundAmount, payerAccount)

        // 4) 펀딩 취소 및 거래 내역 생성
        val funding = fundingService.cancelFunding(oldTransaction.funding!!.fundingId!!)
        val newTransaction = transactionService.createTransaction(
            funding,
            projectAccount,
            payerAccount,
            refundAmount,
            TransactionType.REFUND
        )

        // 5) 프로젝트의 현재 펀딩 금액 차감
        val project = projectRepository.findProjectByTransactionId(transactionId)
        project.currentFunding = project.currentFunding.subtract(refundAmount)

        // 6) 응답 객체 반환
        return mapToAccountRefundResponse(payerAccount, newTransaction, transactionId, refundAmount, beforeMoney)
    }
}
