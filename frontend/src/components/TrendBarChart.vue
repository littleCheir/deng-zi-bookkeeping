<script setup lang="ts">
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import type { TrendPoint } from '@/types/dashboard'

const props = defineProps<{ points: TrendPoint[] }>()

const container = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
let observer: ResizeObserver | null = null

const hasData = computed(() =>
  props.points.some((point) => Number(point.income) > 0 || Number(point.expense) > 0)
)

function buildOption(): echarts.EChartsOption {
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出'], top: 0 },
    grid: { left: 8, right: 8, top: 36, bottom: 4, containLabel: true },
    xAxis: {
      type: 'category',
      // 只显示 MM-DD，手机端更紧凑；完整日期在 tooltip 中展示
      data: props.points.map((point) => point.date.slice(5)),
      axisLabel: { fontSize: 10, hideOverlap: true }
    },
    yAxis: {
      type: 'value',
      axisLabel: { fontSize: 10 }
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        data: props.points.map((point) => Number(point.income)),
        itemStyle: { color: '#0f9d58' },
        barMaxWidth: 14
      },
      {
        name: '支出',
        type: 'bar',
        data: props.points.map((point) => Number(point.expense)),
        itemStyle: { color: '#d93025' },
        barMaxWidth: 14
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
  () => props.points,
  () => {
    // 数据变化后等 DOM 更新再重绘，避免尺寸拿到 0
    void nextTick(render)
  },
  { deep: true }
)

onBeforeUnmount(() => {
  // 必须 dispose，避免路由切换后残留实例与监听
  observer?.disconnect()
  observer = null
  chart?.dispose()
  chart = null
})
</script>

<template>
  <div class="chart-wrapper">
    <div ref="container" class="chart-canvas" />
    <div v-if="!hasData" class="chart-empty">暂无收支数据</div>
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
    height: 230px;
  }
}
</style>
