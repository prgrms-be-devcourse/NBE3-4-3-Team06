package funding.startreum.domain.reward.service

import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.project.service.ProjectService
import funding.startreum.domain.reward.dto.request.RewardRequest
import funding.startreum.domain.reward.dto.request.RewardUpdateRequest
import funding.startreum.domain.reward.dto.response.RewardResponse
import funding.startreum.domain.reward.entity.Reward
import funding.startreum.domain.reward.repository.RewardRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
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
class RewardServiceTest {

    @Mock
    private lateinit var rewardRepository: RewardRepository

    @Mock
    private lateinit var projectService: ProjectService

    @InjectMocks
    private lateinit var rewardService: RewardService

    @Test
    @DisplayName("createReward() 테스트")
    fun createReward() {
        // given
        val request = RewardRequest(
            projectId = 1,
            description = "Reward Description",
            amount = BigDecimal("100")
        )
        val project = Project().apply { projectId = 1 }
        `when`(projectService.getProject(1)).thenReturn(project)

        val now = LocalDateTime.now()
        val reward = Reward(
            project = project,
            description = request.description,
            amount = request.amount,
            createdAt = now,
            updatedAt = now
        )
        `when`(rewardRepository.save(any(Reward::class.java))).thenReturn(reward)

        // when
        val result = rewardService.createReward(request)

        // then
        assertNotNull(result)
        assertEquals(request.description, result.description)
        assertEquals(request.amount, result.amount)
    }

    @Test
    @DisplayName("generateNewRewardResponse() 테스트")
    fun generateNewRewardResponse() {
        // given
        val request = RewardRequest(
            projectId = 1,
            description = "New Reward",
            amount = BigDecimal("200")
        )
        val project = Project().apply { projectId = 1 }
        `when`(projectService.getProject(1)).thenReturn(project)

        val now = LocalDateTime.now()
        val reward = Reward(
            project = project,
            description = request.description,
            amount = request.amount,
            createdAt = now,
            updatedAt = now
        )
        `when`(rewardRepository.save(any(Reward::class.java))).thenReturn(reward)

        // when
        val response: RewardResponse = rewardService.generateNewRewardResponse(request)

        // then
        // 내부적으로 RewardResponse.fromReward(reward)가 호출되어 response 필드에 reward 값이 매핑된다고 가정
        assertNotNull(response)
        assertEquals(reward.description, response.description)
        assertEquals(reward.amount, response.amount)
    }

    @Test
    @DisplayName("getRewardsByProjectId() 테스트")
    fun getRewardsByProjectId() {
        // given
        val projectIds = 1
        val project = Project().apply { projectId = 1 }
        val reward1 = Reward(
            project = project,
            description = "Reward1",
            amount = BigDecimal("100"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val reward2 = Reward(
            project = project,
            description = "Reward2",
            amount = BigDecimal("200"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(rewardRepository.findByProject_ProjectId(projectIds)).thenReturn(listOf(reward1, reward2))

        // when
        val rewards = rewardService.getRewardsByProjectId(projectIds)

        // then
        assertEquals(2, rewards.size)
        assertTrue(rewards.contains(reward1))
        assertTrue(rewards.contains(reward2))
    }

    @Test
    @DisplayName("getRewardByRewardId() 테스트")
    fun getRewardByRewardId1() {
        // given
        val rewardId = 1
        val project = Project().apply { projectId = 1 }
        val reward = Reward(
            project = project,
            description = "Reward",
            amount = BigDecimal("150"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(rewardRepository.findById(rewardId)).thenReturn(Optional.of(reward))

        // when
        val result = rewardService.getRewardByRewardId(rewardId)

        // then
        assertEquals(reward, result)
    }

    @Test
    @DisplayName("getRewardByRewardId() 예외 테스트")
    fun getRewardByRewardId() {
        // given
        val rewardId = 99
        `when`(rewardRepository.findById(rewardId)).thenReturn(Optional.empty())

        // when & then
        assertThrows(EntityNotFoundException::class.java) {
            rewardService.getRewardByRewardId(rewardId)
        }
    }

    @Test
    @DisplayName("generateRewardsResponse() 테스트")
    fun generateRewardsResponse() {
        // given
        val projectIds = 1
        val project = Project().apply { projectId = 1 }
        val reward1 = Reward(
            project = project,
            description = "Reward1",
            amount = BigDecimal("100"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val reward2 = Reward(
            project = project,
            description = "Reward2",
            amount = BigDecimal("200"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(rewardRepository.findByProject_ProjectId(projectIds)).thenReturn(listOf(reward1, reward2))

        // when
        val responses = rewardService.generateRewardsResponse(projectIds)

        // then
        assertEquals(2, responses.size)
        // RewardResponse.fromReward가 reward의 필드를 그대로 매핑한다고 가정
        assertEquals(reward1.description, responses[0].description)
        assertEquals(reward2.description, responses[1].description)
    }

    @Test
    @DisplayName("updateReward() 테스트")
    fun updateReward() {
        // given
        val rewardId = 1
        val project = Project().apply { projectId = 1 }
        val existingReward = Reward(
            project = project,
            description = "Old Reward",
            amount = BigDecimal("100"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(rewardRepository.findById(rewardId)).thenReturn(Optional.of(existingReward))

        val updateRequest = RewardUpdateRequest(
            description = "Updated Reward",
            amount = BigDecimal("150")
        )
        // repository.save() 호출 시 업데이트된 reward를 반환한다고 가정
        `when`(rewardRepository.save(existingReward)).thenReturn(existingReward)

        // when
        val updatedReward = rewardService.updateReward(rewardId, updateRequest)

        // then
        assertEquals("Updated Reward", updatedReward.description)
        assertEquals(BigDecimal("150"), updatedReward.amount)
    }

    @Test
    @DisplayName("generateUpdatedRewardResponse() 테스트")
    fun generateUpdatedRewardResponse() {
        // given
        val rewardId = 1
        val project = Project().apply { projectId = 1 }
        val existingReward = Reward(
            project = project,
            description = "Old Reward",
            amount = BigDecimal("100"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(rewardRepository.findById(rewardId)).thenReturn(Optional.of(existingReward))

        val updateRequest = RewardUpdateRequest(
            description = "Updated Reward",
            amount = BigDecimal("150")
        )
        `when`(rewardRepository.save(existingReward)).thenReturn(existingReward)

        // when
        val response = rewardService.generateUpdatedRewardResponse(rewardId, updateRequest)

        // then
        // 내부적으로 RewardResponse.fromReward(existingReward)가 호출된다고 가정
        assertEquals(existingReward.description, response.description)
        assertEquals(existingReward.amount, response.amount)
    }

    @Test
    @DisplayName("deleteReward() 테스트")
    fun deleteReward() {
        // given
        val rewardId = 1
        val project = Project().apply { projectId = 1 }
        val reward = Reward(
            project = project,
            description = "Reward",
            amount = BigDecimal("100"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        `when`(rewardRepository.findById(rewardId)).thenReturn(Optional.of(reward))

        // when
        rewardService.deleteReward(rewardId)

        // then
        verify(rewardRepository, times(1)).delete(reward)
    }
}