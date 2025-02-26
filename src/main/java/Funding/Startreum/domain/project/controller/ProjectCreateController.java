package funding.startreum.domain.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projects")
public class ProjectCreateController {

    @GetMapping("/new")
    public String showProjectCreationForm() {
        return "projects/new";  // Thymeleaf 템플릿 이름을 반환
    }
}
