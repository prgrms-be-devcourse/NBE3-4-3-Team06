package funding.startreum.domain.comment.dto.response;

import funding.startreum.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        int commentId,
        int projectId,
//        int userId,
        String userName,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getCommentId(),
                comment.getProject().getProjectId(),
//                comment.getUser().getUserId(),
                comment.getUser().getName(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()

        );
    }
}