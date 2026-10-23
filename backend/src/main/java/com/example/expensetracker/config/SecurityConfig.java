package com.example.expensetracker.config;

import com.example.expensetracker.common.security.RestAccessDeniedHandler;
import com.example.expensetracker.common.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Spring Security 配置。
 *
 * <p>认证方式：服务端 Session + HttpOnly Cookie。CSRF 使用 Cookie 仓库，
 * Cookie 名 {@code XSRF-TOKEN}（非 HttpOnly，供前端读取），请求头 {@code X-XSRF-TOKEN}。</p>
 *
 * <p>匿名可访问：CSRF 引导、登录、健康检查、错误页。其余接口一律需要认证。</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 密码只保存 BCrypt 哈希（自带随机盐）；禁止使用 MD5/SHA 等可快速暴力破解的算法。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   RestAuthenticationEntryPoint authenticationEntryPoint,
                                                   RestAccessDeniedHandler accessDeniedHandler,
                                                   @Value("${server.servlet.session.cookie.secure:false}") boolean cookieSecure)
            throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        // CSRF Cookie 与 Session Cookie 使用一致的 SameSite / Secure 策略
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie.sameSite("Lax").secure(cookieSecure));

        // 使用非 XOR 处理器：前端直接把 Cookie 中的原始 token 放进请求头
        CsrfTokenRequestAttributeHandler csrfTokenRequestHandler = new CsrfTokenRequestAttributeHandler();

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(csrfTokenRequestHandler))
                // 说明：当前 Spring Security 版本的 CsrfConfigurer 未暴露 accessDeniedHandler，
                // CSRF 校验失败使用框架默认 403 响应；前端按 HTTP 403 统一显示安全提示。
                .authorizeHttpRequests(auth -> auth
                        // 白名单只放认证引导与健康检查；logout/me 和所有业务接口都要求已登录
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                // 关闭表单登录、HTTP Basic 与默认登出页，统一使用 JSON 接口
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * DaoAuthenticationProvider 负责从数据库加载用户并用 BCrypt 校验密码；
     * 认证失败统一抛 AuthenticationException，由 AuthService 转成 401。
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
