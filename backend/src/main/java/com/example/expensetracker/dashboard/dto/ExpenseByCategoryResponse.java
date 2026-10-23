package com.example.expensetracker.dashboard.dto;

/**
 * 支出分类占比项。
 *
 * <p>只统计支出；{@code percentage} 为 0–100 的百分数，保留两位小数。
 * 总支出为 0 时接口返回空数组，因此不会出现 NaN/Infinity。</p>
 */
public record ExpenseByCategoryResponse(
        Long categoryId,
        String categoryName,
        String amount,
        double percentage) {
}
