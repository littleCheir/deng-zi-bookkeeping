<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategories } from '@/api/categories'
import { deleteTransaction, getTransactions } from '@/api/transactions'
import { ApiClientError } from '@/api/http'
import ErrorState from '@/components/ErrorState.vue'
import LoadingState from '@/components/LoadingState.vue'
import TransactionForm from '@/components/TransactionForm.vue'
import TransactionTable from '@/components/TransactionTable.vue'
import type { TransactionType } from '@/types/api'
import type { Category } from '@/types/category'
import type { Transaction, TransactionQuery } from '@/types/transaction'

const items = ref<Transaction[]>([])
const total = ref(0)
const loading = ref(false)
const errorMessage = ref('')

const query = reactive({ page: 1, size: 20 })

const dateRange = ref<[string, string] | null>(null)
const typeFilter = ref<'ALL' | TransactionType>('ALL')
const categoryFilter = ref<number | null>(null)

const categories = ref<Category[]>([])
const filteredCategories = computed(() =>
  typeFilter.value === 'ALL'
    ? categories.value
    : categories.value.filter((category) => category.type === typeFilter.value)
)

const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const editingTransaction = ref<Transaction | null>(null)

async function load(): Promise<void> {
  loading.value = true
  errorMessage.value = ''
  try {
    const params: TransactionQuery = { page: query.page, size: query.size }
    if (dateRange.value) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    if (typeFilter.value !== 'ALL') {
      params.type = typeFilter.value
    }
    if (categoryFilter.value != null) {
      params.categoryId = categoryFilter.value
    }

    const result = await getTransactions(params)
    items.value = result.items
    total.value = result.total
    query.page = result.page
    query.size = result.size
  } catch (error) {
    errorMessage.value =
      error instanceof ApiClientError ? error.message : '账单加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function loadCategories(): Promise<void> {
  // 分类筛选是辅助功能，失败时不阻塞账单列表
  try {
    categories.value = await getCategories()
  } catch {
    categories.value = []
  }
}

onMounted(async () => {
  await Promise.all([load(), loadCategories()])
})

function onDateRangeChange(value: unknown): void {
  if (
    Array.isArray(value) &&
    value.length === 2 &&
    typeof value[0] === 'string' &&
    typeof value[1] === 'string'
  ) {
    dateRange.value = [value[0], value[1]]
  } else {
    dateRange.value = null
  }
}

function onTypeFilterChange(value: string | number | boolean): void {
  typeFilter.value = value === 'INCOME' || value === 'EXPENSE' ? value : 'ALL'

  // 类型切换后若已选分类不匹配，清空分类筛选
  if (categoryFilter.value != null) {
    const selected = categories.value.find((category) => category.id === categoryFilter.value)
    if (!selected || (typeFilter.value !== 'ALL' && selected.type !== typeFilter.value)) {
      categoryFilter.value = null
    }
  }
}

function onCategoryFilterChange(value: number | string | null | undefined): void {
  categoryFilter.value = typeof value === 'number' ? value : null
}

function search(): void {
  query.page = 1
  void load()
}

function reset(): void {
  dateRange.value = null
  typeFilter.value = 'ALL'
  categoryFilter.value = null
  query.page = 1
  void load()
}

function onPageChange(page: number): void {
  query.page = page
  void load()
}

function onSizeChange(size: number): void {
  query.size = size
  query.page = 1
  void load()
}

function openCreate(): void {
  formMode.value = 'create'
  editingTransaction.value = null
  formVisible.value = true
}

function openEdit(transaction: Transaction): void {
  formMode.value = 'edit'
  editingTransaction.value = transaction
  formVisible.value = true
}

async function onSaved(): Promise<void> {
  // 新增后回到第一页，确保能看到刚创建的记录；编辑则停留在当前页
  if (formMode.value === 'create') {
    query.page = 1
  }
  await load()
  await loadCategories()
}

async function onDelete(transaction: Transaction): Promise<void> {
  try {
    await ElMessageBox.confirm(
      `确定删除「${transaction.categoryName}」${transaction.transactionDate} 的账单吗？`,
      '删除账单',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }

  try {
    await deleteTransaction(transaction.id)
    ElMessage.success('账单已删除')
    // 当前页只剩这一条时回到上一页，避免出现空页
    if (items.value.length === 1 && query.page > 1) {
      query.page -= 1
    }
    await load()
  } catch (error) {
    ElMessage.error(error instanceof ApiClientError ? error.message : '删除失败，请稍后重试')
  }
}
</script>

<template>
  <section class="page">
    <div class="card txn-panel">
      <div class="txn-toolbar">
        <h1 class="txn-title">账单</h1>
        <el-button type="primary" @click="openCreate">新增账单</el-button>
      </div>

      <div class="txn-filters">
        <el-date-picker
          :model-value="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          unlink-panels
          class="txn-filter-item txn-filter-item--date"
          @update:model-value="onDateRangeChange"
        />

        <el-select
          :model-value="typeFilter"
          class="txn-filter-item"
          @change="onTypeFilterChange"
        >
          <el-option label="全部类型" value="ALL" />
          <el-option label="支出" value="EXPENSE" />
          <el-option label="收入" value="INCOME" />
        </el-select>

        <el-select
          :model-value="categoryFilter"
          placeholder="全部分类"
          clearable
          class="txn-filter-item"
          @change="onCategoryFilterChange"
        >
          <el-option
            v-for="category in filteredCategories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>

        <div class="txn-filter-actions">
          <el-button type="primary" plain @click="search">查询</el-button>
          <el-button text @click="reset">重置</el-button>
        </div>
      </div>
    </div>

    <div class="card txn-panel">
      <LoadingState v-if="loading" message="账单加载中…" />

      <ErrorState v-else-if="errorMessage" :message="errorMessage" @retry="load" />

      <el-empty v-else-if="items.length === 0" description="暂无账单" />

      <template v-else>
        <TransactionTable :items="items" @edit="openEdit" @delete="onDelete" />

        <div class="txn-pagination">
          <el-pagination
            :current-page="query.page"
            :page-size="query.size"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            background
            @current-change="onPageChange"
            @size-change="onSizeChange"
          />
        </div>
      </template>
    </div>

    <TransactionForm
      v-model="formVisible"
      :mode="formMode"
      :transaction="editingTransaction"
      :default-type="typeFilter === 'ALL' ? 'EXPENSE' : typeFilter"
      @saved="onSaved"
    />
  </section>
</template>

<style scoped>
.txn-panel {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.txn-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.txn-title {
  margin: 0;
  font-size: 18px;
}

.txn-filters {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.txn-filter-item {
  width: 130px;
}

.txn-filter-item--date {
  width: 260px;
  max-width: 100%;
}

.txn-filter-actions {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.txn-pagination {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: var(--space-2);
  padding-top: var(--space-2);
}

@media (max-width: 480px) {
  .txn-filter-item,
  .txn-filter-item--date {
    width: 100%;
  }

  .txn-pagination {
    justify-content: center;
  }
}
</style>
