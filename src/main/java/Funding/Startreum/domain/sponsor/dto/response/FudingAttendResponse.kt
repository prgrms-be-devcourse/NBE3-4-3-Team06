package Funding.Startreum.domain.sponsor.dto.response

import java.time.LocalDateTime

@JvmRecord
data class FudingAttendResponse(
    @JvmField val status: String,
    @JvmField val statusCode: Int,
    @JvmField val message: String,
    @JvmField val data: Data
) {
    @JvmRecord
    data class Data(
        @JvmField val FudingAttend: FudingAttend?
    )

    @JvmRecord
    data class FudingAttend(
        @JvmField val fundingId: Int,
        @JvmField val projectId: Int,
        @JvmField val projectTitle: String,
        @JvmField val amount: Double,
        @JvmField val rewardId: Int,
        @JvmField val fundedAt: LocalDateTime
    )

    @JvmRecord
    data class FundingRequest(
        val projectId: Int,
        val rewardId: Int,
        val amount: Double
    )

    companion object {
        @JvmStatic
        fun success(data: Data): FudingAttendResponse {
            return FudingAttendResponse(
                "success",
                200,
                "후원 참여 성공.",
                data
            )
        }

        fun error(statusCode: Int, message: String): FudingAttendResponse {
            return FudingAttendResponse(
                "error",
                statusCode,
                message,
                Data(null)
            )
        }
    }
}
