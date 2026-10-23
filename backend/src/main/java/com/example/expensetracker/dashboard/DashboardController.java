package com.example.expensetracker.dashboard;

import com.example.expensetracker.common.api.ApiResponse;
import com.example.expensetracker.dashboard.dto.DashboardSummaryResponse;
import com.example.expensetracker.dashboard.dto.ExpenseByCategoryResponse;
import com.example.expensetracker.dashboard.dto.TrendPointResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页统计接口（均需登录，数据严格限定当前用户）：
 * <ul>
 *   <li>{@code GET /api/v1/dashboard/summary}</li>
 *   <li>{@code GET /api/v1/dashboard/trend?period=day}</li>
 *   <li>{@code GET /api/v1/dashboard/expense-by-category}</li>
 * </ul>
 * 日期参数均可省略，缺省为当前自然月；显式范围包含首尾。
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary(
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(dashboardService.summary(startDate, endDate));
    }

    @GetMapping("/trend")
    public ApiResponse<List<TrendPointResponse>> trend(
            @RequestParam(name = "period", defaultValue = "day") String period,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(dashboardService.trend(period, startDate, endDate));
    }

    @GetMapping("/expense-by-category")
    public ApiResponse<List<ExpenseByCategoryResponse>> expenseByCategory(
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(dashboardService.expenseByCategory(startDate, endDate));
    }
}
