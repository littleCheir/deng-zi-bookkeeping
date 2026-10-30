<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

const isOnline = ref(true)

/** 浏览器 online/offline 事件是唯一可靠的网络状态来源，默认按在线处理避免误报。 */
function syncNetworkState(): void {
  isOnline.value = navigator.onLine
}

onMounted(() => {
  syncNetworkState()
  window.addEventListener('online', syncNetworkState)
  window.addEventListener('offline', syncNetworkState)
})

onBeforeUnmount(() => {
  window.removeEventListener('online', syncNetworkState)
  window.removeEventListener('offline', syncNetworkState)
})
</script>

<template>
  <transition name="offline-banner">
    <div v-if="!isOnline" class="offline-banner" role="alert">
      当前无网络，账单与统计需要联网查看
    </div>
  </transition>
</template>

<style scoped>
.offline-banner {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 2000;
  padding: 8px 12px;
  padding-top: calc(8px + env(safe-area-inset-top));
  background: #fff4e5;
  border-bottom: 1px solid #ffd8a8;
  color: #8a4b00;
  text-align: center;
  font-size: 13px;
}

.offline-banner-enter-active,
.offline-banner-leave-active {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.offline-banner-enter-from,
.offline-banner-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}
</style>
