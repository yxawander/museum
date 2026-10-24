import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import type { LoginPayload, RegisterPayload, User } from '@/types'

const TOKEN_KEY = 'museum_token'
const USER_KEY = 'museum_user'

function readUser(): User | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as User
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) ?? '')
  const user = ref<User | null>(readUser())
  const loading = ref(false)

  const isLoggedIn = computed(() => Boolean(token.value))
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isStaff = computed(() => user.value?.role === 'STAFF')
  const canManage = computed(() => isAdmin.value || isStaff.value)
  const displayName = computed(() => user.value?.realName || user.value?.username || '观众')

  function setAuth(nextToken: string, nextUser: User) {
    token.value = nextToken
    user.value = nextUser
    localStorage.setItem(TOKEN_KEY, nextToken)
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  async function login(payload: LoginPayload) {
    loading.value = true
    try {
      const data = await authApi.login(payload)
      setAuth(data.token, data.user)
      return data.user
    } finally {
      loading.value = false
    }
  }

  async function register(payload: RegisterPayload) {
    await authApi.register(payload)
    return login({ username: payload.username, password: payload.password })
  }

  async function fetchMe() {
    if (!token.value) return null
    try {
      const data = await authApi.me()
      user.value = data
      localStorage.setItem(USER_KEY, JSON.stringify(data))
      return data
    } catch {
      clearAuth()
      return null
    }
  }

  function updateUser(next: User) {
    user.value = next
    localStorage.setItem(USER_KEY, JSON.stringify(next))
  }

  function logout() {
    clearAuth()
  }

  return {
    token,
    user,
    loading,
    isLoggedIn,
    isAdmin,
    isStaff,
    canManage,
    displayName,
    login,
    register,
    fetchMe,
    updateUser,
    logout,
    clearAuth,
  }
})