package funding.startreum.domain.project.service;


import funding.startreum.common.util.JwtUtil;
import funding.startreum.domain.project.dto.*;
import funding.startreum.domain.project.entity.Project;
import funding.startreum.domain.project.repository.ProjectRepository;
import funding.startreum.domain.reward.entity.Reward;
import funding.startreum.domain.reward.repository.RewardRepository;
import funding.startreum.domain.users.User;
import funding.startreum.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;


    @Transactional(readOnly = true)
    public Project getProject(Integer projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다. :" + projectId));
    }

    @Transactional
    public ProjectCreateResponseDto createProject(ProjectCreateRequestDto projectCreateRequestDto, String userId) {

        //사용자 검증
        User user = userRepository.findByEmail(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다"));    //사용자를 찾을 수 없을 시 401 에러

        //프로젝트 생성
        Project project = new Project();
        project.setCreator(user);
        project.setSimpleDescription(projectCreateRequestDto.simpleDescription());  // 제목 밑 간단한 설명 추가
        project.setTitle(projectCreateRequestDto.title());
        project.setBannerUrl(projectCreateRequestDto.bannerUrl());
        project.setDescription(projectCreateRequestDto.description());
        project.setFundingGoal(projectCreateRequestDto.fundingGoal());
        project.setCurrentFunding(BigDecimal.ZERO);
        project.setStartDate(projectCreateRequestDto.startDate());
        project.setEndDate(projectCreateRequestDto.endDate());
        project.setStatus(Project.Status.ONGOING);
        project.setIsApproved(Project.ApprovalStatus.AWAITING_APPROVAL);
        project.setCreatedAt(LocalDateTime.now());
        project.setIsDeleted(false);

        projectRepository.save(project);

        Reward reward = new Reward();
        reward.setProject(project);
        reward.setDescription(project.getSimpleDescription());
        reward.setAmount(BigDecimal.valueOf(10000));

        rewardRepository.save(reward);

        return new ProjectCreateResponseDto(project.getProjectId(), project.getTitle(), project.getCreatedAt());
    }

    @Transactional
    public ProjectUpdateResponseDto modifyProject(Integer projectId, ProjectUpdateRequestDto projectUpdateRequestDto, String token) {
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다"));    //사용자를 찾을 수 없을 시 401 에러
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다."));    //프로젝트를 찾을 수 없을 시 404 에러

        if (!project.getCreator().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."); //로그인한 유저와 프로젝트 유저 다를 시 403 에러 발생
        }

        // 선택적으로 수정 (null이 아닌 값만 업데이트)
        if (projectUpdateRequestDto.title() != null) {
            project.setTitle(projectUpdateRequestDto.title());
        }
        if (projectUpdateRequestDto.description() != null) {
            project.setDescription(projectUpdateRequestDto.description());
        }
        if (projectUpdateRequestDto.fundingGoal() != null) {
            project.setFundingGoal(projectUpdateRequestDto.fundingGoal());
        }
        if (projectUpdateRequestDto.startDate() != null) {
            project.setStartDate(projectUpdateRequestDto.startDate());
        }
        if (projectUpdateRequestDto.endDate() != null) {
            project.setEndDate(projectUpdateRequestDto.endDate());
        }

        project.setUpdatedAt(LocalDateTime.now());

        return new ProjectUpdateResponseDto(
                project.getProjectId(),
                project.getTitle(),
                project.getDescription(),
                project.getFundingGoal(),
                project.getStartDate(),
                project.getEndDate(),
                project.getUpdatedAt() // 수정된 시간
        );
    }

    public void deleteProject(Integer projectId, String token) {
        // "Bearer " 문자열 제거
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
        Project findProject = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "해당 프로젝트를 찾을 수 없습니다."));
        if (!findProject.getCreator().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."); //로그인한 유저와 프로젝트 유저 다를 시 403 에러 발생
        }

        // 프로젝트와 연관된 엔티티 삭제 (Cascade 설정이 되어 있으면 자동 삭제됨)
        projectRepository.delete(findProject);
    }

    public ProjectApprovalResponseDto requestApprove(Integer projectId, String token) {
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다."));

        if (!project.getCreator().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다.");
        }

        project.setIsApproved(Project.ApprovalStatus.AWAITING_APPROVAL);
        projectRepository.save(project);

        return new ProjectApprovalResponseDto(
                200,
                "AWAITING_APPROVAL",
                "승인 요청에 성공하였습니다.",
                new ProjectApprovalResponseDto.Data(
                        projectId,
                        LocalDateTime.now()
                )
        );
    }

}