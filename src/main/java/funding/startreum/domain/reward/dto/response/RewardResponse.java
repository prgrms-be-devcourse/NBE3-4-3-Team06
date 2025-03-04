package funding.startreum.domain.reward.dto.response;

import funding.startreum.domain.reward.entity.Reward;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RewardResponse(
        Integer rewardId,           // 리워드 ID
        Integer projectId,          // 프로젝트 ID
        String description,         // 리워드 설명
        BigDecimal amount,          // 리워드 최소 기준 금액
        LocalDateTime createdAt,    // 생성일자
        LocalDateTime updatedAt     // 수정일자
) {
    public static RewardResponse FromReward(Reward reward) {
        return new RewardResponse(
                reward.getRewardId(),
                reward.getProject().getProjectId(),
                reward.getDescription(),
                reward.getAmount(),
                reward.getCreatedAt(),
                reward.getUpdatedAt()
        );
    }

}
