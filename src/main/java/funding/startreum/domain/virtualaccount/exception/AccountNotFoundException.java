package funding.startreum.domain.virtualaccount.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(int accountId) {
        super("해당 계좌를 찾을 수 없습니다 : " + accountId);
    }

    public AccountNotFoundException(String username) {
        super("해당 유저의 계좌를 찾을 수 없습니다 : " + username);
    }

}
