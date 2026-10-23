import type { TransactionType } from './api'

export interface Transaction {
  id: number
  type: TransactionType
  categoryId: number
  categoryName: string
  /** 十进制字符串，例如 "35.50"；前端不参与金额计算。 */
  amount: string
  /** YYYY-MM-DD */
  transactionDate: string
  note: string | null
  /** 服务端创建时间，例如 "2026-10-23T12:30:00" */
  createdAt: string
}

/** 新增/编辑账单的请求体；不包含 userId。 */
export interface TransactionPayload {
  type: TransactionType
  categoryId: number
  amount: string
  transactionDate: string
  note: string | null
}

export interface TransactionQuery {
  page: number
  size: number
  startDate?: string
  endDate?: string
  type?: TransactionType
  categoryId?: number
}

export interface PageResult<T> {
  items: T[]
  page: number
  size: number
  total: number
}
