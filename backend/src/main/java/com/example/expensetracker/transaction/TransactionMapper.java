package com.example.expensetracker.transaction;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单表 Mapper。本阶段只提供基础 CRUD 能力，不实现账单业务规则。
 */
@Mapper
public interface TransactionMapper extends BaseMapper<TransactionEntity> {
}
