package com.example.expensetracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 初始所有者账号配置（环境变量 {@code APP_INITIAL_OWNER_USERNAME} / {@code APP_INITIAL_OWNER_PASSWORD}）。
 *
 * <p>只在用户名不存在时创建账号，不在每次启动时重置密码。</p>
 */
@ConfigurationProperties(prefix = "app.initial-owner")
public record InitialOwnerProperties(String username, String password) {
}
