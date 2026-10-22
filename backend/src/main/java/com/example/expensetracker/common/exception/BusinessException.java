package com.example.expensetracker.common.exception;

import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常：携带对外错误码、HTTP 状态码与可选字段级详情。
 *
 * <p>错误码与状态码的对应关系见 API 合同：
 * VALIDATION_ERROR(400)、UNAUTHENTICATED(401)、FORBIDDEN(403)、NOT_FOUND(404)、
 * CATEGORY_NAME_CONFLICT(409)、CATEGORY_IN_USE(409)、INTERNAL_ERROR(500)。</p>
 *
 * <p>构造参数含业务含义，保留显式构造方法；访问器由 {@link Getter} 生成。</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final Map<String, Object> details;

    public BusinessException(HttpStatus status, String code, String message) {
        this(status, code, message, Map.of());
    }

    public BusinessException(HttpStatus status, String code, String message, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details == null ? Map.of() : details;
    }
}
