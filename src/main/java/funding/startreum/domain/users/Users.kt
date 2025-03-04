
import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime

@Entity(name = "Users")
@Table(name = "users")
@DynamicUpdate
@DynamicInsert
open class Users() { // ✅ 기본 생성자 추가 (public or protected)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var userId: Int? = null

    @Column(nullable = false)
    lateinit var name: String

    @Column(unique = true, nullable = false)
    lateinit var email: String

    lateinit var password: String

    @Enumerated(EnumType.STRING)
    lateinit var role: Role

    var createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()

    enum class Role {
        BENEFICIARY, SPONSOR, ADMIN
    }

    // ✅ 주 생성자 추가
    constructor(
        userId: Int?,
        name: String,
        email: String,
        password: String,
        role: Role,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime
    ) : this() {
        this.userId = userId
        this.name = name
        this.email = email
        this.password = password
        this.role = role
        this.createdAt = createdAt
        this.updatedAt = updatedAt
    }
}