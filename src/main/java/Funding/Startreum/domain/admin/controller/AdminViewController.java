package funding.startreum.domain.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    /**
     * 🔹 관리자 메인 페이지
     */
    @GetMapping
    public String showAdminPage() {
        return "admin/admin_main";  // admin_main.html 반환
    }

    /**
     * 🔹 프로젝트 관리 페이지
     */
    @GetMapping("/project")
    public String showProjectManagementPage() {
        return "admin/admin_project"; // admin_project.html 반환
    }
}
