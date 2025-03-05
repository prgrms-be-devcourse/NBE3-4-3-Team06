package funding.startreum.domain.users.service

import funding.startreum.domain.users.dto.MyProjectDTO
import funding.startreum.domain.users.repository.MyProjectRepository
import org.springframework.stereotype.Service

@Service
class MyProjectService(
    private val myProjectRepository: MyProjectRepository,
    private val userService: UserService
) {
    fun getProjectsByUser(username: String): List<MyProjectDTO> {
        // 사용자 정보 조회 (널이면 예외 발생)
        val user = userService.getUserByName(username)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

        // 해당 사용자의 프로젝트 조회 후 DTO로 변환
        return myProjectRepository.findByCreator(user)
            .map { MyProjectDTO.from(it) }
    }
}
