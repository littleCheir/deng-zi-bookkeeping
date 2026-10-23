package com.example.expensetracker.auth;

import com.example.expensetracker.auth.dto.CurrentUserResponse;
import com.example.expensetracker.auth.dto.LoginRequest;
import com.example.expensetracker.common.exception.BusinessException;
import com.example.expensetracker.common.security.AuthUser;
import com.example.expensetracker.common.security.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

/**
 * 认证业务：登录、登出、当前用户。
 *
 * <p>会话固定防护：登录成功后废弃旧会话并创建新会话；认证上下文显式写入 Session。
 * 任何失败都统一返回 401 UNAUTHENTICATED，不区分用户名不存在或密码错误。</p>
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CurrentUserService currentUserService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthService(AuthenticationManager authenticationManager, CurrentUserService currentUserService) {
        this.authenticationManager = authenticationManager;
        this.currentUserService = currentUserService;
    }

    public CurrentUserResponse login(LoginRequest request,
                                     HttpServletRequest httpRequest,
                                     HttpServletResponse httpResponse) {
        Authentication authentication;
        try {
            // 统一交给 AuthenticationManager：账号不存在、密码错误、账号禁用都从这里返回
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username().trim(), request.password()));
        } catch (AuthenticationException ex) {
            // 对外只给一种失败原因，避免通过错误信息枚举用户名
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "用户名或密码错误");
        }

        // 会话固定防护：登录成功后废弃旧会话，防止攻击者预先固定的 Session ID 继续有效
        HttpSession existingSession = httpRequest.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        // SecurityContextHolderFilter 默认不自动持久化认证结果，必须显式写入 Session
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return new CurrentUserResponse(authUser.getId(), authUser.getUsername());
    }

    /**
     * 登出：会话失效与当前线程上下文清理必须同时做，避免本次请求后续代码仍读到旧身份。
     */
    public void logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    /**
     * 当前用户：只读认证主体；401 的最终判定由安全过滤器完成。
     */
    public CurrentUserResponse me() {
        AuthUser authUser = currentUserService.requireAuthenticatedUser();
        return new CurrentUserResponse(authUser.getId(), authUser.getUsername());
    }
}
