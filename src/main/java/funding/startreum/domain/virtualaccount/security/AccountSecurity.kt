package funding.startreum.domain.virtualaccount.security

import funding.startreum.domain.users.service.UserService
import funding.startreum.domain.virtualaccount.exception.AccountNotFoundException
import funding.startreum.domain.virtualaccount.repository.VirtualAccountRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class AccountSecurity(
    private val repository: VirtualAccountRepository,
    private val userService: UserService
) {

    /**
     * 계좌 소유자의 username과 현재 로그인한 사용자의 username을 비교하여 권한을 확인합니다.
     *
     * @param userDetails 현재 로그인한 사용자 정보
     * @param accountId   검증할 계좌 ID
     * @return 계좌 소유자와 현재 로그인한 사용자가 일치하면 true
     * @throws AccessDeniedException 권한이 없을 경우 발생*
     */
    fun isAccountOwner(userDetails: UserDetails, accountId: Int): Boolean {
        val account = repository.findById(accountId)
            .orElseThrow {
                AccountNotFoundException(
                    accountId
                )
            }

        // 계좌에 저장된 funding.startreum.domain.users.entity.User 엔티티에서 userId 추출
        val accountUserId = userService.getUserByName(account.user.name).userId
        val loginUserId = userService.getUserByName(userDetails.username).userId

        val isOwner = accountUserId == loginUserId

        if (!isOwner) {
            throw AccessDeniedException("🔒 해당 계좌에 대한 접근 권한이 없습니다.")
        }

        return true
    }
}