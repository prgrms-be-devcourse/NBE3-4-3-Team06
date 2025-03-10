package funding.startreum.domain.users.repository

import funding.startreum.domain.users.entity.User
import funding.startreum.domain.project.entity.Project
import org.springframework.data.jpa.repository.JpaRepository

interface MyProjectRepository : JpaRepository<Project, Int> {
    // 특정 수혜자(creator)의 프로젝트 조회
    fun findByCreator(creator: User): List<Project>
}
