package funding.startreum.domain.virtualaccount.controller

import funding.startreum.common.util.ApiResponse
import funding.startreum.domain.virtualaccount.dto.VirtualAccountDtos
import funding.startreum.domain.virtualaccount.dto.request.AccountPaymentRequest
import funding.startreum.domain.virtualaccount.dto.request.AccountRequest
import funding.startreum.domain.virtualaccount.service.AccountChargeService
import funding.startreum.domain.virtualaccount.service.AccountPaymentService
import funding.startreum.domain.virtualaccount.service.AccountQueryService
import funding.startreum.domain.virtualaccount.service.AccountRefundService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.parameters.P
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/account")
class VirtualAccountRestController(
    private val accountQueryService: AccountQueryService,
    private val accountChargeService: AccountChargeService,
    private val accountPaymentService: AccountPaymentService,
    private val accountRefundService: AccountRefundService,
) {

    /**
     * 특정 사용자의 계좌 조회 API (이름 기반)
     */
    @GetMapping("/user/{name}")
    fun getAccount(@PathVariable name: String, principal: Principal?): ResponseEntity<VirtualAccountDtos> {
        // System.out.println(principal);
        //  System.out.println("🔍 Principal 정보: " + (principal != null ? principal.getName() : "NULL"));
        // System.out.println("🔍 요청된 사용자: " + name);

        if (principal == null) {
            // System.out.println("❌ 인증되지 않은 사용자 요청");
            return ResponseEntity.status(401).body(VirtualAccountDtos(false)) // Unauthorized
        }

        if (principal.name != name) {
            //  System.out.println("❌ 본인 또는 관리자가 아님: 접근 불가");
            return ResponseEntity.status(403).body(VirtualAccountDtos(false)) // Forbidden
        }

        val account = accountQueryService.findByName(name)
        return ResponseEntity.ok().body(account)
    }

    /**
     * 계좌 생성 API
     */
    @PostMapping("/user/{name}/create")
    fun createAccount(@PathVariable name: String, principal: Principal?): ResponseEntity<VirtualAccountDtos> {
        if (principal == null || principal.name != name) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(VirtualAccountDtos(false)) // ✅ HttpStatus.FORBIDDEN 사용
        }

        try {
            val newAccount = accountQueryService.createAccount(name)
            return ResponseEntity.ok().body(newAccount)
        } catch (e: IllegalStateException) {
            return ResponseEntity.badRequest().body(VirtualAccountDtos(false))
        }
    }


    /**
     * 잔액 충전: 계좌에 금액을 충전합니다.
     *
     * @param accountId 충전할 계좌의 ID. 해당 계좌의 소유자여야 합니다.
     * @param request   충전할 금액 및 관련 정보를 담은 DTO.
     * @return funding.startreum.common.util.ApiResponse 객체 안에 충전된 계좌 정보를 포함하여 반환합니다.
     */
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.isAccountOwner(principal, #accountId)")
    @PostMapping("/{accountId}")
    fun chargeAccountByAccountId(
        @PathVariable("accountId") @P("accountId") accountId: Int,
        @RequestBody request: @Valid AccountRequest
    ): ResponseEntity<*> {
        val response = accountChargeService.chargeByAccountId(accountId, request)
        return ResponseEntity.ok(ApiResponse.success("계좌 충전에 성공했습니다.", response))
    }

    /**
     * 잔액 충전: 현재 로그인한 사용자의 계좌에 금액을 충전합니다.
     *
     * @param request 충전할 금액 및 관련 정보를 담은 DTO.
     * @return funding.startreum.common.util.ApiResponse 객체 안에 충전된 계좌 정보를 포함하여 반환합니다.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    fun chargeOwnAccountByUserName(
        @RequestBody request: @Valid AccountRequest,
        principal: Principal
    ): ResponseEntity<*> {
        val response = accountChargeService.chargeByUsername(principal.name, request)
        return ResponseEntity.ok(ApiResponse.success("계좌 충전에 성공했습니다.", response))
    }

    /**
     * 계좌 내역 조회: 특정 계좌의 거래 내역을 조회합니다.
     *
     * @param accountId 조회할 계좌의 ID.
     * @return funding.startreum.common.util.ApiResponse 객체 안에 조회된 계좌 거래 내역 정보를 포함하여 반환합니다.
     */
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.isAccountOwner(principal, #accountId)")
    @GetMapping("/{accountId}")
    fun getAccountByAccountId(
        @PathVariable("accountId") @P("accountId") accountId: Int
    ): ResponseEntity<*> {
        val response = accountQueryService.getAccountInfo(accountId)
        return ResponseEntity.ok(ApiResponse.success("계좌 내역 조회에 성공했습니다.", response))
    }

    /**
     * 계좌 내역 조회: 현재 로그인한 사용자의 계좌 잔액을 조회합니다.
     *
     *
     * @return funding.startreum.common.util.ApiResponse 객체 안에 조회된 계좌 거래 내역 정보를 포함하여 반환합니다.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun getAccountByUserName(
        principal: Principal
    ): ResponseEntity<*> {
        val response = accountQueryService.getAccountInfo(principal.name)
        return ResponseEntity.ok(ApiResponse.success("계좌 내역 조회에 성공했습니다.", response))
    }

    /**
     * 결제 처리: 특정 계좌의 결제 요청을 처리합니다.
     *
     * @param accountId 결제를 진행할 계좌의 ID.
     * @param request   결제 요청 정보를 담은 DTO (예: 프로젝트 ID, 결제 금액 등).
     * @param principal 현재 인증된 사용자의 세부 정보를 포함하는 객체.
     * @return funding.startreum.common.util.ApiResponse 객체 안에 결제가 완료된 계좌 정보를 포함하여 반환합니다.
     */
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.isAccountOwner(principal, #accountId)")
    @PostMapping("/{accountId}/payment")
    fun processPaymentByAccountId(
        @PathVariable("accountId") @P("accountId") accountId: Int,
        @RequestBody request: @Valid AccountPaymentRequest,
        principal: Principal
    ): ResponseEntity<*> {
        val response = accountPaymentService.paymentByAccountId(accountId, request, principal.name)
        return ResponseEntity.ok(ApiResponse.success("결제에 성공했습니다.", response))
    }

    /**
     * 결제 처리: 현재 로그인한 사용자의 계좌 결제 요청을 처리합니다
     *
     * @param request     결제 요청 정보를 담은 DTO (예: 프로젝트 ID, 결제 금액 등).
     * @param userDetails 현재 인증된 사용자의 세부 정보를 포함하는 객체.
     * @return funding.startreum.common.util.ApiResponse 객체 안에 결제가 완료된 계좌 정보를 포함하여 반환합니다.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/payment")
    fun processPaymentByUserName(
        @RequestBody request: @Valid AccountPaymentRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<*> {
        val response = accountPaymentService.paymentByUsername(request, userDetails.username)
        return ResponseEntity.ok(ApiResponse.success("결제에 성공했습니다.", response))
    }

    /**
     * 특정 계좌 환불 처리
     *
     * @param accountId     환불을 요청하는 계좌의 ID (원래 결제에 사용된 계좌).
     * @param transactionId 환불할 거래의 ID.
     * @return funding.startreum.common.util.ApiResponse 객체 안에 환불이 완료된 계좌 정보를 포함하여 반환합니다.
     */
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.isAccountOwner(principal, #accountId)")
    @PostMapping("/{accountId}/transactions/{transactionId}/refund")
    fun processRefund(
        @PathVariable("accountId") @P("accountId") accountId: Int,
        @PathVariable transactionId: Int
    ): ResponseEntity<*> {
        val response = accountRefundService.refund(accountId, transactionId)
        return ResponseEntity.ok(ApiResponse.success("거래 환불에 성공했습니다.", response))
    }
}