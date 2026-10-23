package com.example.expensetracker.category;

import com.example.expensetracker.category.dto.CategoryResponse;
import com.example.expensetracker.category.dto.CreateCategoryRequest;
import com.example.expensetracker.category.dto.UpdateCategoryRequest;
import com.example.expensetracker.common.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
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
 * 分类接口：
 * <ul>
 *   <li>{@code GET /api/v1/categories?type=}：系统分类 + 当前用户个人分类</li>
 *   <li>{@code POST /api/v1/categories}：创建个人分类</li>
 *   <li>{@code PUT /api/v1/categories/{id}}：编辑个人分类（仅 name/icon/sortOrder）</li>
 *   <li>{@code DELETE /api/v1/categories/{id}}：删除个人分类（被引用时 409）</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list(
            @RequestParam(name = "type", required = false) String type) {
        return ApiResponse.success(categoryService.list(type));
    }

    @PostMapping
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        return ApiResponse.success(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable("id") Long id,
                                                @Valid @RequestBody UpdateCategoryRequest request) {
        return ApiResponse.success(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return ApiResponse.success();
    }
}
