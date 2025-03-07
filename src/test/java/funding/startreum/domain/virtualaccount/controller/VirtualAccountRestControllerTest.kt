package funding.startreum.domain.virtualaccount.controller

import com.fasterxml.jackson.databind.ObjectMapper
import funding.startreum.common.config.SecurityConfig
import funding.startreum.domain.users.service.CustomUserDetailsService
import funding.startreum.domain.virtualaccount.dto.VirtualAccountDtos
import funding.startreum.domain.virtualaccount.dto.request.AccountPaymentRequest
import funding.startreum.domain.virtualaccount.dto.request.AccountRequest
import funding.startreum.domain.virtualaccount.dto.response.AccountPaymentResponse
import funding.startreum.domain.virtualaccount.dto.response.AccountRefundResponse
import funding.startreum.domain.virtualaccount.dto.response.AccountResponse
import funding.startreum.domain.virtualaccount.security.AccountSecurity
import funding.startreum.domain.virtualaccount.service.AccountChargeService
import funding.startreum.domain.virtualaccount.service.AccountPaymentService
import funding.startreum.domain.virtualaccount.service.AccountQueryService
import funding.startreum.domain.virtualaccount.service.AccountRefundService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDateTime

@Import(SecurityConfig::class)
@WebMvcTest(controllers = [VirtualAccountRestController::class])
@AutoConfigureMockMvc
class VirtualAccountRestControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var accountQueryService: AccountQueryService

    @MockitoBean
    lateinit var accountChargeService: AccountChargeService

    @MockitoBean
    lateinit var accountPaymentService: AccountPaymentService

    @MockitoBean
    lateinit var accountRefundService: AccountRefundService

    @MockitoBean
    lateinit var customUserDetailsService: CustomUserDetailsService

    @MockitoBean(name = "accountSecurity")
    lateinit var accountSecurity: AccountSecurity

    private val BASE_URL = "/api/account"

    @Nested
    @DisplayName("#1 계좌 조회 API (/user/{name})")
    inner class GetAccountTest {

        @Test
        @DisplayName("1-1) 인증된 사용자이면서 본인일 경우, 200 OK & 정상 데이터 반환")
        @WithMockUser(username = "tester")
        fun getAccount_Success() {
            val targetUser = "tester"
            val mockAccount = VirtualAccountDtos(true)
            given(accountQueryService.findByName(targetUser)).willReturn(mockAccount)

            mockMvc.perform(
                get("$BASE_URL/user/{name}", targetUser)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accountExists").value(true))
        }

        @Test
        @DisplayName("1-2) Principal 이 null 인 경우 => 401 Unauthorized")
        fun getAccount_Unauthorized() {
            mockMvc.perform(
                get("$BASE_URL/user/{name}", "tester")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("1-3) 본인이 아닌 다른 유저의 계좌에 접근할 경우 => 403 Forbidden")
        @WithMockUser(username = "anotherUser")
        fun getAccount_Forbidden() {
            mockMvc.perform(
                get("$BASE_URL/user/{name}", "tester")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    @DisplayName("#2 계좌 생성 API (/user/{name}/create)")
    inner class CreateAccountTest {

        @Test
        @DisplayName("2-1) 인증된 사용자 == {name}, 계좌생성 성공 시 200 OK")
        @WithMockUser(username = "tester")
        fun createAccount_Success() {
            val targetUser = "tester"
            val newAccountDto = VirtualAccountDtos(true)
            given(accountQueryService.createAccount(targetUser)).willReturn(newAccountDto)

            mockMvc.perform(
                post("$BASE_URL/user/{name}/create", targetUser)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accountExists").value(true))
        }

        @Test
        @DisplayName("2-2) 계좌생성 시 principal 이 null 이거나, 요청 유저 != principal => 403 Forbidden")
        @WithMockUser(username = "notOwner")
        fun createAccount_Forbidden() {
            mockMvc.perform(
                post("$BASE_URL/user/{name}/create", "tester")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("2-3) 이미 계좌가 존재하는 등 IllegalStateException 발생 시 400 Bad Request")
        @WithMockUser(username = "tester")
        fun createAccount_Conflict() {
            given(accountQueryService.createAccount("tester"))
                .willThrow(IllegalStateException("이미 존재하는 계좌입니다."))

            mockMvc.perform(
                post("$BASE_URL/user/{name}/create", "tester")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("#3 잔액 충전 API (accountId 기반) => POST /{accountId}")
    inner class ChargeAccountByIdTest {

        @Test
        @DisplayName("3-1) JWT 토큰(ADMIN 권한)이면 충전 성공 200")
        @WithMockUser(username = "tester", roles = ["ADMIN"])
        fun chargeAccount_Success_WithJwt() {
            val accountId = 123
            val mockResponse = AccountPaymentResponse(
                1, accountId,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(500),
                BigDecimal.valueOf(1500), LocalDateTime.now()
            )
            given(
                accountChargeService.chargeByAccountId(eq(accountId), any())
            ).willReturn(mockResponse)

            val requestBody = AccountRequest(BigDecimal.valueOf(500))
            val requestJson = objectMapper.writeValueAsString(requestBody)

            mockMvc.perform(
                post("$BASE_URL/$accountId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.afterMoney").value(1500))
        }

        @Test
        @DisplayName("3-2) 토큰 없이 요청하면 401 or 403")
        fun chargeAccount_Unauthorized() {
            val accountId = 123
            val requestBody = AccountRequest(BigDecimal.valueOf(500))
            val requestJson = objectMapper.writeValueAsString(requestBody)

            mockMvc.perform(
                post("$BASE_URL/$accountId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    @DisplayName("#4 잔액 충전 API (username 기반) => POST /api/account")
    inner class ChargeOwnAccountTest {

        @Test
        @DisplayName("4-1) 현재 로그인한 사용자가 자신의 계좌 잔액을 충전, 200 OK")
        @WithMockUser(username = "tester")
        fun chargeOwnAccount_Success() {
            val amount = BigDecimal.valueOf(3000)
            val request = AccountRequest(amount)

            val mockResponse = AccountPaymentResponse(
                1, 999, BigDecimal.valueOf(1000), amount,
                BigDecimal.valueOf(4000), LocalDateTime.now()
            )
            given(accountChargeService.chargeByUsername("tester", request)).willReturn(mockResponse)

            mockMvc.perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("계좌 충전에 성공했습니다."))
                .andExpect(jsonPath("$.data.afterMoney").value(4000))
        }
    }

    @Nested
    @DisplayName("#5 계좌 조회 API => GET /{accountId}")
    inner class GetAccountByIdTest {

        @Test
        @DisplayName("5-1) 관리자인 경우 해당 계좌 조회 가능, 200 OK")
        @WithMockUser(username = "admin", roles = ["ADMIN"])
        fun getAccount_Admin() {
            val accountId = 111
            val mockResponse = AccountResponse(accountId, BigDecimal.valueOf(2000), LocalDateTime.now())
            given(accountQueryService.getAccountInfo(accountId)).willReturn(mockResponse)

            mockMvc.perform(
                get("$BASE_URL/$accountId")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accountId").value(accountId))
        }

        @Test
        @DisplayName("5-2) 소유자가 아닌 사용자는 403 Forbidden")
        @WithMockUser(username = "notOwner", roles = ["SPONSOR"])
        fun getAccount_NotOwner() {
            mockMvc.perform(
                get("$BASE_URL/999")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    @DisplayName("#6 계좌 결제 API (accountId 기반) => POST /{accountId}/payment")
    inner class PaymentByAccountIdTest {

        @Test
        @DisplayName("6-1) ADMIN 또는 OWNER 권한이 있으면 결제 성공 200")
        @WithMockUser(username = "tester", roles = ["ADMIN"])
        fun payment_Success() {
            val accountId = 123
            val paymentAmount = BigDecimal.valueOf(500)
            val requestBody = AccountPaymentRequest(1, paymentAmount)

            val mockResponse = AccountPaymentResponse(
                1, accountId, BigDecimal.valueOf(1000), paymentAmount,
                BigDecimal.valueOf(500), LocalDateTime.now()
            )
            given(accountPaymentService.paymentByAccountId(accountId, requestBody, "tester"))
                .willReturn(mockResponse)

            mockMvc.perform(
                post("$BASE_URL/$accountId/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accountId").value(accountId))
        }

        @Test
        @DisplayName("6-2) 권한 없는 사용자 => 403 Forbidden")
        @WithMockUser(username = "tester")
        fun payment_Forbidden() {
            val accountId = 123
            val requestBody = AccountPaymentRequest(1, BigDecimal.valueOf(500))

            mockMvc.perform(
                post("$BASE_URL/$accountId/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody))
            )
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    @DisplayName("#7 계좌 결제 API (username 기반) => POST /api/account/payment")
    inner class PaymentByUsernameTest {

        @Test
        @DisplayName("7-1) 로그인 사용자 == OWNER, 결제 성공 200")
        @WithMockUser(username = "tester", roles = ["SPONSOR"])
        fun payment_Success() {
            val paymentAmount = BigDecimal.valueOf(1000)
            val requestBody = AccountPaymentRequest(1, paymentAmount)

            val mockResponse = AccountPaymentResponse(
                10, 555, BigDecimal.valueOf(2000), paymentAmount,
                BigDecimal.valueOf(1000), LocalDateTime.now()
            )
            given(accountPaymentService.paymentByUsername(requestBody, "tester"))
                .willReturn(mockResponse)

            mockMvc.perform(
                post("$BASE_URL/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accountId").value(555))
        }
    }

    @Nested
    @DisplayName("#8 계좌 환불 API => POST /{accountId}/transactions/{transactionId}/refund")
    inner class RefundTest {

        @Test
        @DisplayName("8-1) ADMIN 또는 OWNER 권한이 있으면 환불 성공 200")
        @WithMockUser(username = "tester", roles = ["ADMIN"])
        fun refund_Success() {
            val accountId = 123
            val transactionId = 10
            val mockResponse = AccountRefundResponse(
                99, transactionId, accountId, BigDecimal.valueOf(5000),
                BigDecimal.valueOf(500), BigDecimal.valueOf(4500), LocalDateTime.now()
            )
            given(accountRefundService.refund(accountId, transactionId))
                .willReturn(mockResponse)

            mockMvc.perform(
                post("$BASE_URL/$accountId/transactions/$transactionId/refund")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("거래 환불에 성공했습니다."))
        }

        @Test
        @DisplayName("8-2) 권한 없는 사용자 => 403 Forbidden")
        @WithMockUser(username = "tester", roles = ["SPONSOR"])
        fun refund_Forbidden() {
            mockMvc.perform(
                post("$BASE_URL/999/transactions/111/refund")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isForbidden)
        }
    }
}