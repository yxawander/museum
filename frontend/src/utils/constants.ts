export const EXHIBITION_CATEGORIES = ['常设展览', '专题特展', '临时展览'] as const

export const ANNOUNCEMENT_CATEGORIES: Record<string, string> = {
  NOTICE: '通知公告',
  NEWS: '馆内新闻',
  ACTIVITY: '社教活动',
}

export const RESERVATION_STATUS: Record<
  string,
  { text: string; type: 'success' | 'warning' | 'danger' | 'info'; color: string }
> = {
  CONFIRMED: { text: '待参观', type: 'warning', color: '#b8860b' },
  CHECKED_IN: { text: '已入馆', type: 'success', color: '#2f7d5c' },
  CANCELLED: { text: '已取消', type: 'info', color: '#8a8a8a' },
  EXPIRED: { text: '已过期', type: 'danger', color: '#a94442' },
}

export const EXHIBITION_STATUS: Record<string, { text: string; type: 'success' | 'warning' | 'info' }> = {
  PUBLISHED: { text: '已上架', type: 'success' },
  DRAFT: { text: '草稿', type: 'info' },
  CLOSED: { text: '已下架', type: 'warning' },
}

export const FEEDBACK_TYPES: Record<string, string> = {
  SUGGESTION: '意见建议',
  COMPLAINT: '投诉',
  PRAISE: '表扬',
  OTHER: '其他',
}

export const ROLE_TEXT: Record<string, string> = {
  VISITOR: '普通观众',
  STAFF: '检票员',
  ADMIN: '管理员',
}

export const COVER_PRESETS = [
  { label: '曾侯乙编钟', value: '/images/exhibitions/bells.svg' },
  { label: '越王勾践剑', value: '/images/exhibitions/sword.svg' },
  { label: '楚国八百年', value: '/images/exhibitions/chu.svg' },
  { label: '梁庄王珍藏', value: '/images/exhibitions/jewel.svg' },
  { label: '荆楚文明', value: '/images/exhibitions/yangtze.svg' },
  { label: '长江文明', value: '/images/exhibitions/river.svg' },
]