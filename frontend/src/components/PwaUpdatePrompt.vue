<script setup lang="ts">
import { useRegisterSW } from 'virtual:pwa-register/vue'

const { needRefresh, updateServiceWorker } = useRegisterSW()

function dismiss(): void {
  needRefresh.value = false
}

/** 用户确认后才让新 Service Worker 接管并刷新，避免填写表单时被强制刷新。 */
async function refresh(): Promise<void> {
  await updateServiceWorker(true)
}
</script>

<template>
  <transition name="pwa-update">
    <div v-if="needRefresh" class="pwa-update" role="status">
      <span class="pwa-update__text">应用有新版本，刷新后使用最新版本</span>
      <div class="pwa-update__actions">
        <el-button size="small" type="primary" @click="refresh">刷新</el-button>
        <el-button size="small" text class="pwa-update__later" @click="dismiss">稍后</el-button>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.pwa-update {
  position: fixed;
  left: var(--space-3);
  right: var(--space-3);
  bottom: calc(var(--space-3) + env(safe-area-inset-bottom));
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  max-width: 520px;
  margin: 0 auto;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  background: var(--color-text);
  color: var(--color-surface);
  box-shadow: 0 8px 24px rgb(0 0 0 / 18%);
}

.pwa-update__text {
  font-size: 13px;
  line-height: 1.5;
}

.pwa-update__actions {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex-shrink: 0;
}

.pwa-update__later {
  color: var(--color-surface);
}

.pwa-update-enter-active,
.pwa-update-leave-active {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.pwa-update-enter-from,
.pwa-update-leave-to {
  transform: translateY(12px);
  opacity: 0;
}
</style>
