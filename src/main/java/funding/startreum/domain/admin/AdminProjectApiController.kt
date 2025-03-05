package funding.startreum.domain.admin

import funding.startreum.domain.project.entity.Project
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/projects")
class AdminProjectApiController(
    private val projectAdminRepository: ProjectAdminRepository,
    private val projectAdminService: ProjectAdminService
) {

    /**
     * 🔹 프로젝트 목록 조회 (is_approved 상태 필터링 가능)
     */
    @GetMapping
    fun getProjectsByApprovalStatus(
        @RequestParam(required = false) status: String?,
        authentication: Authentication?
    ): ResponseEntity<List<ProjectAdminSearchDto>> {
        // 🔍 현재 로그인한 사용자의 권한 확인
        if (authentication?.authorities?.map(GrantedAuthority::getAuthority)?.none { it == "ROLE_ADMIN" } == true) {
            return ResponseEntity.status(403).body(null)
        }

        val projects: List<Project> = if (!status.isNullOrBlank()) {
            try {
                val approvalStatus = Project.ApprovalStatus.valueOf(status.uppercase())
                projectAdminRepository.findByIsApproved(approvalStatus)
            } catch (e: IllegalArgumentException) {
                return ResponseEntity.badRequest().build()
            }
        } else {
            projectAdminRepository.findAll() // 전체 조회
        }

        // ✅ 관리자용 DTO로 변환하여 반환
        val projectDtos = projects.map { ProjectAdminSearchDto.from(it) }

        return ResponseEntity.ok(projectDtos)
    }

    /**
     * 🔹 프로젝트 승인 및 진행 상태 변경 API
     */
    /*
    @PatchMapping("/{projectId}/update")
    fun updateProjectStatus(
        @PathVariable projectId: Int,
        @RequestBody updateDto: ProjectAdminUpdateDto,
        authentication: Authentication?
    ): ResponseEntity<String> {
        if (authentication?.authorities?.map(GrantedAuthority::getAuthority)?.none { it == "ROLE_ADMIN" } == true) {
            return ResponseEntity.status(403).body("❌ 권한 없음")
        }

        // Repository 호출 대신 Service 호출로 변경
        projectAdminService.updateProject(projectId, updateDto)

        return ResponseEntity.ok("✅ 프로젝트 상태가 변경되었습니다.")
    }
    */
}
