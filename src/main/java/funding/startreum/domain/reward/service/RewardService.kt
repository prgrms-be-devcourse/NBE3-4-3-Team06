package funding.startreum.domain.reward.service

import funding.startreum.domain.project.service.ProjectService
import funding.startreum.domain.reward.dto.request.RewardRequest
import funding.startreum.domain.reward.dto.request.RewardUpdateRequest
import funding.startreum.domain.reward.dto.response.RewardResponse
import funding.startreum.domain.reward.entity.Reward
import funding.startreum.domain.reward.repository.RewardRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service
@Transactional
class RewardService(
    private val repository: RewardRepository,
    private val projectService: ProjectService
) {

    /**
     * 리워드를 생성합니다.
     *
     * @param request 리워드 생성에 필요한 정보를 담은 RewardRequest 객체
     * @return 생성된 Reward 엔티티
     * @throws EntityNotFoundException 해당 ID에 해당하는 프로젝트를 찾을 수 없을 경우 발생
     */
    fun createReward(request: RewardRequest): Reward {
        // 1. 프로젝트 조회
        val project = projectService.getProject(request.projectId)

        // 2. 리워드 생성 및 저장
        val now = LocalDateTime.now()
        val reward = Reward(
            project = project,
            description = request.description, // ✅ Null 방지
            amount = request.amount, // ✅ Null 방지
            createdAt = now,
            updatedAt = now
        )

        return repository.save(reward)
    }

    /**
     * 리워드를 생성이후 DTO 반환
     *
     * @param request 리워드 생성에 필요한 정보를 담은 RewardRequest 객체
     * @return DTO 객체
     */
    fun generateNewRewardResponse(request: RewardRequest): RewardResponse {
        val reward = createReward(request)
        return RewardResponse.fromReward(reward) // ✅ RewardResponse 클래스에서 정의 확인 필요
    }

    /**
     * 프로젝트 ID에 해당하는 모든 리워드를 조회합니다.
     *
     * @param projectId 조회할 리워드가 속한 프로젝트의 ID
     * @return 해당 프로젝트와 연관된 Reward 리스트
     */
    @Transactional(readOnly = true)
    fun getRewardsByProjectId(projectId: Int): List<Reward> {
        return repository.findByProject_ProjectId(projectId)
    }

    /**
     * 리워드 ID를 조회합니다.
     *
     * @param rewardId 조회할 리워드의 ID
     * @return 해당 ID에 해당하는 Reward 엔티티
     * @throws EntityNotFoundException 해당 ID에 해당하는 리워드를 찾을 수 없을 경우 발생
     */
    @Transactional(readOnly = true)
    fun getRewardByRewardId(rewardId: Int): Reward {
        return repository.findById(rewardId)
            .orElseThrow { EntityNotFoundException("해당 리워드를 찾을 수 없습니다: $rewardId") }
    }

    /**
     * 프로젝트의 모든 리워드 조회 후 DTO 반환
     *
     * @param projectId 조회할 리워드가 속한 프로젝트의 ID
     * @return 해당 프로젝트의 리워드 정보를 담은 RewardResponse DTO 리스트
     */
    @Transactional(readOnly = true)
    fun generateRewardsResponse(projectId: Int): List<RewardResponse> {
        return getRewardsByProjectId(projectId)
            .map { RewardResponse.fromReward(it) } // ✅ RewardResponse 클래스에서 fromReward 정의 확인 필요
    }

    /**
     * 리워드를 업데이트 합니다.
     *
     *
     * @param rewardId 업데이트할 리워드의 ID
     * @param request 업데이트할 정보를 담은 RewardUpdateRequest 객체
     * @return Reward
     * @throws EntityNotFoundException 해당 ID에 해당하는 리워드를 찾을 수 없을 경우 발생
     */
    fun updateReward(rewardId: Int, request: RewardUpdateRequest): Reward {
        val reward = getRewardByRewardId(rewardId)

        reward.apply {
            description = request.description // ✅ 기존 값 유지
            amount = request.amount // ✅ 기존 값 유지
            updatedAt = LocalDateTime.now()
        }

        return repository.save(reward)
    }

    /**
     * 업데이트 후, DTO 객체를 반환합니다.
     *
     * @param rewardId 리워드 ID
     * @param request  RewardUpdateRequest 객체
     * @return  RewardResponse 객체
     */
    fun generateUpdatedRewardResponse(rewardId: Int, request: RewardUpdateRequest): RewardResponse {
        val updatedReward = updateReward(rewardId, request)
        return RewardResponse.fromReward(updatedReward) // ✅ RewardResponse 클래스에서 fromReward 정의 확인 필요
    }

    /**
     * 리워드를 삭제합니다.
     *
     * @param rewardId 삭제할 리워드의 ID
     * @throws EntityNotFoundException 해당 ID에 해당하는 리워드를 찾을 수 없을 경우 발생
     */
    fun deleteReward(rewardId: Int) {
        val reward = getRewardByRewardId(rewardId)
        repository.delete(reward)
    }
}
