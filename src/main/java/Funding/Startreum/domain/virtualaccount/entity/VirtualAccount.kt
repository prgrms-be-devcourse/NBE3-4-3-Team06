package funding.startreum.domain.virtualaccount.entity

import funding.startreum.domain.users.entity.User
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
}
