package funding.startreum.domain.users;

import funding.startreum.common.util.JwtUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UserViewController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserViewController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    // 회원가입 페이지 호출
    @GetMapping("api/users/signup")
    public String showSignupForm(Model model) {
        // 필요 시 모델에 데이터 추가 가능
        return "users/signup"; // templates/users/signup.html
    }

    // 로그인 페이지 호출
    @GetMapping("api/users/login")
    public String showloginForm(Model model) {
        // 필요 시 모델에 데이터 추가 가능
        return "users/login";
    }

    // 🔹 마이페이지(프로필) 호출 (뷰만 반환)
    @GetMapping("/profile/{name}")
    public String showProfilePage(@PathVariable String name, Model model) {
        model.addAttribute("username", name);
        return "users/profile";
    }

    // 🔹 팝업창(이메일 수정 페이지) 뷰 반환
    @GetMapping("/profile/modify/{name}")
    public String showModifyPage(@PathVariable String name, Model model) {
        model.addAttribute("username", name);
        return "users/modify";  // templates/users/modify.html
    }

}