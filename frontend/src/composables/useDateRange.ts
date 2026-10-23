import { computed, ref } from 'vue'
import dayjs from 'dayjs'

export interface DashboardDateRange {
  startDate: string
  endDate: string
}

/**
 * 看板日期范围：
 * - 默认当前自然月（浏览器本地时区）
 * - 可通过月份选择器切换整月
 * - 也可选择任意日期范围覆盖月份
 */
export function useDateRange() {
  const month = ref(dayjs().format('YYYY-MM'))
  const customRange = ref<[string, string] | null>(null)

  const range = computed<DashboardDateRange>(() => {
    if (customRange.value) {
      return { startDate: customRange.value[0], endDate: customRange.value[1] }
    }
    const firstDay = dayjs(`${month.value}-01`)
    return {
      startDate: firstDay.startOf('month').format('YYYY-MM-DD'),
      endDate: firstDay.endOf('month').format('YYYY-MM-DD')
    }
  })

  function setMonth(value: string): void {
    if (!value) {
      return
    }
    month.value = value
    // 选月份时清除自定义范围，避免两个控件互相覆盖
    customRange.value = null
  }

  function setCustomRange(startDate: string, endDate: string): void {
    customRange.value = [startDate, endDate]
  }

  function clearCustomRange(): void {
    customRange.value = null
  }

  function resetToCurrentMonth(): void {
    month.value = dayjs().format('YYYY-MM')
    customRange.value = null
  }

  return {
    month,
    customRange,
    range,
    setMonth,
    setCustomRange,
    clearCustomRange,
    resetToCurrentMonth
  }
}
