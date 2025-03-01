package funding.startreum.domain.funding.controller.FundingAttendTest;

import funding.startreum.common.util.JwtUtil;
import funding.startreum.domain.funding.repository.FundingRepository;
import funding.startreum.domain.sponsor.FudingAttendResponse;
import funding.startreum.domain.sponsor.SponsorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FundingAttendTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private FundingRepository fundingRepository;

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
    @DisplayName("후원 참여 성공 테스트")
    void testSuccessfulFundingAttendResponse() {
        LocalDateTime now = LocalDateTime.now();
        FudingAttendResponse.FudingAttend fudingAttend = new FudingAttendResponse.FudingAttend(
                1, 2, "Test Project", 100.0, 3, now);
        FudingAttendResponse.Data data = new FudingAttendResponse.Data(fudingAttend);

        FudingAttendResponse response = FudingAttendResponse.success(data);

        assertNotNull(response);
        assertEquals("success", response.status());
        assertEquals(200, response.statusCode());
        assertEquals("후원 참여 성공.", response.message());
        assertNotNull(response.data());
        assertEquals(fudingAttend, response.data().FudingAttend());
        assertEquals(1, response.data().FudingAttend().fundingId());
        assertEquals(2, response.data().FudingAttend().projectId());
        assertEquals("Test Project", response.data().FudingAttend().projectTitle());
        assertEquals(100.0, response.data().FudingAttend().amount());
        assertEquals(3, response.data().FudingAttend().rewardId());
        assertEquals(now, response.data().FudingAttend().fundedAt());
    }
}
