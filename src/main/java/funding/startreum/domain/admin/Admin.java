package funding.startreum.domain.admin;

import funding.startreum.domain.project.entity.Project;
import funding.startreum.domain.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "admin")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer actionId; // 관리자 행동 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin; // 관리자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project; // 프로젝트 ID

    @Enumerated(EnumType.STRING)
    private ActionType actionType; // 행동 유형

    private LocalDateTime actionDate; // 행동 일자

    public enum ActionType {
        APPROVE, // 승인
        REJECT   // 거절
    }
}
