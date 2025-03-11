package funding.startreum.domain.admin.dto;


// 환불 요청을 위한 DTO
public record RefundRequestDto(
        Integer projectId  // 환불할 프로젝트 ID
) {}