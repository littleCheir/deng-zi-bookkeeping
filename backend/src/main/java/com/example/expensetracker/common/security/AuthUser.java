package com.example.expensetracker.common.security;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 认证主体：只暴露业务需要的用户 ID 与用户名，密码哈希仅用于认证流程。
 */
@Getter
public class AuthUser implements UserDetails {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;

    public AuthUser(Long id, String username, String passwordHash, boolean enabled) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    /**
     * V1 只有所有者一个角色；后续扩展角色体系时在这里映射，不在业务代码里散落判断。
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
