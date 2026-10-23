package com.example.expensetracker.transaction;

import com.example.expensetracker.common.api.ApiResponse;
import com.example.expensetracker.common.api.PageResponse;
import com.example.expensetracker.transaction.dto.CreateTransactionRequest;
import com.example.expensetracker.transaction.dto.TransactionResponse;
import com.example.expensetracker.transaction.dto.UpdateTransactionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账单接口：
 * <ul>
 *   <li>{@code POST /api/v1/transactions}：新增账单</li>
 *   <li>{@code GET /api/v1/transactions}：分页 + 筛选列表（page 从 1 开始，size 最大 100）</li>
 *   <li>{@code GET /api/v1/transactions/{id}}：单笔账单（跨用户 404）</li>
 *   <li>{@code PUT /api/v1/transactions/{id}}：编辑账单</li>
 *   <li>{@code DELETE /api/v1/transactions/{id}}：删除账单</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/transactions")
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ApiResponse<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        return ApiResponse.success(transactionService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<TransactionResponse>> list(
            @RequestParam(name = "page", defaultValue = "1") @Min(value = 1, message = "页码从 1 开始") int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(value = 1, message = "每页数量至少为 1")
            @Max(value = 100, message = "每页数量不能超过 100") int size,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "categoryId", required = false) @Positive(message = "分类 ID 不合法") Long categoryId) {
        return ApiResponse.success(transactionService.list(page, size, startDate, endDate, type, categoryId));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> get(@PathVariable("id") Long id) {
        return ApiResponse.success(transactionService.get(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TransactionResponse> update(@PathVariable("id") Long id,
                                                   @Valid @RequestBody UpdateTransactionRequest request) {
        return ApiResponse.success(transactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        transactionService.delete(id);
        return ApiResponse.success();
    }
}
