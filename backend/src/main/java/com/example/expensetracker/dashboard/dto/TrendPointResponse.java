package com.example.expensetracker.dashboard.dto;

/**
 * 每日收支趋势点。
 *
 * <p>日期为 {@code yyyy-MM-dd}，金额为两位小数字符串；范围内没有账单的日期也会补齐为 0。</p>
 */
public record TrendPointResponse(String date, String income, String expense) {
}
