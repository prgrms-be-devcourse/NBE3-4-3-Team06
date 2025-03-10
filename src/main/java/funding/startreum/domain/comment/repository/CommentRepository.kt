package funding.startreum.domain.comment.repository

import funding.startreum.domain.comment.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommentRepository : JpaRepository<Comment, Int> {
    fun findByCommentId(commentId: Int): Optional<Comment>

    fun findByProject_ProjectId(projectId: Int): List<Comment>
}
