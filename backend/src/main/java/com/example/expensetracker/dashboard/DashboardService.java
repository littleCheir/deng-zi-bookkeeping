package com.example.expensetracker.dashboard;

import com.example.expensetracker.common.exception.BusinessException;
import com.example.expensetracker.common.security.CurrentUserService;
import com.example.expensetracker.dashboard.dto.DashboardSummaryResponse;
import com.example.expensetracker.dashboard.dto.ExpenseByCategoryResponse;
import com.example.expensetracker.dashboard.dto.TrendPointResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * 首页统计业务。
 *
 * <p>统计口径由后端统一计算：日期范围包含首尾，缺省为当前自然月（Asia/Shanghai）；
 * 所有查询严格限定当前用户；金额只使用 BigDecimal，输出两位小数字符串。</p>
 */
@Service
public class DashboardService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
    /** 防御性上限：避免一次统计范围过大导致趋势点数量失控。 */
    private static final long MAX_RANGE_DAYS = 3660L;

    private final DashboardMapper dashboardMapper;
    private final CurrentUserService currentUserService;

    public DashboardService(DashboardMapper dashboardMapper, CurrentUserService currentUserService) {
        this.dashboardMapper = dashboardMapper;
        this.currentUserService = currentUserService;
    }

    /**
     * 收支概览：收入合计、支出合计、结余。
     */
    public DashboardSummaryResponse summary(LocalDate startDate, LocalDate endDate) {
        Long userId = currentUserService.requireUserId();
        DateRange range = resolveRange(startDate, endDate);

        Map<String, Object> row = dashboardMapper.sumSummary(userId, range.start(), range.end());
        BigDecimal income = toAmount(row == null ? null : row.get("total_income"));
        BigDecimal expense = toAmount(row == null ? null : row.get("total_expense"));

        return new DashboardSummaryResponse(
                money(income),
                money(expense),
                money(income.subtract(expense)),
                range.start().toString(),
                range.end().toString());
    }

    /**
     * 每日收支趋势：范围内逐日补齐，缺失日期金额为 0.00。
     */
    public List<TrendPointResponse> trend(String period, LocalDate startDate, LocalDate endDate) {
        if (period != null && !"day".equals(period)) {
            throw validation("period 目前只支持 day", Map.of("period", period));
        }
        Long userId = currentUserService.requireUserId();
        DateRange range = resolveRange(startDate, endDate);

        List<Map<String, Object>> rows = dashboardMapper.sumByDay(userId, range.start(), range.end());
        Map<String, BigDecimal[]> byDate = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String day = String.valueOf(row.get("day"));
            String type = String.valueOf(row.get("type"));
            BigDecimal amount = toAmount(row.get("amount"));
            BigDecimal[] sums = byDate.computeIfAbsent(day, key -> new BigDecimal[]{ZERO, ZERO});
            if ("INCOME".equals(type)) {
                sums[0] = sums[0].add(amount);
            } else if ("EXPENSE".equals(type)) {
                sums[1] = sums[1].add(amount);
            }
        }

        List<TrendPointResponse> result = new ArrayList<>();
        for (LocalDate day = range.start(); !day.isAfter(range.end()); day = day.plusDays(1)) {
            BigDecimal[] sums = byDate.getOrDefault(day.toString(), new BigDecimal[]{ZERO, ZERO});
            result.add(new TrendPointResponse(day.toString(), money(sums[0]), money(sums[1])));
        }
        return result;
    }

    /**
     * 支出分类占比：只统计支出；总支出为 0 时返回空数组，不产生 NaN/Infinity。
     */
    public List<ExpenseByCategoryResponse> expenseByCategory(LocalDate startDate, LocalDate endDate) {
        Long userId = currentUserService.requireUserId();
        DateRange range = resolveRange(startDate, endDate);

        List<Map<String, Object>> rows = dashboardMapper.sumExpenseByCategory(userId, range.start(), range.end());
        if (rows.isEmpty()) {
            return List.of();
        }

        BigDecimal total = ZERO;
        for (Map<String, Object> row : rows) {
            total = total.add(toAmount(row.get("amount")));
        }
        if (total.signum() == 0) {
            return List.of();
        }

        List<ExpenseByCategoryResponse> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            BigDecimal amount = toAmount(row.get("amount"));
            // 占比统一保留两位小数，前端直接展示，不再二次计算
            double percentage = amount.multiply(BigDecimal.valueOf(100))
                    .divide(total, 2, RoundingMode.HALF_UP)
                    .doubleValue();
            result.add(new ExpenseByCategoryResponse(
                    toLong(row.get("category_id")),
                    String.valueOf(row.get("category_name")),
                    money(amount),
                    percentage));
        }
        return result;
    }

    /**
     * 解析日期范围：
     * <ul>
     *   <li>都不传：当前自然月</li>
     *   <li>只传 startDate：该月月初到月末</li>
     *   <li>只传 endDate：该月月初到 endDate</li>
     * </ul>
     * 显式范围包含首尾，并限制最大跨度。
     */
    private DateRange resolveRange(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate start;
        LocalDate end;

        if (startDate == null && endDate == null) {
            start = today.withDayOfMonth(1);
            end = today.withDayOfMonth(today.lengthOfMonth());
        } else if (startDate == null) {
            start = endDate.withDayOfMonth(1);
            end = endDate;
        } else if (endDate == null) {
            start = startDate;
            end = startDate.withDayOfMonth(startDate.lengthOfMonth());
        } else {
            start = startDate;
            end = endDate;
        }

        if (start.isAfter(end)) {
            throw validation("开始日期不能晚于结束日期", Map.of("startDate", "不能晚于 endDate"));
        }
        if (ChronoUnit.DAYS.between(start, end) > MAX_RANGE_DAYS) {
            throw validation("统计范围不能超过 " + MAX_RANGE_DAYS + " 天",
                    Map.of("range", "范围过大"));
        }
        return new DateRange(start, end);
    }

    /** Map 中的聚合值可能因 JDBC 驱动不同而是 BigDecimal/其他 Number。 */
    private BigDecimal toAmount(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        return ZERO;
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private String money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private BusinessException validation(String message, Map<String, Object> details) {
        return new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, details);
    }

    private record DateRange(LocalDate start, LocalDate end) {
    }
}
