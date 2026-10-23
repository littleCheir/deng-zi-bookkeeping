package com.example.expensetracker.transaction.dto;

import com.example.expensetracker.common.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 新增账单请求。
 *
 * <p>金额必须大于 0、最多两位小数且适配 {@code DECIMAL(12,2)}；日期允许未来日期；
 * 备注先 trim 再校验长度（最多 200 字符）。请求不包含 userId，归属由服务端决定。</p>
 */
public record CreateTransactionRequest(
        @NotNull(message = "收支类型不能为空")
        TransactionType type,

        @NotNull(message = "分类不能为空")
        @Positive(message = "分类 ID 不合法")
        Long categoryId,

        @NotNull(message = "金额不能为空")
        @DecimalMin(value = "0", inclusive = false, message = "金额必须大于 0")
        @Digits(integer = 10, fraction = 2, message = "金额最多两位小数，且整数部分不能超过 10 位")
        BigDecimal amount,

        @NotNull(message = "日期不能为空")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate transactionDate,

        @Size(max = 200, message = "备注不能超过 200 个字符")
        String note) {

    public CreateTransactionRequest {
        note = note == null ? null : note.trim();
        if (note != null && note.isEmpty()) {
            note = null;
        }
    }
}
