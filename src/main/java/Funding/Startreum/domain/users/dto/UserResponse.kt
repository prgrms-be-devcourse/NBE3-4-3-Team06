package funding.startreum.domain.users.dto

import funding.startreum.domain.users.entity.User
import java.time.LocalDateTime

// 응답용 DTO
@JvmRecord
data class UserResponse(
    @JvmField val name: String,
    @JvmField val email: String,
    @JvmField val role: User.Role,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 