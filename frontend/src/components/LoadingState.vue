<script setup lang="ts">
const props = withDefaults(defineProps<{ message?: string }>(), {
  message: '加载中…'
})
</script>

<template>
  <div class="loading-state" role="status" aria-live="polite">
    <span class="loading-state__dot" aria-hidden="true" />
    <span>{{ props.message }}</span>
  </div>
</template>

<style scoped>
.loading-state {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  color: var(--color-text-secondary);
  font-size: 14px;
}

.loading-state__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-primary);
  animation: loading-pulse 900ms ease-in-out infinite;
}

@keyframes loading-pulse {
  0%,
  100% {
    opacity: 0.25;
    transform: scale(0.8);
  }

  50% {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
