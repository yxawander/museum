import { downloadBlob, request } from './http'
import type {
  Announcement,
  AnnouncementForm,
  BatchSlotForm,
  Dashboard,
  Exhibition,
  ExhibitionForm,
  Feedback,
  PageResult,
  Reservation,
  Slot,
  SlotForm,
  User,
} from '@/types'

type Query = Record<string, unknown>

export const adminApi = {
  dashboard() {
    return request<Dashboard>({ url: '/admin/dashboard' })
  },

  exhibitions(params: Query) {
    return request<PageResult<Exhibition>>({ url: '/admin/exhibitions', params })
  },
  exhibition(id: number | string) {
    return request<Exhibition>({ url: `/admin/exhibitions/${id}` })
  },
  createExhibition(data: ExhibitionForm) {
    return request<Exhibition>({ url: '/admin/exhibitions', method: 'post', data })
  },
  updateExhibition(id: number, data: ExhibitionForm) {
    return request<Exhibition>({ url: `/admin/exhibitions/${id}`, method: 'put', data })
  },
  exhibitionStatus(id: number, status: string) {
    return request<void>({ url: `/admin/exhibitions/${id}/status`, method: 'put', params: { status } })
  },
  deleteExhibition(id: number) {
    return request<void>({ url: `/admin/exhibitions/${id}`, method: 'delete' })
  },

  slots(params: Query) {
    return request<PageResult<Slot>>({ url: '/admin/slots', params })
  },
  createSlot(data: SlotForm) {
    return request<Slot>({ url: '/admin/slots', method: 'post', data })
  },
  updateSlot(id: number, data: SlotForm) {
    return request<Slot>({ url: `/admin/slots/${id}`, method: 'put', data })
  },
  slotStatus(id: number, status: string) {
    return request<void>({ url: `/admin/slots/${id}/status`, method: 'put', params: { status } })
  },
  batchSlots(data: BatchSlotForm) {
    return request<{ created: number }>({ url: '/admin/slots/batch', method: 'post', data })
  },
  deleteSlot(id: number) {
    return request<void>({ url: `/admin/slots/${id}`, method: 'delete' })
  },

  reservations(params: Query) {
    return request<PageResult<Reservation>>({ url: '/admin/reservations', params })
  },
  reservation(id: number | string) {
    return request<Reservation>({ url: `/admin/reservations/${id}` })
  },
  checkinReservation(id: number) {
    return request<Reservation>({ url: `/admin/reservations/${id}/checkin`, method: 'post' })
  },
  cancelReservation(id: number, reason: string) {
    return request<void>({ url: `/admin/reservations/${id}/cancel`, method: 'put', data: { reason } })
  },
  exportReservations(params: Query) {
    return downloadBlob({ url: '/admin/reservations/export', params })
  },

  checkinSearch(code: string) {
    return request<Reservation>({ url: '/admin/checkin/search', params: { code } })
  },
  checkin(code: string) {
    return request<Reservation>({ url: '/admin/checkin', method: 'post', data: { code } })
  },

  users(params: Query) {
    return request<PageResult<User>>({ url: '/admin/users', params })
  },
  createUser(data: {
    username: string
    password: string
    realName: string
    phone: string
    email?: string
    role: string
  }) {
    return request<User>({ url: '/admin/users', method: 'post', data })
  },
  userStatus(id: number, status: number) {
    return request<void>({ url: `/admin/users/${id}/status`, method: 'put', data: { status } })
  },
  resetPassword(id: number, newPassword: string) {
    return request<void>({ url: `/admin/users/${id}/password`, method: 'put', data: { newPassword } })
  },

  announcements(params: Query) {
    return request<PageResult<Announcement>>({ url: '/admin/announcements', params })
  },
  announcement(id: number | string) {
    return request<Announcement>({ url: `/admin/announcements/${id}` })
  },
  createAnnouncement(data: AnnouncementForm) {
    return request<Announcement>({ url: '/admin/announcements', method: 'post', data })
  },
  updateAnnouncement(id: number, data: AnnouncementForm) {
    return request<Announcement>({ url: `/admin/announcements/${id}`, method: 'put', data })
  },
  deleteAnnouncement(id: number) {
    return request<void>({ url: `/admin/announcements/${id}`, method: 'delete' })
  },

  feedbacks(params: Query) {
    return request<PageResult<Feedback>>({ url: '/admin/feedbacks', params })
  },
  replyFeedback(id: number, reply: string) {
    return request<void>({ url: `/admin/feedbacks/${id}/reply`, method: 'post', data: { reply } })
  },
}