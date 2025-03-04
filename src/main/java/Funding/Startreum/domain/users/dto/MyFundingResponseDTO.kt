package funding.startreum.domain.users.dto

import funding.startreum.domain.project.entity.Project
import java.math.BigDecimal
import java.time.LocalDateTime

data class MyFundingResponseDTO(
    val projectTitle: String,
    val projectStatus: Project.Status,
    val fundingAmount: BigDecimal,
    val fundedAt: LocalDateTime,
    val transactionStatus: String
)
