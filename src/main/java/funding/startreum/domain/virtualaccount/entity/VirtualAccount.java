package funding.startreum.domain.virtualaccount.entity;

import funding.startreum.domain.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString(exclude = "user") // 순환 참조 방지
@Entity
@Table(name = "virtual_accounts")
public class VirtualAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accountId; // 가상 계좌 ID

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 사용자 ID

    @Column(nullable = false, precision = 18, scale = 0) // 정수만 저장
    private BigDecimal balance; // 현재 잔액

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 계좌 생성 일자

    private LocalDateTime updatedAt; // 계좌 업데이트 일자

    private Boolean fundingBlock; // 펀딩 관련 송금 차단 여부
}
