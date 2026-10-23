package com.example.expensetracker.common.security;

import com.example.expensetracker.user.UserEntity;
import com.example.expensetracker.user.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 从数据库加载登录用户，供 Spring Security 认证使用。
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userService.findByUsername(username);
        if (user == null) {
            // 不区分“用户不存在”与“密码错误”，统一由认证失败处理
            throw new UsernameNotFoundException("用户不存在");
        }
        boolean enabled = user.getStatus() != null && user.getStatus() == 1;
        return new AuthUser(user.getId(), user.getUsername(), user.getPasswordHash(), enabled);
    }
}
