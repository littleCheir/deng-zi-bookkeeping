<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="app-header__inner">
        <RouterLink class="brand" to="/">个人记账</RouterLink>
        <nav class="nav" aria-label="主导航">
          <RouterLink class="nav__link" to="/">首页</RouterLink>
          <RouterLink class="nav__link" to="/transactions">账单</RouterLink>
          <RouterLink class="nav__link" to="/categories">分类</RouterLink>
        </nav>
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
  justify-content: space-between;
  gap: var(--space-4);
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
}

.nav__link {
  padding: 6px 10px;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: 14px;
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

.app-main {
  flex: 1;
  width: 100%;
  max-width: var(--app-max-width);
  margin: 0 auto;
  padding: var(--space-4) var(--space-4) calc(var(--space-6) + env(safe-area-inset-bottom));
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
