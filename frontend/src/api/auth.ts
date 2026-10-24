import { request } from './http'
import type { LoginPayload, LoginResult, ProfilePayload, RegisterPayload, User } from '@/types'

export const authApi = {
  login(data: LoginPayload) {
    return request<LoginResult>({ url: '/auth/login', method: 'post', data })
  },
  register(data: RegisterPayload) {
    return request<User>({ url: '/auth/register', method: 'post', data })
  },
  me() {
    return request<User>({ url: '/auth/me' })
  },
  updateProfile(data: ProfilePayload) {
    return request<User>({ url: '/auth/profile', method: 'put', data })
  },
  changePassword(data: { oldPassword: string; newPassword: string; confirmPassword: string }) {
    return request<void>({ url: '/auth/password', method: 'put', data })
  },
}