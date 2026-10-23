package com.example.expensetracker.dashboard;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 首页统计查询。
 *
 * <p>所有 SQL 都显式限定 {@code user_id}，聚合在数据库侧完成，金额以 DECIMAL 返回。
 * 返回 Map 的 key 与 SQL 别名一致，避免依赖实体/字段映射。</p>
 */
@Mapper
public interface DashboardMapper {

    /**
     * 收入/支出合计。用条件聚合一次查出两列，避免两次往返。
     */
    @Select("""
            SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS total_income,
                   COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS total_expense
            FROM transaction_record
            WHERE user_id = #{userId}
              AND transaction_date >= #{startDate}
              AND transaction_date <= #{endDate}
            """)
    Map<String, Object> sumSummary(@Param("userId") Long userId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * 按天 + 收支类型聚合。只返回有账的日期，缺少的日期由服务层补齐。
     */
    @Select("""
            SELECT DATE_FORMAT(transaction_date, '%Y-%m-%d') AS day,
                   type,
                   SUM(amount) AS amount
            FROM transaction_record
            WHERE user_id = #{userId}
              AND transaction_date >= #{startDate}
              AND transaction_date <= #{endDate}
            GROUP BY DATE_FORMAT(transaction_date, '%Y-%m-%d'), type
            ORDER BY day ASC
            """)
    List<Map<String, Object>> sumByDay(@Param("userId") Long userId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * 按支出分类聚合，join 分类表取名称；分类被引用时不可删除，因此名称总能取到。
     */
    @Select("""
            SELECT t.category_id AS category_id,
                   c.name AS category_name,
                   SUM(t.amount) AS amount
            FROM transaction_record t
            JOIN category c ON c.id = t.category_id
            WHERE t.user_id = #{userId}
              AND t.type = 'EXPENSE'
              AND t.transaction_date >= #{startDate}
              AND t.transaction_date <= #{endDate}
            GROUP BY t.category_id, c.name
            ORDER BY amount DESC, t.category_id ASC
            """)
    List<Map<String, Object>> sumExpenseByCategory(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
}
