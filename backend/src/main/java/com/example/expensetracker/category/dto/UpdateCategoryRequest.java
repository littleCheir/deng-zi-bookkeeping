package com.example.expensetracker.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 编辑个人分类请求。
 *
 * <p>不包含 type：收支类型创建后不可修改；也不接受 userId / isSystem / isActive。</p>
 */
public record UpdateCategoryRequest(
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 50, message = "分类名称不能超过 50 个字符")
        String name,

        @Size(max = 50, message = "图标不能超过 50 个字符")
        String icon,

        Integer sortOrder) {

    public UpdateCategoryRequest {
        name = name == null ? null : name.trim();
        icon = icon == null ? null : icon.trim();
    }
}
