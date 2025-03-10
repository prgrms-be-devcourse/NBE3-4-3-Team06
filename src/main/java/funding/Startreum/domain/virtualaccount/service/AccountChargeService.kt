package funding.startreum.domain.virtualaccount.service

import funding.startreum.domain.transaction.entity.Transaction.TransactionType
import funding.startreum.domain.transaction.service.TransactionService
import funding.startreum.domain.virtualaccount.dto.request.AccountRequest
import funding.startreum.domain.virtualaccount.dto.response.AccountPaymentResponse
import funding.startreum.domain.virtualaccount.dto.response.AccountPaymentResponse.Companion.mapToAccountPaymentResponse
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional
class AccountChargeService(
    private val transactionService: TransactionService,
    private val accountQueryService: AccountQueryService
) {

    /**
     * 계좌를 충전합니다. (계좌 ID 기반)
     *
     * @param accountId 조회할 계좌 ID
     * @param request   잔액 정보가 담긴 DTO
     * @return AccountPaymentResponse
     */
    fun chargeByAccountId(accountId: Int, request: AccountRequest): AccountPaymentResponse {
        val account = accountQueryService.getAccountByAccountId(accountId)
        return chargeAccount(account, request)
    }

    /**
     * 계좌를 충전합니다. (username 기반)
     *
     * @param username  조회할 username
     * @param request   잔액 정보가 담긴 DTO
     * @return AccountPaymentResponse
     */
    fun chargeByUsername(username: String, request: AccountRequest): AccountPaymentResponse {
        val account = accountQueryService.getAccountByUsername(username)
        return chargeAccount(account, request)
    }

    /**
     * 계좌를 충전합니다.
     *
     * @param account 충전할 VirtualAccount 엔티티
     * @param request 잔액 정보가 담긴 DTO
     * @return AccountPaymentResponse
     */
    private fun chargeAccount(account: VirtualAccount, request: AccountRequest): AccountPaymentResponse {
        // 충전 금액이 0 이하일 경우 예외 처리
        if (request.amount <= BigDecimal.ZERO) {
            throw IllegalArgumentException("충전 금액은 0보다 커야 합니다.")
        }

        // 1. 잔액 업데이트
        val beforeMoney = account.balance
        account.balance = account.balance.add(request.amount)

        // 2. 거래 내역 생성 (외부 전달용 ID는 null로 처리)
        val transaction = transactionService.createTransaction(
            funding = null,
            from = account,
            to = account,
            amount = request.amount,
            type = TransactionType.REMITTANCE
        )

        // 3. 응답 객체 생성 및 반환
        return mapToAccountPaymentResponse(account, transaction, beforeMoney, request.amount)
    }
}
