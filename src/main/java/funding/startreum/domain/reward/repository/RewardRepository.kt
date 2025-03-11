package funding.startreum.domain.reward.repository

import funding.startreum.domain.reward.entity.Reward
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RewardRepository : JpaRepository<Reward, Int> {
    fun findByProject_ProjectId(projectId: Int): List<Reward>
}