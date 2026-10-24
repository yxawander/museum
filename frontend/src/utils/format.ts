import dayjs from 'dayjs'

export function formatDate(value?: string, withTime = false): string {
  if (!value) return '—'
  return dayjs(value).format(withTime ? 'YYYY-MM-DD HH:mm' : 'YYYY-MM-DD')
}

export function formatMoney(value?: number): string {
  if (value === undefined || value === null) return '免费'
  return value > 0 ? `¥${value.toFixed(2)}` : '免费'
}

export function weekdayText(value: string): string {
  return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][dayjs(value).day()] ?? ''
}

export function shortTime(value?: string): string {
  return value ? value.slice(0, 5) : ''
}

export function maskPhone(value?: string): string {
  if (!value || value.length < 7) return value ?? ''
  return `${value.slice(0, 3)}****${value.slice(-4)}`
}

/** 时段是否已不可预约（当天需至少提前 30 分钟） */
export function isSlotBookable(visitDate: string, startTime: string): boolean {
  const start = dayjs(`${visitDate} ${startTime}`)
  return start.isAfter(dayjs().add(30, 'minute'))
}

export function dateLabel(value: string): string {
  const date = dayjs(value)
  const today = dayjs().startOf('day')
  if (date.isSame(today, 'day')) return '今天'
  if (date.isSame(today.add(1, 'day'), 'day')) return '明天'
  if (date.isSame(today.add(2, 'day'), 'day')) return '后天'
  return date.format('MM月DD日')
}