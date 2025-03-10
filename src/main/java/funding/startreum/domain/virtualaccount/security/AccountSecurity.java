package funding.startreum.domain.virtualaccount.security;

import funding.startreum.domain.users.service.UserService;
import funding.startreum.domain.virtualaccount.entity.VirtualAccount;
import funding.startreum.domain.virtualaccount.exception.AccountNotFoundException;
import funding.startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountSecurity {

    private final VirtualAccountRepository repository;
    private final UserService userService;

    /**
     * 계좌 소유자의 username과 현재 로그인한 사용자의 username을 비교하여 권한을 확인합니다.
     * <p>
     * ⚠️ 현재 "이름(name)"을 기준으로 권한을 검증하지만,
     * 동명이인 등의 문제가 발생할 수 있으므로 추후 수정이 필요합니다.
     * <p>
     * ⚠️ 계좌가 존재하지 않을 때는 Service Layer에서 처리하고 있습니다.
     * 추후 논의가 필요합니다.
     *
     * @param userDetails 현재 로그인한 사용자 정보
     * @param accountId   검증할 계좌 ID
     * @return 계좌 소유자와 현재 로그인한 사용자가 일치하면 true
     * @throws AccessDeniedException 권한이 없을 경우 발생*
     */
    public boolean isAccountOwner(UserDetails userDetails, int accountId) {
        VirtualAccount account = repository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // 계좌에 저장된 funding.startreum.domain.users.entity.User 엔티티에서 userId 추출
        Integer accountUserId = userService.getUserByName(account.getUser().getName()).getUserId();
        Integer loginUserId = userService.getUserByName(userDetails.getUsername()).getUserId();

        boolean isOwner = accountUserId.equals(loginUserId);

        if (!isOwner) {
            throw new AccessDeniedException("🔒 해당 계좌에 대한 접근 권한이 없습니다.");
        }

        return true;
    }
}