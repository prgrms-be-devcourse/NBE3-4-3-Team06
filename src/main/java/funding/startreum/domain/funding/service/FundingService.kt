package funding.startreum.domain.funding.service


import funding.startreum.domain.funding.entity.Funding
import funding.startreum.domain.funding.exception.FundingNotFoundException
import funding.startreum.domain.funding.repository.FundingRepository
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.users.entity.User
import funding.startreum.domain.users.service.UserService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class FundingService(
    private val fundingRepository: FundingRepository,
    private val userService: UserService,
) {

    /**
     * 펀딩 내역을 저장 후 반환합니다.
     *
     * @param project       펀딩할 프로젝트
     * @param username      펀딩한 유저
     * @param paymentAmount 펀딩 금액
     * @return 정보가 담긴 Funding 객체
     */
    fun createFunding(currentProject: Project, username: String, paymentAmount: BigDecimal): Funding {
        val sponsor: User = userService.getUserByName(username)

        val funding = Funding().apply {
            this.project = currentProject
            this.amount = paymentAmount
            this.fundedAt = LocalDateTime.now()
            this.sponsor = sponsor
        }

        // 리워드 할당: 결제 금액이 리워드 기준 이하인 경우,
        // rewardRepository.findTopByProject_ProjectIdAndAmountLessThanEqualOrderByAmountDesc(project.getProjectId(), paymentAmount)
        //          .ifPresent(funding::setReward);
        fundingRepository.save(funding)
        return funding
    }

    /**
     * 펀딩 내역을 취소합니다.
     *
     * @param fundingId 취소할 펀딩 ID
     * @return 취소된 Funding 객체
     */
    fun cancelFunding(fundingId: Int): Funding {
        val funding = fundingRepository.findByFundingId(fundingId)
            .orElseThrow { FundingNotFoundException(fundingId) }

        funding.isDeleted = true
        fundingRepository.save(funding)

        return funding
    }
}
