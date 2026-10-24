import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResult } from '@/types'

const TOKEN_KEY = 'museum_token'
const USER_KEY = 'museum_user'

const http = axios.create({
  baseURL: '/api',
  timeout: 20000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let redirecting = false

function handleUnauthorized(message: string) {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  if (redirecting) return
  redirecting = true
  ElMessage.error(message || '登录已过期，请重新登录')
  const current = window.location.pathname + window.location.search
  const target = current.startsWith('/login') ? '/login' : `/login?redirect=${encodeURIComponent(current)}`
  window.setTimeout(() => {
    window.location.href = target
    redirecting = false
  }, 300)
}

http.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response
    }
    const body = response.data as ApiResult<unknown>
    if (body && typeof body.code === 'number' && body.code !== 200) {
      if (body.code === 401) {
        handleUnauthorized(body.message)
      } else {
        ElMessage.error(body.message || '请求失败')
      }
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return response
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常，请稍后重试'
    if (status === 401) {
      handleUnauthorized(message)
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  },
)

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await http.request<ApiResult<T>>(config)
  return response.data.data
}

export function downloadBlob(config: AxiosRequestConfig): Promise<Blob> {
  return http.request<Blob>({ ...config, responseType: 'blob' }).then((res) => res.data)
}

export default http