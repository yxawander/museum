<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const scrolled = ref(false)
const mobileOpen = ref(false)

const navItems = [
  { label: '首页', path: '/' },
  { label: '展览陈列', path: '/exhibitions' },
  { label: '预约参观', path: '/booking' },
  { label: '公告资讯', path: '/announcements' },
  { label: '参观指南', path: '/about' },
]

const activePath = computed(() => {
  if (route.path === '/') return '/'
  return navItems.find((item) => item.path !== '/' && route.path.startsWith(item.path))?.path ?? ''
})

const avatarText = computed(() => (auth.user?.realName || auth.user?.username || '观').slice(0, 1))

function onScroll() {
  scrolled.value = window.scrollY > 16
}

function isActive(path: string) {
  return activePath.value === path
}

function go(path: string) {
  mobileOpen.value = false
  void router.push(path)
}

function handleCommand(command: string) {
  if (command === 'logout') {
    auth.logout()
    ElMessage.success('已安全退出')
    void router.push('/')
    return
  }
  if (command === 'admin') {
    void router.push(auth.isAdmin ? '/admin/dashboard' : '/admin/checkin')
    return
  }
  void router.push(command)
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <header class="site-header" :class="{ 'is-scrolled': scrolled }">
    <div class="topbar">
      <div class="container topbar__inner">
        <span>开放时间：周二至周日 09:00—17:00（16:00 停止入馆，周一闭馆）</span>
        <span class="topbar__right">咨询电话 027-86794127 · 武汉市武昌区东湖路160号</span>
      </div>
    </div>

    <div class="navbar">
      <div class="container navbar__inner">
        <router-link to="/" class="brand" aria-label="返回首页">
          <img src="/logo.svg" alt="湖北省博物馆标识" class="brand__logo">
          <span class="brand__text">
            <strong>湖北省博物馆</strong>
            <small>HUBEI PROVINCIAL MUSEUM</small>
          </span>
        </router-link>

        <nav class="nav" aria-label="主导航">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav__link"
            :class="{ 'is-active': isActive(item.path) }"
          >
            {{ item.label }}
          </router-link>
        </nav>

        <div class="actions">
          <template v-if="auth.isLoggedIn">
            <el-dropdown trigger="click" @command="handleCommand">
              <button class="user-chip" type="button">
                <span class="user-chip__avatar">{{ avatarText }}</span>
                <span class="user-chip__name">{{ auth.displayName }}</span>
                <el-icon class="user-chip__arrow"><ArrowDown /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="/user/profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="/user/reservations">我的预约</el-dropdown-item>
                  <el-dropdown-item v-if="auth.canManage" command="admin" divided>
                    {{ auth.isAdmin ? '管理后台' : '现场核销' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text class="login-link" @click="go('/login')">登录</el-button>
            <el-button type="primary" round @click="go('/register')">注册</el-button>
          </template>
          <button class="menu-toggle" type="button" aria-label="打开菜单" @click="mobileOpen = true">
            <span class="hamburger"><i /><i /><i /></span>
          </button>
        </div>
      </div>
    </div>

    <el-drawer v-model="mobileOpen" direction="rtl" size="78%" :with-header="false" class="mobile-drawer">
      <div class="mobile-drawer__head">
        <img src="/logo.svg" alt="标识" class="brand__logo">
        <strong>湖北省博物馆</strong>
        <button type="button" class="drawer-close" aria-label="关闭菜单" @click="mobileOpen = false">
          <el-icon><Close /></el-icon>
        </button>
      </div>
      <nav class="mobile-nav">
        <button
          v-for="item in navItems"
          :key="item.path"
          type="button"
          class="mobile-nav__link"
          :class="{ 'is-active': isActive(item.path) }"
          @click="go(item.path)"
        >
          {{ item.label }}
        </button>
      </nav>
      <div class="mobile-drawer__actions">
        <template v-if="auth.isLoggedIn">
          <el-button type="primary" plain @click="go('/user/profile')">个人中心</el-button>
          <el-button type="primary" @click="go('/user/reservations')">我的预约</el-button>
          <el-button v-if="auth.canManage" @click="go(auth.isAdmin ? '/admin/dashboard' : '/admin/checkin')">
            管理后台
          </el-button>
          <el-button text @click="handleCommand('logout')">退出登录</el-button>
        </template>
        <template v-else>
          <el-button plain @click="go('/login')">登录</el-button>
          <el-button type="primary" @click="go('/register')">注册账号</el-button>
        </template>
      </div>
    </el-drawer>
  </header>
</template>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 50;
  transition: box-shadow 0.25s ease, background 0.25s ease;
}

.topbar {
  height: 34px;
  color: rgba(255, 249, 236, 0.72);
  background: var(--teal-dark);
  font-size: 12px;
  line-height: 34px;
}

.topbar__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.topbar__right {
  opacity: 0.8;
}

.navbar {
  border-bottom: 1px solid rgba(19, 44, 42, 0.08);
  background: rgba(255, 253, 248, 0.96);
  backdrop-filter: blur(12px);
}

.is-scrolled .navbar {
  box-shadow: 0 8px 30px rgba(19, 44, 42, 0.08);
}

.navbar__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 74px;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.brand__logo {
  width: 44px;
  height: 44px;
}

.brand__text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.brand__text strong {
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 20px;
  letter-spacing: 0.12em;
}

.brand__text small {
  margin-top: 3px;
  color: var(--muted);
  font-size: 9px;
  letter-spacing: 0.14em;
}

.nav {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
}

.nav__link {
  position: relative;
  padding: 8px 14px;
  color: #43504d;
  font-size: 14.5px;
  letter-spacing: 0.06em;
  transition: color 0.2s ease;
}

.nav__link::after {
  position: absolute;
  right: 14px;
  bottom: 2px;
  left: 14px;
  height: 1.5px;
  content: '';
  transform: scaleX(0);
  transform-origin: left;
  background: var(--gold);
  transition: transform 0.25s ease;
}

.nav__link:hover,
.nav__link.is-active {
  color: var(--teal);
}

.nav__link.is-active::after,
.nav__link:hover::after {
  transform: scaleX(1);
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.login-link {
  color: var(--teal);
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 5px 12px 5px 6px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: transparent;
  color: var(--ink);
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.user-chip:hover {
  border-color: rgba(14, 75, 71, 0.35);
  background: var(--teal-light);
}

.user-chip__avatar {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border-radius: 50%;
  color: #fff9ec;
  background: linear-gradient(135deg, var(--teal), #1c6b63);
  font-family: var(--font-serif);
  font-size: 15px;
}

.user-chip__name {
  max-width: 80px;
  overflow: hidden;
  font-size: 13.5px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-chip__arrow {
  color: var(--muted);
  font-size: 12px;
}

.menu-toggle {
  display: none;
  width: 38px;
  height: 38px;
  place-items: center;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: transparent;
  color: var(--ink);
  font-size: 18px;
  cursor: pointer;
}

.hamburger {
  display: flex;
  width: 18px;
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

.mobile-drawer__head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border);
  font-family: var(--font-serif);
  font-size: 18px;
  letter-spacing: 0.08em;
}

.mobile-nav {
  display: flex;
  flex-direction: column;
  padding: 18px 0;
}

.mobile-nav__link {
  padding: 14px 4px;
  border: 0;
  border-bottom: 1px solid rgba(19, 44, 42, 0.06);
  background: transparent;
  color: var(--text);
  font-size: 16px;
  text-align: left;
  cursor: pointer;
}

.mobile-nav__link.is-active {
  color: var(--teal);
  font-weight: 600;
}

.mobile-drawer__actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
}

@media (max-width: 1080px) {
  .nav__link {
    padding: 8px 9px;
    font-size: 13.5px;
  }

  .topbar__right {
    display: none;
  }
}

@media (max-width: 900px) {
  .nav,
  .actions > .login-link,
  .actions > .el-button {
    display: none;
  }

  .user-chip__name,
  .user-chip__arrow {
    display: none;
  }

  .user-chip {
    padding: 3px;
  }

  .menu-toggle {
    display: grid;
  }

  .nav {
    display: none;
  }
}

@media (max-width: 640px) {
  .topbar {
    display: none;
  }

  .navbar__inner {
    height: 64px;
  }

  .brand__text strong {
    font-size: 17px;
  }

  .brand__text small {
    display: none;
  }

  .brand__logo {
    width: 38px;
    height: 38px;
  }
}
</style>