package funding.startreum.domain.comment.service

import funding.startreum.domain.comment.dto.request.CommentRequest
import funding.startreum.domain.comment.entity.Comment
import funding.startreum.domain.comment.repository.CommentRepository
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.project.service.ProjectService
import funding.startreum.domain.users.entity.User
import funding.startreum.domain.users.service.UserService
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.springframework.security.access.AccessDeniedException
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class CommentServiceTest {
    @Mock
    lateinit var commentRepository: CommentRepository

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var projectService: ProjectService

    @InjectMocks
    lateinit var commentService: CommentService

    lateinit var testUser: User
    lateinit var testProject: Project
    lateinit var testComment: Comment
    lateinit var now: LocalDateTime

    @BeforeEach
    fun setUp() {
        testUser = User()
        testUser.name = "testUser"

        testProject = Project()
        testProject.projectId = 1

        now = LocalDateTime.now()

        testComment = Comment()
        testComment.commentId = 1
        testComment.content = "Original Content"
        testComment.user = testUser
        testComment.project = testProject
        testComment.createdAt = now
        testComment.updatedAt = now
    }

    @Nested
    @DisplayName("getComment() 테스트")
    internal inner class GetCommentTests {
        @Test
        @DisplayName("존재하는 댓글 반환")
        fun testGetComment() {
            given(commentRepository.findByCommentId(1)).willReturn(
                Optional.of(
                    testComment
                )
            )

            val comment = commentService.getComment(1)

            Assertions.assertThat(comment).isEqualTo(testComment)
        }

        @Test
        @DisplayName("댓글 미존재 시 EntityNotFoundException 발생")
        fun testGetCommentNotFound() {
            BDDMockito.given(commentRepository.findByCommentId(2)).willReturn(Optional.empty())

            val thrown = Assertions.catchThrowable {
                commentService.getComment(
                    2
                )
            }
            Assertions.assertThat(thrown).isInstanceOf(EntityNotFoundException::class.java)
        }
    }

    @Nested
    @DisplayName("getComments() 테스트")
    internal inner class GetCommentsTests {
        @Test
        @DisplayName("프로젝트의 댓글 리스트 반환")
        fun testGetComments() {
            val commentsList = listOf(testComment)
            BDDMockito.given<List<Comment?>>(
                commentRepository.findByProject_ProjectId(1)
            ).willReturn(commentsList)

            val comments = commentService.getComments(1)

            Assertions.assertThat(comments).isEqualTo(commentsList)
        }
    }

    @Nested
    @DisplayName("generateCommentsResponse() 테스트")
    internal inner class GenerateCommentsResponseTests {
        @Test
        @DisplayName("댓글 응답 DTO 리스트 반환")
        fun testGenerateCommentsResponse() {
            val commentsList = listOf(testComment)
            BDDMockito.given<List<Comment?>>(
                commentRepository.findByProject_ProjectId(1)
            ).willReturn(commentsList)

            val responses = commentService.generateCommentsResponse(1)

            Assertions.assertThat(responses).isNotEmpty()
            Assertions.assertThat(responses[0].commentId).isEqualTo(testComment.commentId)
            Assertions.assertThat(responses[0].content).isEqualTo(testComment.content)
        }
    }

    @Nested
    @DisplayName("createComment() 및 generateNewCommentResponse() 테스트")
    internal inner class CreateAndGenerateNewCommentResponseTests {
        @Test
        @DisplayName("새 댓글 생성 및 응답 DTO 반환")
        fun testGenerateNewCommentResponse() {
            val request = CommentRequest("New Comment")

            BDDMockito.given(userService.getUserByName("testUser")).willReturn(testUser)
            BDDMockito.given(projectService.getProject(1)).willReturn(testProject)
            BDDMockito.given(
                commentRepository.save(
                    ArgumentMatchers.any(
                        Comment::class.java
                    )
                )
            ).willAnswer { invocation: InvocationOnMock ->
                val comment =
                    invocation.getArgument<Comment>(0)
                comment.commentId = 100
                comment
            }


            val response = commentService.generateNewCommentResponse(1, request, "testUser")

            Assertions.assertThat(response.content).isEqualTo("New Comment")
            Assertions.assertThat(response.createdAt).isNotNull()
            Assertions.assertThat(response.updatedAt).isNotNull()
        }
    }

    @Nested
    @DisplayName("updateComment() 및 generateUpdatedCommentResponse() 테스트")
    internal inner class UpdateAndGenerateUpdatedCommentResponseTests {
        @Test
        @DisplayName("댓글 작성자와 동일하면 댓글 수정")
        fun testUpdateComment() {
            val request = CommentRequest("Updated Content")
            given(commentRepository.findByCommentId(1)).willReturn(
                Optional.of(
                    testComment
                )
            )

            val updatedComment = commentService.updateComment(request, 1, "testUser")

            Assertions.assertThat(updatedComment.content).isEqualTo("Updated Content")
        }

        @Test
        @DisplayName("댓글 작성자와 다르면 AccessDeniedException 발생")
        fun testUpdateCommentAccessDenied() {
            val request = CommentRequest("Updated Content")
            given(commentRepository.findByCommentId(1)).willReturn(
                Optional.of(
                    testComment
                )
            )

            val thrown = Assertions.catchThrowable {
                commentService.updateComment(
                    request,
                    1,
                    "otherUser"
                )
            }
            Assertions.assertThat(thrown).isInstanceOf(AccessDeniedException::class.java)
        }

        @Test
        @DisplayName("generateUpdatedCommentResponse() 반환값 테스트")
        fun testGenerateUpdatedCommentResponse() {
            val request = CommentRequest("Updated Content")
            given(commentRepository.findByCommentId(1)).willReturn(
                Optional.of(
                    testComment
                )
            )

            val response = commentService.generateUpdatedCommentResponse(request, 1, "testUser")

            Assertions.assertThat(response.content).isEqualTo("Updated Content")
        }
    }

    @Nested
    @DisplayName("deleteComment() 테스트")
    internal inner class DeleteCommentTests {
        @Test
        @DisplayName("댓글 작성자와 동일하면 댓글 삭제")
        fun testDeleteComment() {
            given(commentRepository.findByCommentId(1)).willReturn(
                Optional.of(
                    testComment
                )
            )

            commentService.deleteComment(1, "testUser")

            Mockito.verify(commentRepository).delete(testComment)
        }

        @Test
        @DisplayName("댓글 작성자와 다르면 AccessDeniedException 발생")
        fun testDeleteCommentAccessDenied() {
            given(commentRepository.findByCommentId(1)).willReturn(
                Optional.of(
                    testComment
                )
            )

            val thrown = Assertions.catchThrowable {
                commentService.deleteComment(
                    1,
                    "otherUser"
                )
            }
            Assertions.assertThat(thrown).isInstanceOf(AccessDeniedException::class.java)
        }
    }
}