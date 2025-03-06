package funding.startreum.domain.comment.service;


import funding.startreum.domain.comment.dto.request.CommentRequest;
import funding.startreum.domain.comment.dto.response.CommentResponse;
import funding.startreum.domain.comment.entity.Comment;
import funding.startreum.domain.comment.repository.CommentRepository;
import funding.startreum.domain.project.entity.Project;
import funding.startreum.domain.project.service.ProjectService;
import funding.startreum.domain.users.entity.User;
import funding.startreum.domain.users.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static funding.startreum.domain.comment.dto.response.CommentResponse.toResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final ProjectService projectService;

    @Transactional(readOnly = true)
    public Comment getComment(int commentId) {
        return commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다 : " + commentId));
    }

    @Transactional(readOnly = true)
    public List<Comment> getComments(int projectId) {
        return commentRepository.findByProject_ProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> generateCommentsResponse(int projectId) {
        List<Comment> comments = getComments(projectId);
        return comments.stream()
                .map(CommentResponse::toResponse)
                .collect(Collectors.toList());
    }

    public Comment createComment(int projectId, CommentRequest request, String username) {
        Comment comment = new Comment();

        User user = userService.getUserByName(username);

        Project project = projectService.getProject(projectId);

        comment.setProject(project);
        comment.setUser(user);
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        return comment;
    }

    public CommentResponse generateNewCommentResponse(int projectId, CommentRequest request, String username) {
        Comment comment = createComment(projectId, request, username);
        return toResponse(comment);
    }

    public Comment updateComment(CommentRequest request, int commentId, String username) {
        Comment comment = getComment(commentId);

        if (!comment.getUser().getName().equals(username)) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        comment.setContent(request.getContent());

        return comment;
    }

    public CommentResponse generateUpdatedCommentResponse(CommentRequest request, int commentId, String username) {
        Comment comment = updateComment(request, commentId, username);
        return toResponse(comment);
    }

    public void deleteComment(int commentId, String username) {
        Comment comment = getComment(commentId);

        if (!comment.getUser().getName().equals(username)) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}

