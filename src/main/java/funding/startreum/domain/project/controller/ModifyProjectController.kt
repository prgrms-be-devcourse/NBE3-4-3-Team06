package funding.startreum.domain.project.controller

import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.project.dto.ProjectUpdateRequestDto
import funding.startreum.domain.project.dto.ProjectUpdateResponseDto
import funding.startreum.domain.project.service.ProjectService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/beneficiary")
open class ModifyProjectController(
    private val projectService: ProjectService,
) {

    @PutMapping("/modify/{projectId}")
    @PreAuthorize("hasRole('BENEFICIARY')") // 수혜자만 수정 가능
    fun modifyProject(
        @PathVariable projectId: Int,
        @RequestHeader("Authorization") token: String,
        @RequestBody @Valid projectUpdateRequestDto: ProjectUpdateRequestDto
    ): ResponseEntity<Map<String, Any>> {
        val updatedProject: ProjectUpdateResponseDto = projectService.modifyProject(projectId, projectUpdateRequestDto, token)

        return ResponseEntity.ok(
            mapOf(
                "statusCode" to 200,
                "message" to "프로젝트 수정에 성공하였습니다.",
                "data" to updatedProject
            )
        )
    }
}