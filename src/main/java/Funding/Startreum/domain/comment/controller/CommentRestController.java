package funding.startreum.domain.comment.controller;

import funding.startreum.common.util.ApiResponse;
import funding.startreum.domain.comment.dto.request.CommentRequest;
import funding.startreum.domain.comment.dto.response.CommentResponse;
import funding.startreum.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <h2>CommentRestController 클래스</h2>
 * 댓글 관련 작업을 처리하는 REST 컨트롤러입니다.
 * 결과를 {@link ApiResponse} 형태로 반환하니다.
 *
 * <p><strong>주요 엔드포인트</strong></p>
 * <ul>
 *   <li>GET /api/comment/{projectId} - 지정된 프로젝트의 댓글 조회</li>
 *   <li>POST /api/comment/{projectId} - 지정된 프로젝트에 댓글 생성</li>
 *   <li>PUT /api/comment/{commentId} - 지정된 댓글 수정</li>
 *   <li>DELETE /api/comment/{commentId} - 지정된 댓글 삭제</li>
 * </ul>
 *
 * @author 한상훈
 */
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@Slf4j
public class CommentRestController {

    private final CommentService commentService;

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getComment(
            @PathVariable("projectId") int projectId) {
        log.debug("프로젝트 ID {}의 댓글을 조회합니다.", projectId);
        List<CommentResponse> response = commentService.generateCommentsResponse(projectId);

        if (response.isEmpty()) {
            log.debug("프로젝트 ID {}에 댓글이 없습니다.", projectId);
            return ResponseEntity.ok(ApiResponse.success("댓글이 없습니다.", response));
        } else {
            log.debug("프로젝트 ID {}에 {}개의 댓글이 조회되었습니다.", projectId, response.size());
            return ResponseEntity.ok(ApiResponse.success("댓글 조회에 성공했습니다.", response));
        }

    }

    @PostMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createComment(
            @PathVariable("projectId") int projectId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.debug("사용자 {}가 프로젝트 ID {}에 댓글을 생성합니다.", userDetails.getUsername(), projectId);
        CommentResponse response = commentService.generateNewCommentResponse(projectId, request, userDetails.getUsername());
        log.debug("프로젝트 ID {}에 댓글 생성에 성공했습니다.", projectId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("댓글 생성에 성공했습니다.", response));
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> modifyComment(
            @PathVariable("commentId") int commentId,
            @RequestBody @Valid CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.debug("사용자 {}가 댓글 ID {}를 수정합니다.", userDetails.getUsername(), commentId);
        CommentResponse response = commentService.generateUpdatedCommentResponse(request, commentId, userDetails.getUsername());
        log.debug("댓글 ID {} 수정에 성공했습니다.", commentId);
        return ResponseEntity.ok(ApiResponse.success("댓글 수정에 성공했습니다.", response));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(
            @PathVariable("commentId") int commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.debug("사용자 {}가 댓글 ID {}를 삭제합니다.", userDetails.getUsername(), commentId);
        commentService.deleteComment(commentId, userDetails.getUsername());
        log.debug("댓글 ID {} 삭제에 성공했습니다.", commentId);
        return ResponseEntity.ok(ApiResponse.success("댓글 삭제에 성공했습니다."));
    }

}
