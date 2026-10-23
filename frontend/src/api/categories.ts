import { http } from './http'
import type { TransactionType } from '@/types/api'
import type { Category, CreateCategoryPayload, UpdateCategoryPayload } from '@/types/category'

/**
 * 查询当前用户可见的分类（系统分类 + 个人分类）。
 * 不传 type 时返回收入与支出两类；服务端按 type、sortOrder、id 稳定排序。
 */
export function getCategories(type?: TransactionType): Promise<Category[]> {
  return http.get<Category[]>('/categories', {
    params: type ? { type } : undefined
  })
}

/** 创建个人分类；归属与 isActive 由服务端决定，请求体不包含 userId/isSystem。 */
export function createCategory(payload: CreateCategoryPayload): Promise<Category> {
  return http.post<Category>('/categories', payload)
}

/** 编辑个人分类；只允许 name/icon/sortOrder，type 不可修改。 */
export function updateCategory(id: number, payload: UpdateCategoryPayload): Promise<Category> {
  return http.put<Category>(`/categories/${id}`, payload)
}

/** 删除个人分类；被账单引用时后端返回 409 CATEGORY_IN_USE。 */
export function deleteCategory(id: number): Promise<void> {
  return http.delete<void>(`/categories/${id}`)
}
