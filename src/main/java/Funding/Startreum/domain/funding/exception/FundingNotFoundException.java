package funding.startreum.domain.funding.exception;

public class FundingNotFoundException extends RuntimeException {

    public FundingNotFoundException(int fundingId) {
        super("펀딩 내역을 찾을 수 없습니다. 펀딩 ID: : " + fundingId);
    }

}
