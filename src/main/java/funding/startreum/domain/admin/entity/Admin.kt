package funding.startreum.domain.admin.entity

import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.users.entity.User


import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "admin")
data class Admin(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val actionId: Int? = null, // 관리자 행동 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    val admin: User, // 관리자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    val project: Project?, // 프로젝트 ID (nullable 가능성 고려)

    @Enumerated(EnumType.STRING)
    val actionType: ActionType, // 행동 유형

    val actionDate: LocalDateTime // 행동 일자
) {
    enum class ActionType {
        APPROVE, // 승인
        REJECT   // 거절
    }

    // No-Args Constructor를 강제로 추가
    constructor() : this(null, User(), null, ActionType.APPROVE, LocalDateTime.now())
}
