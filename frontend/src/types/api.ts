/**
 * API 基础类型骨架（阶段 0）。
 * 金额在 API 中一律使用十进制字符串，日期一律使用 YYYY-MM-DD 字符串。
 */
export type TransactionType = 'INCOME' | 'EXPENSE'

export interface ApiError {
  code: string
  message: string
  details?: Record<string, unknown>
}

export interface ApiResponse<T> {
  data: T | null
  error: ApiError | null
}
