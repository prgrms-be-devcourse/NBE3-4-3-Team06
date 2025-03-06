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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Project testProject;
    private Comment testComment;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("testUser");

        testProject = new Project();
        testProject.setProjectId(1);

        now = LocalDateTime.now();

        testComment = new Comment();
        testComment.setCommentId(1);
        testComment.setContent("Original Content");
        testComment.setUser(testUser);
        testComment.setProject(testProject);
        testComment.setCreatedAt(now);
        testComment.setUpdatedAt(now);
    }

    @Nested
    @DisplayName("getComment() 테스트")
    class GetCommentTests {
        @Test
        @DisplayName("존재하는 댓글 반환")
        void testGetComment() {
            given(commentRepository.findByCommentId(1)).willReturn(Optional.of(testComment));

            Comment comment = commentService.getComment(1);

            assertThat(comment).isEqualTo(testComment);
        }

        @Test
        @DisplayName("댓글 미존재 시 EntityNotFoundException 발생")
        void testGetCommentNotFound() {
            given(commentRepository.findByCommentId(2)).willReturn(Optional.empty());

            Throwable thrown = catchThrowable(() -> commentService.getComment(2));
            assertThat(thrown).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getComments() 테스트")
    class GetCommentsTests {
        @Test
        @DisplayName("프로젝트의 댓글 리스트 반환")
        void testGetComments() {
            List<Comment> commentsList = Collections.singletonList(testComment);
            given(commentRepository.findByProject_ProjectId(1)).willReturn(commentsList);

            List<Comment> comments = commentService.getComments(1);

            assertThat(comments).isEqualTo(commentsList);
        }
    }

    @Nested
    @DisplayName("generateCommentsResponse() 테스트")
    class GenerateCommentsResponseTests {
        @Test
        @DisplayName("댓글 응답 DTO 리스트 반환")
        void testGenerateCommentsResponse() {
            List<Comment> commentsList = Collections.singletonList(testComment);
            given(commentRepository.findByProject_ProjectId(1)).willReturn(commentsList);

            List<CommentResponse> responses = commentService.generateCommentsResponse(1);

            assertThat(responses).isNotEmpty();
            assertThat(responses.get(0).getCommentId()).isEqualTo(testComment.getCommentId());
            assertThat(responses.get(0).getContent()).isEqualTo(testComment.getContent());
        }
    }

    @Nested
    @DisplayName("createComment() 및 generateNewCommentResponse() 테스트")
    class CreateAndGenerateNewCommentResponseTests {
        @Test
        @DisplayName("새 댓글 생성 및 응답 DTO 반환")
        void testGenerateNewCommentResponse() {
            CommentRequest request = new CommentRequest("New Comment");

            given(userService.getUserByName("testUser")).willReturn(testUser);
            given(projectService.getProject(1)).willReturn(testProject);
            given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
                Comment comment = invocation.getArgument(0);
                comment.setCommentId(100);
                return comment;
            });


            CommentResponse response = commentService.generateNewCommentResponse(1, request, "testUser");

            assertThat(response.getContent()).isEqualTo("New Comment");
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("updateComment() 및 generateUpdatedCommentResponse() 테스트")
    class UpdateAndGenerateUpdatedCommentResponseTests {
        @Test
        @DisplayName("댓글 작성자와 동일하면 댓글 수정")
        void testUpdateComment() {
            CommentRequest request = new CommentRequest("Updated Content");
            given(commentRepository.findByCommentId(1)).willReturn(Optional.of(testComment));

            Comment updatedComment = commentService.updateComment(request, 1, "testUser");

            assertThat(updatedComment.getContent()).isEqualTo("Updated Content");
        }

        @Test
        @DisplayName("댓글 작성자와 다르면 AccessDeniedException 발생")
        void testUpdateCommentAccessDenied() {
            CommentRequest request = new CommentRequest("Updated Content");
            given(commentRepository.findByCommentId(1)).willReturn(Optional.of(testComment));

            Throwable thrown = catchThrowable(() -> commentService.updateComment(request, 1, "otherUser"));
            assertThat(thrown).isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("generateUpdatedCommentResponse() 반환값 테스트")
        void testGenerateUpdatedCommentResponse() {
            CommentRequest request = new CommentRequest("Updated Content");
            given(commentRepository.findByCommentId(1)).willReturn(Optional.of(testComment));

            CommentResponse response = commentService.generateUpdatedCommentResponse(request, 1, "testUser");

            assertThat(response.getContent()).isEqualTo("Updated Content");
        }
    }

    @Nested
    @DisplayName("deleteComment() 테스트")
    class DeleteCommentTests {
        @Test
        @DisplayName("댓글 작성자와 동일하면 댓글 삭제")
        void testDeleteComment() {
            given(commentRepository.findByCommentId(1)).willReturn(Optional.of(testComment));

            commentService.deleteComment(1, "testUser");

            verify(commentRepository).delete(testComment);
        }

        @Test
        @DisplayName("댓글 작성자와 다르면 AccessDeniedException 발생")
        void testDeleteCommentAccessDenied() {
            given(commentRepository.findByCommentId(1)).willReturn(Optional.of(testComment));

            Throwable thrown = catchThrowable(() -> commentService.deleteComment(1, "otherUser"));
            assertThat(thrown).isInstanceOf(AccessDeniedException.class);
        }
    }
}