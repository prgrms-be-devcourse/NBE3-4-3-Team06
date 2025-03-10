package funding.startreum.domain.comment.controller

import funding.startreum.common.util.ApiResponse
import funding.startreum.domain.comment.dto.request.CommentRequest
import funding.startreum.domain.comment.service.CommentService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

/**
 * **CommentRestController 클래스**
 *
 * 댓글 관련 작업을 처리하는 REST 컨트롤러입니다.
 * 결과를 [ApiResponse] 형태로 반환합니다.
 *
 * **주요 엔드포인트**
 * - `GET /api/comment/{projectId}` - 지정된 프로젝트의 댓글 조회
 * - `POST /api/comment/{projectId}` - 지정된 프로젝트에 댓글 생성
 * - `PUT /api/comment/{commentId}` - 지정된 댓글 수정
 * - `DELETE /api/comment/{commentId}` - 지정된 댓글 삭제
 *
 * @author 한상훈
 */
@RestController
@RequestMapping("/api/comment")
open class CommentRestController(
    private val commentService: CommentService
) {
    private val log = LoggerFactory.getLogger(CommentRestController::class.java)

    /**
     * **프로젝트의 댓글 조회**
     *
     * @param projectId 프로젝트 ID
     * @return 댓글 목록을 포함한 응답
     */
    @GetMapping("/{projectId}")
    fun getComment(@PathVariable projectId: Int): ResponseEntity<Any> {
        log.debug("프로젝트 ID $projectId 의 댓글을 조회합니다.")
        val response = commentService.generateCommentsResponse(projectId)

        return if (response.isEmpty()) {
            log.debug("프로젝트 ID $projectId 에 댓글이 없습니다.")
            ResponseEntity.ok(ApiResponse.success("댓글이 없습니다.", response))
        } else {
            log.debug("프로젝트 ID $projectId 에 ${response.size}개의 댓글이 조회되었습니다.")
            ResponseEntity.ok(ApiResponse.success("댓글 조회에 성공했습니다.", response))
        }
    }

    /**
     * **댓글 생성**
     *
     * @param projectId 프로젝트 ID
     * @param request 댓글 요청 데이터
     * @param userDetails 인증된 사용자 정보
     * @return 생성된 댓글 응답
     */
    @PostMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    fun createComment(
        @PathVariable projectId: Int,
        @Valid @RequestBody request: CommentRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Any> {
        log.debug("사용자 ${userDetails.username} 가 프로젝트 ID $projectId 에 댓글을 생성합니다.")
        val response = commentService.generateNewCommentResponse(projectId, request, userDetails.username)
        log.debug("프로젝트 ID $projectId 에 댓글 생성에 성공했습니다.")

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("댓글 생성에 성공했습니다.", response))
    }

    /**
     * **댓글 수정**
     *
     * @param commentId 댓글 ID
     * @param request 댓글 요청 데이터
     * @param userDetails 인증된 사용자 정보
     * @return 수정된 댓글 응답
     */
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    fun modifyComment(
        @PathVariable commentId: Int,
        @Valid @RequestBody request: CommentRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Any> {
        log.debug("사용자 ${userDetails.username} 가 댓글 ID $commentId 를 수정합니다.")
        val response = commentService.generateUpdatedCommentResponse(request, commentId, userDetails.username)
        log.debug("댓글 ID $commentId 수정에 성공했습니다.")

        return ResponseEntity.ok(ApiResponse.success("댓글 수정에 성공했습니다.", response))
    }


    /**
     * **댓글 삭제**
     *
     * @param commentId 댓글 ID
     * @param userDetails 인증된 사용자 정보
     * @return 삭제 성공 응답
     */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    fun deleteComment(
        @PathVariable commentId: Int,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<String>> {
        log.debug("사용자 ${userDetails.username} 가 댓글 ID $commentId 를 삭제합니다.")
        commentService.deleteComment(commentId, userDetails.username)
        log.debug("댓글 ID $commentId 삭제에 성공했습니다.")

        return ResponseEntity.ok(ApiResponse.success("댓글 삭제에 성공했습니다."))
    }

}
