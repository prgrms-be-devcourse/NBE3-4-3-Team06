package funding.startreum.domain.inquiry;

import java.time.LocalDateTime;

public record InquiryResponse (
        String status,
        int statusCode,
        String message,
        Data data
) {
    public record Data(
            Integer inquiryId,
            String title,
            String content,
            Inquiry.Status status,
            LocalDateTime createdAt
    ) {}

    public static InquiryResponse success(Data data) {
        return new InquiryResponse(
                "success",
                201,
                "문의 생성 성공.",
                data
        );
    }

    public static InquiryResponse error(int statusCode, String message) {
        return new InquiryResponse(
                "error",
                statusCode,
                message,
                null
        );
    }
}
