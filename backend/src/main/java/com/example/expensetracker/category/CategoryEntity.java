package com.example.expensetracker.category;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类实体，对应表 {@code category}。
 *
 * <p>{@code userId} 为 {@code null} 表示系统默认分类；否则为该用户的个人分类。
 * {@code type} 在数据库中只允许 {@code INCOME} / {@code EXPENSE}，
 * 实体使用字符串保存，业务层统一通过 {@code TransactionType} 校验。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("category")
public class CategoryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String type;

    private String icon;

    private Integer sortOrder;

    /** 1=启用，0=停用；V1 不提供启停接口。 */
    private Integer isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
