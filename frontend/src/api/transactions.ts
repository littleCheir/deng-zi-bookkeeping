import { http } from './http'
import type {
  PageResult,
  Transaction,
  TransactionPayload,
  TransactionQuery
} from '@/types/transaction'

/**
 * 分页查询当前用户账单。
 * 日期范围包含首尾；不传 startDate/endDate 表示不限；默认按日期倒序、同日 id 倒序。
 */
export function getTransactions(query: TransactionQuery): Promise<PageResult<Transaction>> {
  return http.get<PageResult<Transaction>>('/transactions', { params: query })
}

/** 查询单笔账单；不存在或跨用户由后端返回 404。 */
export function getTransaction(id: number): Promise<Transaction> {
  return http.get<Transaction>(`/transactions/${id}`)
}

/** 新增账单；userId 由服务端从登录态写入。 */
export function createTransaction(payload: TransactionPayload): Promise<Transaction> {
  return http.post<Transaction>('/transactions', payload)
}

/** 编辑账单；后端会重新校验分类归属、启用状态与类型。 */
export function updateTransaction(id: number, payload: TransactionPayload): Promise<Transaction> {
  return http.put<Transaction>(`/transactions/${id}`, payload)
}

/** 删除账单；只能删除当前用户自己的记录。 */
export function deleteTransaction(id: number): Promise<void> {
  return http.delete<void>(`/transactions/${id}`)
}
