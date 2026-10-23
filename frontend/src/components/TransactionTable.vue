<script setup lang="ts">
import type { Transaction } from '@/types/transaction'
import { formatSignedCurrency } from '@/utils/currency'
import { formatDate } from '@/utils/date'

defineProps<{ items: Transaction[] }>()

const emit = defineEmits<{
  edit: [transaction: Transaction]
  delete: [transaction: Transaction]
}>()
</script>

<template>
  <ul class="txn-list">
    <li v-for="item in items" :key="item.id" class="txn-item">
      <div class="txn-item__main">
        <div class="txn-item__row">
          <el-tag :type="item.type === 'INCOME' ? 'success' : 'danger'" size="small">
            {{ item.type === 'INCOME' ? '收入' : '支出' }}
          </el-tag>
          <span class="txn-item__category">{{ item.categoryName }}</span>
          <span
            class="txn-item__amount"
            :class="item.type === 'INCOME' ? 'txn-item__amount--income' : 'txn-item__amount--expense'"
          >
            {{ formatSignedCurrency(item.type, item.amount) }}
          </span>
        </div>
        <div class="txn-item__meta">
          <span>{{ formatDate(item.transactionDate) }}</span>
          <span v-if="item.note" class="txn-item__note">{{ item.note }}</span>
        </div>
      </div>

      <div class="txn-item__actions">
        <el-button text size="small" @click="emit('edit', item)">编辑</el-button>
        <el-button text size="small" type="danger" @click="emit('delete', item)">删除</el-button>
      </div>
    </li>
  </ul>
</template>

<style scoped>
.txn-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.txn-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--color-border);
}

.txn-item:last-child {
  border-bottom: none;
}

.txn-item__main {
  flex: 1;
  min-width: 0;
}

.txn-item__row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.txn-item__category {
  font-size: 15px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.txn-item__amount {
  margin-left: auto;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}

.txn-item__amount--income {
  color: var(--color-income);
}

.txn-item__amount--expense {
  color: var(--color-expense);
}

.txn-item__meta {
  margin-top: var(--space-1);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--color-text-tertiary);
  font-size: 12px;
  min-width: 0;
}

.txn-item__note {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.txn-item__actions {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex-shrink: 0;
}
</style>
