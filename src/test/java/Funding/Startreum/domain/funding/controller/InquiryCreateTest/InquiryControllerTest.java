package Funding.Startreum.domain.funding.controller.InquiryCreateTest;



import funding.startreum.common.util.JwtUtil;
import funding.startreum.domain.inquiry.Inquiry;
import funding.startreum.domain.inquiry.InquiryRequest;
import funding.startreum.domain.inquiry.InquiryResponse;
import funding.startreum.domain.inquiry.InquiryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private InquiryService inquiryService;

    private String token;
    private static final String TEST_NAME = "testSponsor";
    private static final String TEST_EMAIL = "sponsor@test.com";
    private static final String TEST_ROLE = "SPONSOR";

    @BeforeEach
    void setUp() {
        token = jwtUtil.generateAccessToken(TEST_NAME, TEST_EMAIL, TEST_ROLE);
        System.out.println("token 문자열:" + token);
    }

    @Test
    @DisplayName("문의 생성 성공 테스트")
    void createInquiry_Success() throws Exception {

        String email = "test@test.com";
        InquiryRequest request = new InquiryRequest(
                "테스트 문의 제목",
                "테스트 문의 내용입니다."
        );

        var responseData = new InquiryResponse.Data(
                1,
                request.title(),
                request.content(),
                Inquiry.Status.PENDING,
                LocalDateTime.of(2025, 1, 24, 12, 0)
        );

        InquiryResponse response = InquiryResponse.success(responseData);
        when(inquiryService.createInquiry(anyString(), eq(request))).thenReturn(response);

        ResultActions result = mockMvc.perform(post("/api/sponsor/inquiries")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("문의 생성 성공."))
                .andExpect(jsonPath("$.data.inquiryId").value(1))
                .andExpect(jsonPath("$.data.title").value(request.title()))
                .andExpect(jsonPath("$.data.content").value(request.content()))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andDo(print());
    }
}

