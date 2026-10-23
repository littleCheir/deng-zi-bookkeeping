<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import dayjs from 'dayjs'
import { getExpenseByCategory, getSummary, getTrend } from '@/api/dashboard'
import { getTransactions } from '@/api/transactions'
import { ApiClientError } from '@/api/http'
import SummaryCard from '@/components/SummaryCard.vue'
import TrendBarChart from '@/components/TrendBarChart.vue'
import ExpenseCategoryChart from '@/components/ExpenseCategoryChart.vue'
import ErrorState from '@/components/ErrorState.vue'
import LoadingState from '@/components/LoadingState.vue'
import { useDateRange } from '@/composables/useDateRange'
import type { DashboardSummary, ExpenseByCategory, TrendPoint } from '@/types/dashboard'
import type { Transaction } from '@/types/transaction'
import { formatSignedCurrency } from '@/utils/currency'
import { formatDate } from '@/utils/date'

const { month, customRange, range, setMonth, setCustomRange, clearCustomRange, resetToCurrentMonth } =
  useDateRange()

const summary = ref<DashboardSummary | null>(null)
const summaryLoading = ref(false)
const summaryError = ref('')

const trend = ref<TrendPoint[]>([])
const trendLoading = ref(false)
const trendError = ref('')

const expenseItems = ref<ExpenseByCategory[]>([])
const expenseLoading = ref(false)
const expenseError = ref('')

const recent = ref<Transaction[]>([])
const recentLoading = ref(false)
const recentError = ref('')

function messageOf(error: unknown, fallback: string): string {
  return error instanceof ApiClientError ? error.message : fallback
}

/**
 * 四个模块各自维护 loading/error，单个接口失败不影响其他模块展示。
 */
async function loadSummary(): Promise<void> {
  summaryLoading.value = true
  summaryError.value = ''
  try {
    summary.value = await getSummary(range.value)
  } catch (error) {
    summary.value = null
    summaryError.value = messageOf(error, '收支统计加载失败')
  } finally {
    summaryLoading.value = false
  }
}

async function loadTrend(): Promise<void> {
  trendLoading.value = true
  trendError.value = ''
  try {
    trend.value = await getTrend(range.value)
  } catch (error) {
    trend.value = []
    trendError.value = messageOf(error, '趋势数据加载失败')
  } finally {
    trendLoading.value = false
  }
}

async function loadExpense(): Promise<void> {
  expenseLoading.value = true
  expenseError.value = ''
  try {
    expenseItems.value = await getExpenseByCategory(range.value)
  } catch (error) {
    expenseItems.value = []
    expenseError.value = messageOf(error, '分类统计加载失败')
  } finally {
    expenseLoading.value = false
  }
}

async function loadRecent(): Promise<void> {
  recentLoading.value = true
  recentError.value = ''
  try {
    // 最近记录复用账单列表接口，默认排序即日期倒序、同日 id 倒序
    const result = await getTransactions({ page: 1, size: 10 })
    recent.value = result.items
  } catch (error) {
    recent.value = []
    recentError.value = messageOf(error, '最近记录加载失败')
  } finally {
    recentLoading.value = false
  }
}

async function loadAll(): Promise<void> {
  await Promise.all([loadSummary(), loadTrend(), loadExpense(), loadRecent()])
}

onMounted(loadAll)

// 月份或自定义范围变化后统一刷新四个模块
watch(range, () => {
  void loadAll()
})

function onMonthChange(value: string | number | Date | null): void {
  if (typeof value === 'string') {
    setMonth(value)
  } else if (value instanceof Date) {
    setMonth(dayjs(value).format('YYYY-MM'))
  }
}

function onCustomRangeChange(value: unknown): void {
  if (
    Array.isArray(value) &&
    value.length === 2 &&
    typeof value[0] === 'string' &&
    typeof value[1] === 'string'
  ) {
    setCustomRange(value[0], value[1])
  } else {
    // 清空自定义范围时回到当前选择的月份
    clearCustomRange()
  }
}

function onResetRange(): void {
  resetToCurrentMonth()
}
</script>

