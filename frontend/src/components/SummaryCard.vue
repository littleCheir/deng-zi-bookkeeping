<script setup lang="ts">
import { formatCurrency } from '@/utils/currency'

const props = withDefaults(
  defineProps<{
    label: string
    value: string
    tone?: 'income' | 'expense' | 'balance'
  }>(),
  { tone: 'balance' }
)
</script>

<template>
  <div class="card summary-card">
    <span class="summary-card__label">{{ props.label }}</span>
    <strong class="summary-card__value" :class="`summary-card__value--${props.tone}`">
      {{ formatCurrency(props.value) }}
    </strong>
  </div>
</template>

<style scoped>
.summary-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  min-width: 0;
}

.summary-card__label {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.summary-card__value {
  font-size: 22px;
  font-weight: 600;
  line-height: 1.2;
  overflow-wrap: anywhere;
}

.summary-card__value--income {
  color: var(--color-income);
}

.summary-card__value--expense {
  color: var(--color-expense);
}

.summary-card__value--balance {
  color: var(--color-text);
}
</style>
