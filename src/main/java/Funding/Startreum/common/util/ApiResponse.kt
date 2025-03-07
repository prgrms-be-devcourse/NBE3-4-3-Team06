package funding.startreum.common.util

class ApiResponse<T>(val status: String, message: String?, data: T) {
    val message: String
    val data: T

    init {
        var message = message
        var data: T? = data
        if (message == null) message = ""

        if (data == null) data = arrayOfNulls<Any>(0) as T
        this.message = message
        this.data = data
    }

    companion object {
        /*
    ===================================
       성공 응답을 위한 정적 메서드
    ===================================
    */
        fun <T> success(): ApiResponse<T?> {
            return ApiResponse("success", "응답에 성공했습니다.", null)
        }

        fun <T> success(message: String?): ApiResponse<T?> {
            return ApiResponse("success", message, null)
        }

        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse("success", "응답에 성공했습니다.", data)
        }

        fun <T> success(message: String?, data: T): ApiResponse<T> {
            return ApiResponse("success", message, data)
        }

        /*
    ===================================
       오류 응답을 위한 정적 메서드
    ===================================
    */
        fun <T> error(): ApiResponse<T?> {
            return ApiResponse("error", "응답에 싪패했습니다.", null)
        }

        fun <T> error(message: String?): ApiResponse<T?> {
            return ApiResponse("error", message, null)
        }

        fun <T> error(Data: T): ApiResponse<T> {
            return ApiResponse("error", "응답에 싪패했습니다.", Data)
        }

        fun <T> error(message: String?, data: T): ApiResponse<T> {
            return ApiResponse("error", message, data)
        }
    }
}