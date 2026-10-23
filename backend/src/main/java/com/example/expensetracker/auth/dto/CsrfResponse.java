package com.example.expensetracker.auth.dto;

/**
 * CSRF 引导响应：只告诉前端请求头/参数名，token 本身通过 XSRF-TOKEN Cookie 下发。
 */
public record CsrfResponse(String headerName, String parameterName) {
}
