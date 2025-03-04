package funding.startreum.domain.project.entity;

import funding.startreum.domain.comment.entity.Comment;
import funding.startreum.domain.funding.entity.Funding;
import funding.startreum.domain.reward.entity.Reward;

import funding.startreum.domain.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@ToString
@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId; // 프로젝트 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator; // 수혜자 ID (User와 다대일 관계)

    private String title; // 프로젝트 제목

    private String simpleDescription;   // 제목 밑 간단한 설명

    private String bannerUrl; // 배너 이미지 URL

    @Lob
    private String description; // 프로젝트 설명

    private BigDecimal fundingGoal; // 펀딩 목표 금액

    private BigDecimal currentFunding; // 현재 펀딩 금액

    private LocalDateTime startDate; // 펀딩 시작일

    private LocalDateTime endDate; // 펀딩 종료일

    @Enumerated(EnumType.STRING)
    private Status status; // 상태 (ONGOING, SUCCESS, FAILED)

    @Enumerated(EnumType.STRING)
    private ApprovalStatus isApproved; // 관리자 승인 여부 (승인 대기, 승인, 거절)

    private Boolean isDeleted; // 삭제 여부

    private LocalDateTime createdAt; // 생성 일자

    private LocalDateTime updatedAt; // 수정 일자

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Funding> fundings; // 받은 펀딩 목록

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments; // 댓글 목록

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reward> rewards; // 리워드 목록

    public enum Status {
        ONGOING, // 진행중
        SUCCESS, // 성공
        FAILED   // 실패
    }

    public enum ApprovalStatus {
        AWAITING_APPROVAL, // 승인 대기
        APPROVE,            // 승인
        REJECTED            // 거절
    }
}