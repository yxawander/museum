import { request } from './http'
import type { Announcement, Exhibition, HomeData, PageResult, Slot } from '@/types'

export const publicApi = {
  home() {
    return request<HomeData>({ url: '/public/home' })
  },
  exhibitions(params: { page?: number; size?: number; keyword?: string; category?: string }) {
    return request<PageResult<Exhibition>>({ url: '/public/exhibitions', params })
  },
  exhibition(id: number | string) {
    return request<Exhibition>({ url: `/public/exhibitions/${id}` })
  },
  slots(id: number | string, params?: { startDate?: string; endDate?: string }) {
    return request<Slot[]>({ url: `/public/exhibitions/${id}/slots`, params })
  },
  announcements(params: { page?: number; size?: number; category?: string; keyword?: string }) {
    return request<PageResult<Announcement>>({ url: '/public/announcements', params })
  },
  announcement(id: number | string) {
    return request<Announcement>({ url: `/public/announcements/${id}` })
  },
}