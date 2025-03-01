package funding.startreum.domain.users

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

// 입력용 DTO
@JvmRecord
data class SignupRequest(
        @JvmField val name: @NotBlank(message = "이름은 필수 입력값입니다.") String?,

        @JvmField val email: @Email(message = "유효하지 않은 이메일 형식입니다.") @NotBlank(message = "이메일은 필수 입력값입니다.") String?,

        @JvmField val password: @NotBlank(message = "비밀번호는 필수 입력값입니다.") @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.") String?,

        @JvmField val role: @NotNull(message = "역할(Role)은 필수 입력값입니다.") User.Role?
) 