package Funding.Startreum.domain.funding.service;

import Funding.Startreum.domain.funding.entity.Funding;
import Funding.Startreum.domain.funding.repository.FundingRepository;
import Funding.Startreum.domain.project.entity.Project;
import Funding.Startreum.domain.reward.repository.RewardRepository;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserService;
import Funding.Startreum.domain.funding.exception.FundingNotFoundException;
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
