package com.example.expensetracker.transaction.dto;

import com.example.expensetracker.common.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单响应。
 *
 * <p>金额使用十进制字符串（如 {@code "35.50"}），避免 JavaScript 浮点精度问题；
 * 日期固定 {@code yyyy-MM-dd}，createdAt 固定到秒，避免前端因时区产生偏差。</p>
 */
public record TransactionResponse(
        Long id,
        TransactionType type,
        Long categoryId,
        String categoryName,
        String amount,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate transactionDate,
        String note,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt) {
}
