package funding.startreum.domain.virtualaccount.exception

class AccountNotFoundException : RuntimeException {
    constructor(accountId: Int) : super("해당 계좌를 찾을 수 없습니다 : $accountId")

    constructor(username: String) : super("해당 유저의 계좌를 찾을 수 없습니다 : $username")
}
