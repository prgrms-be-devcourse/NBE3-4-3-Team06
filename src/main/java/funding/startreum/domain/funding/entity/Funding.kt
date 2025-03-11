package funding.startreum.domain.funding.entity


import funding.startreum.domain.users.entity.User
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.reward.entity.Reward
import funding.startreum.domain.transaction.entity.Transaction
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "funding")
class Funding(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var fundingId: Int? = null, // 펀딩 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsor_id", nullable = false)
    var sponsor: User, // 후원자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Project, // 프로젝트 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    var reward: Reward?, // 리워드 ID (nullable 가능)

    var amount: BigDecimal, // 후원 금액
    var fundedAt: LocalDateTime, // 후원 일자

    @Column(nullable = false)
    var isDeleted: Boolean = false, // 삭제 여부, 기본 false

    @OneToMany(mappedBy = "funding", cascade = [CascadeType.ALL], orphanRemoval = true)
    var transactions: MutableList<Transaction> = mutableListOf() // 트랜잭션 리스트 추가

) {
    // 기본 생성자 추가 (JPA 사용 시 필수)
    constructor() : this(
        null, User(), Project(), null, BigDecimal.ZERO, LocalDateTime.now(), false, mutableListOf()
    )
}
