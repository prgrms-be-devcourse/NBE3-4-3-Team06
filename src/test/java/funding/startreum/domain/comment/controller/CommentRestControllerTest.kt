package funding.startreum.domain.comment.controller

import com.fasterxml.jackson.databind.ObjectMapper
import funding.startreum.common.config.SecurityConfig
import funding.startreum.domain.comment.dto.request.CommentRequest
import funding.startreum.domain.comment.dto.response.CommentResponse
import funding.startreum.domain.comment.service.CommentService
import funding.startreum.domain.users.service.CustomUserDetailsService
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@Import(SecurityConfig::class)
@WebMvcTest(controllers = [CommentRestController::class])
@AutoConfigureMockMvc
class CommentRestControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var commentService: CommentService

    @MockitoBean
    lateinit var customUserDetailsService: CustomUserDetailsService

    private val BASE_URL = "/api/comment"

    @Nested
    @DisplayName("#1 댓글 조회 API (GET /api/comment/{projectId})")
    inner class GetCommentsTest {
        @Test
        @DisplayName("1-1) 댓글이 존재할 경우 댓글 조회에 성공한다")
        fun getComments_Success() {
            val projectId = 1
            val now = LocalDateTime.now()
            val commentResponse = CommentResponse(
                commentId = 1,
                projectId = projectId,
                userName = "testUser",
                content = "Test Comment",
                createdAt = now,
                updatedAt = now
            )
            given(commentService.generateCommentsResponse(projectId)).willReturn(listOf(commentResponse))

            mockMvc.perform(
                get("$BASE_URL/{projectId}", projectId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", `is`("댓글 조회에 성공했습니다.")))
                .andExpect(jsonPath("$.data[0].commentId", `is`(1)))
                .andExpect(jsonPath("$.data[0].content", `is`("Test Comment")))
        }

        @Test
        @DisplayName("1-2) 댓글이 없을 경우 '댓글이 없습니다.' 메시지를 반환한다")
        fun getComments_Empty() {
            val projectId = 1
            given(commentService.generateCommentsResponse(projectId)).willReturn(emptyList())

            mockMvc.perform(
                get("$BASE_URL/{projectId}", projectId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", `is`("댓글이 없습니다.")))
                .andExpect(jsonPath("$.data").isEmpty)
        }
    }

    @Nested
    @DisplayName("#2 댓글 생성 API (POST /api/comment/{projectId})")
    inner class CreateCommentTest {
        @Test
        @DisplayName("2-1) 인증된 사용자는 댓글 생성에 성공하고 201을 반환한다")
        @WithMockUser(username = "testUser")
        fun createComment_Success() {
            val projectId = 1
            val request = CommentRequest("New Comment")
            val now = LocalDateTime.now()
            val commentResponse = CommentResponse(
                commentId = 1,
                projectId = projectId,
                userName = "testUser",
                content = "Test Comment",
                createdAt = now,
                updatedAt = now
            )
            given(commentService.generateNewCommentResponse(projectId, request, "testUser"))
                .willReturn(commentResponse)

            mockMvc.perform(
                post("$BASE_URL/{projectId}", projectId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.message", `is`("댓글 생성에 성공했습니다.")))
                .andExpect(jsonPath("$.data.commentId", `is`(1)))
                .andExpect(jsonPath("$.data.content", `is`("Test Comment")))
        }
    }

    @Nested
    @DisplayName("#3 댓글 수정 API (PUT /api/comment/{commentId})")
    inner class UpdateCommentTest {
        @Test
        @DisplayName("3-1) 인증된 사용자는 댓글 수정에 성공한다")
        @WithMockUser(username = "testUser")
        fun updateComment_Success() {
            val commentId = 1
            val request = CommentRequest("Updated Comment")
            val now = LocalDateTime.now()
            val commentResponse = CommentResponse(
                commentId = 1,
                projectId = 1,
                userName = "testUser",
                content = "Updated Comment",
                createdAt = now,
                updatedAt = now
            )
            given(commentService.generateUpdatedCommentResponse(request, commentId, "testUser"))
                .willReturn(commentResponse)

            mockMvc.perform(
                put("$BASE_URL/{commentId}", commentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", `is`("댓글 수정에 성공했습니다.")))
                .andExpect(jsonPath("$.data.commentId", `is`(commentId)))
                .andExpect(jsonPath("$.data.content", `is`("Updated Comment")))
        }
    }

    @Nested
    @DisplayName("#4 댓글 삭제 API (DELETE /api/comment/{commentId})")
    inner class DeleteCommentTest {
        @Test
        @DisplayName("4-1) 인증된 사용자는 댓글 삭제에 성공한다")
        @WithMockUser(username = "testUser")
        fun deleteComment_Success() {
            val commentId = 1

            // delete는 응답 데이터 없이 단순 메시지 반환
            mockMvc.perform(
                delete("$BASE_URL/{commentId}", commentId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", `is`("댓글 삭제에 성공했습니다.")))
        }
    }
}