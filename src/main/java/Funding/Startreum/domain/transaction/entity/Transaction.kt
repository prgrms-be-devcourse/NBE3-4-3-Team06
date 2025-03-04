package funding.startreum.domain.transaction.entity

import funding.startreum.domain.users.entity.User
import funding.startreum.domain.funding.entity.Funding
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "Transaction")
class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var transactionId: Int? = null, // 거래 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funding_id", updatable = false)
    var funding: Funding, // 펀딩 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", updatable = false)
    var admin: User, // 관리자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id", updatable = false, nullable = false)
    var senderAccount: VirtualAccount, // 송신 계좌

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_account_id", updatable = false, nullable = false)
    var receiverAccount: VirtualAccount, // 수신 계좌

    @Column(updatable = false, nullable = false)
    var amount: BigDecimal = BigDecimal.ZERO, // 거래 금액

    @Enumerated(EnumType.STRING)
    @Column(updatable = false, nullable = false)
    var type: TransactionType, // 거래 유형

    @Column(updatable = false, nullable = false)
    var transactionDate: LocalDateTime = LocalDateTime.now() // 거래 일자

) {
    // 기본 생성자 추가 (JPA에서 필수)
    constructor() : this(
        null, Funding(), User(), VirtualAccount(), VirtualAccount(), BigDecimal.ZERO, TransactionType.REMITTANCE, LocalDateTime.now()
    )

    enum class TransactionType {
        REMITTANCE,  // 송금
        REFUND       // 환불
    }
}
