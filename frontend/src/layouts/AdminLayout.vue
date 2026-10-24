<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { useAuthStore } from '@/stores/auth'
import { ROLE_TEXT } from '@/utils/constants'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const drawerOpen = ref(false)

const allMenus = [
  { group: '概览', path: '/admin/dashboard', label: '数据看板', icon: 'DataAnalysis', roles: ['ADMIN'] },
  { group: '运营管理', path: '/admin/exhibitions', label: '展览管理', icon: 'Collection', roles: ['ADMIN'] },
  { group: '运营管理', path: '/admin/slots', label: '时段管理', icon: 'Calendar', roles: ['ADMIN'] },
  { group: '运营管理', path: '/admin/reservations', label: '预约管理', icon: 'Tickets', roles: ['ADMIN'] },
  { group: '运营管理', path: '/admin/checkin', label: '现场核销', icon: 'CircleCheck', roles: ['ADMIN', 'STAFF'] },
  { group: '内容与用户', path: '/admin/users', label: '用户管理', icon: 'User', roles: ['ADMIN'] },
  { group: '内容与用户', path: '/admin/announcements', label: '公告管理', icon: 'Bell', roles: ['ADMIN'] },
  { group: '内容与用户', path: '/admin/feedbacks', label: '意见反馈', icon: 'ChatDotRound', roles: ['ADMIN'] },
]

const role = computed(() => auth.user?.role ?? 'VISITOR')
const menus = computed(() => allMenus.filter((item) => item.roles.includes(role.value)))
const groups = computed(() => {
  const map = new Map<string, typeof allMenus>()
  for (const item of menus.value) {
    const list = map.get(item.group) ?? []
    list.push(item)
    map.set(item.group, list)
  }
  return [...map.entries()]
})

const pageTitle = computed(() => (route.meta.title as string | undefined) ?? '管理后台')
const avatarText = computed(() => (auth.user?.realName || auth.user?.username || '管').slice(0, 1))
const roleText = computed(() => ROLE_TEXT[role.value] ?? '管理员')
const today = dayjs().format('YYYY年MM月DD日 dddd')

function isActive(path: string) {
  return route.path === path
}

function go(path: string) {
  drawerOpen.value = false
  void router.push(path)
}

function logout() {
  auth.logout()
  ElMessage.success('已退出登录')
  void router.push('/login')
}

function handleCommand(command: string) {
  if (command === 'logout') {
    logout()
    return
  }
  if (command === 'front') {
    void router.push('/')
    return
  }
  void router.push(command)
}
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <router-link to="/admin/dashboard" class="sidebar__brand">
        <img src="/logo.svg" alt="标识" class="sidebar__logo">
        <div>
          <strong>湖北省博物馆</strong>
          <span>预约管理平台</span>
        </div>
      </router-link>

      <nav class="sidebar__nav">
        <div v-for="[group, items] in groups" :key="group" class="sidebar__group">
          <p class="sidebar__group-title">{{ group }}</p>
          <router-link
            v-for="item in items"
            :key="item.path"
            :to="item.path"
            class="sidebar__link"
            :class="{ 'is-active': isActive(item.path) }"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </router-link>
        </div>
      </nav>

      <div class="sidebar__foot">
        <router-link to="/" class="sidebar__front">
          <el-icon><Monitor /></el-icon>
          查看前台网站
        </router-link>
        <span class="sidebar__version">Museum Booking · v1.0</span>
      </div>
    </aside>

    <div class="admin-main">
      <header class="admin-topbar">
        <div class="admin-topbar__left">
          <button type="button" class="admin-topbar__menu" aria-label="打开菜单" @click="drawerOpen = true">
            <span class="hamburger"><i /><i /><i /></span>
          </button>
          <div>
            <h2>{{ pageTitle }}</h2>
            <span class="admin-topbar__date">{{ today }}</span>
          </div>
        </div>
        <div class="admin-topbar__right">
          <span class="status-dot">系统运行正常</span>
          <el-dropdown trigger="click" @command="handleCommand">
            <button type="button" class="admin-user">
              <span class="admin-user__avatar">{{ avatarText }}</span>
              <span class="admin-user__meta">
                <strong>{{ auth.displayName }}</strong>
                <small>{{ roleText }}</small>
              </span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="front">返回前台</el-dropdown-item>
                <el-dropdown-item v-if="auth.isAdmin" command="/user/profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </div>

    <el-drawer v-model="drawerOpen" direction="ltr" size="80%" :with-header="false" class="admin-drawer">
      <div class="drawer-brand">
        <img src="/logo.svg" alt="标识">
        <strong>预约管理平台</strong>
        <button type="button" class="drawer-close" aria-label="关闭菜单" @click="drawerOpen = false">
          <el-icon><Close /></el-icon>
        </button>
      </div>
      <nav class="drawer-nav">
        <div v-for="[group, items] in groups" :key="group">
          <p class="sidebar__group-title">{{ group }}</p>
          <button
            v-for="item in items"
            :key="item.path"
            type="button"
            class="drawer-nav__link"
            :class="{ 'is-active': isActive(item.path) }"
            @click="go(item.path)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            {{ item.label }}
          </button>
        </div>
      </nav>
    </el-drawer>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f4f1e9;
}

