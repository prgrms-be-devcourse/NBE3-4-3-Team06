package Funding.Startreum.domain.reward.service;


import Funding.Startreum.domain.project.entity.Project;
import Funding.Startreum.domain.project.repository.ProjectRepository;
import Funding.Startreum.domain.reward.dto.request.RewardRequest;
import Funding.Startreum.domain.reward.dto.request.RewardUpdateRequest;
import Funding.Startreum.domain.reward.dto.response.RewardResponse;
import Funding.Startreum.domain.reward.entity.Reward;
import Funding.Startreum.domain.reward.repository.RewardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static Funding.Startreum.domain.reward.dto.response.RewardResponse.FromReward;

/**
 * <h2>RewardService 클래스</h2>
 * <p>
 * 이 클래스는 프로젝트에 관련된 리워드를 관리하는 서비스 계층입니다.
 * 리워드의 생성, 조회, 업데이트, 삭제 기능을 제공합니다.
 * </p>
 *
 * <p><strong>주요 기능:</strong></p>
 * <ul>
 *   <li>리워드 생성</li>
 *   <li>리워드 조회</li>
 *   <li>리워드 업데이트</li>
 *   <li>리워드 삭제</li>
 * </ul>
 *
 * @author 한상훈
 */
@Service
@RequiredArgsConstructor
public class RewardService {
    private final RewardRepository repository;
    private final ProjectRepository projectRepository;

    /**
     * <p>
     * 요청 객체에서 제공된 프로젝트 ID로 프로젝트를 조회한 후, 해당 프로젝트에 연결된 리워드를 생성하고 저장합니다.
     * </p>
     *
     * @param request 리워드 생성에 필요한 정보를 담은 {@link RewardRequest} 객체
     * @return 생성된 {@link Reward} 엔티티
     * @throws EntityNotFoundException 해당 ID에 해당하는 프로젝트를 찾을 수 없을 경우 발생
     */
    @Transactional
    public Reward createReward(RewardRequest request) {
        // 1. 프로젝트 조회
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. 프로젝트 ID: " + request.projectId()));

        // 2. 리워드 생성 및 저장
        Reward reward = new Reward();
        reward.setDescription(request.description());
        reward.setAmount(request.amount());
        reward.setProject(project);

        // 현재 시간을 생성 및 업데이트 시간으로 설정
        LocalDateTime now = LocalDateTime.now();

        reward.setCreatedAt(now);
        reward.setUpdatedAt(now);

        // 3. 리워드 저장
        repository.save(reward);

        // 4. 생성된 리워드 반환
        return reward;
    }


    /**
     * 리워드를 생성하고, 생성된 리워드를 기반으로 {@link RewardResponse} 객체로 변환하여 반환합니다.
     *
     * @param request request 리워드 생성에 필요한 정보를 담은 {@link RewardRequest} 객체
     * @return 생성된 리워드의 정보를 담은 {@link RewardResponse} DTO 객체
     */
    @Transactional
    public RewardResponse generateNewRewardResponse(RewardRequest request) {
        Reward reward = createReward(request);
        return FromReward(reward);
    }

    /**
     * 지정된 프로젝트 ID에 해당하는 모든 리워드를 조회합니다.
     *
     * @param projectId 조회할 리워드가 속한 프로젝트의 ID
     * @return 해당 프로젝트와 연관된 {@link Reward} List
     */
    @Transactional(readOnly = true)
    public List<Reward> getRewardsByProjectId(Integer projectId) {
        return repository.findByProject_ProjectId(projectId);
    }

    /**
     * 지정된 리워드 ID에 해당하는 리워드를 조회합니다.
     *
     * @param rewardId 조회할 리워드의 ID
     * @return 해당 ID에 해당하는 {@link Reward} 엔티티
     * @throws EntityNotFoundException 해당 ID에 해당하는 리워드를 찾을 수 없을 경우 발생
     */

    @Transactional(readOnly = true)
    public Reward getRewardsByRewardId(Integer rewardId) {
        return repository.findById(rewardId)
                .orElseThrow(() -> new EntityNotFoundException("해당 리워드를 찾을 수 없습니다 : " + rewardId));
    }

    /**
     * 지정된 프로젝트 ID에 속한 모든 리워드를 조회한 후,
     *      * 각 리워드 엔티티를 {@link RewardResponse} DTO로 변환하여 리스트로 반환합니다.
     *
     * @param projectId 조회할 리워드가 속한 프로젝트의 ID
     * @return 해당 프로젝트의 리워드 정보를 담은 {@link RewardResponse} DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<RewardResponse> generateRewardsResponse(Integer projectId) {
        List<Reward> rewards = getRewardsByProjectId(projectId);
        return rewards.stream()
                .map(RewardResponse::FromReward)
                .collect(Collectors.toList());
    }

    /**
     * 지정된 리워드 ID에 해당하는 리워드를 업데이트합니다.
     * <p>
     * 업데이트 요청 객체에 포함된 정보를 바탕으로 리워드의 설명과 금액을 수정하고, 업데이트 시간을 갱신합니다.
     * </p>
     *
     * @param rewardId 업데이트할 리워드의 ID
     * @param request  업데이트할 정보를 담은 {@link RewardUpdateRequest} 객체
     * @return 업데이트된 {@link Reward} 엔티티
     * @throws EntityNotFoundException 해당 ID에 해당하는 리워드를 찾을 수 없을 경우 발생
     */
    @Transactional
    public Reward updateReward(Integer rewardId, RewardUpdateRequest request) {
        // 1. 리워드 조회
        Reward reward = getRewardsByRewardId(rewardId);

        // 2. 리워드 업데이트 및 저장
        reward.setDescription(request.description());
        reward.setAmount(request.amount());
        reward.setUpdatedAt(LocalDateTime.now());
        repository.save(reward);

        // 3. 리워드 반환
        return reward;
    }

    /**
     * 지정된 리워드 ID에 대한 업데이트를 수행한 후,
     * 업데이트된 리워드 정보를 {@link RewardResponse} DTO로 변환하여 반환합니다.
     *
     * @param rewardId 업데이트할 리워드의 ID
     * @param request  업데이트할 정보를 담은 {@link RewardUpdateRequest} 객체
     * @return 업데이트된 리워드 정보를 담은 {@link RewardResponse} 객체
     */
    @Transactional
    public RewardResponse generateUpdatedRewardResponse(Integer rewardId, RewardUpdateRequest request) {
        return FromReward(updateReward(rewardId, request));
    }

    /**
     * 지정된 리워드 ID에 해당하는 리워드를 삭제합니다.
     *
     * @param rewardId 삭제할 리워드의 ID
     * @throws EntityNotFoundException 해당 ID에 해당하는 리워드를 찾을 수 없을 경우 발생
     */
    @Transactional
    public void deleteReward(Integer rewardId) {
        Reward reward = getRewardsByRewardId(rewardId);
        repository.delete(reward);
    }

}