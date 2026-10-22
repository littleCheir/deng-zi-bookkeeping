package com.example.expensetracker.transaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账单实体，对应表 {@code transaction_record}。
 *
 * <p>金额使用 {@link BigDecimal}，数据库为 {@code DECIMAL(12,2)}；
 * {@code userId} 由服务端从认证上下文写入，请求 DTO 不接受该字段。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("transaction_record")
public class TransactionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

    private String type;

    private BigDecimal amount;

    private LocalDate transactionDate;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
