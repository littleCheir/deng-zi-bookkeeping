package com.example.expensetracker.category.dto;

import com.example.expensetracker.common.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 分类响应：对前端只暴露业务字段。
 *
 * <p>{@code isSystem=true} 表示系统默认分类（user_id 为 NULL），用户不可编辑或删除。</p>
 */
public record CategoryResponse(
        Long id,
        String name,
        TransactionType type,
        String icon,
        Integer sortOrder,
        @JsonProperty("isSystem") boolean isSystem,
        @JsonProperty("isActive") boolean isActive) {
}
