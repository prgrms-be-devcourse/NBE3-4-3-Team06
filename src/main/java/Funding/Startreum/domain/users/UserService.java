package funding.startreum.domain.users;

import funding.startreum.common.util.JwtUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    //  Refresh Token 저장소 (임시 Map 사용 → DB 또는 Redis로 변경 가능)
    private final Map<String, String> refreshTokenStorage = new HashMap<>();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // 허용된 역할 목록 (처음에는 ADMIN 포함)
    private final Set<User.Role> allowedRoles = Set.of(User.Role.BENEFICIARY, User.Role.SPONSOR, User.Role.ADMIN);
    /*
    // ADMIN 역할을 제거한 허용된 역할 목록
    private final Set<User.Role> allowedRoles = Set.of(User.Role.BENEFICIARY, User.Role.SPONSOR);
    */

    //  회원가입
    public void registerUser(SignupRequest signupRequest) {
        // 입력 값 검증
        validateSignupRequest(signupRequest);

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(signupRequest.password());

        // 사용자 엔티티 생성
        User user = new User();
        user.setName(signupRequest.name());
        user.setEmail(signupRequest.email());
        user.setPassword(encryptedPassword);
        user.setRole(signupRequest.role());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 데이터베이스에 저장
        userRepository.save(user);
    }

    //  입력 값 검증
    private void validateSignupRequest(SignupRequest signupRequest) {
        // 이메일 중복 확인
        if (isEmailDuplicate(signupRequest.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 이름(ID) 중복 확인
        if (isNameDuplicate(signupRequest.name())) {
            throw new IllegalArgumentException("이미 사용 중인 이름(ID)입니다.");
        }

        // 역할 검증
        if (!allowedRoles.contains(signupRequest.role())) {
            throw new IllegalArgumentException("허용되지 않은 역할(Role)입니다.");
        }
    }

    //  이름(ID) 중복 확인
    public boolean isNameDuplicate(String name) {
        return userRepository.findByName(name).isPresent();
    }

    //  이메일 중복 확인
    public boolean isEmailDuplicate(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // 사용자 인증 (name 기반)
    public UserResponse authenticateUser(String name, String password) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 인증 성공: 응답 DTO 생성
        return new UserResponse(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // Refresh Token 저장 (name을 기반으로 저장)
    public void saveRefreshToken(String name, String refreshToken) {
        refreshTokenStorage.put(name, refreshToken);
    }

    // 저장된 Refresh Token 조회
    public String getRefreshToken(String name) {
        return refreshTokenStorage.get(name);
    }

    // Refresh Token 검증
    public boolean isRefreshTokenValid(String name, String refreshToken) {
        return refreshToken.equals(refreshTokenStorage.get(name));
    }

    // name을 기반으로 사용자 정보 조회 (Refresh 토큰 재발급 시 사용)
    public User getUserByName(String name) {
        return userRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 사용자를 찾을 수 없습니다."));
    }

    // 사용자 마이페이지 조회
    public UserResponse getUserProfile(String name, String loggedInUsername) {
        // System.out.println("🔍 프로필 조회: 요청한 사용자 = " + name + ", 로그인한 사용자 = " + loggedInUsername);

        User loggedInUser = userRepository.findByName(loggedInUsername)
                .orElseThrow(() -> {
                    // System.out.println("❌ 현재 로그인한 사용자 정보 없음");
                    return new IllegalArgumentException("현재 로그인한 사용자를 찾을 수 없습니다.");
                });

        User targetUser = userRepository.findByName(name)
                .orElseThrow(() -> {
                    //System.out.println("❌ 요청된 사용자 정보 없음: " + name);
                    return new IllegalArgumentException("해당 사용자를 찾을 수 없습니다.");
                });

        // 🔹 본인 또는 ADMIN 역할만 프로필 조회 가능
        if (!loggedInUser.getName().equalsIgnoreCase(targetUser.getName())
                && !loggedInUser.getRole().name().equalsIgnoreCase("ADMIN")) {
            // System.out.println("⛔ 권한 없음: " + loggedInUser.getName() + "이(가) " + targetUser.getName() + "의 정보를 조회하려 함");
            throw new AccessDeniedException("권한이 없습니다.");
        }

        System.out.println("✅ 프로필 조회 성공: " + targetUser.getName());

        return new UserResponse(
                targetUser.getName(),
                targetUser.getEmail(),
                targetUser.getRole(),
                targetUser.getCreatedAt(),
                targetUser.getUpdatedAt()
        );
    }


    // ✅ 이메일 업데이트 (PUT 요청)
    public void updateUserEmail(String name, String newEmail) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 이메일 중복 확인
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 이메일 업데이트
        user.setEmail(newEmail);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }


}