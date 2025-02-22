package Funding.Startreum.domain.users;

import jakarta.validation.constraints.*;


// 입력용 DTO
public record SignupRequest(
        @NotBlank(message = "이름은 필수 입력값입니다.")
        String name,

        @Email(message = "유효하지 않은 이메일 형식입니다.")
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        String password,

        @NotNull(message = "역할(Role)은 필수 입력값입니다.")
        User.Role role
) {}