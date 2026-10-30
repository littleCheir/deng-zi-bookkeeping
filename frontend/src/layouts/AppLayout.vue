<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

/**
 * 登出：即使后端请求失败，也要清除本地状态并回到登录页，避免界面停留在“已登录”。
 */
async function onLogout(): Promise<void> {
  try {
    await auth.logout()
  } catch {
    // 网络失败时 auth.logout 已清除本地登录态，仍然回到登录页
  }
  await router.push({ name: 'login' })
}
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="app-header__inner">
        <RouterLink class="brand" to="/">凳子记账</RouterLink>
        <nav class="nav" aria-label="主导航">
          <RouterLink class="nav__link" to="/">首页</RouterLink>
          <RouterLink class="nav__link" to="/transactions">账单</RouterLink>
          <RouterLink class="nav__link" to="/categories">分类</RouterLink>
        </nav>
        <div class="app-header__right">
          <span v-if="auth.user" class="current-user">{{ auth.user.username }}</span>
          <el-button v-if="auth.user" text size="small" @click="onLogout">退出</el-button>
        </div>
      </div>
    </header>

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.app-header__inner {
  max-width: var(--app-max-width);
  margin: 0 auto;
  padding: 12px var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.brand {
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text);
  text-decoration: none;
  white-space: nowrap;
}

.nav {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex: 1;
}

.nav__link {
  padding: 6px 10px;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: 14px;
  white-space: nowrap;
}

.nav__link:hover {
  background: var(--color-muted-bg);
  color: var(--color-text);
}

.nav__link.router-link-active {
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-weight: 600;
}

.app-header__right {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-shrink: 0;
}

.current-user {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.app-main {
  flex: 1;
  width: 100%;
  max-width: var(--app-max-width);
  margin: 0 auto;
  padding: var(--space-4) var(--space-4) calc(var(--space-6) + env(safe-area-inset-bottom));
}

@media (max-width: 480px) {
  .app-header__inner {
    flex-wrap: wrap;
    gap: var(--space-2);
  }

  .nav {
    order: 3;
    flex-basis: 100%;
    justify-content: flex-start;
  }

  .app-header__right {
    margin-left: auto;
  }
}

@media (min-width: 768px) {
  .app-header__inner {
    padding-inline: var(--space-6);
  }

  .app-main {
    padding-inline: var(--space-6);
  }
}
</style>
