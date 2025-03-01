package funding.startreum.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

// 수정할때 USER Mock으로 추가 필요
@SpringBootTest
@AutoConfigureMockMvc
class RewardRestControllerTest {
/*
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private RewardService rewardService;

    // JWT 토큰
    private String adminToken;                  // 관리자 토큰
    private String sponsorToken;                // 후원자 토큰
    private String beneficiaryToken;            // 수혜자 토큰

    // 테스트용 상수 값
    private final String ADMIN = "admin";
    private final String SPONSER = "sponsor";
    private final String BENEFICIARY = "beneficiary";
    private final Integer REWARD_ID = 1;
    private final Integer PROJECT_ID = 1;
    private final String DESCRIPTION = "이 리워드는 특별한 혜택을 제공합니다.";
    private final String UPDATE_DESCRIPTION = "수정된 리워드 입니다.";
    private final BigDecimal AMOUNT = new BigDecimal("10000");
    private final BigDecimal UPDATE_AMOUNT = new BigDecimal("20000");

    private static final String CREATE_REQUEST_JSON = """
            {
                "projectId": "1",
                "description": "이 리워드는 특별한 혜택을 제공합니다.",
                "amount": "100000"
            }
            """;

    private static final String UPDATE_REQUEST_JSON = """
            {
                "description": "수정된 리워드 입니다.",
                "amount": "1000"
            }
            """;

    private ResultActions performPost(String url, String token, String requestJson) throws Exception {
        return mockMvc.perform(
                post(url)
                        .header("Authorization", "Bearer " + token)
                        .content(requestJson.stripIndent())
                        .contentType(MediaType.APPLICATION_JSON)
        );
    }


    *//**
     * 테스트 시작 전 계정 및 프로젝트를 설정합니다.
     *//*
    @BeforeEach
    void setUp() {
        // 가상의 프로젝트 생성 (프로젝트 ID: 1, 소유자: SPONSOR)
        createVirtualProject(PROJECT_ID, SPONSER);

        // JWT 토큰 생성
        adminToken = jwtUtil.generateAccessToken(ADMIN, "admin@test.com", ADMIN);
        sponsorToken = jwtUtil.generateAccessToken(SPONSER, "sponsor@test.com", SPONSER);
        beneficiaryToken = jwtUtil.generateAccessToken(BENEFICIARY, "beneficiary@test.com", BENEFICIARY);
    }

    private void createVirtualProject(int projectId, String projectOwner) {
        funding.startreum.domain.users.User user = new funding.startreum.domain.users.User();
        user.setName(projectOwner);
        Project project = new Project();
        project.setCreator(user);

        given(projectRepository.findById(projectId))
                .willReturn(Optional.of(project));
    }

    // ========================================
    // 생성 관련 테스트
    // ========================================

    @Test
    @DisplayName("[생성 201] 관리자 계정으로 리워드 생성 시")
    void createTest1() throws Exception {

        RewardResponse response = new RewardResponse(
                REWARD_ID,
                PROJECT_ID,
                DESCRIPTION,
                AMOUNT,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(rewardService.generateNewRewardResponse(any(RewardRequest.class)))
                .willReturn(response);

        ResultActions result = performPost("/api/reward", adminToken, CREATE_REQUEST_JSON);

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("리워드 생성에 성공했습니다."))
                .andExpect(jsonPath("$.data.rewardId").value(REWARD_ID))
                .andExpect(jsonPath("$.data.projectId").value(PROJECT_ID))
                .andExpect(jsonPath("$.data.description").value(DESCRIPTION))
                .andExpect(jsonPath("$.data.amount").value(AMOUNT));
    }

    @Test
    @DisplayName("[생성 201] 수혜자 계정으로 리워드 생성 시")
    void createTest2() throws Exception {

        RewardResponse response = new RewardResponse(
                REWARD_ID,
                PROJECT_ID,
                DESCRIPTION,
                AMOUNT,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(rewardService.generateNewRewardResponse(any(RewardRequest.class)))
                .willReturn(response);

        ResultActions result = performPost("/api/reward", beneficiaryToken, CREATE_REQUEST_JSON);

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("리워드 생성에 성공했습니다."))
                .andExpect(jsonPath("$.data.rewardId").value(REWARD_ID))
                .andExpect(jsonPath("$.data.projectId").value(PROJECT_ID))
                .andExpect(jsonPath("$.data.description").value(DESCRIPTION))
                .andExpect(jsonPath("$.data.amount").value(AMOUNT));
    }

    @Test
    @DisplayName("[생성 403] 후원자 계정으로 리워드 생성 시 (권한 부족)")
    void createTest3() throws Exception {
        RewardResponse response = new RewardResponse(REWARD_ID, PROJECT_ID, DESCRIPTION, AMOUNT, LocalDateTime.now(), LocalDateTime.now());

        given(rewardService.generateNewRewardResponse(any(RewardRequest.class)))
                .willReturn(response);

        ResultActions result = performPost("/api/reward", sponsorToken, CREATE_REQUEST_JSON);

        result.andExpect(status().isForbidden());
    }

    // ========================================
    // 조회 관련 테스트
    // ========================================


    @Test
    @DisplayName("[조회 200] 존재하는 리워드 검색")
    void getTest1() throws Exception {
        List<RewardResponse> response = List.of(
                new RewardResponse(
                        REWARD_ID,
                        PROJECT_ID,
                        DESCRIPTION,
                        AMOUNT,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        given(rewardService.generateRewardsResponse(PROJECT_ID))
                .willReturn(response);

        ResultActions result = mockMvc.perform(
                get("/api/reward/" + PROJECT_ID)
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("리워드 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].rewardId").value(REWARD_ID))
                .andExpect(jsonPath("$.data[0].projectId").value(PROJECT_ID))
                .andExpect(jsonPath("$.data[0].description").value(DESCRIPTION))
                .andExpect(jsonPath("$.data[0].amount").value(AMOUNT))
                .andExpect(jsonPath("$.data[0].createdAt").exists())
                .andExpect(jsonPath("$.data[0].updatedAt").exists());
    }

    @Test
    @DisplayName("[조회 200] 비어있는 리워드 조회")
    void getReward_NoProject_ReturnsOk() throws Exception {
        List<RewardResponse> response = Collections.emptyList();

        given(rewardService.generateRewardsResponse(PROJECT_ID))
                .willReturn(response);

        ResultActions result = mockMvc.perform(
                get("/api/reward/" + PROJECT_ID)
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("리워드가 존재하지 않습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }*/

//      TODO 수정필요
//    @Test
//    @DisplayName("[수정 200] 수혜자 계정으로 리워드 수정")
//    void updateReward_ReturnsOk() throws Exception {
//        LocalDateTime now = LocalDateTime.now();
//        RewardResponse expectedResponse = new RewardResponse(
//                REWARD_ID,
//                PROJECT_ID,
//                UPDATE_DESCRIPTION,
//                UPDATE_AMOUNT,
//                now,
//                now
//        );
//
//        given(rewardService.generateUpdatedRewardResponse(eq(REWARD_ID), any(RewardUpdateRequest.class)))
//                .willReturn(expectedResponse);
//
//        ResultActions result = mockMvc.perform(
//                put("/api/reward/" + REWARD_ID)
//                        .header("Authorization", "Bearer " + beneficiaryToken)
//                        .content(UPDATE_REQUEST_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//        );
//
//        result.andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.message").value("리워드 수정에 성공했습니다."))
//                .andExpect(jsonPath("$.data.rewardId").value(REWARD_ID))
//                .andExpect(jsonPath("$.data.projectId").value(PROJECT_ID))
//                .andExpect(jsonPath("$.data.description").value(UPDATE_DESCRIPTION))
//                .andExpect(jsonPath("$.data.amount").value(UPDATE_AMOUNT))
//                .andExpect(jsonPath("$.data.createdAt").exists())
//                .andExpect(jsonPath("$.data.updatedAt").exists());
//    }

//    @Test
//    @DisplayName("리워드 수정 실패 - description 누락")
//    void updateReward_MissingDescription_ReturnsBadRequest() throws Exception {
//        int rewardId = 10;
//
//        ResultActions result = mockMvc.perform(
//                put("/api/reward/" + rewardId)
//                        .header("Authorization", "Bearer " + token)
//                        .content("""
//                                {
//                                    "amount": "1000"
//                                }
//                                """.stripIndent())
//                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
//        ).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("리워드 수정 실패 - amount 누락")
//    void updateReward_MissingAmount_ReturnsBadRequest() throws Exception {
//        int rewardId = 10;
//
//        ResultActions result = mockMvc.perform(
//                put("/api/reward/" + rewardId)
//                        .header("Authorization", "Bearer " + token)
//                        .content("""
//                                {
//                                    "description": "수정된 리워드 입니다."
//                                }
//                                """.stripIndent())
//                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
//        ).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("리워드 수정 실패 - amount == 0")
//    void updateReward_AmountZero_ReturnsBadRequest() throws Exception {
//        int rewardId = 10;
//
//        ResultActions result = mockMvc.perform(
//                put("/api/reward/" + rewardId)
//                        .header("Authorization", "Bearer " + token)
//                        .content("""
//                                {
//                                    "description": "수정된 리워드 입니다.",
//                                    "amount": "0"
//                                }
//                                """.stripIndent())
//                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
//        ).andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void deleteReward() {
//    }
}