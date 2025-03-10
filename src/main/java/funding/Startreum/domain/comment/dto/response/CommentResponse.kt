package funding.startreum.domain.comment.dto.response

import funding.startreum.domain.comment.entity.Comment
import java.time.LocalDateTime

data class CommentResponse(
    val commentId: Int,
    val projectId: Int,
    val userName: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
) {
    companion object {
        @JvmStatic
        fun toResponse(comment: Comment): CommentResponse {
            return CommentResponse(
                comment.commentId!!,
                comment.project.projectId!!,
                comment.user.name,
                comment.content,
                comment.createdAt,
                comment.updatedAt

            )
        }
    }
}