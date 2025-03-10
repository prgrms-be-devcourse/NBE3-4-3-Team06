package funding.startreum.domain.funding.service

import funding.startreum.domain.funding.entity.Funding
import funding.startreum.domain.funding.exception.FundingNotFoundException
import funding.startreum.domain.funding.repository.FundingRepository
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.users.entity.User
import funding.startreum.domain.users.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
class FundingServiceTest {

    @Mock
    private lateinit var fundingRepository: FundingRepository

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var fundingService: FundingService

    @Nested
    @DisplayName("createFunding() 테스트")
    inner class CreateFundingTests {

        @Test
        @DisplayName("정상적으로 Funding이 생성되는 경우")
        fun whenValid_thenCreateFunding() {
            // Given
            val mockUser = User().apply { this.name = "testUser" }
            val mockProject = Project().apply {
                projectId = 123
            }
            `when`(userService.getUserByName("testUser"))
                .thenReturn(mockUser)

            doAnswer {
                val fundingArg = it.arguments[0] as Funding
                fundingArg.fundingId = 1
                fundingArg
            }.`when`(fundingRepository).save(any(Funding::class.java))

            // When
            val result = fundingService.createFunding(
                currentProject = mockProject,
                username = "testUser",
                paymentAmount = BigDecimal.valueOf(10000)
            )

            // Then
            assertNotNull(result)
            assertEquals(1, result.fundingId)
            assertEquals(mockProject, result.project)
            assertEquals(mockUser, result.sponsor)
            assertTrue(result.amount.compareTo(BigDecimal.ZERO) > 0)
            assertNotNull(result.fundedAt)
            verify(fundingRepository).save(any(Funding::class.java))
        }
    }

    @Nested
    @DisplayName("cancelFunding() 테스트")
    inner class CancelFundingTests {

        @Test
        @DisplayName("정상적으로 Funding을 취소하는 경우 => isDeleted = true")
        fun whenFundingExists_thenSetIsDeletedTrue() {
            // Given
            val fundingId = 999
            val mockFunding = Funding().apply {
                this.fundingId = fundingId
                isDeleted = false
                fundedAt = LocalDateTime.now()
            }
            `when`(fundingRepository.findByFundingId(fundingId))
                .thenReturn(Optional.of(mockFunding))

            // When
            val canceled = fundingService.cancelFunding(fundingId)

            // Then
            assertNotNull(canceled)
            assertTrue(canceled.isDeleted)
            verify(fundingRepository).save(mockFunding)
        }

        @Test
        @DisplayName("Funding을 찾을 수 없을 경우 => FundingNotFoundException")
        fun whenFundingNotFound_thenThrowFundingNotFoundException() {
            // Given
            val fundingId = 888
            `when`(fundingRepository.findByFundingId(fundingId))
                .thenReturn(Optional.empty())

            // When & Then
            assertThrows<FundingNotFoundException> {
                fundingService.cancelFunding(fundingId)
            }
            verify(fundingRepository, never()).save(any(Funding::class.java))
        }
    }
}