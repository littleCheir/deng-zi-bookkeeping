<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const title = computed(() => (typeof route.meta.title === 'string' ? route.meta.title : '功能页面'))
</script>

<template>
  <section class="page">
    <div class="card placeholder">
      <h1 class="placeholder__title">{{ title }}</h1>
      <p class="placeholder__status">功能尚未接通</p>
      <p class="placeholder__hint">
        登录与访问控制已完成（阶段 1）；该业务页面尚未接入真实接口，因此不展示任何数据。
      </p>
      <p class="placeholder__hint">后续阶段会替换为真实页面与接口调用。</p>
    </div>
  </section>
</template>

<style scoped>
.placeholder {
  text-align: left;
}

.placeholder__title {
  margin: 0 0 var(--space-2);
  font-size: 20px;
}

.placeholder__status {
  margin: 0 0 var(--space-3);
  color: var(--color-text-secondary);
  font-size: 15px;
}

.placeholder__hint {
  margin: 0 0 var(--space-1);
  color: var(--color-text-tertiary);
  font-size: 13px;
  line-height: 1.6;
}
</style>
