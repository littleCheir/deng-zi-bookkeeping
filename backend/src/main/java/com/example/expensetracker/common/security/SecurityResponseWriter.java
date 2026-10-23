package com.example.expensetracker.common.security;

import com.example.expensetracker.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * 安全过滤器链中的统一 JSON 错误输出（401 / 403）。
 */
final class SecurityResponseWriter {

    private SecurityResponseWriter() {
    }

    static void write(ObjectMapper objectMapper,
                      HttpServletResponse response,
                      HttpStatus status,
                      String code,
                      String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(code, message));
    }
}
