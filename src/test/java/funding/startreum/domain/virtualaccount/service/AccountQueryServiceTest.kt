package funding.startreum.domain.virtualaccount.service

import funding.startreum.domain.funding.service.FundingService
import funding.startreum.domain.project.repository.ProjectRepository
import funding.startreum.domain.project.service.ProjectService
import funding.startreum.domain.transaction.repository.TransactionRepository
import funding.startreum.domain.transaction.service.TransactionService
import funding.startreum.domain.users.entity.User
import funding.startreum.domain.users.repository.UserRepository
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import funding.startreum.domain.virtualaccount.exception.AccountNotFoundException
import funding.startreum.domain.virtualaccount.repository.VirtualAccountRepository
import funding.startreum.domain.virtualaccount.service.AccountQueryService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class AccountQueryServiceTest {

    @Mock
    private lateinit var virtualAccountRepository: VirtualAccountRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @Mock
    private lateinit var fundingService: FundingService

    @Mock
    private lateinit var transactionService: TransactionService

    @Mock
    private lateinit var projectService: ProjectService

    @InjectMocks
    private lateinit var accountQueryService: AccountQueryService

    @Nested
    @DisplayName("findByName() 관련 테스트")
    inner class FindByNameTests {

        @Test
        @DisplayName("유저를 찾을 수 없는 경우")
        fun whenUserNotFound_thenReturnDtosWithFalse() {
            // Given
            val name = "nonexistent"
            `when`(userRepository.findByName(name)).thenReturn(Optional.empty())

            // When
            val dtos = accountQueryService.findByName(name)

            // Then
            assertFalse(dtos.accountExists, "User가 없으면 isAccountExists는 false여야 합니다.")
        }

        @Test
        @DisplayName("유저는 존재하지만 계좌를 찾을 수 없는 경우")
        fun whenUserFoundButAccountNotFound_thenReturnDtosWithFalse() {
            // Given
            val name = "user"
            val user = createUser(1)
            `when`(userRepository.findByName(name)).thenReturn(Optional.of(user))
            `when`(virtualAccountRepository.findByUser_UserId(user.userId)).thenReturn(Optional.empty())

            // When
            val dtos = accountQueryService.findByName(name)

            // Then
            assertFalse(dtos.accountExists, "계좌가 없으면 isAccountExists는 false여야 합니다.")
        }

        @Test
        @DisplayName("계좌가 올바르게 존재할 경우")
        fun whenUserAndAccountFound_thenReturnDtosFromEntity() {
            // Given
            val name = "user"
            val user = createUser(1)
            `when`(userRepository.findByName(name)).thenReturn(Optional.of(user))
            val account = createVirtualAccount(user, BigDecimal.valueOf(100)).apply {
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }
            `when`(virtualAccountRepository.findByUser_UserId(user.userId)).thenReturn(Optional.of(account))

            // When
            val dtos = accountQueryService.findByName(name)

            // Then
            assertTrue(dtos.accountExists, "계좌가 존재하면 isAccountExists는 true여야 합니다.")
            assertEquals(BigDecimal.valueOf(100), dtos.balance, "계좌 잔액이 매핑되어야 합니다.")
        }
    }

    @Nested
    @DisplayName("createAccount() 관련 테스트")
    inner class CreateAccountTests {

        @Test
        @DisplayName("유저를 찾을 수 없는 경우")
        fun whenUserNotFound_thenThrowException() {
            // Given
            val name = "nonexistent"
            `when`(userRepository.findByName(name)).thenReturn(Optional.empty())

            // When & Then
            val exception = assertThrows(IllegalArgumentException::class.java) {
                accountQueryService.createAccount(name)
            }
            assertTrue(exception.message!!.contains("사용자를 찾을 수 없습니다"), "적절한 에러 메시지가 포함되어야 합니다.")
            verify(userRepository, times(1)).findByName(name)
        }

        @Test
        @DisplayName("계좌가 이미 존재할 경우")
        fun whenAccountAlreadyExists_thenThrowException() {
            // Given
            val name = "user"
            val user = createUser(1)
            `when`(userRepository.findByName(name)).thenReturn(Optional.of(user))
            val existingAccount = createVirtualAccount(user, BigDecimal.TEN)
            `when`(virtualAccountRepository.findByUser_UserId(user.userId)).thenReturn(Optional.of(existingAccount))

            // When & Then
            val exception = assertThrows(IllegalStateException::class.java) {
                accountQueryService.createAccount(name)
            }
            assertEquals("이미 계좌가 존재합니다.", exception.message)
        }

        @Test
        @DisplayName("계좌를 성공적으로 생성했을 경우")
        fun whenValid_thenCreateAccount() {
            // Given
            val name = "user"
            val user = createUser(1)
            `when`(userRepository.findByName(name)).thenReturn(Optional.of(user))
            `when`(virtualAccountRepository.findByUser_UserId(user.userId)).thenReturn(Optional.empty())
            `when`(virtualAccountRepository.save(any(VirtualAccount::class.java))).thenAnswer { invocation ->
                invocation.getArgument<VirtualAccount>(0).apply { this.user = user }
            }

            // When
            val dtos = accountQueryService.createAccount(name)

            // Then
            assertTrue(dtos.accountExists, "계좌 생성 시 success flag는 true여야 합니다.")
            assertEquals(BigDecimal.ZERO, dtos.balance, "새 계좌의 초기 잔액은 0이어야 합니다.")
            verify(virtualAccountRepository, times(1)).save(any(VirtualAccount::class.java))
        }
    }

    @Nested
    @DisplayName("getAccountInfo() 관련 테스트")
    inner class GetAccountInfoTests {

        @Test
        @DisplayName("계좌 ID 기반 계좌 정보 조회")
        fun testGetAccountInfoByAccountId() {
            // Given
            val accountId = 1
            val account = createVirtualAccount(accountId, BigDecimal.valueOf(500))
            `when`(virtualAccountRepository.findById(accountId)).thenReturn(Optional.of(account))

            // When
            val response = accountQueryService.getAccountInfo(accountId)

            // Then
            assertNotNull(response, "계좌 정보 응답은 null이 아니어야 합니다.")
            assertEquals(accountId, response.accountId, "응답 계좌 ID가 일치해야 합니다.")
        }

        @Test
        @DisplayName("username 기반 계좌 정보 조회")
        fun testGetAccountInfoByUsername() {
            // Given
            val username = "userInfo"
            val user = createUser(2)
            val account = createVirtualAccount(user, BigDecimal.valueOf(750))
            `when`(virtualAccountRepository.findByUser_Name(username)).thenReturn(Optional.of(account))

            // When
            val response = accountQueryService.getAccountInfo(username)

            // Then
            assertNotNull(response, "계좌 정보 응답은 null이 아니어야 합니다.")
            // createVirtualAccount(user, BigDecimal) 메서드는 accountId를 1로 설정함
            assertEquals(1, response.accountId, "응답 계좌 ID가 일치해야 합니다.")
        }
    }

    @Nested
    @DisplayName("getAccount() 예외 처리 테스트")
    inner class GetAccountExceptionTests {

        @Test
        @DisplayName("계좌 ID로 계좌를 찾을 수 없는 경우")
        fun whenAccountNotFoundById_thenThrowAccountNotFoundException() {
            // Given
            val accountId = 999
            `when`(virtualAccountRepository.findById(accountId)).thenReturn(Optional.empty())

            // When & Then
            assertThrows(AccountNotFoundException::class.java) {
                accountQueryService.getAccountByAccountId(accountId)
            }
        }

        @Test
        @DisplayName("username으로 계좌를 찾을 수 없는 경우")
        fun whenAccountNotFoundByUsername_thenThrowAccountNotFoundException() {
            // Given
            val username = "nonexistentUser"
            `when`(virtualAccountRepository.findByUser_Name(username)).thenReturn(Optional.empty())

            // When & Then
            assertThrows(AccountNotFoundException::class.java) {
                accountQueryService.getAccountByUsername(username)
            }
        }
    }

    // 헬퍼 메서드들
    private fun createUser(userId: Int): User =
        User().apply { this.userId = userId }

    private fun createVirtualAccount(user: User, balance: BigDecimal): VirtualAccount =
        VirtualAccount().apply {
            accountId = 1
            this.user = user
            this.balance = balance
        }

    private fun createVirtualAccount(accountId: Int, balance: BigDecimal): VirtualAccount =
        VirtualAccount().apply {
            this.accountId = accountId
            this.balance = balance
        }
}