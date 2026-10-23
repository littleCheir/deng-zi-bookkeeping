import type { TransactionType } from '@/types/api'

const CURRENCY_SYMBOL = '¥'

/**
 * 千分位金额格式化。全程按字符串处理，避免把金额转成浮点数参与展示计算。
 */
export function formatCurrency(amount: string | null | undefined): string {
  const raw = (amount ?? '').trim()
  if (!raw) {
    return `${CURRENCY_SYMBOL}0.00`
  }

  const negative = raw.startsWith('-')
  const unsigned = negative ? raw.slice(1) : raw
  const [integerPartRaw = '0', fractionRaw = ''] = unsigned.split('.')
  const integerPart = integerPartRaw.replace(/^0+(?=\d)/, '') || '0'
  const grouped = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  const fraction = `${fractionRaw}00`.slice(0, 2)

  return `${negative ? '-' : ''}${CURRENCY_SYMBOL}${grouped}.${fraction}`
}

/** 按收支类型加正负号；金额本身始终为正数，符号仅用于展示。 */
export function formatSignedCurrency(type: TransactionType, amount: string): string {
  return `${type === 'INCOME' ? '+' : '-'}${formatCurrency(amount)}`
}
