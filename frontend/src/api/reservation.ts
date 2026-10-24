import { request } from './http'
import type {
  CreateReservationPayload,
  Feedback,
  PageResult,
  Reservation,
} from '@/types'

export const reservationApi = {
  list(params: { page?: number; size?: number; status?: string }) {
    return request<PageResult<Reservation>>({ url: '/reservations', params })
  },
  detail(id: number | string) {
    return request<Reservation>({ url: `/reservations/${id}` })
  },
  create(data: CreateReservationPayload) {
    return request<Reservation>({ url: '/reservations', method: 'post', data })
  },
  cancel(id: number | string, reason?: string) {
    return request<void>({ url: `/reservations/${id}/cancel`, method: 'put', data: { reason } })
  },
}

export const feedbackApi = {
  create(data: { type: string; content: string; contact?: string }) {
    return request<void>({ url: '/feedbacks', method: 'post', data })
  },
  my(params: { page?: number; size?: number }) {
    return request<PageResult<Feedback>>({ url: '/feedbacks/my', params })
  },
}