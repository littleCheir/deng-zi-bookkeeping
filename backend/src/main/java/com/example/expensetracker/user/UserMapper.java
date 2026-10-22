package com.example.expensetracker.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper。本阶段只提供基础 CRUD 能力，不实现认证逻辑。
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
