import dayjs from 'dayjs'

export const DATE_FORMAT = 'YYYY-MM-DD'

/** 今天（浏览器本地时区），用于表单默认日期；不要用 toISOString 以免 UTC 偏移。 */
export function todayString(): string {
  return dayjs().format(DATE_FORMAT)
}

/** 把 Date 对象格式化为本地 YYYY-MM-DD。 */
export function toDateString(value: Date): string {
  return dayjs(value).format(DATE_FORMAT)
}

/** 展示用日期；空值返回空字符串。 */
export function formatDate(value: string | null | undefined): string {
  if (!value) {
    return ''
  }
  return dayjs(value).format(DATE_FORMAT)
}

/** 展示用日期时间；空值返回空字符串。 */
export function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return ''
  }
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}
