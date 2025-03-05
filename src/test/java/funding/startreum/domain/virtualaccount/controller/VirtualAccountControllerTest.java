package funding.startreum.domain.virtualaccount.controller;

import funding.startreum.common.util.JwtUtil;
import funding.startreum.domain.project.repository.ProjectRepository;
import funding.startreum.domain.users.service.CustomUserDetailsService;
import funding.startreum.domain.users.service.UserService;
import funding.startreum.domain.virtualaccount.dto.request.AccountPaymentRequest;
import funding.startreum.domain.virtualaccount.dto.request.AccountRequest;
import funding.startreum.domain.virtualaccount.dto.response.AccountPaymentResponse;
import funding.startreum.domain.virtualaccount.dto.response.AccountRefundResponse;
import funding.startreum.domain.virtualaccount.dto.response.AccountResponse;
import funding.startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import funding.startreum.domain.virtualaccount.service.AccountChargeService;
import funding.startreum.domain.virtualaccount.service.AccountPaymentService;
import funding.startreum.domain.virtualaccount.service.AccountQueryService;
import funding.startreum.domain.virtualaccount.service.AccountRefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static funding.startreum.util.TokenUtil.createUserToken;
import static funding.startreum.util.utilMethod.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @see VirtualAccountController
 */
@SpringBootTest
@AutoConfigureMockMvc
class VirtualAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private VirtualAccountRepository virtualAccountRepository;
    @MockitoBean
    private AccountQueryService accountQueryService;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;
    @MockitoBean
    private ProjectRepository projectRepository;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AccountChargeService accountChargeService;
    @MockitoBean
    private AccountPaymentService accountPaymentService;
    @MockitoBean
    private AccountRefundService accountRefundService;

    private static final String BASE_URL = "/api/account";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int ACCOUNT_ID = 100;
    private static final int NON_EXISTENT_ACCOUNT_ID = 500;
    private static final int PROJECT_ID = 1;
    private static final int TRANSACTION_ID = 200;
    private static final String OWNER = "owner";
    private static final String ADMIN = "admin";
    private static final String OTHER = "other";

    private String adminToken;
    private String ownerToken;
    private String notOwnerToken;

    @BeforeEach
    void setUp() {
        createVirtualAccount(virtualAccountRepository, ACCOUNT_ID, OWNER);
        createVirtualProject(projectRepository, PROJECT_ID, OWNER);

        createVirtualDetails(userDetailsService, ADMIN, "ADMIN");
        createVirtualDetails(userDetailsService, OWNER, "SPONSOR");
        createVirtualDetails(userDetailsService, OTHER, "SPONSOR");

        setVirtualUser(userService, 1, ADMIN, funding.startreum.domain.users.entity.User.Role.ADMIN);
        setVirtualUser(userService, 2, OWNER, funding.startreum.domain.users.entity.User.Role.SPONSOR);
        setVirtualUser(userService, 3, OTHER, funding.startreum.domain.users.entity.User.Role.SPONSOR);

        adminToken = createUserToken(jwtUtil, ADMIN, "admin@test.com", "ADMIN");
        ownerToken = createUserToken(jwtUtil, OWNER, "owner@test.com", "SPONSOR");
        notOwnerToken = createUserToken(jwtUtil, OTHER, "other@test.com", "SPONSOR");
    }

    // 헬퍼 메서드: Bearer 토큰 생성
    private String bearerToken(String token) {
        return BEARER_PREFIX + token;
    }

    // 헬퍼 메서드: GET 요청 수행
    private ResultActions performGet(String url, String token) throws Exception {
        return mockMvc.perform(get(url)
                .header(AUTHORIZATION_HEADER, bearerToken(token))
                .contentType(MediaType.APPLICATION_JSON));
    }

    // 헬퍼 메서드: POST 요청 수행
    private ResultActions performPost(String url, String token, String content) throws Exception {
        return mockMvc.perform(post(url)
                .header(AUTHORIZATION_HEADER, bearerToken(token))
                .content(content)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("계좌 조회 API 테스트 (accountId 기반)")
    class AccountInquiryTests {

        @Test
        @DisplayName("[조회 200] OWNER가 자신의 계좌를 조회할 경우")
        void getOwnAccount() throws Exception {
            var response = new AccountResponse(ACCOUNT_ID, BigDecimal.valueOf(5000), LocalDateTime.now());
            given(accountQueryService.getAccountInfo(OWNER)).willReturn(response);

            performGet(BASE_URL, ownerToken)
                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.status").value("success"))
//                    .andExpect(jsonPath("$.message").value("계좌 내역 조회에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID));
        }

    }

    @Nested
    @DisplayName("계좌 충전 API 테스트 (username 기반)")
    class AccountChargeTests {

        @Test
        @DisplayName("[충전 200] OWNER가 자신의 계좌를 충전할 경우")
        void chargeOwnAccountByUserName() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(1000);
            AccountPaymentResponse response = new AccountPaymentResponse(
                    0, ACCOUNT_ID, BigDecimal.ZERO, amount, amount, LocalDateTime.now()
            );
            given(accountChargeService.chargeByUsername(eq(OWNER), any(AccountRequest.class))).willReturn(response);

            performPost(BASE_URL, ownerToken, "{ \"amount\": 1000 }")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 충전에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.data.afterMoney").value(1000));
        }
    }

    @Nested
    @DisplayName("계좌 충전 API 테스트 (accountId 기반)")
    class AccountChargeByAccountIdTests {

        @Test
        @DisplayName("[충전 200] OWNER가 자신의 계좌를 계좌ID 기반으로 충전할 경우")
        void chargeAccountByAccountIdSuccess() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(1500);
            AccountPaymentResponse response = new AccountPaymentResponse(
                    0, ACCOUNT_ID, BigDecimal.valueOf(5000), amount, BigDecimal.valueOf(6500), LocalDateTime.now()
            );
            given(accountChargeService.chargeByAccountId(eq(ACCOUNT_ID), any(AccountRequest.class)))
                    .willReturn(response);

            performPost(BASE_URL + "/" + ACCOUNT_ID, ownerToken, "{ \"amount\": 1500 }")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 충전에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.data.afterMoney").value(6500));
        }

        @Test
        @DisplayName("[충전 403] NOT OWNER가 계좌ID 기반으로 충전 요청할 경우")
        void chargeAccountByAccountIdNotOwner() throws Exception {
            performPost(BASE_URL + "/" + ACCOUNT_ID, notOwnerToken, "{ \"amount\": 1500 }")
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("계좌 결제 API 테스트 (accountId 기반)")
    class AccountPaymentTests {

        @Test
        @DisplayName("[결제 200] OWNER가 자신의 계좌로 결제할 경우")
        void paymentByAccountId() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(1000);
            AccountPaymentResponse response = new AccountPaymentResponse(
                    0, ACCOUNT_ID, BigDecimal.ZERO, amount, amount, LocalDateTime.now()
            );
            given(accountPaymentService.paymentByAccountId(eq(ACCOUNT_ID), any(AccountPaymentRequest.class), eq(OWNER)))
                    .willReturn(response);

            performPost(BASE_URL + "/" + ACCOUNT_ID + "/payment", ownerToken, "{ \"projectId\": 1, \"amount\": 1000 }")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("결제에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.data.afterMoney").value(1000));
        }

        @Test
        @DisplayName("[결제 403] NOT OWNER가 다른 계좌로 결제할 경우")
        void paymentNotOwner() throws Exception {
            performPost(BASE_URL + "/" + ACCOUNT_ID + "/payment", notOwnerToken, "{ \"projectId\": 1, \"amount\": 1000 }")
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("계좌 결제 API 테스트 (username 기반)")
    class AccountPaymentByUserNameTests {

        @Test
        @DisplayName("[결제 200] 로그인한 사용자가 자신의 이름 기반으로 결제할 경우")
        void paymentByUserName() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(1000);
            AccountPaymentResponse response = new AccountPaymentResponse(
                    0, ACCOUNT_ID, BigDecimal.ZERO, amount, amount, LocalDateTime.now()
            );
            given(accountPaymentService.paymentByUsername(any(AccountPaymentRequest.class), eq(OWNER)))
                    .willReturn(response);

            performPost(BASE_URL + "/payment", ownerToken, "{ \"projectId\": 1, \"amount\": 1000 }")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("결제에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.data.afterMoney").value(1000));
        }
    }

    @Nested
    @DisplayName("계좌 환불 API 테스트 (accountId 기반)")
    class AccountRefundTests {

        @Test
        @DisplayName("[환불 200] OWNER가 자신의 거래를 환불할 경우 (accountId 기반)")
        void refundPayment() throws Exception {
            BigDecimal refundAmount = BigDecimal.valueOf(1000);
            AccountRefundResponse refundResponse = new AccountRefundResponse(
                    TRANSACTION_ID, TRANSACTION_ID, ACCOUNT_ID, BigDecimal.ZERO, refundAmount, refundAmount, LocalDateTime.now()
            );
            given(accountRefundService.refund(eq(ACCOUNT_ID), eq(TRANSACTION_ID))).willReturn(refundResponse);

            performPost(BASE_URL + "/" + ACCOUNT_ID + "/transactions/" + TRANSACTION_ID + "/refund", ownerToken, "")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("거래 환불에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.data.refundTransactionId").value(TRANSACTION_ID));
        }

        @Test
        @DisplayName("[환불 403] NOT OWNER가 거래 환불을 요청할 경우 (accountId 기반)")
        void refundNotOwner() throws Exception {
            performPost(BASE_URL + "/" + ACCOUNT_ID + "/transactions/" + TRANSACTION_ID + "/refund", notOwnerToken, "")
                    .andExpect(status().isForbidden());
        }
    }
}
