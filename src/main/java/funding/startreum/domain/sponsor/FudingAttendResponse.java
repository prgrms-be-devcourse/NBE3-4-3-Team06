package funding.startreum.domain.sponsor;

import java.time.LocalDateTime;

public record FudingAttendResponse(
        String status,
        int statusCode,
        String message,
        Data data
) {

    public record Data(
            FudingAttend FudingAttend
    ) {}

    public record FudingAttend(
        Integer fundingId,
        Integer projectId,
        String projectTitle,
        Double amount,
        Integer rewardId,
        LocalDateTime fundedAt
    ) {}

    public record FundingRequest(
            Integer projectId,
            Integer rewardId,
            Double amount
    ) {}

    public static FudingAttendResponse success(Data data) {
        return new FudingAttendResponse(
                "success",
                200,
                "후원 참여 성공.",
                data
        );
    }

    public static FudingAttendResponse error(int statusCode, String message) {
        return new FudingAttendResponse(
                "error",
                statusCode,
                message,
                new Data(null)
        );
    }
}
