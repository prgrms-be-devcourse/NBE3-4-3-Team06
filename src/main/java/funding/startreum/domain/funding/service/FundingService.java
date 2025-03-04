package funding.startreum.domain.funding.service;

import funding.startreum.domain.funding.entity.Funding;
import funding.startreum.domain.funding.repository.FundingRepository;
import funding.startreum.domain.project.entity.Project;
import funding.startreum.domain.reward.repository.RewardRepository;
import funding.startreum.domain.users.User;
import funding.startreum.domain.users.UserService;
import funding.startreum.domain.funding.exception.FundingNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FundingService {
    private final RewardRepository rewardRepository;
    private final FundingRepository fundingRepository;

    private final UserService userService;

    /**
     * 펀딩 내역을 저장 후 반환합니다.
     *
     * @param project       펀딩할 프로젝트
     * @param username      펀딩한 유저
     * @param paymentAmount 펀딩 금액
     * @return 정보가 담긴 Funding 객체
     */
    public Funding createFunding(Project project, String username, BigDecimal paymentAmount) {
        User sponsor = userService.getUserByName(username);

        Funding funding = new Funding();
        funding.setProject(project);
        funding.setAmount(paymentAmount);
        funding.setFundedAt(LocalDateTime.now());
        funding.setSponsor(sponsor);

        // 리워드 할당: 결제 금액이 리워드 기준 이하인 경우,
       // rewardRepository.findTopByProject_ProjectIdAndAmountLessThanEqualOrderByAmountDesc(project.getProjectId(), paymentAmount)
    //          .ifPresent(funding::setReward);

        fundingRepository.save(funding);
        return funding;
    }

    /**
     * 펀딩 내역을 취소합니다.
     *
     * @param fundingId 취소할 펀딩 ID
     * @return 취소된 Funding 객체
     */
    public Funding cancelFunding(Integer fundingId) {
        Funding funding = fundingRepository.findByFundingId(fundingId)
                .orElseThrow(() -> new FundingNotFoundException(fundingId));

        funding.setDeleted(true);
        fundingRepository.save(funding);

        return funding;
    }

}
