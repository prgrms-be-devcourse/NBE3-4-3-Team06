package funding.startreum.domain.reward.dto.response

import funding.startreum.domain.reward.entity.Reward
import java.math.BigDecimal
import java.time.LocalDateTime

data class RewardResponse(
    val rewardId: Int,
    val projectId: Int,
    val description: String,
    val amount: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun fromReward(reward: Reward): RewardResponse {
            return RewardResponse(
                rewardId = reward.rewardId ?: 0, // null 방지
                projectId = reward.project.projectId ?: 0, // null 방지
                description = reward.description,
                amount = reward.amount,
                createdAt = reward.createdAt,
                updatedAt = reward.updatedAt
            )
        }
    }
}
