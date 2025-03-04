package funding.startreum.domain.project.controller

import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.project.dto.ProjectCreateRequestDto
import funding.startreum.domain.project.dto.ProjectCreateResponseDto
import funding.startreum.domain.project.service.ProjectService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI


@RestController
@RequestMapping("/api/beneficiary")
class NewProjectController(
    private val projectService: ProjectService,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/create/projects")
    fun createProject(
        @RequestHeader("Authorization") token: String,
        @RequestBody projectRequest: ProjectCreateRequestDto
    ): ResponseEntity<ProjectCreateResponseDto> {

        // 1. "Bearer " 문자열 제거 후 JWT에서 email 추출
        val email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""))

        // 2. 프로젝트 생성 서비스 호출
        val response = projectService.createProject(projectRequest, email)

        return ResponseEntity.created(URI.create("/api/create/projects/${response.projectId}")).body(response)
    }
}