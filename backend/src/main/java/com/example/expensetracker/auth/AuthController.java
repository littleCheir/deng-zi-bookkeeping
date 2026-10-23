package com.example.expensetracker.auth;

import com.example.expensetracker.auth.dto.CsrfResponse;
import com.example.expensetracker.auth.dto.CurrentUserResponse;
import com.example.expensetracker.auth.dto.LoginRequest;
import com.example.expensetracker.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：
 * <ul>
 *   <li>{@code GET /api/v1/auth/csrf}：匿名，触发 CSRF Cookie 下发</li>
 *   <li>{@code POST /api/v1/auth/login}：匿名，登录后建立 Session</li>
 *   <li>{@code POST /api/v1/auth/logout}：需登录，需 CSRF，清除会话</li>
 *   <li>{@code GET /api/v1/auth/me}：需登录，返回当前用户</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * CSRF 引导：返回请求头名/参数名，token 本身由 Security 过滤器写入 XSRF-TOKEN Cookie。
     * 必须匿名可访问，否则前端首次进入登录页时拿不到 CSRF token。
     */
    @GetMapping("/csrf")
    public ApiResponse<CsrfResponse> csrf(CsrfToken csrfToken) {
        return ApiResponse.success(new CsrfResponse(csrfToken.getHeaderName(), csrfToken.getParameterName()));
    }

    /**
     * 登录：认证成功后由 AuthService 换新会话并写入认证上下文。
     * 参数错误由全局异常处理映射为 400，凭据错误统一 401。
     */
    @PostMapping("/login")
    public ApiResponse<CurrentUserResponse> login(@Valid @RequestBody LoginRequest request,
                                                  HttpServletRequest httpRequest,
                                                  HttpServletResponse httpResponse) {
        return ApiResponse.success(authService.login(request, httpRequest, httpResponse));
    }

    /**
     * 登出：仅登录用户可调用，且必须携带 CSRF 头；成功后服务端会话失效。
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest) {
        authService.logout(httpRequest);
        return ApiResponse.success();
    }

    /**
     * 当前用户：未登录请求会被安全过滤器拦下并返回 401，不会进入该方法。
     */
    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.success(authService.me());
    }
}
