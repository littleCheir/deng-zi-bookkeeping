package com.example.expensetracker.common.api;

/**
 * 统一响应外壳。
 *
 * <p>成功：{@code {"data":<payload>,"error":null}}；
 * 失败：{@code {"data":null,"error":{"code":"...","message":"...","details":{}}}}。</p>
 *
 * @param <T> data 载荷类型
 */
public record ApiResponse<T>(T data, ApiError error) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(null, null);
    }

    public static <T> ApiResponse<T> failure(ApiError error) {
        return new ApiResponse<>(null, error);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(null, ApiError.of(code, message));
    }
}
