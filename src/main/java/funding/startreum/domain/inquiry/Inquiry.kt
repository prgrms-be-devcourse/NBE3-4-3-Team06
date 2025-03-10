package funding.startreum.domain.inquiry


import funding.startreum.domain.users.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "inquiries")
class Inquiry(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var inquiryId: Int? = null, // 문의 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User, // 문의 작성자 ID

    var title: String, // 문의 제목

    @Lob
    var content: String, // 문의 내용

    @Enumerated(EnumType.STRING)
    var status: Status = Status.PENDING, // 문의 상태 (기본값: PENDING)

    @Lob
    var adminResponse: String? = null, // 관리자 응답 내용 (nullable)

    var createdAt: LocalDateTime = LocalDateTime.now(), // 문의 작성 일자
    var updatedAt: LocalDateTime? = null // 문의 업데이트 일자 (nullable)

) {
    // 기본 생성자 추가 (JPA 필수)
    constructor() : this(null, User(), "", "", Status.PENDING, null, LocalDateTime.now(), null)

    enum class Status {
        PENDING, // 대기중
        RESOLVED // 완료
    }
}
