package funding.startreum

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MainController {

    @GetMapping("/")
    fun showMainPage(): String {
        return "main"
    }
    
}
