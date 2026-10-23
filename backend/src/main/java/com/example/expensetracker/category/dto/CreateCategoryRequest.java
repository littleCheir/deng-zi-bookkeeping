package com.example.expensetracker.category.dto;

import com.example.expensetracker.common.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建个人分类请求。
 *
 * <p>不接受 userId / isSystem / isActive：归属与状态由服务端决定。
 * 名称与图标先 trim 再参与 Bean Validation，保证“trim 后最多 50 字符”的口径一致。</p>
 */
public record CreateCategoryRequest(
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 50, message = "分类名称不能超过 50 个字符")
        String name,

        @NotNull(message = "收支类型不能为空")
        TransactionType type,

        @Size(max = 50, message = "图标不能超过 50 个字符")
        String icon,

        Integer sortOrder) {

    public CreateCategoryRequest {
        name = name == null ? null : name.trim();
        icon = icon == null ? null : icon.trim();
    }
}
