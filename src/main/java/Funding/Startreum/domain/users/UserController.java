package funding.startreum.domain.users;

import funding.startreum.common.util.JwtUtil;
import funding.startreum.domain.users.UserResponse;
import funding.startreum.domain.users.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MyFundingService myFundingService;
    private final MyProjectService myProjectService;

    public UserController(UserService userService, JwtUtil jwtUtil,
                          RefreshTokenRepository refreshTokenRepository,
                          MyFundingService myFundingService,
                          MyProjectService myProjectService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.myFundingService = myFundingService;
        this.myProjectService = myProjectService;
    }

    // ID 중복 확인
    @GetMapping("/check-name")
    public ResponseEntity<Boolean> checkNameDuplicate(@RequestParam String name) {
        boolean isDuplicate = userService.isNameDuplicate(name);
        return ResponseEntity.ok(isDuplicate);
    }

    // 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = userService.isEmailDuplicate(email);
        return ResponseEntity.ok(isDuplicate);
    }

    // 회원가입 처리 (REST API)
    @PostMapping("/registrar")
    public ResponseEntity<?> registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam User.Role role) {

        SignupRequest signupRequest = new SignupRequest(name, email, password, role);
        userService.registerUser(signupRequest);

        // 메인 페이지로 리디렉션
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    // 로그아웃
    @PostMapping("/api/users/logout")
    public ResponseEntity<Map<String, String>> logout() {
        //System.out.println("🔹 로그아웃 API 호출됨");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            //System.out.println("❌ 로그아웃 실패: 인증되지 않은 사용자");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "로그인 상태가 아닙니다."));
        }

        String username = authentication.getName();
        //System.out.println("✅ 로그아웃 성공 - 사용자: " + username);

        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("status", "success", "message", "로그아웃 성공"));
    }

    // ✅ 로그인 API (JWT 발급)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // System.out.println("로그인 요청 받음: name=" + loginRequest.name());

            UserResponse user = userService.authenticateUser(loginRequest.name(), loginRequest.password());

            // ✅ 기존 Refresh Token 삭제 후 새로 저장
            refreshTokenRepository.deleteByUsername(user.name());

            // ✅ 새 Refresh Token 생성
            String accessToken = jwtUtil.generateAccessToken(user.name(), user.email(), user.role().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.name());

            RefreshToken refreshTokenEntity = new RefreshToken();
            refreshTokenEntity.setToken(refreshToken);
            refreshTokenEntity.setUsername(user.name());
            refreshTokenEntity.setExpiryDate(new Date(System.currentTimeMillis() + jwtUtil.getRefreshTokenExpiration())); // 7일 후 만료

            refreshTokenRepository.save(refreshTokenEntity);

            // ✅ 응답 반환
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("userName", user.name());
            response.put("role", user.role().name());
            response.put("refreshTokenExpiry", refreshTokenEntity.getExpiryDate().getTime());

            //System.out.println("발급된 액세스 토큰: " + accessToken);
            //System.out.println("발급된 리프레시 토큰: " + refreshToken);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 내부 오류 발생"));
        }
    }


    // ✅ Access Token 갱신 (Refresh Token 사용)
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "유효하지 않은 Refresh Token"));
        }

        // ✅ Refresh Token에서 사용자 정보 추출
        String name = jwtUtil.getNameFromToken(refreshToken);

        // ✅ DB에서 저장된 Refresh Token 가져오기
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh Token이 존재하지 않습니다. 다시 로그인하세요."));

        if (!refreshToken.equals(storedToken.getToken())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Refresh Token 불일치"));
        }

        // ✅ Refresh Token 만료 여부 확인
        if (storedToken.getExpiryDate().before(new Date())) {
            refreshTokenRepository.deleteByToken(refreshToken); // ✅ 만료된 토큰 삭제
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Refresh Token이 만료되었습니다. 다시 로그인하세요."));
        }

        // ✅ 사용자 정보 조회
        User user = userService.getUserByName(name);

        // ✅ 새 Access Token 생성
        String newAccessToken = jwtUtil.generateAccessToken(user.getName(), user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    //  DTO 클래스 추가
    record LoginRequest(String name, String password) {
    }


    // 🔹 사용자 프로필 정보 조회 API (본인 또는 관리자만 조회 가능)
    @GetMapping("/profile/{name}")
    @PreAuthorize("#name == authentication.name or hasRole('ADMIN')")
    public ResponseEntity<?> getUserProfile(@PathVariable String name) {
        System.out.println("📌 API 요청됨: /api/users/profile/" + name);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            System.out.println("❌ SecurityContext에서 인증 정보가 없음");
        } else {
            System.out.println("✅ 인증된 사용자: " + authentication.getName());
            System.out.println("✅ 사용자 역할: " + authentication.getAuthorities());
        }

        String loggedInUsername = authentication.getName();
        System.out.println("✅ 인증된 사용자: " + loggedInUsername);

        User user = userService.getUserByName(name);
        if (user == null) {
            System.out.println("❌ DB에서 사용자 정보를 찾을 수 없음: " + name);
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "사용자를 찾을 수 없습니다."
            ));
        }

        UserResponse userProfile = new UserResponse(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );


        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", userProfile
        ));
    }

    // ✅ 이메일 수정 API (PUT)
    @PreAuthorize("#name == authentication.name or hasRole('ROLE_ADMIN')")
    @PutMapping("profile/modify/{name}")
    public ResponseEntity<?> updateEmail(@PathVariable String name, @Valid @RequestBody EmailUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.getName().equals(name)) {
            System.out.println("❌ 인증 실패 또는 다른 유저가 접근을 시도함: " + authentication.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "권한이 없습니다."));
        }

        userService.updateUserEmail(name, request.newEmail());
        return ResponseEntity.ok(Map.of("message", "이메일이 성공적으로 변경되었습니다."));
    }


    // 🔹 로그인한 사용자의 후원 내역 조회 API
    @GetMapping("/fundings/{username}")  // 🔹 경로 변수 추가
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFundingsByUsername(@PathVariable String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "로그인 상태가 아닙니다."));
        }

        // 현재 로그인한 사용자와 요청한 사용자 이름이 일치하는지 확인
        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "error", "message", "권한이 없습니다."));
        }

        // 사용자 정보 조회
        User user = userService.getUserByName(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "사용자 정보를 찾을 수 없습니다."));
        }

        // 후원 내역 조회
        List<MyFundingResponseDTO> fundings = myFundingService.getMyFundings(user.getUserId());

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", fundings
        ));
    }

    // 🔹 로그인한 수혜자의 프로젝트 목록 조회 API
    @GetMapping("/projects/{username}")
    @PreAuthorize("hasRole('ROLE_BENEFICIARY') and #username == authentication.name")
    public ResponseEntity<?> getMyProjects(@PathVariable String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "로그인 상태가 아닙니다."));
        }

        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "error", "message", "권한이 없습니다."));
        }

        // 사용자 이름을 기준으로 프로젝트 조회
        List<MyProjectDTO> projects = myProjectService.getProjectsByUser(username);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", projects
        ));
    }


}