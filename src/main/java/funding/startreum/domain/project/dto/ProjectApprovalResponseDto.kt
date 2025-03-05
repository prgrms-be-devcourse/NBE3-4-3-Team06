package funding.startreum.domain.project.dto

import java.time.LocalDateTime

data class ProjectApprovalResponseDto(
    val statusCode: Int,
    val status: String,
    val message: String,
    val data: Data
) {
    data class Data(
        val projectId: Int,
        val requestedAt: LocalDateTime
    )
}
