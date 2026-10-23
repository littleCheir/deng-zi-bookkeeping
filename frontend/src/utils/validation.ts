// 金额最多 10 位整数 + 2 位小数，且不允许前导零（"0.50" 允许）
const AMOUNT_PATTERN = /^(?:0|[1-9]\d{0,9})(?:\.\d{1,2})?$/

/**
 * 校验金额输入；返回错误文案，合法时返回 null。
 * 不把输入转成浮点数参与业务计算，只用于判断是否大于 0。
 *
 * 注意：负号要先于格式校验判断，否则 "-100" 会先命中正则而得到“格式错误”，
 * 把真正的原因（金额必须大于 0）掩盖掉。
 */
export function validateAmount(raw: string): string | null {
  const value = raw.trim()
  if (!value) {
    return '请输入金额'
  }
  if (value.startsWith('-')) {
    return '金额必须大于 0'
  }
  if (!AMOUNT_PATTERN.test(value)) {
    return '金额最多两位小数，整数部分不能超过 10 位'
  }
  if (Number(value) <= 0) {
    return '金额必须大于 0'
  }
  return null
}

/** 备注长度校验（后端同样会校验，前端只做即时提示）。 */
export function validateNote(raw: string): string | null {
  return raw.length > 200 ? '备注不能超过 200 个字符' : null
}
