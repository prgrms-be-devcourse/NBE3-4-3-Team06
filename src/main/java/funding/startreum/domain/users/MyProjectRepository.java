package funding.startreum.domain.users;

import funding.startreum.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyProjectRepository extends JpaRepository<Project, Integer> {
    // 특정 수혜자(creator)의 프로젝트 조회
    List<Project> findByCreator(User creator);
}