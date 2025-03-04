package funding.startreum.domain.reward.service

import funding.startreum.domain.project.repository.ProjectRepository
import funding.startreum.domain.reward.dto.request.RewardRequest
import funding.startreum.domain.reward.dto.request.RewardUpdateRequest
import funding.startreum.domain.reward.dto.response.RewardResponse
import funding.startreum.domain.reward.entity.Reward
import funding.startreum.domain.reward.repository.RewardRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * RewardService 클래스
 *
 * 이 클래스는 프로젝트에 관련된 리워드를 관리하는 서비스 계층입니다.
 * 리워드의 생성, 조회, 업데이트, 삭제 기능을 제공합니다.
 */
@Service
open class RewardService(
    private val repository: RewardRepository,
    private val projectRepository: ProjectRepository
) {

    /**
     * 요청 객체에서 제공된 프로젝트 ID로 프로젝트를 조회한 후, 해당 프로젝트에 연결된 리워드를 생성하고 저장합니다.
     *
     * @param request 리워드 생성에 필요한 정보를 담은 RewardRequest 객체
     * @return 생성된 Reward 엔티티
     * @throws EntityNotFoundException 해당 ID에 해당하는 프로젝트를 찾을 수 없을 경우 발생
     */
    @Transactional
    open fun createReward(request: RewardRequest): Reward {
        // 1. 프로젝트 조회
        val project = projectRepository.findById(request.projectId)
            .orElseThrow { EntityNotFoundException("프로젝트를 찾을 수 없습니다. 프로젝트 ID: ${request.projectId}") }

        // 2. 리워드 생성 및 저장
        val now = LocalDateTime.now()
        val reward = Reward(
            project = project,
            description = request.description ?: "", // ✅ Null 방지
            amount = request.amount ?: BigDecimal.ZERO, // ✅ Null 방지
            createdAt = now,
            updatedAt = now
        )

        return repository.save(reward)
    }

    /**
     * 리워드를 생성하고, 생성된 리워드를 기반으로 RewardResponse 객체로 변환하여 반환합니다.
     *
     * @param request 리워드 생성에 필요한 정보를 담은 RewardRequest 객체
     * @return 생성된 리워드의 정보를 담은 RewardResponse DTO 객체
     */
    @Transactional
    open fun generateNewRewardResponse(request: RewardRequest): RewardResponse {
        val reward = createReward(request)
        return RewardResponse.fromReward(reward) // ✅ RewardResponse 클래스에서 정의 확인 필요
    }

    /**
     * 지정된 프로젝트 ID에 해당하는 모든 리워드를 조회합니다.
     *
     * @param projectId 조회할 리워드가 속한 프로젝트의 ID
     * @return 해당 프로젝트와 연관된 Reward 리스트
     */
    @Transactional(readOnly = true)
    open fun getRewardsByProjectId(projectId: Int): List<Reward> {
        return repository.findByProject_ProjectId(projectId)
    }

    /**
     * 지정된 리워드 ID에 해당하는 리워드를 조회합니다.
     *
     * @param rewardId 조회할 리워드의 ID
     * @return 해당 ID에 해당하는 Reward 엔티티
     * @throws EntityNotFoundException 해당 ID에 해당하는 리워드를 찾을 수 없을 경우 발생
     */
    @Transactional(readOnly = true)
    open fun getRewardByRewardId(rewardId: Int): Reward {
        return repository.findById(rewardId)
            .orElseThrow { EntityNotFoundException("해당 리워드를 찾을 수 없습니다: $rewardId") }
    }

    /**
     * 지정된 프로젝트 ID에 속한 모든 리워드를 조회한 후,
     * 각 리워드 엔티티를 RewardResponse DTO로 변환하여 리스트로 반환합니다.
     *
     * @param projectId 조회할 리워드가 속한 프로젝트의 ID
     * @return 해당 프로젝트의 리워드 정보를 담은 RewardResponse DTO 리스트
     */
    @Transactional(readOnly = true)
    open fun generateRewardsResponse(projectId: Int): List<RewardResponse> {
        return getRewardsByProjectId(projectId)
            .map { RewardResponse.fromReward(it) } // ✅ RewardResponse 클래스에서 fromReward 정의 확인 필요
    }

    /**
     * 지정된 리워드 ID에 해당하는 리워드를 업데이트합니다.
     *
     * 업데이트 요청 객체에 포함된 정보를 바탕으로 리워드의 설명과 금액을 수정하고, 업데이트 시간을 갱신합니다.
     *
     * @param rewardId 업데이트할 리워드의 ID
     * @param request 업데이트할 정보를 담은 RewardUpdateRequest 객체
     * @return 업데이트된 Reward 엔티티
     * @throws EntityNotFoundException 해당 ID에 해당하는 리워드를 찾을 수 없을 경우 발생
     */
    @Transactional
    open fun updateReward(rewardId: Int, request: RewardUpdateRequest): Reward {
        // 1. 리워드 조회
        val reward = getRewardByRewardId(rewardId)

        // 2. 리워드 업데이트 및 저장
        reward.apply {
            description = request.description ?: description // ✅ 기존 값 유지
            amount = request.amount ?: amount // ✅ 기존 값 유지
            updatedAt = LocalDateTime.now()
        }

        return repository.save(reward)
    }

    /**
     * 지정된 리워드 ID에 대한 업데이트를 수행한 후,
     * 업데이트된 리워드 정보를 RewardResponse DTO로 변환하여 반환합니다.
     *
     * @param rewardId 업데이트할 리워드의 ID
     * @param request 업데이트할 정보를 담은 RewardUpdateRequest 객체
     * @return 업데이트된 리워드 정보를 담은 RewardResponse 객체
     */
    @Transactional
    open fun generateUpdatedRewardResponse(rewardId: Int, request: RewardUpdateRequest): RewardResponse {
        val updatedReward = updateReward(rewardId, request)
        return RewardResponse.fromReward(updatedReward) // ✅ RewardResponse 클래스에서 fromReward 정의 확인 필요
    }

    /**
     * 지정된 리워드 ID에 해당하는 리워드를 삭제합니다.
     *
     * @param rewardId 삭제할 리워드의 ID
     * @throws EntityNotFoundException 해당 ID에 해당하는 리워드를 찾을 수 없을 경우 발생
     */
    @Transactional
    open fun deleteReward(rewardId: Int) {
        val reward = getRewardByRewardId(rewardId)
        repository.delete(reward)
    }
}
