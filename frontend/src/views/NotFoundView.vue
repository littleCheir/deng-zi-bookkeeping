<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <section class="page">
    <div class="card not-found">
      <p class="not-found__code">404</p>
      <h1 class="not-found__title">页面不存在</h1>
      <p class="not-found__hint">你访问的地址没有对应页面，请检查链接是否正确。</p>
      <RouterLink class="not-found__link" to="/">返回首页</RouterLink>
    </div>
  </section>
</template>

<style scoped>
.not-found {
  text-align: center;
}

.not-found__code {
  margin: 0;
  font-size: 40px;
  font-weight: 700;
  color: var(--color-border-strong);
}

.not-found__title {
  margin: var(--space-2) 0;
  font-size: 18px;
}

.not-found__hint {
  margin: 0 0 var(--space-4);
  color: var(--color-text-secondary);
  font-size: 14px;
}

.not-found__link {
  color: var(--color-primary);
  font-size: 14px;
  text-decoration: none;
}

.not-found__link:hover {
  text-decoration: underline;
}
</style>
