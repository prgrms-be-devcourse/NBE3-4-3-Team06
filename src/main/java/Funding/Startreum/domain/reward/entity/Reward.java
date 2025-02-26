package funding.startreum.domain.reward.entity;

import funding.startreum.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "reward")
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rewardId; // 리워드 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // 프로젝트 ID

    private String description; // 리워드 설명

    private BigDecimal amount; // 리워드 최소 기준 금액

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
