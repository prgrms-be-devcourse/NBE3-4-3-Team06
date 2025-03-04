package funding.startreum.domain.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailUpdateRequest(
        @Email(message = "유효하지 않은 이메일 형식입니다.")
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        String newEmail
) {}