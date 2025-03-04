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
import funding.startreum.domain.virtualaccount.exception.AccountNotFoundException;
import funding.startreum.domain.virtualaccount.exception.NotEnoughBalanceException;
import funding.startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import funding.startreum.domain.virtualaccount.service.VirtualAccountService;
import jakarta.persistence.EntityNotFoundException;
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
 * API, 인증, 인가, 응답 검증
 * 컨트롤러 호출만을 검증함
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
    private VirtualAccountService virtualAccountService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private UserService userService;

    // 상수 설정
    private static final String BASE_URL = "/api/account";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // 테스트에 사용할 값들
    private final int ACCOUNT_ID = 100;
    private final int NON_EXISTENT_ACCOUNT_ID = 500;
    private final int PROJECT_ID = 1;
    private final int TRANSACTION_ID = 200;
    private final String OWNER = "owner";
    private final String ADMIN = "admin";
    private final String OTHER = "other";

    // JWT 토큰
    private String adminToken;
    private String ownerToken;
    private String notOwnerToken;

    /**
     * 테스트 시작 전 계정 및 토큰을 설정합니다.
     */
    @BeforeEach
    void setUp() {
        // 가상 계좌 및 프로젝트 생성
        createVirtualAccount(virtualAccountRepository, ACCOUNT_ID, OWNER);
        createVirtualProject(projectRepository, PROJECT_ID, OWNER);

        // 가상 사용자 세부 정보 생성
        createVirtualDetails(userDetailsService, ADMIN, "ADMIN");
        createVirtualDetails(userDetailsService, OWNER, "SPONSOR");
        createVirtualDetails(userDetailsService, OTHER, "SPONSOR");

        // 가상 사용자 정보 설정
        setVirtualUser(userService, 1, ADMIN, funding.startreum.domain.users.entity.User.Role.ADMIN);
        setVirtualUser(userService, 2, OWNER, funding.startreum.domain.users.entity.User.Role.SPONSOR);
        setVirtualUser(userService, 3, OTHER, funding.startreum.domain.users.entity.User.Role.SPONSOR);

        // JWT 토큰 생성
        adminToken = createUserToken(jwtUtil, ADMIN, "admin@test.com", "ADMIN");
        ownerToken = createUserToken(jwtUtil, OWNER, "owner@test.com", "SPONSOR");
        notOwnerToken = createUserToken(jwtUtil, OTHER, "other@test.com", "SPONSOR");
    }


    // ===============================================================
    // 조회 관련 테스트
    // ===============================================================

    @Nested
    @DisplayName("계좌 조회 테스트")
    class AccountInquiryTests {

        @Test
        @DisplayName("[조회 200] ADMIN 계정으로 계좌 조회 시")
        void getAccountAsAdmin() throws Exception {
            AccountResponse response = new AccountResponse(ACCOUNT_ID, BigDecimal.ZERO, LocalDateTime.now());
            given(virtualAccountService.getAccountInfo(ACCOUNT_ID)).willReturn(response);

            mockMvc.perform(get(BASE_URL + "/{accountId}", ACCOUNT_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 내역 조회에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID));
        }

        @Test
        @DisplayName("[조회 200] OWNER 계정으로 계좌 조회 시")
        void getAccountAsOwner() throws Exception {
            AccountResponse response = new AccountResponse(ACCOUNT_ID, BigDecimal.ZERO, LocalDateTime.now());
            given(virtualAccountService.getAccountInfo(ACCOUNT_ID)).willReturn(response);

            mockMvc.perform(get(BASE_URL + "/{accountId}", ACCOUNT_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + ownerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 내역 조회에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID));
        }

        @Test
        @DisplayName("[조회 404] OWNER 계정으로 없는 계좌 조회 시")
        void getNonExistingAccount() throws Exception {
            given(virtualAccountService.getAccountInfo(NON_EXISTENT_ACCOUNT_ID))
                    .willThrow(new AccountNotFoundException(NON_EXISTENT_ACCOUNT_ID));

            mockMvc.perform(get(BASE_URL + "/{accountId}", NON_EXISTENT_ACCOUNT_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + ownerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("해당 계좌를 찾을 수 없습니다 : " + NON_EXISTENT_ACCOUNT_ID))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("[조회 403] NOT OWNER 계정으로 OWNER 계좌 조회 시")
        void getAccountNotOwner() throws Exception {
            AccountResponse response = new AccountResponse(ACCOUNT_ID, BigDecimal.ZERO, LocalDateTime.now());
            given(virtualAccountService.getAccountInfo(ACCOUNT_ID)).willReturn(response);

            mockMvc.perform(get(BASE_URL + "/{accountId}", ACCOUNT_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + notOwnerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("[조회 200] 로그인한 사용자가 자신의 계좌 조회 시")
        void getOwnAccount() throws Exception {
            AccountResponse response = new AccountResponse(ACCOUNT_ID, BigDecimal.valueOf(5000), LocalDateTime.now());
            given(virtualAccountService.getAccountInfo(OWNER)).willReturn(response);

            mockMvc.perform(get(BASE_URL)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + ownerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 내역 조회에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID));
        }

        @Test
        @DisplayName("[조회 403] 비로그인 사용자가 자신의 계좌 조회 시")
        void getOwnAccountUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

    }

    // ===============================================================
    // 충전 관련 테스트
    // ===============================================================

    @Nested
    @DisplayName("계좌 충전 테스트")
    class AccountChargeTests {
        @Test
        @DisplayName("[충전 200] ADMIN 계정으로 존재하는 계좌 충전 시")
        void chargeAccountAsAdmin() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(1000);
            AccountPaymentResponse response = new AccountPaymentResponse(0, ACCOUNT_ID, BigDecimal.ZERO, amount, amount, LocalDateTime.now());

            given(virtualAccountService.chargeByAccountId(eq(ACCOUNT_ID), any(AccountRequest.class))).willReturn(response);

            mockMvc.perform(post(BASE_URL + "/{accountId}", ACCOUNT_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + adminToken)
                            .content("{ \"amount\": 1000 }")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 충전에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.data.afterMoney").value(1000));
        }

        @Test
        @DisplayName("[충전 200] OWNER 계정으로 존재하는 계좌 충전 시")
        void chargeAccountAsOwner() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(1000);
            AccountPaymentResponse response = new AccountPaymentResponse(0, ACCOUNT_ID, BigDecimal.ZERO, amount, amount, LocalDateTime.now());

            given(virtualAccountService.chargeByAccountId(eq(ACCOUNT_ID), any(AccountRequest.class))).willReturn(response);

            mockMvc.perform(post(BASE_URL + "/{accountId}", ACCOUNT_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + ownerToken)
                            .content("{ \"amount\": 1000 }")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 충전에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.data.afterMoney").value(1000));
        }

        @Test
        @DisplayName("[충전 404] OWNER 계정으로 존재하지 않는 계좌 충전 시")
        void chargeNonExistingAccount() throws Exception {
            given(virtualAccountService.chargeByAccountId(eq(NON_EXISTENT_ACCOUNT_ID), any(AccountRequest.class)))
                    .willThrow(new AccountNotFoundException(NON_EXISTENT_ACCOUNT_ID));

            mockMvc.perform(post(BASE_URL + "/{accountId}", NON_EXISTENT_ACCOUNT_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + ownerToken)
                            .content("{ \"amount\": 1000 }")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("해당 계좌를 찾을 수 없습니다 : " + NON_EXISTENT_ACCOUNT_ID))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("[충전 403] NOT OWNER 계정으로 OWNER 계좌 충전 시")
        void chargeAccountNotOwner() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(1000);
            AccountPaymentResponse response = new AccountPaymentResponse(0, ACCOUNT_ID, BigDecimal.ZERO, amount, amount, LocalDateTime.now());

            given(virtualAccountService.chargeByAccountId(eq(ACCOUNT_ID), any(AccountRequest.class))).willReturn(response);

            mockMvc.perform(post(BASE_URL + "/{accountId}", ACCOUNT_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + notOwnerToken)
                            .content("{ \"amount\": 1000 }")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("[충전 200] 로그인한 사용자가 자신의 계좌 충전 시")
        void chargeOwnAccount() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(1000);
            AccountPaymentResponse response = new AccountPaymentResponse(0, ACCOUNT_ID, BigDecimal.ZERO, amount, amount, LocalDateTime.now());

            given(virtualAccountService.chargeByUsername(eq(OWNER), any(AccountRequest.class))).willReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + ownerToken)
                            .content("{ \"amount\": 1000 }")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 충전에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.data.afterMoney").value(1000));
        }
    }

    // ===============================================================
    // 결제 관련 테스트
    // ===============================================================

    @Nested
    @DisplayName("계좌 결제 테스트")
    class AccountPaymentTests {
        @Test
        @DisplayName("[결제 200] OWNER 계정으로 계좌 결제 시")
        void paymentByAccountId() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(1000);
            AccountPaymentResponse response = new AccountPaymentResponse(0, ACCOUNT_ID, BigDecimal.ZERO, amount, amount, LocalDateTime.now());

            given(virtualAccountService.payment(eq(ACCOUNT_ID), any(AccountPaymentRequest.class), eq(OWNER))).willReturn(response);

            mockMvc.perform(post(BASE_URL + "/{accountId}/payment", ACCOUNT_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + ownerToken)
                            .content("{ \"projectId\": 1, \"amount\": 1000 }")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("결제에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.data.afterMoney").value(1000));
        }

        @Test
        @DisplayName("[결제 200] 로그인한 사용자 계정으로 결제 시")
        void paymentByUserName() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(1000);
            AccountPaymentResponse response = new AccountPaymentResponse(0, ACCOUNT_ID, BigDecimal.ZERO, amount, amount, LocalDateTime.now());

            given(virtualAccountService.payment(any(AccountPaymentRequest.class), eq(OWNER))).willReturn(response);

            mockMvc.perform(post(BASE_URL + "/payment")
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + ownerToken)
                            .content("{ \"projectId\": 1, \"amount\": 1000 }")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("결제에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.data.afterMoney").value(1000));
        }

        @Test
        @DisplayName("[결제 403] NOT OWNER 계정으로 OWNER 계좌 결제 시")
        void paymentNotOwner() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{accountId}/payment", ACCOUNT_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + notOwnerToken)
                            .content("{ \"projectId\": 1, \"amount\": 1000 }")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("[결제 400] 잔액 부족 시 결제 실패")
        void paymentInsufficientBalance() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(1000);

            given(virtualAccountService.payment(eq(ACCOUNT_ID), any(AccountPaymentRequest.class), eq(OWNER)))
                    .willThrow(new NotEnoughBalanceException(BigDecimal.valueOf(500)));

            mockMvc.perform(post(BASE_URL + "/{accountId}/payment", ACCOUNT_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + ownerToken)
                            .content("{ \"projectId\": 1, \"amount\": 1000 }")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("잔액이 부족합니다. 현재 잔액:" + 500))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("[결제 404] 존재하지 않는 프로젝트로 결제 시 실패")
        void paymentNonExistingProject() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(1000);

            given(virtualAccountService.payment(eq(ACCOUNT_ID), any(AccountPaymentRequest.class), eq(OWNER)))
                    .willThrow(new EntityNotFoundException("프로젝트를 찾을 수 없습니다. 프로젝트 ID: 1"));

            mockMvc.perform(post(BASE_URL + "/{accountId}/payment", ACCOUNT_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + ownerToken)
                            .content("{ \"projectId\": 1, \"amount\": 1000 }")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("프로젝트를 찾을 수 없습니다. 프로젝트 ID: " + PROJECT_ID))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    // ===============================================================
    // 환불 관련 테스트
    // ===============================================================

    @Nested
    @DisplayName("계좌 환불 테스트")
    class AccountRefundTests {
        @Test
        @DisplayName("[환불 200] OWNER 계정으로 거래 환불 시")
        void refundPayment() throws Exception {
            BigDecimal refundAmount = BigDecimal.valueOf(1000);
            // 원 거래 ID와 환불 거래 ID는 테스트를 단순화하기 위해 같은 값(TRANSACTION_ID)으로 처리합니다.
            AccountRefundResponse refundResponse = new AccountRefundResponse(
                    TRANSACTION_ID, TRANSACTION_ID, ACCOUNT_ID, BigDecimal.ZERO, refundAmount, refundAmount, LocalDateTime.now()
            );

            given(virtualAccountService.refund(ACCOUNT_ID, TRANSACTION_ID)).willReturn(refundResponse);

            mockMvc.perform(post(BASE_URL + "/{accountId}/transactions/{transactionId}/refund", ACCOUNT_ID, TRANSACTION_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + ownerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("거래 환불에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.data.refundTransactionId").value(TRANSACTION_ID))
                    .andExpect(jsonPath("$.data.originalTransactionId").value(TRANSACTION_ID))
                    .andExpect(jsonPath("$.data.afterMoney").value(refundAmount.intValue()));
        }

        @Test
        @DisplayName("[환불 403] NOT OWNER 계정으로 거래 환불 시")
        void refundNotOwner() throws Exception {
            mockMvc.perform(post(BASE_URL + "/{accountId}/transactions/{transactionId}/refund", ACCOUNT_ID, TRANSACTION_ID)
                            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + notOwnerToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

}