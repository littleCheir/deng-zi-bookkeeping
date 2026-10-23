<script setup lang="ts">
import { RouterView } from 'vue-router'
import LoadingState from '@/components/LoadingState.vue'
import { useAuthStore } from '@/stores/auth'

// 首次导航完成前显示加载态，避免登录页与受保护页面之间出现闪烁
const auth = useAuthStore()
</script>

<template>
  <LoadingState v-if="!auth.initialized" message="正在检查登录状态…" />
  <RouterView v-else />
</template>
