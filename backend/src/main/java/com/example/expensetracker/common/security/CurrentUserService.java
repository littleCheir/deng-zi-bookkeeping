package com.example.expensetracker.common.security;

import com.example.expensetracker.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 当前用户上下文：只从 SecurityContext 读取身份，不接受前端传入的 userId。
 */
@Service
public class CurrentUserService {

    /**
     * 取得当前登录用户；未登录、匿名或主体类型不符一律按未认证处理。
     * 这里抛异常而不是返回 null，避免调用方漏判后继续执行造成越权。
     */
    public AuthUser requireAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "未登录或会话已失效");
        }
        return authUser;
    }

    /**
     * 业务层统一通过该方法取得 user_id；禁止从请求参数接收 userId 作为数据归属。
     */
    public Long requireUserId() {
        return requireAuthenticatedUser().getId();
    }
}
