package com.example.expensetracker.user;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.expensetracker.config.InitialOwnerProperties;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户业务服务：按用户名查询、初始化所有者账号。
 *
 * <p>初始账号只从配置读取；不存在时创建，已存在时不做任何修改（不重置密码）。
 * 日志中不输出密码、密码哈希或其他凭据。</p>
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    // 业务时间统一按 Asia/Shanghai 计算，避免服务器时区不同导致“今天/本月”边界偏移
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int USERNAME_MAX_LENGTH = 64;
    private static final int BCRYPT_PASSWORD_MAX_LENGTH = 72;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final InitialOwnerProperties initialOwnerProperties;

    public UserService(UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       InitialOwnerProperties initialOwnerProperties) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.initialOwnerProperties = initialOwnerProperties;
    }

    /**
     * 按登录名精确查询用户；不存在或参数为空时返回 {@code null}。
     */
    public UserEntity findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getUsername, username));
    }

    /**
     * 初始化所有者账号：仅在用户名不存在时创建。
     */
    public void ensureInitialOwner() {
        String username = initialOwnerProperties.username() == null
                ? null
                : initialOwnerProperties.username().trim();
        String password = initialOwnerProperties.password();

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            // 配置缺失只警告、不阻断启动：V1 允许先启动服务，再由用户补齐配置
            log.warn("未配置 APP_INITIAL_OWNER_USERNAME / APP_INITIAL_OWNER_PASSWORD，跳过初始所有者创建");
            return;
        }
        if (username.length() > USERNAME_MAX_LENGTH) {
            log.warn("初始所有者用户名超过 {} 个字符，跳过创建", USERNAME_MAX_LENGTH);
            return;
        }
        if (password.length() > BCRYPT_PASSWORD_MAX_LENGTH) {
            log.warn("初始所有者密码超过 {} 个字符（BCrypt 上限），跳过创建", BCRYPT_PASSWORD_MAX_LENGTH);
            return;
        }
        if (findByUsername(username) != null) {
            // 已存在则绝不重置密码，避免重启把用户后来修改的密码覆盖回初始值
            log.info("初始所有者账号已存在，跳过创建");
            return;
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(1);
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        try {
            userMapper.insert(user);
            log.info("已创建初始所有者账号");
        } catch (DataIntegrityViolationException ex) {
            // 唯一索引兜底：多实例同时启动时只允许成功一个
            log.warn("初始所有者账号已存在（可能由其他实例创建），跳过创建");
        }
    }
}
