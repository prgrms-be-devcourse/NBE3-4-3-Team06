package funding.startreum.domain.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;  // ✅ Refresh Token 값을 저장

    @Column(nullable = false)
    private String username; // ✅ 어떤 사용자의 토큰인지 저장

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;  // ✅ Refresh Token 만료 시간 저장

    // 생성자 추가
    public RefreshToken(String token, String username, Date expiryDate) {
        this.token = token;
        this.username = username;
        this.expiryDate = expiryDate;
    }
}