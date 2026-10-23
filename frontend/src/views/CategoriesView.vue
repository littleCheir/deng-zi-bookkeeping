<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteCategory, getCategories } from '@/api/categories'
import { ApiClientError } from '@/api/http'
import CategoryForm from '@/components/CategoryForm.vue'
import ErrorState from '@/components/ErrorState.vue'
import LoadingState from '@/components/LoadingState.vue'
import type { TransactionType } from '@/types/api'
import type { Category } from '@/types/category'

const activeType = ref<TransactionType>('EXPENSE')
const categories = ref<Category[]>([])
const loading = ref(false)
const errorMessage = ref('')

const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const editingCategory = ref<Category | null>(null)

async function loadCategories(): Promise<void> {
  loading.value = true
  errorMessage.value = ''
  try {
    categories.value = await getCategories(activeType.value)
  } catch (error) {
    errorMessage.value =
      error instanceof ApiClientError ? error.message : '分类加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(loadCategories)
watch(activeType, loadCategories)

/** el-radio-group 的 change 值类型较宽，这里收敛为 TransactionType。 */
function onTypeChange(value: string | number | boolean): void {
  activeType.value = value === 'INCOME' ? 'INCOME' : 'EXPENSE'
}

function openCreate(): void {
  formMode.value = 'create'
  editingCategory.value = null
  formVisible.value = true
}

function openEdit(category: Category): void {
  formMode.value = 'edit'
  editingCategory.value = category
  formVisible.value = true
}

async function onSaved(): Promise<void> {
  // 新增/编辑后刷新当前收支类型的列表
  await loadCategories()
}

async function onDelete(category: Category): Promise<void> {
  try {
    await ElMessageBox.confirm(
      `确定删除分类「${category.name}」吗？已被账单使用的分类无法删除。`,
      '删除分类',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }

  try {
    await deleteCategory(category.id)
    ElMessage.success('分类已删除')
    await loadCategories()
  } catch (error) {
    if (error instanceof ApiClientError) {
      // 409：分类已被账单引用；403：系统分类
      if (error.code === 'CATEGORY_IN_USE') {
        ElMessage.error('该分类已被账单使用，无法删除')
      } else if (error.code === 'FORBIDDEN') {
        ElMessage.error('系统分类不可删除')
      } else {
        ElMessage.error(error.message)
      }
    } else {
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}
</script>

<template>
  <section class="page">
    <div class="card category-panel">
      <div class="category-toolbar">
        <el-radio-group :model-value="activeType" size="default" @change="onTypeChange">
          <el-radio-button value="EXPENSE">支出</el-radio-button>
          <el-radio-button value="INCOME">收入</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="openCreate">新增分类</el-button>
      </div>

      <p class="category-hint">系统分类不可编辑或删除；个人分类仅自己可见。</p>

      <LoadingState v-if="loading" message="分类加载中…" />

      <ErrorState
        v-else-if="errorMessage"
        :message="errorMessage"
        @retry="loadCategories"
      />

      <el-empty v-else-if="categories.length === 0" description="暂无分类" />

      <ul v-else class="category-list">
        <li v-for="category in categories" :key="category.id" class="category-item">
          <span class="category-item__icon" aria-hidden="true">{{ category.icon || '•' }}</span>

          <div class="category-item__main">
            <div class="category-item__name">{{ category.name }}</div>
            <div class="category-item__meta">排序 {{ category.sortOrder }}</div>
          </div>

          <div class="category-item__actions">
            <el-tag v-if="category.isSystem" size="small" type="info">系统</el-tag>
            <template v-else>
              <el-button text size="small" @click="openEdit(category)">编辑</el-button>
              <el-button text size="small" type="danger" @click="onDelete(category)">删除</el-button>
            </template>
          </div>
        </li>
      </ul>
    </div>

    <CategoryForm
      v-model="formVisible"
      :mode="formMode"
      :category="editingCategory"
      :default-type="activeType"
      @saved="onSaved"
    />
  </section>
</template>

<style scoped>
.category-panel {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.category-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.category-hint {
  margin: 0;
  color: var(--color-text-tertiary);
  font-size: 12px;
}

.category-list {
  list-style: none;
  margin: 0;
  padding: 0;
  border-top: 1px solid var(--color-border);
}

.category-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--color-border);
}

.category-item__icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--color-muted-bg);
  font-size: 16px;
}

.category-item__main {
  flex: 1;
  min-width: 0;
}

.category-item__name {
  font-size: 15px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-item__meta {
  color: var(--color-text-tertiary);
  font-size: 12px;
}

.category-item__actions {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex-shrink: 0;
}
</style>
