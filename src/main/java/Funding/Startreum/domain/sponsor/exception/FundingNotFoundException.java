package funding.startreum.domain.sponsor.exception;

public class FundingNotFoundException extends RuntimeException {
    public FundingNotFoundException(Integer fundingId) {
        super("후원 정보(" + fundingId + ")를 찾을 수 없습니다.");
    }
}
