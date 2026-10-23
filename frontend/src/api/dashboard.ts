import { http } from './http'
import type {
  DashboardDateParams,
  DashboardSummary,
  ExpenseByCategory,
  TrendPoint
} from '@/types/dashboard'

/** 收支概览；后端默认当前自然月，显式范围包含首尾。 */
export function getSummary(params: DashboardDateParams): Promise<DashboardSummary> {
  return http.get<DashboardSummary>('/dashboard/summary', { params })
}

/** 每日趋势；后端会补齐范围内没有账单的日期。 */
export function getTrend(params: DashboardDateParams): Promise<TrendPoint[]> {
  return http.get<TrendPoint[]>('/dashboard/trend', {
    params: { ...params, period: 'day' }
  })
}

/** 支出分类占比；总支出为 0 时返回空数组。 */
export function getExpenseByCategory(params: DashboardDateParams): Promise<ExpenseByCategory[]> {
  return http.get<ExpenseByCategory[]>('/dashboard/expense-by-category', { params })
}
