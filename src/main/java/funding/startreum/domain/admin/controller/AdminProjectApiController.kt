package funding.startreum.domain.admin.controller

import funding.startreum.domain.admin.dto.ProjectAdminSearchDto
import funding.startreum.domain.admin.dto.ProjectAdminUpdateDto
import funding.startreum.domain.admin.repository.ProjectAdminRepository
import funding.startreum.domain.admin.service.ProjectAdminService
import funding.startreum.domain.project.entity.Project
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/projects")
class AdminProjectApiController(
    private val projectAdminRepository: ProjectAdminRepository,
    private val projectAdminService: ProjectAdminService
) {
    private val logger = LoggerFactory.getLogger(AdminProjectApiController::class.java)

    /**
     * 🔹 프로젝트 목록 조회 (is_approved 상태 필터링 가능)
     * @param status 프로젝트 승인 상태 필터링 (선택적)
     * @param authentication 현재 로그인한 사용자의 인증 정보
     * @return 승인 상태에 따른 프로젝트 목록을 DTO 형태로 반환
     */
    @GetMapping
    fun getProjectsByApprovalStatus(
        @RequestParam(required = false) status: String?,
        authentication: Authentication?
    ): ResponseEntity<List<ProjectAdminSearchDto>> {
        // 관리자 권한 확인
        if (authentication?.authorities?.map(GrantedAuthority::getAuthority)?.none { it == "ROLE_ADMIN" } == true) {
            logger.warn("🚨 관리자 권한 없음")
            return ResponseEntity.status(403).body(null)
        }

        logger.info("📌 프로젝트 목록 조회 API 호출됨 - status: $status")

        val projects: List<Project> = if (!status.isNullOrBlank()) {
            try {
                val approvalStatus = Project.ApprovalStatus.valueOf(status.uppercase())
                projectAdminRepository.findByIsApproved(approvalStatus)
            } catch (e: IllegalArgumentException) {
                logger.error("❌ 잘못된 승인 상태 요청: $status")
                return ResponseEntity.badRequest().build()
            }
        } else {
            projectAdminRepository.findAll()
        }

        // DTO 변환 후 반환
        val projectDtos = projects.map { ProjectAdminSearchDto.from(it) }

        return ResponseEntity.ok(projectDtos)
    }

    /**
     * 🔹 프로젝트 승인 및 진행 상태 변경 API
     * @param projectId 변경할 프로젝트 ID
     * @param updateDto 변경할 승인 및 진행 상태 데이터
     * @param authentication 현재 로그인한 사용자의 인증 정보
     * @return 상태 변경 결과 메시지 반환
     */
    @PatchMapping("/{projectId}/update")
    fun updateProjectStatus(
        @PathVariable projectId: Int,
        @RequestBody updateDto: ProjectAdminUpdateDto,
        authentication: Authentication?
    ): ResponseEntity<String> {
        if (authentication?.authorities?.map(GrantedAuthority::getAuthority)?.none { it == "ROLE_ADMIN" } == true) {
            logger.warn("❌ 관리자 권한 없음")
            return ResponseEntity.status(403).body("❌ 권한 없음")
        }

        projectAdminService.updateProject(projectId, updateDto)

        return ResponseEntity.ok("✅ 프로젝트 상태가 변경되었습니다.")
    }
}
