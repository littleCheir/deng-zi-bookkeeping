package com.example.expensetracker.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 已登录但无权操作，或 CSRF 校验失败：统一返回 403 FORBIDDEN。
 *
 * <p>不区分具体原因，避免向客户端泄露安全实现细节。</p>
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        SecurityResponseWriter.write(objectMapper, response, HttpStatus.FORBIDDEN,
                "FORBIDDEN", "无权执行该操作，或安全校验失败");
    }
}
