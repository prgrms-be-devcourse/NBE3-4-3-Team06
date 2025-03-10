package funding.startreum.common.util

class ApiResponse<T>(
    val status: String,
    val message: String = "",
    val data: T? = null
) {
    companion object {
        fun <T> success(message: String = "응답에 성공했습니다.", data: T? = null): ApiResponse<T> {
            return ApiResponse("success", message, data)
        }

        fun <T> error(message: String = "응답에 실패했습니다.", data: T? = null): ApiResponse<T> {
            return ApiResponse("error", message, data)
        }
    }
}