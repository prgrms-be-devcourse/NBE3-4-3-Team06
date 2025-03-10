package funding.startreum.domain.comment.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CommentRequest(
    @field:NotBlank(message = "내용은 필수 입력 항목입니다.")
    @field:Size(max = 500, message = "내용은 최대 500자까지 입력할 수 있습니다.")
    val content: String
) 