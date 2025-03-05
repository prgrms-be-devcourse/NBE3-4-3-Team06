package funding.startreum.domain.users.entity

import funding.startreum.domain.project.entity.Project
import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@DynamicUpdate
@DynamicInsert
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var userId: Int = 0 // ❌ Int? → ✅ Int (nullable 제거)

    @Column(nullable = false)
    var name: String = ""

    @Column(unique = true, nullable = false)
    var email: String = ""

    var password: String = ""

    @Enumerated(EnumType.STRING)
    var role: Role = Role.SPONSOR

    var createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @OneToMany(mappedBy = "creator", cascade = [CascadeType.ALL], orphanRemoval = true)
    var projects: MutableList<Project> = mutableListOf()

    constructor() // 기본 생성자 추가

    constructor(

        name: String,
        email: String,
        password: String,
        role: Role,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime
    ) {

        this.name = name
        this.email = email
        this.password = password
        this.role = role
        this.createdAt = createdAt
        this.updatedAt = updatedAt
    }

    enum class Role {
        BENEFICIARY, SPONSOR, ADMIN
    }
}
