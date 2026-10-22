package com.example.expensetracker.category;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类表 Mapper。本阶段只提供基础 CRUD 能力，不实现分类业务规则。
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {
}
