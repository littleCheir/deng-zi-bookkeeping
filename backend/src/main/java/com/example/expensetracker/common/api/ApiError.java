package com.example.expensetracker.common.api;

import java.util.Map;

/**
 * 统一错误体。
 *
 * <p>合同要求失败响应为 {@code {"data":null,"error":{"code":"...","message":"...","details":{}}}}。
 * details 至少为空对象，不允许为 null。</p>
 */
public record ApiError(String code, String message, Map<String, Object> details) {

    public ApiError {
        if (details == null) {
            details = Map.of();
        }
    }

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, Map.of());
    }
}
