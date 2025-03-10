package funding.startreum.domain.users.controller

import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.users.service.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class UserViewController(private val userService: UserService, private val jwtUtil: JwtUtil) {
    // 회원가입 페이지 호출
    @GetMapping("api/users/signup")
    fun showSignupForm(model: Model?): String {
        // 필요 시 모델에 데이터 추가 가능
        return "users/signup" // templates/users/signup.html
    }

    // 로그인 페이지 호출
    @GetMapping("api/users/login")
    fun showloginForm(model: Model?): String {
        // 필요 시 모델에 데이터 추가 가능
        return "users/login"
    }

    // 🔹 마이페이지(프로필) 호출 (뷰만 반환)
    @GetMapping("/profile/{name}")
    fun showProfilePage(@PathVariable name: String?, model: Model): String {
        model.addAttribute("username", name)
        return "users/profile"
    }

    // 🔹 팝업창(이메일 수정 페이지) 뷰 반환
    @GetMapping("/profile/modify/{name}")
    fun showModifyPage(@PathVariable name: String?, model: Model): String {
        model.addAttribute("username", name)
        return "users/modify" // templates/users/modify.html
    }
}