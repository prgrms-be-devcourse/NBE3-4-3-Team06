package funding.startreum.domain.project.entity

import funding.startreum.domain.comment.entity.Comment
import funding.startreum.domain.funding.entity.Funding
import funding.startreum.domain.reward.entity.Reward
import funding.startreum.domain.users.entity.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "project")
class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var projectId: Int? = null, // 프로젝트 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    var creator: User, // 수혜자 ID (User와 다대일 관계)

    var title: String, // 프로젝트 제목
    var simpleDescription: String, // 제목 밑 간단한 설명
    var bannerUrl: String?, // 배너 이미지 URL (nullable 가능)

    @Lob
    var description: String, // 프로젝트 설명

    var fundingGoal: BigDecimal, // 펀딩 목표 금액
    var currentFunding: BigDecimal, // 현재 펀딩 금액
    var startDate: LocalDateTime, // 펀딩 시작일
    var endDate: LocalDateTime, // 펀딩 종료일

    @Enumerated(EnumType.STRING)
    var status: Status, // 상태 (ONGOING, SUCCESS, FAILED)

    @Enumerated(EnumType.STRING)
    var isApproved: ApprovalStatus, // 관리자 승인 여부 (승인 대기, 승인, 거절)

    var isDeleted: Boolean, // 삭제 여부
    var createdAt: LocalDateTime, // 생성 일자
    var updatedAt: LocalDateTime, // 수정 일자

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    var fundings: MutableList<Funding> = mutableListOf(), // 받은 펀딩 목록

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<Comment> = mutableListOf(), // 댓글 목록

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    var rewards: MutableList<Reward> = mutableListOf() // 리워드 목록

) {
    // Kotlin의 enum class
    enum class Status {
        ONGOING, // 진행중
        SUCCESS, // 성공
        FAILED   // 실패
    }

    enum class ApprovalStatus {
        AWAITING_APPROVAL, // 승인 대기
        APPROVE,            // 승인
        REJECTED            // 거절
    }

    // JPA용 기본 생성자 (필수)
    constructor() : this(
        null, User(), "", "", null, "", BigDecimal.ZERO, BigDecimal.ZERO,
        LocalDateTime.now(), LocalDateTime.now(), Status.ONGOING, ApprovalStatus.AWAITING_APPROVAL,
        false, LocalDateTime.now(), LocalDateTime.now()
    )
}
