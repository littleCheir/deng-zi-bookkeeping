package com.example.expensetracker.category;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.expensetracker.category.dto.CategoryResponse;
import com.example.expensetracker.category.dto.CreateCategoryRequest;
import com.example.expensetracker.category.dto.UpdateCategoryRequest;
import com.example.expensetracker.common.exception.BusinessException;
import com.example.expensetracker.common.model.TransactionType;
import com.example.expensetracker.common.security.CurrentUserService;
import com.example.expensetracker.transaction.TransactionEntity;
import com.example.expensetracker.transaction.TransactionMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 分类业务。
 *
 * <p>可见范围：系统分类（user_id 为 NULL）与当前用户个人分类的并集。
 * 同一收支类型内不允许重名（与系统分类或其他个人分类比较，trim 后精确匹配，
 * 不做大小写/Unicode 归一化）。系统分类不可编辑/删除；他人分类按 404 处理，避免泄露存在性。</p>
 */
@Service
public class CategoryService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int NAME_MAX_LENGTH = 50;
    private static final int ICON_MAX_LENGTH = 50;

    private final CategoryMapper categoryMapper;
    private final TransactionMapper transactionMapper;
    private final CurrentUserService currentUserService;

    public CategoryService(CategoryMapper categoryMapper,
                           TransactionMapper transactionMapper,
                           CurrentUserService currentUserService) {
        this.categoryMapper = categoryMapper;
        this.transactionMapper = transactionMapper;
        this.currentUserService = currentUserService;
    }

    /**
     * 查询当前用户可见分类；type 为空时返回收入与支出两类。
     */
    public List<CategoryResponse> list(String typeParam) {
        TransactionType type = parseType(typeParam);
        Long userId = currentUserService.requireUserId();

        LambdaQueryWrapper<CategoryEntity> wrapper = Wrappers.<CategoryEntity>lambdaQuery()
                // 可见性：系统分类 OR 当前用户个人分类
                .and(w -> w.isNull(CategoryEntity::getUserId)
                        .or()
                        .eq(CategoryEntity::getUserId, userId))
                // 合同约定的稳定排序：type、sortOrder、id
                .orderByAsc(CategoryEntity::getType)
                .orderByAsc(CategoryEntity::getSortOrder)
                .orderByAsc(CategoryEntity::getId);
        if (type != null) {
            wrapper.eq(CategoryEntity::getType, type.name());
        }
        return categoryMapper.selectList(wrapper).stream().map(this::toResponse).toList();
    }

    /**
     * 创建个人分类：归属当前用户，type 由请求指定且之后不可修改。
     */
    public CategoryResponse create(CreateCategoryRequest request) {
        Long userId = currentUserService.requireUserId();
        String name = normalizeName(request.name());
        String icon = normalizeIcon(request.icon());
        ensureNameAvailable(request.type(), name, null, userId);

        CategoryEntity entity = new CategoryEntity();
        entity.setUserId(userId);
        entity.setName(name);
        entity.setType(request.type().name());
        entity.setIcon(icon);
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity.setIsActive(1);
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        try {
            categoryMapper.insert(entity);
        } catch (DuplicateKeyException ex) {
            // 并发创建同名个人分类时由 (user_id,type,name) 唯一索引兜底，转成合同里的 409
            throw nameConflict();
        }
        return toResponse(entity);
    }

    /**
     * 编辑个人分类：只允许 name / icon / sortOrder，type 保持数据库原值。
     */
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        Long userId = currentUserService.requireUserId();
        CategoryEntity entity = requireOwnedCategory(id, userId);

        String name = normalizeName(request.name());
        String icon = normalizeIcon(request.icon());
        ensureNameAvailable(TransactionType.valueOf(entity.getType()), name, id, userId);

        entity.setName(name);
        entity.setIcon(icon);
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity.setUpdatedAt(LocalDateTime.now(BUSINESS_ZONE));

        int updated;
        try {
            // UPDATE 条件同时限定 id 与 user_id，避免只按主键写入
            updated = categoryMapper.update(entity, Wrappers.<CategoryEntity>lambdaUpdate()
                    .eq(CategoryEntity::getId, id)
                    .eq(CategoryEntity::getUserId, userId));
        } catch (DuplicateKeyException ex) {
            throw nameConflict();
        }
        if (updated == 0) {
            // 0 行可能是记录被并发删除，也可能是驱动开启 useAffectedRows 后“值未变化”；
            // 再查一次归属，真正不存在才返回 404。
            requireOwnedCategory(id, userId);
        }
        return toResponse(entity);
    }

    /**
     * 删除个人分类：被当前用户账单引用时拒绝删除（409），不做级联删除。
     */
    public void delete(Long id) {
        Long userId = currentUserService.requireUserId();
        requireOwnedCategory(id, userId);

        Long referenced = transactionMapper.selectCount(Wrappers.<TransactionEntity>lambdaQuery()
                .eq(TransactionEntity::getUserId, userId)
                .eq(TransactionEntity::getCategoryId, id));
        if (referenced != null && referenced > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "CATEGORY_IN_USE", "该分类已被账单使用，无法删除");
        }
        // DELETE 条件同样限定 user_id，避免只按主键删除
        int deleted = categoryMapper.delete(Wrappers.<CategoryEntity>lambdaQuery()
                .eq(CategoryEntity::getId, id)
                .eq(CategoryEntity::getUserId, userId));
        if (deleted == 0) {
            throw categoryNotFound();
        }
    }

    /**
     * 取分类并校验归属：不存在/他人分类 404，系统分类 403（不可编辑或删除）。
     */
    private CategoryEntity requireOwnedCategory(Long id, Long userId) {
        CategoryEntity entity = categoryMapper.selectById(id);
        if (entity == null) {
            throw categoryNotFound();
        }
        if (entity.getUserId() == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "FORBIDDEN", "系统分类不可编辑或删除");
        }
        if (!entity.getUserId().equals(userId)) {
            // 他人分类按不存在处理，避免泄露存在性
            throw categoryNotFound();
        }
        return entity;
    }

    /**
     * 同一收支类型 + 当前用户可见范围内名称必须唯一。
     *
     * @param excludeId 编辑时排除自身；创建时传 null
     */
    private void ensureNameAvailable(TransactionType type, String name, Long excludeId, Long userId) {
        Long count = categoryMapper.selectCount(Wrappers.<CategoryEntity>lambdaQuery()
                .eq(CategoryEntity::getType, type.name())
                .eq(CategoryEntity::getName, name)
                .and(w -> w.isNull(CategoryEntity::getUserId)
                        .or()
                        .eq(CategoryEntity::getUserId, userId))
                .ne(excludeId != null, CategoryEntity::getId, excludeId));
        if (count != null && count > 0) {
            throw nameConflict();
        }
    }

    private BusinessException nameConflict() {
        return new BusinessException(HttpStatus.CONFLICT, "CATEGORY_NAME_CONFLICT",
                "同一收支类型下已存在同名分类");
    }

    private BusinessException categoryNotFound() {
        return new BusinessException(HttpStatus.NOT_FOUND, "NOT_FOUND", "分类不存在");
    }

    private TransactionType parseType(String typeParam) {
        if (!StringUtils.hasText(typeParam)) {
            return null;
        }
        TransactionType type = TransactionType.fromValue(typeParam);
        if (type == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "收支类型只能是 INCOME 或 EXPENSE", Map.of("type", typeParam));
        }
        return type;
    }

    private String normalizeName(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "分类名称不能为空", Map.of("name", "不能为空"));
        }
        if (trimmed.length() > NAME_MAX_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "分类名称不能超过 50 个字符", Map.of("name", "长度超过 50"));
        }
        return trimmed;
    }

    /** 图标允许为空；空字符串统一存 NULL，避免前端出现“看起来有图标但实际为空”的状态。 */
    private String normalizeIcon(String icon) {
        if (!StringUtils.hasText(icon)) {
            return null;
        }
        String trimmed = icon.trim();
        if (trimmed.length() > ICON_MAX_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "图标不能超过 50 个字符", Map.of("icon", "长度超过 50"));
        }
        return trimmed;
    }

    private CategoryResponse toResponse(CategoryEntity entity) {
        return new CategoryResponse(
                entity.getId(),
                entity.getName(),
                TransactionType.valueOf(entity.getType()),
                entity.getIcon(),
                entity.getSortOrder(),
                entity.getUserId() == null,
                entity.getIsActive() != null && entity.getIsActive() == 1);
    }
}
