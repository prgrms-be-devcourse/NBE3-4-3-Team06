package funding.startreum.common.util;

public record ApiResponse<T>(
        String status,      // 응답 상태 ("success", "error")
        String message,     // 응답 메시지
        T data              // 응답 데이터
) {

    public ApiResponse {
        if (message == null)
            message = "";

        if (data == null)
            data = (T) new Object[0];
    }

    /*
    ===================================
       성공 응답을 위한 정적 메서드
    ===================================
    */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>("success", "응답에 성공했습니다.", null);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>("success", message, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", "응답에 성공했습니다.", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data);
    }

    /*
    ===================================
       오류 응답을 위한 정적 메서드
    ===================================
    */
    public static <T> ApiResponse<T> error() {
        return new ApiResponse<>("error", "응답에 싪패했습니다.", null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null);
    }

    public static <T> ApiResponse<T> error(T Data) {
        return new ApiResponse<>("error", "응답에 싪패했습니다.", Data);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>("error", message, data);
    }
}