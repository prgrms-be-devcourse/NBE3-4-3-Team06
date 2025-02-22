package Funding.Startreum.domain.users;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //System.out.println("🔍 데이터베이스에서 사용자 조회 시도: " + username);

        return userRepository.findByName(username)
                .map(user -> {
                   // System.out.println("✅ 사용자 찾음: " + user.getName() + " (Role: " + user.getRole() + ")");

                    // ✅ ROLE_ 접두어 추가 (Spring Security의 권한과 맞추기)
                    String role = "ROLE_" + user.getRole().name();

                    return User.withUsername(user.getName())
                            .password(user.getPassword())
                            .authorities(role)  // ✅ Spring Security에서 요구하는 역할 적용
                            .build();
                })
                .orElseThrow(() -> {
                   // System.out.println("❌ 사용자 정보 조회 실패 (DB에 존재하지 않음): " + username);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                });
    }
}