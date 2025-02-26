package funding.startreum.domain.reward.repository;

import funding.startreum.domain.reward.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Integer> {
    List<Reward> findByProject_ProjectId(Integer projectId);

// 조건에 맞는 가장 큰 첫번째 리워드 반환, 일시적 주석 처리
//    Optional<Reward> findTopByProject_ProjectIdAndAmountLessThanEqualOrderByAmountDesc(
//            int projectId,
//            BigDecimal funding.startreum
//    );
}