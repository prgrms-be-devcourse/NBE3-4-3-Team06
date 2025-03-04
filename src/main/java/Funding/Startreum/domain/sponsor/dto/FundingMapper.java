package funding.startreum.domain.sponsor.dto;

import funding.startreum.domain.funding.entity.Funding;
import funding.startreum.domain.sponsor.dto.response.FudingAttendResponse;
import funding.startreum.domain.sponsor.dto.response.SponListResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FundingMapper {
    public FudingAttendResponse toResponse(Funding funding) {
        var fundingAttend = new FudingAttendResponse.FudingAttend(
                funding.getFundingId(),
                funding.getProject().getProjectId(),
                funding.getProject().getTitle(),
                funding.getAmount().doubleValue(),
                funding.getReward().getRewardId(),
                funding.getFundedAt()
        );

        return FudingAttendResponse.success(new FudingAttendResponse.Data(fundingAttend));
    }

    public SponListResponse toFundingListResponse(Page<Funding> fundingPage) {
        List<SponListResponse.Funding> fundings = fundingPage.getContent().stream()
                .map(this::toFundingDTO)
                .toList();

        var pagination = new SponListResponse.Pagination(
                (int) fundingPage.getTotalElements(),
                fundingPage.getNumber() + 1,
                fundingPage.getSize()
        );

        return SponListResponse.success(fundings, pagination);
    }

    private SponListResponse.Funding toFundingDTO(Funding funding) {
        return new SponListResponse.Funding(
                funding.getFundingId(),
                funding.getProject().getProjectId(),
                funding.getProject().getTitle(),
                funding.getReward().getRewardId(),
                funding.getAmount().doubleValue(),
                funding.getProject().getCreatedAt()
        );
    }
}
