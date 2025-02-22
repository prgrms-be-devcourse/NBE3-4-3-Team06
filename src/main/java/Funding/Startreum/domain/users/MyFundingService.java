package Funding.Startreum.domain.users;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MyFundingService {

    private final MyFundingRepository myFundingRepository;

    public MyFundingService(MyFundingRepository myFundingRepository) {
        this.myFundingRepository = myFundingRepository;
    }

    public List<MyFundingResponseDTO> getMyFundings(Integer sponsorId) {
        return myFundingRepository.findMyFundingsBySponsorId(sponsorId);
    }
}