package funding.startreum.domain.sponsor;

import java.time.LocalDateTime;
import java.util.List;

public record SponListResponse(
        String status,
        int statusCode,
        String message,
        Data data
) {
    public record Data(
            List<Funding> fundings,
            Pagination pagination) {}

    public record Funding(
            Integer fundingId,
            Integer projectId,
            String projectTitle,
            Integer rewardId,
            Double amount,
            LocalDateTime fundedAt
    ) {}

    public record Pagination(int total, int page, int pageSize) {}

    public static SponListResponse success(List<Funding> fundings, Pagination pagination) {
        return new SponListResponse(
                "success",
                200,
                "후원 목록 조회 성공.",
                new Data(fundings, pagination));
    }

    public static SponListResponse error(int statusCode, String message) {
        return new SponListResponse(
                "error",
                statusCode,
                message,
                new Data(List.of(), null));
    }
}
