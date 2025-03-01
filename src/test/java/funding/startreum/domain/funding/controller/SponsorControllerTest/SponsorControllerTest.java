package funding.startreum.domain.funding.controller.SponsorControllerTest;

import funding.startreum.common.util.JwtUtil;
import funding.startreum.domain.sponsor.SponListResponse;
import funding.startreum.domain.sponsor.SponsorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SponsorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private SponsorService sponsorService;

    private String token;
    private static final String TEST_NAME = "testSponsor";
    private static final String TEST_EMAIL = "sponsor@test.com";
    private static final String TEST_ROLE = "SPONSOR";

    @BeforeEach
    void setUp() {
        token = jwtUtil.generateAccessToken(TEST_NAME, TEST_EMAIL, TEST_ROLE);
        System.out.println(token);
    }

    @Test
    @DisplayName("후원 목록 조회 성공 테스트")
    void getFundingListSuccess() throws Exception {
        var funding = new SponListResponse.Funding(
                1,
                1,
                "testProject",
                1,
                10000.0,
                LocalDateTime.now()
        );
        var pagination = new SponListResponse.Pagination(1, 1, 5);
        var response = SponListResponse.success(List.of(funding), pagination);

        given(sponsorService.getFundingList(eq(TEST_EMAIL), any(Pageable.class))).willReturn(response);

        ResultActions result = mockMvc.perform(
                get("/api/sponsor/sponsoredList")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.fundings[0].projectTitle").value("testProject"))
                .andExpect(jsonPath("$.data.fundings[0].amount").value(10000.0))
                .andExpect(jsonPath("$.data.pagination.total").value(1));
    }
}
