package funding.startreum.domain.virtualaccount.entity

import funding.startreum.domain.users.entity.User
import funding.startreum.domain.virtualaccount.exception.NotEnoughBalanceException
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "virtual_accounts")
class VirtualAccount(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var accountId: Int? = null, // 가상 계좌 ID

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User, // 사용자 ID

    @Column(nullable = false, precision = 18, scale = 0) // 정수만 저장
    var balance: BigDecimal = BigDecimal.ZERO, // 현재 잔액

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(), // 계좌 생성 일자

    var updatedAt: LocalDateTime? = null, // 계좌 업데이트 일자

    var fundingBlock: Boolean = false // 펀딩 관련 송금 차단 여부
) {
    // 기본 생성자 추가 (JPA에서 필수)
    constructor() : this(
        null, User(), BigDecimal.ZERO, LocalDateTime.now(), null, false
    )

    /**
     * 현재 계좌에서 출금하여 대상 계좌로 자금을 이체합니다.
     *
     * @param amount        거래 금액
     * @param to 입금(또는 환불 입금) 대상 계좌
     * @throws NotEnoughBalanceException 잔액이 부족할 경우 예외 발생
     */
    fun transferTo(amount: BigDecimal, to: VirtualAccount) {
        if (this.balance < amount) throw NotEnoughBalanceException(this.balance)
        // 대상 계좌에 입금
        this.balance = balance.subtract(amount)
        to.balance = to.balance.add(amount)
    }
}
