package funding.startreum.domain.project.service


import funding.startreum.common.util.JwtUtil
import funding.startreum.domain.project.dto.*
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.project.repository.ProjectRepository
import funding.startreum.domain.reward.entity.Reward
import funding.startreum.domain.reward.repository.RewardRepository
import funding.startreum.domain.users.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
open class ProjectService(
    private val projectRepository: ProjectRepository,
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository,
    private val rewardRepository: RewardRepository
) {

    @Transactional(readOnly = true)
    open fun getProject(projectId: Int): Project {
        return projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다: $projectId") }
    }

    @Transactional
    open fun createProject(projectCreateRequestDto: ProjectCreateRequestDto, userId: String): ProjectCreateResponseDto {
        val user = userRepository.findByEmail(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다.") }

        val project = Project().apply {
            creator = user
            simpleDescription = projectCreateRequestDto.simpleDescription
            title = projectCreateRequestDto.title
            bannerUrl = projectCreateRequestDto.bannerUrl
            description = projectCreateRequestDto.description
            fundingGoal = projectCreateRequestDto.fundingGoal
            currentFunding = BigDecimal.ZERO
            startDate = projectCreateRequestDto.startDate
            endDate = projectCreateRequestDto.endDate
            status = Project.Status.ONGOING
            isApproved = Project.ApprovalStatus.AWAITING_APPROVAL
            createdAt = LocalDateTime.now()
            isDeleted = false
        }

        projectRepository.save(project)

        val reward = Reward(
            project = project,
            description = project.simpleDescription,
            amount = BigDecimal.valueOf(10000)
        )

        rewardRepository.save(reward)

        return ProjectCreateResponseDto(project.projectId, project.title, project.createdAt)
    }

    @Transactional
    open fun modifyProject(
        projectId: Int,
        projectUpdateRequestDto: ProjectUpdateRequestDto,
        token: String
    ): ProjectUpdateResponseDto {
        val email = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다.") }
        val project = projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다.") }

        if (project.creator?.userId != user.userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다.")
        }

        // ⚠️ `val` → `var` 변경이 필요함 (DTO는 `val` 유지)
        project.apply {
            projectUpdateRequestDto.title?.let { this.title = it }
            projectUpdateRequestDto.description?.let { this.description = it }
            projectUpdateRequestDto.fundingGoal?.let { this.fundingGoal = it }
            projectUpdateRequestDto.startDate?.let { this.startDate = it }
            projectUpdateRequestDto.endDate?.let { this.endDate = it }
            updatedAt = LocalDateTime.now()
        }

        return ProjectUpdateResponseDto(
            project.projectId,
            project.title,
            project.description,
            project.fundingGoal,
            project.startDate,
            project.endDate,
            project.updatedAt
        )
    }

    fun deleteProject(projectId: Int, token: String) {
        val email = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.") }
        val findProject = projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다.") }

        if (findProject.creator?.userId != user.userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다.")
        }

        projectRepository.delete(findProject)
    }

    fun requestApprove(projectId: Int, token: String): ProjectApprovalResponseDto {
        val email = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다.") }
        val project = projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다.") }

        if (project.creator?.userId != user.userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다.")
        }

        project.isApproved = Project.ApprovalStatus.AWAITING_APPROVAL
        projectRepository.save(project)

        return ProjectApprovalResponseDto(
            statusCode = 200, // 🔹 status 추가
            status = "AWAITING_APPROVAL", // 🔹 status 값 설정
            message = "승인 요청에 성공하였습니다.", // 🔹 기존 `detailMessage`를 `message`로 변경
            data = ProjectApprovalResponseDto.Data(projectId, LocalDateTime.now())
        )
    }
}
