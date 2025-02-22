package Funding.Startreum.domain.reward.controller;

import Funding.Startreum.common.util.ApiResponse;
import Funding.Startreum.domain.project.entity.Project;
import Funding.Startreum.domain.reward.dto.request.RewardRequest;
import Funding.Startreum.domain.reward.dto.request.RewardUpdateRequest;
import Funding.Startreum.domain.reward.dto.response.RewardResponse;
import Funding.Startreum.domain.reward.service.RewardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <h2>RewardRestController 클래스</h2>
 * <p>
 * 이 컨트롤러는 리워드 관련 API 엔드포인트를 제공하며,
 * 리워드 생성, 조회, 수정, 삭제 요청을 처리합니다.
 * 클라이언트로부터 들어오는 요청을 받아 {@link RewardService}를 통해 비즈니스 로직을 실행하고,
 * 결과를 {@link ApiResponse} 형태로 반환합니다.
 * </p>
 *
 * <p><strong>주요 엔드포인트</strong></p>
 * <ul>
 *   <li>POST /api/reward - 리워드 생성</li>
 *   <li>GET /api/reward/{projectId} - 프로젝트에 속한 리워드 조회</li>
 *   <li>PUT /api/reward/{rewardId} - 리워드 수정</li>
 *   <li>DELETE /api/reward/{rewardId} - 리워드 삭제</li>
 * </ul>
 *
 * <p>※ 일부 엔드포인트는 {@code ADMIN} 또는 {@code BENEFICIARY} 권한이 필요합니다.</p>
 *
 * @author 한상훈
 * @see Project
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reward")
@Slf4j
public class RewardRestController {

    private final RewardService service;

    /**
     * 리워드 생성 요청을 처리합니다.
     *
     * <p>
     * 요청 본문에 포함된 {@link RewardRequest} 객체를 기반으로 새로운 리워드를 생성하며,
     * 생성 성공 시 HTTP 201(CREATED) 상태와 함께 생성된 리워드 정보를 반환합니다.
     * </p>
     *
     * @param request 클라이언트가 전송한 리워드 생성 정보가 담긴 {@link RewardRequest} 객체
     * @return 생성된 리워드 정보를 포함한 {@link ApiResponse} 객체를 담은 {@link ResponseEntity}
     * @see RewardService#generateNewRewardResponse(RewardRequest)
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BENEFICIARY')")
    @PostMapping
    public ResponseEntity<?> createReward(
            @RequestBody @Valid RewardRequest request
    ) {
        log.debug("프로젝트 ID {}에 리워드를 생성합니다.", request.projectId());
        RewardResponse response = service.generateNewRewardResponse(request);
        log.debug("프로젝트 ID {}에 리워드 생성에 성공했습니다..", request.projectId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("리워드 생성에 성공했습니다.", response));
    }

    /**
     * 지정된 프로젝트 ID에 속한 모든 리워드를 조회합니다.
     *
     * <p>
     * 경로 변수로 전달된 {@code projectId}를 기반으로 관련 리워드를 조회하며,
     * 조회 결과가 없을 경우에도 성공 응답과 함께 빈 리스트를 반환합니다.
     * </p>
     *
     * @param projectId 리워드가 속한 프로젝트의 ID
     * @return 조회된 리워드 리스트를 포함한 {@link ApiResponse} 객체를 담은 {@link ResponseEntity}
     * @see RewardService#generateRewardsResponse(Integer)
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<?> getRewardByProjectId(
            @PathVariable(name = "projectId") int projectId
    ) {
        log.debug("프로젝트 ID {}에 있는 리워드를 조회합니다.", projectId);
        List<RewardResponse> response = service.generateRewardsResponse(projectId);
        log.debug("프로젝트 ID {}에 있는 리워드 조회에 성공했습니다.", projectId);

        if (response.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("리워드가 존재하지 않습니다.", response));
        } else {
            return ResponseEntity.ok(ApiResponse.success("리워드 조회에 성공했습니다.", response));
        }

    }

    /**
     * 리워드 수정 요청을 처리합니다.
     *
     * <p>
     * 경로 변수로 전달된 {@code rewardId}와 요청 본문의 {@link RewardUpdateRequest} 정보를 사용하여,
     * 기존 리워드 정보를 수정합니다. 수정 성공 시 수정된 리워드 정보를 반환합니다.
     * </p>
     *
     * @param rewardId 수정할 리워드의 ID
     * @param request  리워드 수정에 필요한 정보가 담긴 {@link RewardUpdateRequest} 객체
     * @return 수정된 리워드 정보를 포함한 {@link ApiResponse} 객체를 담은 {@link ResponseEntity}
     * @see RewardService#generateUpdatedRewardResponse(Integer, RewardUpdateRequest)
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BENEFICIARY')")
    @PutMapping("/{rewardId}")
    public ResponseEntity<?> updateReward(
            @PathVariable("rewardId") int rewardId,
            @Valid @RequestBody RewardUpdateRequest request
    ) {
        log.debug("리워드 ID {}에 있는 내역을 수정합니다. ", rewardId);
        RewardResponse response = service.generateUpdatedRewardResponse(rewardId, request);
        log.debug("리워드 ID {}에 있는 내역을 수정에 성공했습니다.", rewardId);
        return ResponseEntity.ok(ApiResponse.success("리워드 수정에 성공했습니다.", response));
    }

    /**
     * 리워드 삭제 요청을 처리합니다.
     *
     * <p>
     * 경로 변수로 전달된 {@code rewardId}에 해당하는 리워드를 삭제하며,
     * 삭제 성공 시 성공 메시지를 반환합니다.
     * </p>
     *
     * @param rewardId 삭제할 리워드의 ID
     * @return 삭제 성공 메시지를 포함한 {@link ApiResponse} 객체를 담은 {@link ResponseEntity}
     * @see RewardService#deleteReward(Integer)
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BENEFICIARY')")
    @DeleteMapping("/{rewardId}")
    public ResponseEntity<?> deleteReward(
            @PathVariable("rewardId") int rewardId
    ) {
        log.debug("리워드 ID {}를 삭제합니다.", rewardId);
        service.deleteReward(rewardId);
        log.debug("리워드 ID {} 삭제에 완료했습니다.", rewardId);
        return ResponseEntity.ok(ApiResponse.success("리워드 삭제에 성공했습니다."));
    }
}
