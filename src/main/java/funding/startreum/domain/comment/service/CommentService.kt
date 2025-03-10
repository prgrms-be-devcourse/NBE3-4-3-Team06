package funding.startreum.domain.comment.service

import funding.startreum.domain.comment.dto.request.CommentRequest
import funding.startreum.domain.comment.dto.response.CommentResponse
import funding.startreum.domain.comment.dto.response.CommentResponse.Companion.toResponse
import funding.startreum.domain.comment.entity.Comment
import funding.startreum.domain.comment.repository.CommentRepository
import funding.startreum.domain.project.service.ProjectService
import funding.startreum.domain.users.service.UserService
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.stream.Collectors

@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepository,
    private val userService: UserService,
    private val projectService: ProjectService,
) {

    @Transactional(readOnly = true)
    fun getComment(commentId: Int): Comment {
        return commentRepository.findByCommentId(commentId)
            .orElseThrow { EntityNotFoundException("댓글을 찾을 수 없습니다 : $commentId") }
    }

    @Transactional(readOnly = true)
    fun getComments(projectId: Int): List<Comment> {
        return commentRepository.findByProject_ProjectId(projectId)
    }

    @Transactional(readOnly = true)
    fun generateCommentsResponse(projectId: Int): List<CommentResponse> {
        val comments = getComments(projectId)
        return comments.stream()
            .map { obj: Comment -> toResponse(comment = obj) }
            .collect(Collectors.toList())
    }

    fun createComment(projectId: Int, request: CommentRequest, username: String): Comment {
        val currentUser = userService.getUserByName(username)
        val currentProject = projectService.getProject(projectId)

        val comment = Comment().apply {
            project = currentProject
            user = currentUser
            content = request.content
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        commentRepository.save(comment)

        return comment
    }

    fun generateNewCommentResponse(projectId: Int, request: CommentRequest, username: String): CommentResponse {
        val comment = createComment(projectId, request, username)
        return toResponse(comment)
    }

    fun updateComment(request: CommentRequest, commentId: Int, username: String): Comment {
        val comment = getComment(commentId)
        comment.verifyPermission(username)
        comment.content = request.content
        return comment
    }

    fun generateUpdatedCommentResponse(request: CommentRequest, commentId: Int, username: String): CommentResponse {
        val comment = updateComment(request, commentId, username)
        return toResponse(comment)
    }

    fun deleteComment(commentId: Int, username: String) {
        val comment = getComment(commentId)
        comment.verifyPermission(username)
        commentRepository.delete(comment)
    }
}