.sidebar {
  position: fixed;
  z-index: 30;
  display: flex;
  width: 238px;
  height: 100vh;
  flex-direction: column;
  color: rgba(247, 244, 236, 0.72);
  background:
    radial-gradient(circle at 30% 0%, rgba(199, 161, 90, 0.16), transparent 42%),
    #0a2b28;
}

.sidebar__brand {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 22px 20px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar__logo {
  width: 38px;
  height: 38px;
}

.sidebar__brand strong {
  display: block;
  color: #f7f1e3;
  font-family: var(--font-serif);
  font-size: 16px;
  letter-spacing: 0.08em;
}

.sidebar__brand span {
  display: block;
  margin-top: 2px;
  color: rgba(247, 241, 227, 0.45);
  font-size: 10px;
  letter-spacing: 0.12em;
}

.sidebar__nav {
  flex: 1;
  overflow-y: auto;
  padding: 14px 12px;
}

.sidebar__group {
  margin-bottom: 10px;
}

.sidebar__group-title {
  padding: 10px 12px 6px;
  color: rgba(247, 241, 227, 0.36);
  font-size: 10px;
  letter-spacing: 0.18em;
}

.sidebar__link {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 3px;
  padding: 10px 12px;
  border-radius: 8px;
  color: rgba(247, 244, 236, 0.72);
  font-size: 13.5px;
  transition: background 0.2s ease, color 0.2s ease;
}

.sidebar__link:hover {
  color: #fff9ec;
  background: rgba(255, 255, 255, 0.06);
}

.sidebar__link.is-active {
  color: #fff9ec;
  background: linear-gradient(90deg, rgba(199, 161, 90, 0.3), rgba(199, 161, 90, 0.08));
  box-shadow: inset 2px 0 0 var(--gold);
}

.sidebar__foot {
  padding: 14px 20px 18px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar__front {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(247, 241, 227, 0.6);
  font-size: 12.5px;
}

.sidebar__front:hover {
  color: var(--gold);
}

.sidebar__version {
  display: block;
  margin-top: 8px;
  color: rgba(247, 241, 227, 0.26);
  font-size: 10px;
}

.admin-main {
  width: calc(100% - 238px);
  margin-left: 238px;
}

.admin-topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  padding: 0 30px;
  border-bottom: 1px solid rgba(19, 44, 42, 0.08);
  background: rgba(247, 244, 236, 0.94);
  backdrop-filter: blur(10px);
}

.admin-topbar__left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.admin-topbar__menu {
  display: none;
  width: 36px;
  height: 36px;
  place-items: center;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: transparent;
  color: var(--ink);
  font-size: 18px;
}

.hamburger {
  display: flex;
  width: 17px;
  flex-direction: column;
  gap: 4px;
}

.hamburger i {
  display: block;
  width: 100%;
  height: 1.5px;
  border-radius: 2px;
  background: currentColor;
}

.drawer-close {
  display: grid;
  width: 32px;
  height: 32px;
  margin-left: auto;
  place-items: center;
  border: 1px solid var(--border);
  border-radius: 50%;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
}

.admin-topbar h2 {
  font-size: 20px;
  letter-spacing: 0.06em;
}

.admin-topbar__date {
  color: var(--muted);
  font-size: 11.5px;
}

.admin-topbar__right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.status-dot {
  position: relative;
  padding-left: 13px;
  color: #61807a;
  font-size: 12px;
}

.status-dot::before {
  position: absolute;
  top: 50%;
  left: 0;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  content: '';
  transform: translateY(-50%);
  background: #3f9a72;
  box-shadow: 0 0 0 3px rgba(63, 154, 114, 0.15);
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 4px 8px 4px 4px;
  border: 1px solid transparent;
  border-radius: 999px;
  background: transparent;
  color: var(--ink);
  cursor: pointer;
}

.admin-user:hover {
  border-color: var(--border);
  background: var(--white);
}

.admin-user__avatar {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 50%;
  color: #fff9ec;
  background: linear-gradient(135deg, var(--teal), #1c6b63);
  font-family: var(--font-serif);
}

.admin-user__meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  text-align: left;
}

.admin-user__meta strong {
  font-size: 12.5px;
}

.admin-user__meta small {
  color: var(--muted);
  font-size: 10px;
}

.admin-content {
  padding: 26px 30px 44px;
}

.drawer-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
  font-family: var(--font-serif);
  font-size: 17px;
}

.drawer-brand img {
  width: 36px;
  height: 36px;
}

.drawer-nav {
  padding-top: 12px;
}

.drawer-nav__link {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 12px 10px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--text);
  font-size: 14px;
  text-align: left;
}

.drawer-nav__link.is-active {
  color: var(--teal);
  background: var(--teal-light);
  font-weight: 600;
}

@media (max-width: 1024px) {
  .sidebar {
    display: none;
  }

  .admin-main {
    width: 100%;
    margin-left: 0;
  }

  .admin-topbar__menu {
    display: grid;
  }
}

@media (max-width: 720px) {
  .admin-topbar {
    height: 64px;
    padding: 0 16px;
  }

  .admin-topbar__date,
  .status-dot,
  .admin-user__meta {
    display: none;
  }

  .admin-topbar h2 {
    font-size: 17px;
  }

  .admin-content {
    padding: 18px 14px 32px;
  }
}
</style>