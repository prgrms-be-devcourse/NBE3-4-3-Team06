package Funding.Startreum.domain.comment.entity;

import Funding.Startreum.domain.project.entity.Project;
import Funding.Startreum.domain.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString(exclude = {"user","project"}) // 순환 참조 방지
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId; // 댓글 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // 프로젝트 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 작성자 ID

    @Lob
    private String content; // 댓글 내용

    private LocalDateTime createdAt; // 작성 일자

    private LocalDateTime updatedAt; // 수정 일자
}