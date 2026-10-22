package com.example.expensetracker.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户实体，对应表 {@code sys_user}。
 *
 * <p>V1 只初始化一个所有者账号，但数据模型按多用户隔离设计。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 哈希，禁止存明文密码；不进入 toString，避免敏感信息进日志。 */
    @ToString.Exclude
    private String passwordHash;

    /** 1=启用，0=禁用。 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
