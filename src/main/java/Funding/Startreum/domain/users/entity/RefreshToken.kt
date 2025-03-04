package funding.startreum.domain.users.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,  // ✅ 자동 증가 ID

    @Column(nullable = false, unique = true)
    var token: String,  // ✅ Refresh Token 값 저장

    @Column(nullable = false)
    var username: String, // ✅ 어떤 사용자의 토큰인지 저장

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var expiryDate: Date  // ✅ Refresh Token 만료 시간 저장
) {
    constructor() : this(null, "", "", Date()) // ✅ 기본 생성자 추가 (JPA 요구사항)

    override fun toString(): String {
        return "RefreshToken(id=$id, token='$token', username='$username', expiryDate=$expiryDate)"
    }
}
