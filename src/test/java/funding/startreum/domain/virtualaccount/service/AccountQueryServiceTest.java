package funding.startreum.domain.virtualaccount.service;

import funding.startreum.domain.funding.service.FundingService;
import funding.startreum.domain.project.repository.ProjectRepository;
import funding.startreum.domain.project.service.ProjectService;
import funding.startreum.domain.transaction.repository.TransactionRepository;
import funding.startreum.domain.transaction.service.TransactionService;
import funding.startreum.domain.users.entity.User;
import funding.startreum.domain.users.repository.UserRepository;
import funding.startreum.domain.virtualaccount.dto.VirtualAccountDtos;
import funding.startreum.domain.virtualaccount.dto.response.AccountResponse;
import funding.startreum.domain.virtualaccount.entity.VirtualAccount;
import funding.startreum.domain.virtualaccount.exception.AccountNotFoundException;
import funding.startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountQueryServiceTest {

    @Mock
    private VirtualAccountRepository virtualAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private FundingService fundingService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private AccountQueryService accountQueryService;

    @Nested
    @DisplayName("findByName() 관련 테스트")
    class FindByNameTests {

        @Test
        @DisplayName("유저를 찾을 수 없는 경우")
        void whenUserNotFound_thenReturnDtosWithFalse() {
            // Given
            String name = "nonexistent";
            when(userRepository.findByName(name)).thenReturn(Optional.empty());

            // When
            VirtualAccountDtos dtos = accountQueryService.findByName(name);

            // Then
            assertFalse(dtos.isAccountExists(), "User가 없으면 isAccountExists는 false여야 합니다.");
        }

        @Test
        @DisplayName("유저는 존재하지만 계좌를 찾을 수 없는 경우")
        void whenUserFoundButAccountNotFound_thenReturnDtosWithFalse() {
            // Given
            String name = "user";
            User user = createUser(1);
            when(userRepository.findByName(name)).thenReturn(Optional.of(user));
            when(virtualAccountRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.empty());

            // When
            VirtualAccountDtos dtos = accountQueryService.findByName(name);

            // Then
            assertFalse(dtos.isAccountExists(), "계좌가 없으면 isAccountExists는 false여야 합니다.");
        }

        @Test
        @DisplayName("계좌가 올바르게 존재할 경우")
        void whenUserAndAccountFound_thenReturnDtosFromEntity() {
            // Given
            String name = "user";
            User user = createUser(1);
            when(userRepository.findByName(name)).thenReturn(Optional.of(user));
            VirtualAccount account = createVirtualAccount(user, BigDecimal.valueOf(100));
            account.setCreatedAt(LocalDateTime.now());
            account.setUpdatedAt(LocalDateTime.now());
            when(virtualAccountRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(account));

            // When
            VirtualAccountDtos dtos = accountQueryService.findByName(name);

            // Then
            assertTrue(dtos.isAccountExists(), "계좌가 존재하면 isAccountExists는 true여야 합니다.");
            assertEquals(BigDecimal.valueOf(100), dtos.getBalance(), "계좌 잔액이 매핑되어야 합니다.");
        }
    }

    @Nested
    @DisplayName("createAccount() 관련 테스트")
    class CreateAccountTests {

        @Test
        @DisplayName("유저를 찾을 수 없는 경우")
        void whenUserNotFound_thenThrowException() {
            // Given
            String name = "nonexistent";
            when(userRepository.findByName(name)).thenReturn(Optional.empty());

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    accountQueryService.createAccount(name)
            );
            assertTrue(exception.getMessage().contains("사용자를 찾을 수 없습니다"), "적절한 에러 메시지가 포함되어야 합니다.");
            verify(userRepository, times(1)).findByName(name);
        }

        @Test
        @DisplayName("계좌가 이미 존재할 경우")
        void whenAccountAlreadyExists_thenThrowException() {
            // Given
            String name = "user";
            User user = createUser(1);
            when(userRepository.findByName(name)).thenReturn(Optional.of(user));
            VirtualAccount existingAccount = createVirtualAccount(user, BigDecimal.TEN);
            when(virtualAccountRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(existingAccount));

            // When & Then
            Exception exception = assertThrows(IllegalStateException.class, () ->
                    accountQueryService.createAccount(name)
            );
            assertEquals("이미 계좌가 존재합니다.", exception.getMessage());
        }

        @Test
        @DisplayName("계좌를 성공적으로 생성했을 경우")
        void whenValid_thenCreateAccount() {
            // Given
            String name = "user";
            User user = createUser(1);
            when(userRepository.findByName(name)).thenReturn(Optional.of(user));
            when(virtualAccountRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.empty());
            when(virtualAccountRepository.save(any(VirtualAccount.class)))
                    .thenAnswer(invocation -> {
                        VirtualAccount account = invocation.getArgument(0);
                        account.setUser(user);
                        return account;
                    });

            // When
            VirtualAccountDtos dtos = accountQueryService.createAccount(name);

            // Then
            assertTrue(dtos.isAccountExists(), "계좌 생성 시 success flag는 true여야 합니다.");
            assertEquals(BigDecimal.ZERO, dtos.getBalance(), "새 계좌의 초기 잔액은 0이어야 합니다.");
            verify(virtualAccountRepository, times(1)).save(any(VirtualAccount.class));
        }
    }

    @Nested
    @DisplayName("getAccountInfo() 관련 테스트")
    class GetAccountInfoTests {

        @Test
        @DisplayName("계좌 ID 기반 계좌 정보 조회")
        void testGetAccountInfoByAccountId() {
            // Given
            int accountId = 1;
            VirtualAccount account = createVirtualAccount(accountId, BigDecimal.valueOf(500));
            when(virtualAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

            // When
            AccountResponse response = accountQueryService.getAccountInfo(accountId);

            // Then
            assertNotNull(response, "계좌 정보 응답은 null이 아니어야 합니다.");
            assertEquals(accountId, response.getAccountId(), "응답 계좌 ID가 일치해야 합니다.");
        }

        @Test
        @DisplayName("username 기반 계좌 정보 조회")
        void testGetAccountInfoByUsername() {
            // Given
            int accountId = 1;
            String username = "userInfo";
            User user = createUser(2);
            VirtualAccount account = createVirtualAccount(user, BigDecimal.valueOf(750));
            when(virtualAccountRepository.findByUser_Name(username)).thenReturn(Optional.of(account));

            // When
            AccountResponse response = accountQueryService.getAccountInfo(username);

            // Then
            assertNotNull(response, "계좌 정보 응답은 null이 아니어야 합니다.");
            assertEquals(1, response.getAccountId(), "응답 계좌 ID가 일치해야 합니다.");
        }
    }

    @Nested
    @DisplayName("getAccount() 예외 처리 테스트")
    class GetAccountExceptionTests {

        @Test
        @DisplayName("계좌 ID로 계좌를 찾을 수 없는 경우")
        void whenAccountNotFoundById_thenThrowAccountNotFoundException() {
            // Given
            int accountId = 999;
            when(virtualAccountRepository.findById(accountId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(AccountNotFoundException.class, () -> accountQueryService.getAccount(accountId));
        }

        @Test
        @DisplayName("username으로 계좌를 찾을 수 없는 경우")
        void whenAccountNotFoundByUsername_thenThrowAccountNotFoundException() {
            // Given
            String username = "nonexistentUser";
            when(virtualAccountRepository.findByUser_Name(username)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(AccountNotFoundException.class, () -> accountQueryService.getAccount(username));
        }
    }

    // 헬퍼 메서드들
    private User createUser(int userId) {
        User user = new User();
        user.setUserId(userId);
        return user;
    }

    private VirtualAccount createVirtualAccount(User user, BigDecimal balance) {
        VirtualAccount account = new VirtualAccount();
        account.setAccountId(1);
        account.setUser(user);
        account.setBalance(balance);
        return account;
    }

    private VirtualAccount createVirtualAccount(int accountId, BigDecimal balance) {
        VirtualAccount account = new VirtualAccount();
        account.setAccountId(accountId);
        account.setBalance(balance);
        return account;
    }
}