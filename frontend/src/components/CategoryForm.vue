<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { createCategory, updateCategory } from '@/api/categories'
import { ApiClientError } from '@/api/http'
import ErrorState from '@/components/ErrorState.vue'
import type { TransactionType } from '@/types/api'
import type { Category, CreateCategoryPayload, UpdateCategoryPayload } from '@/types/category'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  category: Category | null
  defaultType: TransactionType
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const errorMessage = ref('')

const form = reactive({
  name: '',
  type: 'EXPENSE' as TransactionType,
  icon: '',
  sortOrder: 0
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 50, message: '分类名称不能超过 50 个字符', trigger: 'blur' }
  ],
  icon: [{ max: 50, message: '图标不能超过 50 个字符', trigger: 'blur' }]
}

const typeLabel = computed(() => (form.type === 'INCOME' ? '收入' : '支出'))

/** el-dialog 的 v-model 回传，单独成函数避免在模板里写类型注解。 */
function onVisibleChange(value: boolean): void {
  emit('update:modelValue', value)
}

/** el-radio-group 的 change 事件类型是宽泛的 string | number | boolean，这里收敛为事务类型。 */
function onFormTypeChange(value: string | number | boolean): void {
  form.type = value === 'INCOME' ? 'INCOME' : 'EXPENSE'
}

// 每次打开弹窗都重置表单：编辑时回填，创建时使用当前收支类型作为默认值
watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) {
      return
    }
    errorMessage.value = ''
    if (props.mode === 'edit' && props.category) {
      form.name = props.category.name
      form.type = props.category.type
      form.icon = props.category.icon ?? ''
      form.sortOrder = props.category.sortOrder
    } else {
      form.name = ''
      form.type = props.defaultType
      form.icon = ''
      form.sortOrder = 0
    }
    formRef.value?.clearValidate()
  }
)

function close(): void {
  emit('update:modelValue', false)
}

async function submit(): Promise<void> {
  if (submitting.value || !formRef.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  const name = form.name.trim()
  if (!name) {
    errorMessage.value = '请输入分类名称'
    return
  }
  const icon = form.icon.trim() ? form.icon.trim() : null
  const sortOrder = Number.isFinite(form.sortOrder) ? form.sortOrder : 0

  submitting.value = true
  errorMessage.value = ''
  try {
    if (props.mode === 'create') {
      const payload: CreateCategoryPayload = { name, type: form.type, icon, sortOrder }
      await createCategory(payload)
    } else if (props.category) {
      // 编辑不发送 type：后端会拒绝变更收支类型
      const payload: UpdateCategoryPayload = { name, icon, sortOrder }
      await updateCategory(props.category.id, payload)
    }
    emit('saved')
    close()
  } catch (error) {
    if (error instanceof ApiClientError) {
      // 重名是分类管理里最常见的业务错误，单独给出明确提示
      errorMessage.value =
        error.code === 'CATEGORY_NAME_CONFLICT' ? '同一收支类型下已存在同名分类' : error.message
    } else {
      errorMessage.value = '保存失败，请稍后重试'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="props.modelValue"
    :title="props.mode === 'create' ? '新增分类' : '编辑分类'"
    width="min(420px, 92vw)"
    :close-on-click-modal="false"
    @update:model-value="onVisibleChange"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="例如：餐饮" />
      </el-form-item>

      <el-form-item label="收支类型">
        <el-radio-group
          v-if="props.mode === 'create'"
          :model-value="form.type"
          @change="onFormTypeChange"
        >
          <el-radio-button value="EXPENSE">支出</el-radio-button>
          <el-radio-button value="INCOME">收入</el-radio-button>
        </el-radio-group>
        <el-tag v-else type="info">{{ typeLabel }}（创建后不可修改）</el-tag>
      </el-form-item>

      <el-form-item label="图标（可选）" prop="icon">
        <el-input v-model="form.icon" maxlength="50" placeholder="如 🍜，可留空" />
      </el-form-item>

      <el-form-item label="排序（升序）" prop="sortOrder">
        <el-input-number v-model="form.sortOrder" :min="0" :max="9999" :step="1" />
      </el-form-item>

      <ErrorState v-if="errorMessage" :message="errorMessage" :retryable="false" />
    </el-form>

    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">
        {{ submitting ? '保存中…' : '保存' }}
      </el-button>
    </template>
  </el-dialog>
</template>
