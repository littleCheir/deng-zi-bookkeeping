<script setup lang="ts">
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import type { ExpenseByCategory } from '@/types/dashboard'

const props = defineProps<{ items: ExpenseByCategory[] }>()

const container = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
let observer: ResizeObserver | null = null

const hasData = computed(() => props.items.length > 0)

function buildOption(): echarts.EChartsOption {
  return {
    tooltip: { trigger: 'item' },
    legend: { type: 'scroll', bottom: 0, textStyle: { fontSize: 11 } },
    series: [
      {
        name: '支出分类',
        type: 'pie',
        radius: ['42%', '66%'],
        center: ['50%', '46%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { fontSize: 11, formatter: '{b}\n{d}%' },
        // 金额为后端返回的十进制字符串，这里只转成绘图数值，不参与业务计算
        data: props.items.map((item) => ({ name: item.categoryName, value: Number(item.amount) }))
      }
    ]
  }
}

function render(): void {
  if (!chart) {
    return
  }
  chart.setOption(buildOption())
}

onMounted(() => {
  if (!container.value) {
    return
  }
  chart = echarts.init(container.value)
  observer = new ResizeObserver(() => chart?.resize())
  observer.observe(container.value)
  render()
})

watch(
  () => props.items,
  () => {
    void nextTick(render)
  },
  { deep: true }
)

onBeforeUnmount(() => {
  observer?.disconnect()
  observer = null
  chart?.dispose()
  chart = null
})
</script>

<template>
  <div class="chart-wrapper">
    <div ref="container" class="chart-canvas" />
    <div v-if="!hasData" class="chart-empty">暂无支出数据</div>
  </div>
</template>

<style scoped>
.chart-wrapper {
  position: relative;
  width: 100%;
}

.chart-canvas {
  width: 100%;
  height: 260px;
}

.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-tertiary);
  font-size: 13px;
  background: var(--color-surface);
}

@media (max-width: 480px) {
  .chart-canvas {
    height: 240px;
  }
}
</style>
