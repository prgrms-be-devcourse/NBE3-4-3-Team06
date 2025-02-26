package funding.startreum.domain.funding.entity;


import funding.startreum.domain.reward.entity.Reward;
import funding.startreum.domain.transaction.entity.Transaction;


import funding.startreum.domain.project.entity.Project;
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
@ToString(exclude = {"project", "sponsor"}) // 순환 참조 방지
@Entity
@Table(name = "funding")
public class Funding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fundingId; // 펀딩 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sponsor_id", nullable = false)
    private User sponsor; // 후원자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // 프로젝트 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward; // 리워드 ID

    private BigDecimal amount; // 후원 금액

    private LocalDateTime fundedAt; // 후원 일자

    @Column(nullable = false)
    private boolean isDeleted = false; // 삭제 여부, 기본 false

    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;  // 트랜잭션 리스트 추가
}