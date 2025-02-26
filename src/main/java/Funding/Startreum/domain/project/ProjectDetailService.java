package funding.startreum.domain.project;

import funding.startreum.domain.project.entity.Project;
import funding.startreum.domain.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectDetailService {

    private final ProjectRepository projectRepository;

    public ProjectDetailService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public ProjectDetailDto getProjectDetail(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다. ID: " + projectId));

        return ProjectDetailDto.from(project);
    }
}