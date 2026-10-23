package com.example.expensetracker.transaction;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.expensetracker.category.CategoryEntity;
import com.example.expensetracker.category.CategoryMapper;
import com.example.expensetracker.common.api.PageResponse;
import com.example.expensetracker.common.exception.BusinessException;
import com.example.expensetracker.common.model.TransactionType;
import com.example.expensetracker.common.security.CurrentUserService;
import com.example.expensetracker.transaction.dto.CreateTransactionRequest;
import com.example.expensetracker.transaction.dto.TransactionResponse;
import com.example.expensetracker.transaction.dto.UpdateTransactionRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 账单业务。
 *
 * <p>所有查询、更新、删除都必须同时限定“记录 ID + 当前用户 ID”，跨用户或不存在一律 404。
 * 创建/更新时由服务端根据认证上下文写入 userId，请求 DTO 不接受 userId。
 * 分类必须存在、启用、类型匹配，且属于系统分类或当前用户个人分类。</p>
 */
@Service
public class TransactionService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int NOTE_MAX_LENGTH = 200;
    private static final String UNKNOWN_CATEGORY_NAME = "未知分类";

    private final TransactionMapper transactionMapper;
    private final CategoryMapper categoryMapper;
    private final CurrentUserService currentUserService;

    public TransactionService(TransactionMapper transactionMapper,
                              CategoryMapper categoryMapper,
                              CurrentUserService currentUserService) {
        this.transactionMapper = transactionMapper;
        this.categoryMapper = categoryMapper;
        this.currentUserService = currentUserService;
    }

    /**
     * 分页查询当前用户账单：日期范围包含首尾，默认按日期倒序、同日 id 倒序。
     */
    public PageResponse<TransactionResponse> list(int page, int size,
                                                  LocalDate startDate, LocalDate endDate,
                                                  String typeParam, Long categoryId) {
        Long userId = currentUserService.requireUserId();
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw validation("开始日期不能晚于结束日期", Map.of("startDate", "不能晚于 endDate"));
        }
        TransactionType type = parseType(typeParam);

        LambdaQueryWrapper<TransactionEntity> wrapper = Wrappers.<TransactionEntity>lambdaQuery()
                .eq(TransactionEntity::getUserId, userId)
                .ge(startDate != null, TransactionEntity::getTransactionDate, startDate)
                .le(endDate != null, TransactionEntity::getTransactionDate, endDate)
                .orderByDesc(TransactionEntity::getTransactionDate)
                .orderByDesc(TransactionEntity::getId);
        if (type != null) {
            wrapper.eq(TransactionEntity::getType, type.name());
        }
        if (categoryId != null) {
            wrapper.eq(TransactionEntity::getCategoryId, categoryId);
        }

        // 显式声明分页对象类型，避免 selectPage 泛型推断产生歧义
        Page<TransactionEntity> pageRequest = new Page<>(page, size);
        IPage<TransactionEntity> result = transactionMapper.selectPage(pageRequest, wrapper);
        Map<Long, String> categoryNames = loadCategoryNames(result.getRecords());
        List<TransactionResponse> items = result.getRecords().stream()
                .map(entity -> toResponse(entity, categoryNames.get(entity.getCategoryId())))
                .toList();
        return PageResponse.of(items, result.getCurrent(), result.getSize(), result.getTotal());
    }

    /**
     * 查询当前用户单笔账单；不存在或不属于当前用户均返回 404。
     */
    public TransactionResponse get(Long id) {
        Long userId = currentUserService.requireUserId();
        TransactionEntity entity = requireOwnedTransaction(id, userId);
        CategoryEntity category = categoryMapper.selectById(entity.getCategoryId());
        return toResponse(entity, category == null ? null : category.getName());
    }

    /**
     * 新增账单：分类校验通过后写入数据库，userId 来自认证上下文。
     */
    public TransactionResponse create(CreateTransactionRequest request) {
        Long userId = currentUserService.requireUserId();
        CategoryEntity category = requireUsableCategory(request.categoryId(), request.type(), userId);

        TransactionEntity entity = new TransactionEntity();
        entity.setUserId(userId);
        entity.setCategoryId(category.getId());
        entity.setType(request.type().name());
        entity.setAmount(normalizeAmount(request.amount()));
        entity.setTransactionDate(request.transactionDate());
        entity.setNote(normalizeNote(request.note()));
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE).withNano(0);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        transactionMapper.insert(entity);
        return toResponse(entity, category.getName());
    }

    /**
     * 编辑账单：先确认记录归属当前用户，再重新校验分类与类型。
     */
    public TransactionResponse update(Long id, UpdateTransactionRequest request) {
        Long userId = currentUserService.requireUserId();
        TransactionEntity entity = requireOwnedTransaction(id, userId);
        CategoryEntity category = requireUsableCategory(request.categoryId(), request.type(), userId);

        entity.setCategoryId(category.getId());
        entity.setType(request.type().name());
        entity.setAmount(normalizeAmount(request.amount()));
        entity.setTransactionDate(request.transactionDate());
        entity.setNote(normalizeNote(request.note()));
        entity.setUpdatedAt(LocalDateTime.now(BUSINESS_ZONE).withNano(0));

        transactionMapper.updateById(entity);
        return toResponse(entity, category.getName());
    }

    /**
     * 删除当前用户账单；删除条件同时包含 id 与 userId，避免越权。
     */
    public void delete(Long id) {
        Long userId = currentUserService.requireUserId();
        int deleted = transactionMapper.delete(Wrappers.<TransactionEntity>lambdaQuery()
                .eq(TransactionEntity::getId, id)
                .eq(TransactionEntity::getUserId, userId));
        if (deleted == 0) {
            throw notFound();
        }
    }

    /**
     * 按“id + 当前用户”查询，禁止只按主键读取。
     */
    private TransactionEntity requireOwnedTransaction(Long id, Long userId) {
        TransactionEntity entity = transactionMapper.selectOne(Wrappers.<TransactionEntity>lambdaQuery()
                .eq(TransactionEntity::getId, id)
                .eq(TransactionEntity::getUserId, userId));
        if (entity == null) {
            throw notFound();
        }
        return entity;
    }

    /**
     * 分类必须存在、启用、类型匹配，且是系统分类或当前用户个人分类；否则按参数错误处理。
     */
    private CategoryEntity requireUsableCategory(Long categoryId, TransactionType type, Long userId) {
        CategoryEntity category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw invalidCategory();
        }
        boolean visible = category.getUserId() == null || category.getUserId().equals(userId);
        boolean active = category.getIsActive() != null && category.getIsActive() == 1;
        boolean typeMatches = type.name().equals(category.getType());
        if (!visible || !active || !typeMatches) {
            throw invalidCategory();
        }
        return category;
    }

    /** 批量取分类名，避免逐条查询；分类被引用时不允许删除，正常情况都能取到。 */
    private Map<Long, String> loadCategoryNames(List<TransactionEntity> records) {
        Set<Long> categoryIds = records.stream()
                .map(TransactionEntity::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));
    }

    private TransactionType parseType(String typeParam) {
        if (!StringUtils.hasText(typeParam)) {
            return null;
        }
        TransactionType type = TransactionType.fromValue(typeParam);
        if (type == null) {
            throw validation("收支类型只能是 INCOME 或 EXPENSE", Map.of("type", typeParam));
        }
        return type;
    }

    /** 统一为两位小数，保证创建响应与数据库读取后的值一致。 */
    private BigDecimal normalizeAmount(BigDecimal amount) {
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw validation("金额最多两位小数", Map.of("amount", "最多两位小数"));
        }
    }

    private String normalizeNote(String note) {
        if (note == null) {
            return null;
        }
        String trimmed = note.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > NOTE_MAX_LENGTH) {
            throw validation("备注不能超过 200 个字符", Map.of("note", "长度超过 200"));
        }
        return trimmed;
    }

    private TransactionResponse toResponse(TransactionEntity entity, String categoryName) {
        return new TransactionResponse(
                entity.getId(),
                TransactionType.valueOf(entity.getType()),
                entity.getCategoryId(),
                categoryName == null ? UNKNOWN_CATEGORY_NAME : categoryName,
                // 金额始终输出两位小数字符串，不依赖 Jackson 的 BigDecimal 序列化配置
                entity.getAmount() == null
                        ? "0.00"
                        : entity.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                entity.getTransactionDate(),
                entity.getNote(),
                entity.getCreatedAt());
    }

    private BusinessException invalidCategory() {
        return validation("分类不存在、已停用或与收支类型不匹配", Map.of("categoryId", "不可用"));
    }

    private BusinessException validation(String message, Map<String, Object> details) {
        return new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, details);
    }

    private BusinessException notFound() {
        return new BusinessException(HttpStatus.NOT_FOUND, "NOT_FOUND", "账单不存在");
    }
}
