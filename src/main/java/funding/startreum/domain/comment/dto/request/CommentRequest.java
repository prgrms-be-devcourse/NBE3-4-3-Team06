package funding.startreum.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(max = 500, message = "내용은 최대 500자까지 입력할 수 있습니다.")
        String content
) {
}