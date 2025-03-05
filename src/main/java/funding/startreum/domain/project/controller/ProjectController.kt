package funding.startreum.domain.project.controller

import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.project.dto.ProjectApprovalResponseDto
import funding.startreum.domain.project.dto.ProjectCreateRequestDto
import funding.startreum.domain.project.dto.ProjectCreateResponseDto
import funding.startreum.domain.project.dto.ProjectUpdateRequestDto
import funding.startreum.domain.project.service.ProjectService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/beneficiary")
public open class ProjectController(  // ✅ open 키워드 추가
    private val projectService: ProjectService,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/create/projects")
    fun createProject(
        @RequestHeader("Authorization") token: String,
        @RequestBody projectRequest: ProjectCreateRequestDto
    ): ResponseEntity<ProjectCreateResponseDto> {
        val email = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        val response = projectService.createProject(projectRequest, email)
        return ResponseEntity.created(URI.create("/api/create/projects/${response.projectId}")).body(response)
    }

    @PutMapping("/modify/{projectId}")
    @PreAuthorize("hasRole('BENEFICIARY')")  // ✅ 메서드에만 적용
    fun modifyProject(
        @PathVariable projectId: Int,
        @RequestHeader("Authorization") token: String,
        @RequestBody @Valid projectUpdateRequestDto: ProjectUpdateRequestDto
    ): ResponseEntity<Map<String, Any>> {
        val updatedProject = projectService.modifyProject(projectId, projectUpdateRequestDto, token)
        return ResponseEntity.ok(
            mapOf(
                "statusCode" to 200,
                "message" to "프로젝트 수정에 성공하였습니다.",
                "data" to updatedProject
            )
        )
    }

    @DeleteMapping("/delete/{projectId}")
    fun deleteProject(
        @PathVariable projectId: Int,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Void> {
        projectService.deleteProject(projectId, token)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/requestApprove/{projectId}")
    @PreAuthorize("hasRole('BENEFICIARY')")  // ✅ 메서드에만 적용
    fun requestApprove(
        @PathVariable projectId: Int,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<ProjectApprovalResponseDto> {
        val responseDto = projectService.requestApprove(projectId, token)
        return ResponseEntity.ok(responseDto)
    }
}
