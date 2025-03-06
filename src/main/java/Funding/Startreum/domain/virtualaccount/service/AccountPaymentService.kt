package funding.startreum.domain.virtualaccount.service

import funding.startreum.domain.funding.service.FundingService
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.project.service.ProjectService
import funding.startreum.domain.transaction.entity.Transaction.TransactionType
import funding.startreum.domain.transaction.service.TransactionService
import funding.startreum.domain.virtualaccount.dto.request.AccountPaymentRequest
import funding.startreum.domain.virtualaccount.dto.response.AccountPaymentResponse
import funding.startreum.domain.virtualaccount.dto.response.AccountPaymentResponse.Companion.mapToAccountPaymentResponse
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountPaymentService(
    private val transactionService: TransactionService,
    private val projectService: ProjectService,
    private val fundingService: FundingService,
    private val accountQueryService: AccountQueryService
) {

    /**
     * 계좌 ID 기반으로 결제합니다.
     *
     * @param accountId 계좌 ID
     * @param request   프로젝트 ID, 결제 금액
     * @param username  유저 이름
     * @return 결제자 기반 DTO
     */
    @Transactional
    fun paymentByAccountId(
        accountId: Int,
        request: AccountPaymentRequest,
        username: String
    ): AccountPaymentResponse {
        val project = projectService.getProject(request.projectId)

        val payerAccount = accountQueryService.getAccountByAccountId(accountId)
        val projectAccount = accountQueryService.getAccountByProjectId(request.projectId)

        return processPayment(project, payerAccount, projectAccount, request, username)
    }

    /**
     * username 기반을 결제합니다.
     *
     * @param request  프로젝트 ID, 결제 금액
     * @param username 유저 이름
     * @return 결제자 기반 DTO
     */
    @Transactional
    fun paymentByUsername(
        request: AccountPaymentRequest,
        username: String
    ): AccountPaymentResponse {
        val project = projectService.getProject(request.projectId)

        val payerAccount = accountQueryService.getAccountByUsername(username)
        val projectAccount = accountQueryService.getAccountByProjectId(request.projectId)

        return processPayment(project, payerAccount, projectAccount, request, username)
    }

    /**
     * 공통 결제 처리 로직입니다.
     *
     * @param project  결제 대상 프로젝트
     * @param from     결제자 계좌
     * @param to       수혜자 계좌
     * @param request  결제 정보 DTO
     * @param username 결제자 사용자 이름
     * @return AccountPaymentResponse
     */
    private fun processPayment(
        project: Project,
        from: VirtualAccount,
        to: VirtualAccount,
        request: AccountPaymentRequest,
        username: String
    ): AccountPaymentResponse {
        val payerBalanceBefore = from.balance
        val paymentAmount = request.amount

        // 1) 결제 처리
        from.transferTo(paymentAmount, to)

        // 2) 프로젝트 모금액 업데이트
        project.currentFunding = project.currentFunding.add(paymentAmount)


        // 3) 펀딩 및 거래 내역 생성
        val funding = fundingService.createFunding(project, username, paymentAmount)
        val transaction =
            transactionService.createTransaction(funding, from, to, paymentAmount, TransactionType.REMITTANCE)

        // 4) 응답 객체 반환
        return mapToAccountPaymentResponse(from, transaction, payerBalanceBefore, paymentAmount)
    }
}
