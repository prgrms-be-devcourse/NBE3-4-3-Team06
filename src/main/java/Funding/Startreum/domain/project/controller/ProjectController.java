package funding.startreum.domain.project.controller;

import funding.startreum.common.util.JwtUtil;
import funding.startreum.domain.project.dto.ProjectCreateRequestDto;
import funding.startreum.domain.project.dto.ProjectCreateResponseDto;

import funding.startreum.domain.project.dto.ProjectUpdateRequestDto;
import funding.startreum.domain.project.dto.ProjectUpdateResponseDto;
import funding.startreum.domain.project.dto.ProjectApprovalResponseDto;
import funding.startreum.domain.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import java.util.Map;

@RestController
@RequestMapping("/api/beneficiary")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create/projects")
    public ResponseEntity<ProjectCreateResponseDto> createProject(
            @RequestHeader("Authorization") String token,
            @RequestBody ProjectCreateRequestDto projectRequest) {


        // 1. "Bearer " 문자열 제거 후 JWT에서 email 추출
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));

        // 2. 프로젝트 생성 서비스 호출
        ProjectCreateResponseDto response = projectService.createProject(projectRequest, email);

        return ResponseEntity.created(URI.create("/api/create/projects/" + response.projectId())).body(response);

    }

    @PutMapping("/modify/{projectId}")
    @PreAuthorize("hasRole('BENEFICIARY')") //수혜자만 수정 가능
    public ResponseEntity<?> modifyProject(@PathVariable Integer projectId, @RequestHeader("Authorization") String token, @RequestBody @Valid ProjectUpdateRequestDto projectUpdateRequestDto) {
        ProjectUpdateResponseDto updatedProject = projectService.modifyProject(projectId, projectUpdateRequestDto, token);
        return ResponseEntity.ok(Map.of(
                "statusCode", 200,
                "message", "프로젝트 수정에 성공하였습니다.",
                "data", updatedProject
        ));
    }

    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable Integer projectId, @RequestHeader("Authorization") String token) {
        projectService.deleteProject(projectId, token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/requestApprove/{projectId}")
    @PreAuthorize("hasRole('BENEFICIARY')")
    public ResponseEntity<ProjectApprovalResponseDto> requestApprove(@PathVariable("projectId") Integer projectId, @RequestHeader("Authorization") String token) {
        ProjectApprovalResponseDto responseDto = projectService.requestApprove(projectId, token);

        return ResponseEntity.ok(responseDto);
    }
}
