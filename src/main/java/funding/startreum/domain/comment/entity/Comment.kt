package funding.startreum.domain.comment.entity



import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.users.entity.User

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "comments")
class Comment(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var commentId: Int? = null, // 댓글 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Project, // 프로젝트 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User, // 작성자 ID

    @Lob
    var content: String, // 댓글 내용

    var createdAt: LocalDateTime = LocalDateTime.now(), // 작성 일자
    var updatedAt: LocalDateTime? = null // 수정 일자 (nullable)
) {
    // 기본 생성자 추가 (JPA 필수)
    constructor() : this(null, Project(), User(), "", LocalDateTime.now(), null)
}
