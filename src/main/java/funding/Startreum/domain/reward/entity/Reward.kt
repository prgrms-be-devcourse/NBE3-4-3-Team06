package funding.startreum.domain.reward.entity

import funding.startreum.domain.project.entity.Project
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "reward")
class Reward(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val rewardId: Int? = null, // 리워드 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Project, // 프로젝트 ID (변경 가능)

    var description: String, // ❗ `val` → `var` (값 변경 가능)

    var amount: BigDecimal, // ❗ `val` → `var` (값 변경 가능)

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime? = null // ❗ `val` → `var` (업데이트 가능)
) {
    // JPA 기본 생성자
    protected constructor() : this(
        project = Project(), // 기본 생성자에서 사용될 더미 객체
        description = "",
        amount = BigDecimal.ZERO
    )
}
