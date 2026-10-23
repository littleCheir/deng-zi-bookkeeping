import type { TransactionType } from './api'

export interface Category {
  id: number
  name: string
  type: TransactionType
  icon: string | null
  sortOrder: number
  isSystem: boolean
  isActive: boolean
}

export interface CreateCategoryPayload {
  name: string
  type: TransactionType
  icon: string | null
  sortOrder: number
}

export interface UpdateCategoryPayload {
  name: string
  icon: string | null
  sortOrder: number
}
