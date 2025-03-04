package funding.startreum.domain.sponsor.exception;

public class InvalidFundingAmountException extends RuntimeException {
    public InvalidFundingAmountException() {
        super("0보다 큰 숫자를 입력하세요.");
    }
}
