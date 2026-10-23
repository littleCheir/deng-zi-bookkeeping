<script setup lang="ts">
const props = withDefaults(defineProps<{ message?: string; retryable?: boolean }>(), {
  message: '请求失败，请稍后重试',
  retryable: true
})

const emit = defineEmits<{ retry: [] }>()
</script>

<template>
  <div class="error-state" role="alert">
    <p class="error-state__message">{{ props.message }}</p>
    <el-button v-if="props.retryable" size="small" @click="emit('retry')">重试</el-button>
  </div>
</template>

<style scoped>
.error-state {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3);
  border: 1px solid #f3c2bd;
  border-radius: var(--radius-md);
  background: #fdf3f2;
}

.error-state__message {
  margin: 0;
  color: var(--color-expense);
  font-size: 13px;
  line-height: 1.5;
}
</style>
