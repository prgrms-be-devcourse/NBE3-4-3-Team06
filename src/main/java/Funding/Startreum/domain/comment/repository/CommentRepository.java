package funding.startreum.domain.comment.repository;

import funding.startreum.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    Optional<Comment> findByCommentId(Integer commentId);

    List<Comment> findByProject_ProjectId(int projectId);
}
