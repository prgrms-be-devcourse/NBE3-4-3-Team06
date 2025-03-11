package funding.startreum.domain.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 환불 결과를 위한 DTO
public record RefundResponseDto(
        Integer fundingId,          // 환불된 펀딩 ID
        Integer sponsorId,          // 후원자 ID
        BigDecimal refundAmount,    // 환불 금액
        LocalDateTime refundDate    // 환불 처리 일자
) {}