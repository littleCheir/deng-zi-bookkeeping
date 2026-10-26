<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getCategories } from '@/api/categories'
import { createTransaction, updateTransaction } from '@/api/transactions'
import { ApiClientError } from '@/api/http'
import ErrorState from '@/components/ErrorState.vue'
import type { TransactionType } from '@/types/api'
import type { Category } from '@/types/category'
import type { Transaction, TransactionPayload } from '@/types/transaction'
import { todayString, toDateString } from '@/utils/date'
import { validateAmount } from '@/utils/validation'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  transaction: Transaction | null
  defaultType: TransactionType
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const formError = ref('')
const amountError = ref('')

const categories = ref<Category[]>([])
const categoriesLoading = ref(false)
const categoriesError = ref('')

const form = reactive({
  type: 'EXPENSE' as TransactionType,
  categoryId: null as number | null,
  amount: '',
  transactionDate: todayString(),
  note: ''
})

const rules: FormRules = {
  type: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  transactionDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  note: [{ max: 200, message: '备注不能超过 200 个字符', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  async (visible) => {
    if (!visible) {
      return
    }
    formError.value = ''
    amountError.value = ''

    if (props.mode === 'edit' && props.transaction) {
      form.type = props.transaction.type
      form.categoryId = props.transaction.categoryId
      form.amount = props.transaction.amount
      form.transactionDate = props.transaction.transactionDate
      form.note = props.transaction.note ?? ''
    } else {
      form.type = props.defaultType
      form.categoryId = null
      form.amount = ''
      form.transactionDate = todayString()
      form.note = ''
    }
    formRef.value?.clearValidate()
    await loadCategories(form.type)
  }
)

async function loadCategories(type: TransactionType): Promise<void> {
  categoriesLoading.value = true
  categoriesError.value = ''
  try {
    categories.value = await getCategories(type)
    // 编辑时若原分类不在当前列表（例如已停用），清空选择，避免提交非法组合
    if (form.categoryId != null && !categories.value.some((item) => item.id === form.categoryId)) {
      form.categoryId = null
    }
  } catch (error) {
    categories.value = []
    categoriesError.value =
      error instanceof ApiClientError ? error.message : '分类加载失败，请稍后重试'
  } finally {
    categoriesLoading.value = false
  }
}

/** 切换收支类型必须清空已选分类，并重新加载对应类型的分类。 */
function onTypeChange(value: string | number | boolean | undefined): void {
  const next: TransactionType = value === 'INCOME' ? 'INCOME' : 'EXPENSE'
  if (next === form.type) {
    return
  }
  form.type = next
  form.categoryId = null
  void loadCategories(next)
}

function onCategoryChange(value: number | string | null | undefined): void {
  form.categoryId = typeof value === 'number' ? value : null
}

/** el-date-picker 在 value-format 下返回字符串；Date 兜底转换成本地日期。 */
function onDateChange(value: string | number | Date | null): void {
  if (typeof value === 'string') {
    form.transactionDate = value
  } else if (value instanceof Date) {
    form.transactionDate = toDateString(value)
  } else {
    form.transactionDate = ''
  }
}

function onVisibleChange(value: boolean): void {
  emit('update:modelValue', value)
}

function close(): void {
  emit('update:modelValue', false)
}

async function submit(): Promise<void> {
  if (submitting.value || !formRef.value) {
    return
  }

  formError.value = ''
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  const amountMessage = validateAmount(form.amount)
  if (amountMessage) {
    amountError.value = amountMessage
    return
  }
  if (form.categoryId == null) {
    formError.value = '请选择分类'
    return
  }

  const payload: TransactionPayload = {
    type: form.type,
    categoryId: form.categoryId,
    amount: form.amount.trim(),
    transactionDate: form.transactionDate,
    note: form.note.trim() ? form.note.trim() : null
  }

  submitting.value = true
  try {
    if (props.mode === 'create') {
      await createTransaction(payload)
    } else if (props.transaction) {
      await updateTransaction(props.transaction.id, payload)
    }
    emit('saved')
    close()
  } catch (error) {
    formError.value = error instanceof ApiClientError ? error.message : '保存失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="props.modelValue"
    :title="props.mode === 'create' ? '新增账单' : '编辑账单'"
    width="min(460px, 94vw)"
    :close-on-click-modal="false"
    @update:model-value="onVisibleChange"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
      <el-form-item label="收支类型" prop="type">
        <el-radio-group :model-value="form.type" @change="onTypeChange">
          <el-radio-button value="EXPENSE">支出</el-radio-button>
          <el-radio-button value="INCOME">收入</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="分类" prop="categoryId">
        <el-select
          :model-value="form.categoryId"
          placeholder="请选择分类"
          style="width: 100%"
          :loading="categoriesLoading"
          :disabled="categoriesLoading || categories.length === 0"
          @change="onCategoryChange"
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.icon ? `${category.icon} ${category.name}` : category.name"
            :value="category.id"
          />
        </el-select>
        <p v-if="categoriesError" class="field-hint field-hint--error">{{ categoriesError }}</p>
        <p v-else-if="!categoriesLoading && categories.length === 0" class="field-hint">
          该收支类型下暂无分类，请先到「分类」页添加。
        </p>
      </el-form-item>

      <el-form-item label="金额" prop="amount">
        <el-input
          v-model="form.amount"
          inputmode="decimal"
          maxlength="13"
          placeholder="例如 35.50"
          @input="amountError = ''"
        />
        <p v-if="amountError" class="field-hint field-hint--error">{{ amountError }}</p>
      </el-form-item>

      <el-form-item label="日期" prop="transactionDate">
        <el-date-picker
          :model-value="form.transactionDate"
          type="date"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :clearable="false"
          style="width: 100%"
          @update:model-value="onDateChange"
        />
      </el-form-item>

      <el-form-item label="备注（可选）" prop="note">
        <el-input
          v-model="form.note"
          type="textarea"
          :rows="3"
          maxlength="200"
          show-word-limit
          placeholder="最多 200 个字符"
        />
      </el-form-item>

      <ErrorState v-if="formError" :message="formError" :retryable="false" />
    </el-form>

    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">
        {{ submitting ? '保存中…' : '保存' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.field-hint {
  margin: var(--space-1) 0 0;
  color: var(--color-text-tertiary);
  font-size: 12px;
  line-height: 1.5;
}

.field-hint--error {
  color: var(--color-expense);
}
</style>
