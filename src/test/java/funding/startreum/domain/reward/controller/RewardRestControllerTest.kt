package funding.startreum.domain.reward.controller

import com.fasterxml.jackson.databind.ObjectMapper
import funding.startreum.common.config.SecurityConfig
import funding.startreum.domain.reward.dto.request.RewardRequest
import funding.startreum.domain.reward.dto.request.RewardUpdateRequest
import funding.startreum.domain.reward.dto.response.RewardResponse
import funding.startreum.domain.reward.service.RewardService
import funding.startreum.domain.users.service.CustomUserDetailsService
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * @see RewardRestController
 */
@Import(SecurityConfig::class)
@WebMvcTest(controllers = [RewardRestController::class])
@AutoConfigureMockMvc
class RewardRestControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var rewardService: RewardService

    @MockitoBean
    lateinit var customUserDetailsService: CustomUserDetailsService

    private val BASE_URL = "/api/reward"

    @Nested
    @DisplayName("#1 리워드 생성 API (POST /api/reward)")
    inner class CreateRewardTest {

        @Test
        @DisplayName("1-1) ADMIN 또는 BENEFICIARY 권한을 가진 사용자는 리워드 생성 성공 201")
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun createReward_Success() {
            // given
            val request = RewardRequest(
                projectId = 1,
                description = "Test Reward",
                amount = BigDecimal("100")
            )
            val now = LocalDateTime.now()
            val expectedResponse = RewardResponse(
                rewardId = 10,
                projectId = 1,
                description = "Test Reward",
                amount = BigDecimal("100"),
                createdAt = now,
                updatedAt = null
            )
            given(rewardService.generateNewRewardResponse(any())).willReturn(expectedResponse)

            // when & then
            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.message", `is`("리워드 생성에 성공했습니다.")))
                .andExpect(jsonPath("$.data.rewardId", `is`(10)))
                .andExpect(jsonPath("$.data.projectId", `is`(1)))
                .andExpect(jsonPath("$.data.description", `is`("Test Reward")))
                .andExpect(jsonPath("$.data.amount", `is`(100)))
        }

        @Test
        @DisplayName("1-2) 권한 없는 사용자는 403 Forbidden 응답을 받는다")
        @WithMockUser(username = "user", roles = ["USER"])
        fun createReward_Forbidden() {
            val request = RewardRequest(
                projectId = 1,
                description = "Test Reward",
                amount = BigDecimal("100")
            )

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    @DisplayName("#2 리워드 조회 API (GET /api/reward/{projectId})")
    inner class GetRewardByProjectIdTest {

        @Test
        @DisplayName("2-1) 리워드가 존재할 경우 정상 조회")
        fun getReward_Success() {
            // given
            val projectId = 1
            val now = LocalDateTime.now()
            val rewardList = listOf(
                RewardResponse(10, projectId, "Reward1", BigDecimal("100"), now, null),
                RewardResponse(11, projectId, "Reward2", BigDecimal("200"), now, null)
            )
            given(rewardService.generateRewardsResponse(projectId)).willReturn(rewardList)

            // when & then
            mockMvc.perform(
                get("$BASE_URL/{projectId}", projectId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", `is`("리워드 조회에 성공했습니다.")))
                .andExpect(jsonPath("$.data[0].rewardId", `is`(10)))
                .andExpect(jsonPath("$.data[1].rewardId", `is`(11)))
        }

        @Test
        @DisplayName("2-2) 리워드가 없을 경우 '리워드가 존재하지 않습니다.' 메시지를 반환")
        fun getReward_NotFound() {
            // given
            val projectId = 1
            val emptyList = emptyList<RewardResponse>()
            given(rewardService.generateRewardsResponse(projectId)).willReturn(emptyList)

            // when & then
            mockMvc.perform(
                get("$BASE_URL/{projectId}", projectId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", `is`("리워드가 존재하지 않습니다.")))
                .andExpect(jsonPath("$.data").isEmpty)
        }
    }

    @Nested
    @DisplayName("#3 리워드 수정 API (PUT /api/reward/{rewardId})")
    inner class UpdateRewardTest {

        @Test
        @DisplayName("3-1) ADMIN 또는 BENEFICIARY 권한을 가진 사용자는 리워드 수정을 성공한다")
        @WithMockUser(username = "beneficiary", roles = ["BENEFICIARY"])
        fun updateReward_Success() {
            // given
            val rewardId = 10
            val updateRequest = RewardUpdateRequest(
                description = "Updated Reward",
                amount = BigDecimal("150")
            )
            val now = LocalDateTime.now()
            val updatedResponse = RewardResponse(
                rewardId = rewardId,
                projectId = 1,
                description = "Updated Reward",
                amount = BigDecimal("150"),
                createdAt = now,
                updatedAt = now
            )
            given(rewardService.generateUpdatedRewardResponse(rewardId, updateRequest))
                .willReturn(updatedResponse)

            // when & then
            mockMvc.perform(
                put("$BASE_URL/{rewardId}", rewardId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", `is`("리워드 수정에 성공했습니다.")))
                .andExpect(jsonPath("$.data.rewardId", `is`(rewardId)))
                .andExpect(jsonPath("$.data.description", `is`("Updated Reward")))
                .andExpect(jsonPath("$.data.amount", `is`(150)))
        }
    }

    @Nested
    @DisplayName("#4 리워드 삭제 API (DELETE /api/reward/{rewardId})")
    inner class DeleteRewardTest {

        @Test
        @DisplayName("4-1) ADMIN 또는 BENEFICIARY 권한을 가진 사용자는 리워드 삭제에 성공한다")
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun deleteReward_Success() {
            val rewardId = 10

            // when & then
            mockMvc.perform(
                delete("$BASE_URL/{rewardId}", rewardId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.message", `is`("리워드 삭제에 성공했습니다.")))
        }
    }
}