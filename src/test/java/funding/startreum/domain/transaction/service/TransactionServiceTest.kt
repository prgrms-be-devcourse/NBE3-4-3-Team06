package funding.startreum.domain.transaction.service

import funding.startreum.domain.funding.entity.Funding
import funding.startreum.domain.transaction.entity.Transaction
import funding.startreum.domain.transaction.entity.Transaction.TransactionType
import funding.startreum.domain.transaction.repository.TransactionRepository
import funding.startreum.domain.transaction.transaction.TransactionNotFoundException
import funding.startreum.domain.users.entity.User
import funding.startreum.domain.users.repository.UserRepository
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import funding.startreum.domain.virtualaccount.service.AccountQueryService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class TransactionServiceTest {

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var userRepository: UserRepository

    // 필요하다면 accountQueryService를 Mock으로 선언
    @Mock
    private lateinit var accountQueryService: AccountQueryService

    @InjectMocks
    private lateinit var transactionService: TransactionService

    @Nested
    @DisplayName("getTransaction() 테스트")
    inner class GetTransactionTests {

        @Test
        @DisplayName("정상적으로 거래 내역이 존재할 경우 => Transaction 반환")
        fun whenTransactionExists_thenReturnTransaction() {
            // Given
            val transactionId = 123
            val mockTransaction = Transaction().apply {
                this.transactionId = transactionId
            }
            `when`(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(mockTransaction))

            // When
            val result = transactionService.getTransaction(transactionId)

            // Then
            assertNotNull(result)
            assertEquals(transactionId, result.transactionId)
            verify(transactionRepository).findById(transactionId)
        }

        @Test
        @DisplayName("거래 내역을 찾을 수 없는 경우 => TransactionNotFoundException 발생")
        fun whenTransactionNotFound_thenThrowTransactionNotFoundException() {
            // Given
            val transactionId = 999
            `when`(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty())

            // When & Then
            assertThrows<TransactionNotFoundException> {
                transactionService.getTransaction(transactionId)
            }
            verify(transactionRepository).findById(transactionId)
        }
    }

    @Nested
    @DisplayName("createTransaction() 테스트")
    inner class CreateTransactionTests {

        @Test
        @DisplayName("정상적으로 거래가 생성되는 경우 => Transaction 반환")
        fun whenValid_thenCreateTransaction() {
            // Given
            val fromAccount = VirtualAccount()
            val toAccount = VirtualAccount()
            val funding = Funding() // 실제 사용 시 필요 필드 설정
            val mockAdmin = User().apply { name = "Admin" }
            `when`(userRepository.findByName("Admin"))
                .thenReturn(Optional.of(mockAdmin))

            // 생성할 거래 데이터
            val amount = BigDecimal("10000")
            val type = TransactionType.REMITTANCE

            // 저장 직전 Transaction 객체를 캡쳐 가능 (필요 시 ArgumentCaptor 활용)
            doAnswer {
                val transactionArg = it.arguments[0] as Transaction
                transactionArg.transactionId = 1
                transactionArg // 그대로 반환
            }.`when`(transactionRepository).save(any(Transaction::class.java))

            // When
            val created = transactionService.createTransaction(
                funding = funding,
                from = fromAccount,
                to = toAccount,
                amount = amount,
                type = type
            )

            // Then
            assertNotNull(created)
            assertEquals(1, created.transactionId)
            assertEquals(funding, created.funding)
            assertEquals(fromAccount, created.senderAccount)
            assertEquals(toAccount, created.receiverAccount)
            assertEquals(amount, created.amount)
            assertEquals(type, created.type)
            verify(transactionRepository).save(any(Transaction::class.java))
        }

        @Test
        @DisplayName("Admin 계정을 찾을 수 없는 경우 => null 상태로 생성되는지 확인")
        fun whenAdminNotFound_thenTransactionAdminIsNull() {
            // Given
            `when`(userRepository.findByName("Admin"))
                .thenReturn(Optional.empty())

            val fromAccount = VirtualAccount()
            val toAccount = VirtualAccount()

            // When
            val created = transactionService.createTransaction(
                funding = null,
                from = fromAccount,
                to = toAccount,
                amount = BigDecimal("2000"),
                type = TransactionType.REFUND
            )

            // Then
            assertNotNull(created)
            assertNull(created.admin)
            verify(transactionRepository).save(any(Transaction::class.java))
        }
    }
}