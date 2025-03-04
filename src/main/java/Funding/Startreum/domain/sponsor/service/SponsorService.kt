package funding.startreum.domain.sponsor.service

import funding.startreum.domain.funding.exception.FundingNotFoundException
import funding.startreum.domain.funding.repository.FundingRepository
import funding.startreum.domain.sponsor.dto.FundingMapper
import funding.startreum.domain.sponsor.dto.FundingValidator
import funding.startreum.domain.sponsor.dto.response.FudingAttendResponse
import funding.startreum.domain.sponsor.dto.response.SponListResponse
import funding.startreum.domain.sponsor.exception.ProjectNotFoundException
import jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
class SponsorService {
    private val fundingRepository: FundingRepository? = null
    private val fundingValidator: FundingValidator? = null
    private val fundingMapper: FundingMapper? = null

    // 후원 목록 조회 로직
    @Transactional
    fun getFundingList(username: String?, pageable: Pageable?): SponListResponse {
        fundingValidator!!.validateUsername(username)

        val fundingPage = fundingRepository!!.findBySponsorEmail(username, pageable)

        if (fundingPage.isEmpty) {
            throw ProjectNotFoundException()
        }

        return fundingMapper!!.toFundingListResponse(fundingPage)
    }

    // 후원 참여 로직
    @Transactional
    fun getAttendFunding(email: String?, fundingId: Int): FudingAttendResponse {
        val funding = fundingRepository!!.findById(fundingId)
            .orElseThrow {
                FundingNotFoundException(
                    fundingId
                )
            }

        fundingValidator!!.validateFundingAccess(email, funding)
        fundingValidator.validateFundingAmount(funding)

        return fundingMapper!!.toResponse(funding)
    }
}
