package funding.startreum.domain.reward.controller

import funding.startreum.common.util.ApiResponse
import funding.startreum.domain.reward.dto.request.RewardRequest
import funding.startreum.domain.reward.dto.request.RewardUpdateRequest
import funding.startreum.domain.reward.dto.response.RewardResponse
import funding.startreum.domain.reward.service.RewardService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * RewardRestController 클래스
 *
 * 이 컨트롤러는 리워드 관련 API 엔드포인트를 제공하며,
 * 리워드 생성, 조회, 수정, 삭제 요청을 처리합니다.
 */
@RestController
@RequestMapping("/api/reward")
public open class RewardRestController( // ✅ open 추가하여 프록시 생성 가능하도록 설정
    private val service: RewardService
) {

    private val log = LoggerFactory.getLogger(RewardRestController::class.java)

    /**
     * 리워드 생성 요청을 처리합니다.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BENEFICIARY')")
    @PostMapping
    fun createReward(@Valid @RequestBody request: RewardRequest): ResponseEntity<ApiResponse<RewardResponse>> {
        log.debug("프로젝트 ID {}에 리워드를 생성합니다.", request.projectId)
        val response = service.generateNewRewardResponse(request)
        log.debug("프로젝트 ID {}에 리워드 생성에 성공했습니다.", request.projectId)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("리워드 생성에 성공했습니다.", response))
    }

    /**
     * 지정된 프로젝트 ID에 속한 모든 리워드를 조회합니다.
     */
    @GetMapping("/{projectId}")
    fun getRewardByProjectId(@PathVariable projectId: Int): ResponseEntity<ApiResponse<List<RewardResponse>>> {
        log.debug("프로젝트 ID {}에 있는 리워드를 조회합니다.", projectId)
        val response = service.generateRewardsResponse(projectId)
        log.debug("프로젝트 ID {}에 있는 리워드 조회에 성공했습니다.", projectId)

        val message = if (response.isEmpty()) "리워드가 존재하지 않습니다." else "리워드 조회에 성공했습니다."
        return ResponseEntity.ok(ApiResponse.success(message, response))
    }

    /**
     * 리워드 수정 요청을 처리합니다.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BENEFICIARY')")
    @PutMapping("/{rewardId}")
    fun updateReward(
        @PathVariable rewardId: Int,
        @Valid @RequestBody request: RewardUpdateRequest
    ): ResponseEntity<ApiResponse<RewardResponse>> {
        log.debug("리워드 ID {}에 있는 내역을 수정합니다.", rewardId)
        val response = service.generateUpdatedRewardResponse(rewardId, request)
        log.debug("리워드 ID {}에 있는 내역을 수정에 성공했습니다.", rewardId)

        return ResponseEntity.ok(ApiResponse.success("리워드 수정에 성공했습니다.", response))
    }

    /**
     * 리워드 삭제 요청을 처리합니다.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BENEFICIARY')")
    @DeleteMapping("/{rewardId}")
    fun deleteReward(@PathVariable rewardId: Int): ResponseEntity<ApiResponse<Unit>> {
        log.debug("리워드 ID {}를 삭제합니다.", rewardId)
        service.deleteReward(rewardId)
        log.debug("리워드 ID {} 삭제에 완료했습니다.", rewardId)

        return ResponseEntity.ok(ApiResponse.success("리워드 삭제에 성공했습니다."))
    }
}
