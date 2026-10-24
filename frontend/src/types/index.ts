export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

export type Role = 'VISITOR' | 'STAFF' | 'ADMIN'

export interface User {
  id: number
  username: string
  realName: string
  idCard?: string
  phone: string
  email?: string
  avatar?: string
  role: Role
  status: number
  lastLoginAt?: string
  createdAt: string
}

export interface LoginResult {
  token: string
  user: User
}

export interface Exhibition {
  id: number
  title: string
  subtitle?: string
  category: string
  coverUrl?: string
  description?: string
  venue: string
  startDate: string
  endDate: string
  openTime: string
  closeTime: string
  ticketPrice: number
  tips?: string
  status: 'DRAFT' | 'PUBLISHED' | 'CLOSED'
  sortOrder: number
  viewCount: number
  createdAt: string
  updatedAt: string
}

export interface Slot {
  id: number
  exhibitionId: number
  exhibitionTitle?: string
  visitDate: string
  startTime: string
  endTime: string
  capacity: number
  bookedCount: number
  remaining: number
  status: 'OPEN' | 'CLOSED'
  statusText: string
  remark?: string
}

export type ReservationStatus = 'CONFIRMED' | 'CHECKED_IN' | 'CANCELLED' | 'EXPIRED'

export interface Reservation {
  id: number
  orderNo: string
  userId: number
  username?: string
  slotId: number
  exhibitionId: number
  exhibitionTitle: string
  coverUrl?: string
  venue?: string
  visitDate: string
  startTime: string
  endTime: string
  visitorName: string
  visitorIdCard: string
  visitorPhone: string
  ticketCount: number
  status: ReservationStatus
  statusText: string
  ticketCode: string
  qrContent: string
  remark?: string
  cancelReason?: string
  cancelledAt?: string
  checkinAt?: string
  createdAt: string
  canCancel: boolean
}

export interface Announcement {
  id: number
  title: string
  summary?: string
  content?: string
  category: 'NOTICE' | 'NEWS' | 'ACTIVITY'
  coverUrl?: string
  isTop: number
  status: number
  publishTime?: string
  viewCount: number
  createdAt: string
  updatedAt: string
}

export interface Feedback {
  id: number
  userId: number
  username?: string
  realName?: string
  type: 'SUGGESTION' | 'COMPLAINT' | 'PRAISE' | 'OTHER'
  typeText: string
  content: string
  contact?: string
  reply?: string
  status: 'PENDING' | 'REPLIED'
  statusText: string
  repliedAt?: string
  createdAt: string
}

export interface HomeData {
  exhibitions: Exhibition[]
  announcements: Announcement[]
  stats: {
    exhibitionCount: number
    todayVisitorCount: number
    totalReservationCount: number
    todayRemaining: number
    todayCapacity: number
    openTime: string
    closeTime: string
  }
}

export interface TrendPoint {
  day: string
  total: number
  checked: number
}

export interface NameValue {
  name: string
  value: number
}

export interface Dashboard {
  visitorCount: number
  exhibitionCount: number
  todayReservationCount: number
  todayVisitorCount: number
  todayCheckedCount: number
  pendingFeedbackCount: number
  totalReservationCount: number
  todayCapacity: number
  todayBooked: number
  checkinRate: number
  trend: TrendPoint[]
  categoryDistribution: NameValue[]
  topExhibitions: NameValue[]
  recentReservations: Reservation[]
}

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  password: string
  confirmPassword: string
  realName: string
  phone: string
  email?: string
  idCard?: string
}

export interface ProfilePayload {
  realName: string
  phone: string
  email?: string
  idCard?: string
  avatar?: string
}

export interface CreateReservationPayload {
  slotId: number
  visitorName: string
  visitorIdCard: string
  visitorPhone: string
  ticketCount: number
  remark?: string
}

export interface ExhibitionForm {
  title: string
  subtitle?: string
  category: string
  coverUrl?: string
  description?: string
  venue: string
  startDate: string
  endDate: string
  openTime: string
  closeTime: string
  ticketPrice: number
  tips?: string
  status: string
  sortOrder: number
}

export interface SlotForm {
  exhibitionId: number
  visitDate: string
  startTime: string
  endTime: string
  capacity: number
  status: string
  remark?: string
}

export interface BatchSlotForm {
  exhibitionId: number
  startDate: string
  endDate: string
  startTimes: string[]
  capacity: number
  skipMonday: boolean
}

export interface AnnouncementForm {
  title: string
  summary?: string
  content: string
  category: string
  coverUrl?: string
  isTop: number
  status: number
}