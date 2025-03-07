package funding.startreum.domain.transaction.service

import funding.startreum.domain.funding.entity.Funding
import funding.startreum.domain.transaction.entity.Transaction
import funding.startreum.domain.transaction.entity.Transaction.TransactionType
import funding.startreum.domain.transaction.repository.TransactionRepository
import funding.startreum.domain.transaction.transaction.TransactionNotFoundException
import funding.startreum.domain.users.repository.UserRepository
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
) {

    /**
     * 거래 내역 조회
     * @param transactionId 거래 ID
     * @return Transaction 객체
     */
    @Transactional(readOnly = true)
    fun getTransaction(transactionId: Int): Transaction =
        transactionRepository.findById(transactionId)
            .orElseThrow { TransactionNotFoundException(transactionId) }!!

    /**
     * 거래 내역 생성 메서드
     *
     * @param funding         관련 펀딩 내역
     * @param from   자금 출금 계좌 (결제 시에는 결제자, 환불 시에는 프로젝트 계좌)
     * @param to 자금 입금 계좌 (결제 시에는 프로젝트 계좌, 환불 시에는 결제자 계좌)
     * @param amount          거래 금액
     * @param type            거래 유형 (REMITTANCE 또는 REFUND)
     * @return 생성된 Transaction 객체
     */
    @Transactional
    fun createTransaction(
        funding: Funding?,
        from: VirtualAccount,
        to: VirtualAccount,
        amount: BigDecimal,
        type: TransactionType
    ): Transaction {
        val transaction = Transaction().apply {
            this.funding = funding
            this.admin = userRepository.findByName("funding.startreum.domain.admin.entity.Admin").orElse(null)
            this.senderAccount = from
            this.receiverAccount = to
            this.amount = amount
            this.type = type
            this.transactionDate = LocalDateTime.now()
        }

        transactionRepository.save(transaction)
        return transaction
    }
}
