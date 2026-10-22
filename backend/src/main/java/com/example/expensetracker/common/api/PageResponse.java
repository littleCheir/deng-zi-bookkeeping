package com.example.expensetracker.common.api;

import java.util.List;

/**
 * 分页响应载荷：{@code {items, page, size, total}}。
 *
 * <p>page 从 1 开始；size 默认 20、最大 100（由具体接口校验）。</p>
 *
 * @param <T> 列表项类型
 */
public record PageResponse<T>(List<T> items, long page, long size, long total) {

    public static <T> PageResponse<T> of(List<T> items, long page, long size, long total) {
        return new PageResponse<>(items == null ? List.of() : items, page, size, total);
    }
}
