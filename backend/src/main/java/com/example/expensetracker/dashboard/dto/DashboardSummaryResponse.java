package com.example.expensetracker.dashboard.dto;

/**
 * 收支概览响应。
 *
 * <p>金额一律为两位小数字符串；{@code balance = totalIncome - totalExpense}，
 * 允许为负数。无数据时金额为 {@code "0.00"}。</p>
 */
public record DashboardSummaryResponse(
        String totalIncome,
        String totalExpense,
        String balance,
        String startDate,
        String endDate) {
}
