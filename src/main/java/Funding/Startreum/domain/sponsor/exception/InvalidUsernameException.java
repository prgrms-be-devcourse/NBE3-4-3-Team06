package funding.startreum.domain.sponsor.exception;

public class InvalidUsernameException extends RuntimeException {
    public InvalidUsernameException() {
        super("후원 목록 조회에 실패했습니다. 필수 필드를 확인해주세요.");
    }
}
