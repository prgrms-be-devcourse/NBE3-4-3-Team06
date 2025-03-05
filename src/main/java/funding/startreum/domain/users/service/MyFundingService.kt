package funding.startreum.domain.users.service

import funding.startreum.domain.users.dto.MyFundingResponseDTO
import funding.startreum.domain.users.repository.MyFundingRepository
import org.springframework.stereotype.Service

@Service
class MyFundingService(private val myFundingRepository: MyFundingRepository) {

    fun getMyFundings(sponsorId: Int): List<MyFundingResponseDTO> {
        return myFundingRepository.findMyFundingsBySponsorId(sponsorId)
    }
}
