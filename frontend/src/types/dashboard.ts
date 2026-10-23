export interface DashboardSummary {
  totalIncome: string
  totalExpense: string
  balance: string
  startDate: string
  endDate: string
}

export interface TrendPoint {
  date: string
  income: string
  expense: string
}

export interface ExpenseByCategory {
  categoryId: number
  categoryName: string
  amount: string
  /** 0–100，后端已保留两位小数 */
  percentage: number
}

export interface DashboardDateParams {
  startDate: string
  endDate: string
}
