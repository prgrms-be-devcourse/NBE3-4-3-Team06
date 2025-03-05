package funding.startreum.domain.users.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class EmailUpdateRequest(
    @field:Email(message = "유효하지 않은 이메일 형식입니다.")
    @field:NotBlank(message = "이메일은 필수 입력값입니다.")
    val newEmail: String
)
