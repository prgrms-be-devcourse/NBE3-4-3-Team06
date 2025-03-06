package funding.startreum.domain.virtualaccount.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import funding.startreum.common.config.SecurityConfig;
import funding.startreum.common.util.JwtUtil;
import funding.startreum.domain.users.service.CustomUserDetailsService;
import funding.startreum.domain.virtualaccount.dto.VirtualAccountDtos;
import funding.startreum.domain.virtualaccount.dto.request.AccountPaymentRequest;
import funding.startreum.domain.virtualaccount.dto.request.AccountRequest;
import funding.startreum.domain.virtualaccount.dto.response.AccountPaymentResponse;
import funding.startreum.domain.virtualaccount.dto.response.AccountRefundResponse;
import funding.startreum.domain.virtualaccount.dto.response.AccountResponse;
import funding.startreum.domain.virtualaccount.security.AccountSecurity;
import funding.startreum.domain.virtualaccount.service.AccountChargeService;
import funding.startreum.domain.virtualaccount.service.AccountPaymentService;
import funding.startreum.domain.virtualaccount.service.AccountQueryService;
import funding.startreum.domain.virtualaccount.service.AccountRefundService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @see VirtualAccountController
 */
@Import({SecurityConfig.class})
@WebMvcTest(controllers = VirtualAccountController.class)
@AutoConfigureMockMvc
class VirtualAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountQueryService accountQueryService;

    @MockitoBean
    private AccountChargeService accountChargeService;

    @MockitoBean
    private AccountPaymentService accountPaymentService;

    @MockitoBean
    private AccountRefundService accountRefundService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean(name = "accountSecurity")
    private AccountSecurity accountSecurity;

    @Autowired
    private ObjectMapper objectMapper;

    private final String BASE_URL = "/api/account";

    @Nested
    @DisplayName("#1 계좌 조회 API (/user/{name})")
    class GetAccountTest {

        @Test
        @DisplayName("1-1) 인증된 사용자이면서 본인일 경우, 200 OK & 정상 데이터 반환")
        @WithMockUser(username = "tester")
        void getAccount_Success() throws Exception {
            // given
            String targetUser = "tester";
            VirtualAccountDtos mockAccount = new VirtualAccountDtos(true);
            given(accountQueryService.findByName(targetUser)).willReturn(mockAccount);

            // when, then: @WithMockUser로 인해 SecurityContext에 인증 정보가 채워져 Principal이 'tester'로 주입됩니다.
            mockMvc.perform(get(BASE_URL + "/user/{name}", targetUser)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountExists").value(true));
        }

        @Test
        @DisplayName("1-2) Principal 이 null 인 경우 => 401 Unauthorized")
        void getAccount_Unauthorized() throws Exception {
            // @WithMockUser 미사용 시 SecurityContext에 인증 정보가 없으므로, 컨트롤러에서 401을 반환합니다.
            mockMvc.perform(get(BASE_URL + "/user/{name}", "tester"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("1-3) 본인이 아닌 다른 유저의 계좌에 접근할 경우 => 403 Forbidden")
        @WithMockUser(username = "anotherUser")
        void getAccount_Forbidden() throws Exception {
            mockMvc.perform(get(BASE_URL + "/user/{name}", "tester"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("#2 계좌 생성 API (/user/{name}/create)")
    class CreateAccountTest {

        @Test
        @DisplayName("2-1) 인증된 사용자 == {name}, 계좌생성 성공 시 200 OK")
        @WithMockUser(username = "tester")
        void createAccount_Success() throws Exception {
            String targetUser = "tester";

            // given
            VirtualAccountDtos newAccountDto = new VirtualAccountDtos(true);
            given(accountQueryService.createAccount(targetUser)).willReturn(newAccountDto);

            // when
            mockMvc.perform(post(BASE_URL + "/user/{name}/create", targetUser)
                            .with(request -> {
                                request.setRequestURI(BASE_URL + "/user/" + targetUser + "/create");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountExists").value(true));
        }


        @Test
        @DisplayName("2-2) 계좌생성 시 principal 이 null 이거나, 요청 유저 != principal => 403 Forbidden")
        @WithMockUser(username = "notOwner")
        void createAccount_Forbidden() throws Exception {
            mockMvc.perform(post(BASE_URL + "/user/{name}/create", "tester")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("2-3) 이미 계좌가 존재하는 등 IllegalStateException 발생 시 400 Bad Request")
        @WithMockUser(username = "tester")
        void createAccount_Conflict() throws Exception {
            given(accountQueryService.createAccount("tester"))
                    .willThrow(new IllegalStateException("이미 존재하는 계좌입니다."));

            mockMvc.perform(post(BASE_URL + "/user/{name}/create", "tester")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("#3 잔액 충전 API (accountId 기반) => POST /{accountId}")
    class ChargeAccountByIdTest {

        @Test
        @DisplayName("3-1) JWT 토큰(ADMIN 권한)이면 충전 성공 200")
        @WithMockUser(username = "tester", roles = {"ADMIN"})
        void chargeAccount_Success_WithJwt() throws Exception {
            int accountId = 123;
            AccountPaymentResponse mockResponse = new AccountPaymentResponse(
                    1, accountId,
                    BigDecimal.valueOf(1000), BigDecimal.valueOf(500),
                    BigDecimal.valueOf(1500), LocalDateTime.now()
            );
            given(accountChargeService.chargeByAccountId(ArgumentMatchers.eq(accountId),
                    ArgumentMatchers.any(AccountRequest.class)))
                    .willReturn(mockResponse);

            AccountRequest requestBody = new AccountRequest(BigDecimal.valueOf(500));
            String requestJson = objectMapper.writeValueAsString(requestBody);

            mockMvc.perform(post(BASE_URL + "/" + accountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.afterMoney").value(1500));
        }

        @Test
        @DisplayName("3-2) 토큰 없이 요청하면 401 or 403")
        void chargeAccount_Unauthorized() throws Exception {
            int accountId = 123;
            AccountRequest requestBody = new AccountRequest(BigDecimal.valueOf(500));
            String requestJson = objectMapper.writeValueAsString(requestBody);

            mockMvc.perform(post(BASE_URL + "/" + accountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("#4 잔액 충전 API (username 기반) => POST /api/account")
    class ChargeOwnAccountTest {

        @Test
        @DisplayName("4-1) 현재 로그인한 사용자가 자신의 계좌 잔액을 충전, 200 OK")
        @WithMockUser(username = "tester")
        void chargeOwnAccount_Success() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(3000);
            AccountRequest request = new AccountRequest(amount);

            // 가정 응답
            AccountPaymentResponse mockResponse = new AccountPaymentResponse(
                    1, 999, BigDecimal.valueOf(1000), amount,
                    BigDecimal.valueOf(4000), LocalDateTime.now()
            );
            given(accountChargeService.chargeByUsername("tester", request)).willReturn(mockResponse);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 충전에 성공했습니다."))
                    .andExpect(jsonPath("$.data.afterMoney").value(4000));
        }
    }

    @Nested
    @DisplayName("#5 계좌 조회 API => GET /{accountId}")
    class GetAccountByIdTest {

        @Test
        @DisplayName("5-1) 관리자인 경우 해당 계좌 조회 가능, 200 OK")
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void getAccount_Admin() throws Exception {
            int accountId = 111;
            AccountResponse mockResponse = new AccountResponse(accountId, BigDecimal.valueOf(2000), LocalDateTime.now());
            given(accountQueryService.getAccountInfo(accountId)).willReturn(mockResponse);

            mockMvc.perform(get(BASE_URL + "/" + accountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.accountId").value(accountId));
        }

        @Test
        @DisplayName("5-2) 소유자가 아닌 사용자는 403 Forbidden")
        @WithMockUser(username = "notOwner", roles = {"SPONSOR"})
        void getAccount_NotOwner() throws Exception {
            mockMvc.perform(get(BASE_URL + "/999"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("#6 계좌 결제 API (accountId 기반) => POST /{accountId}/payment")
    class PaymentByAccountIdTest {

        @Test
        @DisplayName("6-1) ADMIN 또는 OWNER 권한이 있으면 결제 성공 200")
        @WithMockUser(username = "tester", roles = {"ADMIN"})
        void payment_Success() throws Exception {
            int accountId = 123;
            BigDecimal paymentAmount = BigDecimal.valueOf(500);
            AccountPaymentRequest requestBody = new AccountPaymentRequest(1, paymentAmount);

            AccountPaymentResponse mockResponse = new AccountPaymentResponse(
                    1, accountId, BigDecimal.valueOf(1000), paymentAmount,
                    BigDecimal.valueOf(500), LocalDateTime.now()
            );
            given(accountPaymentService.paymentByAccountId(accountId, requestBody, "tester"))
                    .willReturn(mockResponse);

            mockMvc.perform(post(BASE_URL + "/" + accountId + "/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.accountId").value(accountId));
        }

        @Test
        @DisplayName("6-2) 권한 없는 사용자 => 403 Forbidden")
        @WithMockUser(username = "tester")
        void payment_Forbidden() throws Exception {
            int accountId = 123;
            AccountPaymentRequest requestBody = new AccountPaymentRequest(1, BigDecimal.valueOf(500));

            mockMvc.perform(post(BASE_URL + "/" + accountId + "/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("#7 계좌 결제 API (username 기반) => POST /api/account/payment")
    class PaymentByUsernameTest {

        @Test
        @DisplayName("7-1) 로그인 사용자 == OWNER, 결제 성공 200")
        @WithMockUser(username = "tester", roles = {"SPONSOR"})
        void payment_Success() throws Exception {
            BigDecimal paymentAmount = BigDecimal.valueOf(1000);
            AccountPaymentRequest requestBody = new AccountPaymentRequest(1, paymentAmount);

            AccountPaymentResponse mockResponse = new AccountPaymentResponse(
                    10, 555, BigDecimal.valueOf(2000), paymentAmount,
                    BigDecimal.valueOf(1000), LocalDateTime.now()
            );
            given(accountPaymentService.paymentByUsername(requestBody, "tester"))
                    .willReturn(mockResponse);

            mockMvc.perform(post(BASE_URL + "/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.accountId").value(555));
        }
    }

    @Nested
    @DisplayName("#8 계좌 환불 API => POST /{accountId}/transactions/{transactionId}/refund")
    class RefundTest {

        @Test
        @DisplayName("8-1) ADMIN 또는 OWNER 권한이 있으면 환불 성공 200")
        @WithMockUser(username = "tester", roles = {"ADMIN"})
        void refund_Success() throws Exception {
            int accountId = 123;
            int transactionId = 10;
            AccountRefundResponse mockResponse = new AccountRefundResponse(
                    99, transactionId, accountId, BigDecimal.valueOf(5000),
                    BigDecimal.valueOf(500), BigDecimal.valueOf(4500), LocalDateTime.now()
            );
            given(accountRefundService.refund(accountId, transactionId))
                    .willReturn(mockResponse);

            mockMvc.perform(post(BASE_URL + "/" + accountId + "/transactions/" + transactionId + "/refund")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("거래 환불에 성공했습니다."));
        }

        @Test
        @DisplayName("8-2) 권한 없는 사용자 => 403 Forbidden")
        @WithMockUser(username = "tester", roles = {"SPONSOR"})
        void refund_Forbidden() throws Exception {
            mockMvc.perform(post(BASE_URL + "/999/transactions/111/refund"))
                    .andExpect(status().isForbidden());
        }
    }
}