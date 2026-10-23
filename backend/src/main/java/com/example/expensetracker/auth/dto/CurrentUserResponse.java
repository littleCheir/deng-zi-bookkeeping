package com.example.expensetracker.auth.dto;

/**
 * 当前登录用户响应：只包含 id 与 username，不返回任何会话信息。
 */
public record CurrentUserResponse(Long id, String username) {
}
