package funding.startreum.domain.sponsor.exception;

public class FundingAccessDeniedException extends RuntimeException {
    public FundingAccessDeniedException() {
        super("해당 후원에 대한 접근 권한이 없습니다.");
    }
}