<template>
  <section class="page dashboard">
    <div class="card dashboard-toolbar">
      <div class="dashboard-controls">
        <el-date-picker
          :model-value="month"
          type="month"
          format="YYYY-MM"
          value-format="YYYY-MM"
          :clearable="false"
          class="dashboard-month"
          @update:model-value="onMonthChange"
        />
        <el-date-picker
          :model-value="customRange"
          type="daterange"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          unlink-panels
          class="dashboard-range"
          @update:model-value="onCustomRangeChange"
        />
        <el-button text @click="onResetRange">回到本月</el-button>
      </div>
      <span class="dashboard-range-label">{{ range.startDate }} ~ {{ range.endDate }}</span>
    </div>

    <div class="summary-grid">
      <template v-if="summary">
        <SummaryCard label="收入" :value="summary.totalIncome" tone="income" />
        <SummaryCard label="支出" :value="summary.totalExpense" tone="expense" />
        <SummaryCard label="结余" :value="summary.balance" tone="balance" />
      </template>
      <LoadingState v-else-if="summaryLoading" message="收支统计加载中…" />
      <ErrorState v-else-if="summaryError" :message="summaryError" @retry="loadSummary" />
    </div>

    <div class="chart-grid">
      <div class="card chart-card">
        <h2 class="chart-title">收支趋势</h2>
        <LoadingState v-if="trendLoading" message="趋势加载中…" />
        <ErrorState v-else-if="trendError" :message="trendError" @retry="loadTrend" />
        <TrendBarChart v-else :points="trend" />
      </div>

      <div class="card chart-card">
        <h2 class="chart-title">支出分类</h2>
        <LoadingState v-if="expenseLoading" message="分类统计加载中…" />
        <ErrorState v-else-if="expenseError" :message="expenseError" @retry="loadExpense" />
        <ExpenseCategoryChart v-else :items="expenseItems" />
      </div>
    </div>

    <div class="card">
      <div class="recent-header">
        <h2 class="chart-title">最近记录</h2>
        <RouterLink class="recent-link" to="/transactions">查看全部</RouterLink>
      </div>

      <LoadingState v-if="recentLoading" message="最近记录加载中…" />
      <ErrorState v-else-if="recentError" :message="recentError" @retry="loadRecent" />
      <el-empty v-else-if="recent.length === 0" description="暂无账单" />

      <ul v-else class="recent-list">
        <li v-for="item in recent" :key="item.id" class="recent-item">
          <div class="recent-item__row">
            <el-tag :type="item.type === 'INCOME' ? 'success' : 'danger'" size="small">
              {{ item.type === 'INCOME' ? '收入' : '支出' }}
            </el-tag>
            <span class="recent-item__category">{{ item.categoryName }}</span>
            <span
              class="recent-item__amount"
              :class="
                item.type === 'INCOME'
                  ? 'recent-item__amount--income'
                  : 'recent-item__amount--expense'
              "
            >
              {{ formatSignedCurrency(item.type, item.amount) }}
            </span>
          </div>
          <div class="recent-item__meta">
            <span>{{ formatDate(item.transactionDate) }}</span>
            <span v-if="item.note" class="recent-item__note">{{ item.note }}</span>
          </div>
        </li>
      </ul>
    </div>
  </section>
</template>

<style scoped>
.dashboard {
  gap: var(--space-4);
}

.dashboard-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.dashboard-controls {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.dashboard-month {
  width: 130px;
}

.dashboard-range {
  width: 260px;
  max-width: 100%;
}

.dashboard-range-label {
  color: var(--color-text-tertiary);
  font-size: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.summary-grid > :only-child {
  grid-column: 1 / -1;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}

.chart-card {
  min-width: 0;
}

.chart-title {
  margin: 0 0 var(--space-2);
  font-size: 15px;
}

.recent-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.recent-link {
  color: var(--color-primary);
  font-size: 13px;
  text-decoration: none;
}

.recent-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.recent-item {
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--color-border);
}

.recent-item:last-child {
  border-bottom: none;
}

.recent-item__row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.recent-item__category {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-item__amount {
  margin-left: auto;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
}

.recent-item__amount--income {
  color: var(--color-income);
}

.recent-item__amount--expense {
  color: var(--color-expense);
}

.recent-item__meta {
  margin-top: var(--space-1);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--color-text-tertiary);
  font-size: 12px;
  min-width: 0;
}

.recent-item__note {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 720px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-month,
  .dashboard-range {
    width: 100%;
  }
}
</style>
