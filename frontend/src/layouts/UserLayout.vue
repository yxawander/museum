<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SiteHeader from '@/components/layout/SiteHeader.vue'
import SiteFooter from '@/components/layout/SiteFooter.vue'
import { useAuthStore } from '@/stores/auth'
import { ROLE_TEXT } from '@/utils/constants'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const avatarText = computed(() => (auth.user?.realName || auth.user?.username || '观').slice(0, 1))
const roleText = computed(() => ROLE_TEXT[auth.user?.role ?? 'VISITOR'] ?? '普通观众')

const menus = [
  { path: '/user/profile', label: '个人资料', icon: 'User' },
  { path: '/user/reservations', label: '我的预约', icon: 'Tickets' },
  { path: '/user/feedback', label: '意见反馈', icon: 'ChatLineRound' },
  { path: '/user/password', label: '修改密码', icon: 'Lock' },
]

function isActive(path: string) {
  return route.path === path || (path === '/user/reservations' && route.path.startsWith('/user/reservations/'))
}

function logout() {
  auth.logout()
  ElMessage.success('已安全退出')
  void router.push('/')
}
</script>

<template>
  <div class="user-layout">
    <SiteHeader />
    <div class="user-hero">
      <div class="container user-hero__inner">
        <div>
          <span class="user-hero__eyebrow">个人中心</span>
          <h1>您好，{{ auth.displayName }}</h1>
          <p>管理您的预约、资料与参观反馈，让每一次到馆都从容有序。</p>
        </div>
        <div class="user-hero__seal">楚</div>
      </div>
    </div>

    <div class="container user-grid">
      <aside class="user-side">
        <div class="user-card">
          <div class="user-card__avatar">{{ avatarText }}</div>
          <strong>{{ auth.displayName }}</strong>
          <span class="user-card__role">{{ roleText }}</span>
          <span class="user-card__account">@{{ auth.user?.username }}</span>
        </div>
        <nav class="user-menu">
          <router-link
            v-for="item in menus"
            :key="item.path"
            :to="item.path"
            class="user-menu__link"
            :class="{ 'is-active': isActive(item.path) }"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </router-link>
          <button type="button" class="user-menu__link user-menu__link--danger" @click="logout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </button>
        </nav>
      </aside>

      <section class="user-content">
        <router-view />
      </section>
    </div>

    <SiteFooter />
  </div>
</template>

<style scoped>
.user-layout {
  min-height: 100vh;
  background: var(--paper);
}

.user-hero {
  position: relative;
  overflow: hidden;
  color: #f9f4e8;
  background:
    linear-gradient(110deg, #0a3532 0%, #0e4b47 62%, #155c55 100%);
}

.user-hero::after {
  position: absolute;
  right: -60px;
  bottom: -120px;
  width: 420px;
  height: 420px;
  border: 1px solid rgba(199, 161, 90, 0.25);
  border-radius: 50%;
  content: '';
  box-shadow: 0 0 0 40px rgba(199, 161, 90, 0.04), 0 0 0 90px rgba(199, 161, 90, 0.03);
}

.user-hero__inner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 42px 0;
}

.user-hero__eyebrow {
  color: var(--gold);
  font-size: 12px;
  letter-spacing: 0.3em;
}

.user-hero h1 {
  margin-top: 8px;
  color: #fff9ec;
  font-size: 32px;
  letter-spacing: 0.08em;
}

.user-hero p {
  margin-top: 8px;
  color: rgba(255, 249, 236, 0.62);
  font-size: 14px;
}

.user-hero__seal {
  display: grid;
  width: 76px;
  height: 76px;
  place-items: center;
  border: 1px solid rgba(199, 161, 90, 0.5);
  color: var(--gold);
  font-family: var(--font-serif);
  font-size: 34px;
  opacity: 0.85;
}

.user-grid {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 30px;
  padding: 34px 0 70px;
}

.user-side {
  align-self: start;
  position: sticky;
  top: 126px;
}

.user-card {
  display: flex;
  align-items: center;
  flex-direction: column;
  padding: 26px 18px 22px;
  border: 1px solid var(--border);
  border-radius: 12px 12px 0 0;
  background: var(--white);
}

.user-card__avatar {
  display: grid;
  width: 64px;
  height: 64px;
  place-items: center;
  border-radius: 50%;
  color: #fff9ec;
  background: linear-gradient(135deg, var(--teal), #1c6b63);
  font-family: var(--font-serif);
  font-size: 28px;
  box-shadow: 0 10px 24px rgba(14, 75, 71, 0.22);
}

.user-card strong {
  margin-top: 12px;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 18px;
}

.user-card__role {
  margin-top: 4px;
  padding: 1px 10px;
  border-radius: 999px;
  color: var(--teal);
  background: var(--teal-light);
  font-size: 11px;
}

.user-card__account {
  margin-top: 6px;
  color: var(--muted);
  font-size: 12px;
}

.user-menu {
  display: flex;
  flex-direction: column;
  padding: 8px;
  border: 1px solid var(--border);
  border-top: 0;
  border-radius: 0 0 12px 12px;
  background: var(--white);
}

.user-menu__link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border: 0;
  border-radius: 8px;
  color: #4d5754;
  background: transparent;
  font-size: 14px;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.user-menu__link:hover {
  color: var(--teal);
  background: #f4f0e6;
}

.user-menu__link.is-active {
  color: var(--teal);
  background: var(--teal-light);
  font-weight: 600;
}

.user-menu__link--danger {
  margin-top: 4px;
  color: #9a4b45;
}

.user-content {
  min-width: 0;
}

@media (max-width: 860px) {
  .user-grid {
    grid-template-columns: 1fr;
    gap: 20px;
    padding-top: 22px;
  }

  .user-side {
    position: static;
  }

  .user-card {
    display: none;
  }

  .user-menu {
    flex-direction: row;
    overflow-x: auto;
    padding: 6px;
    border: 1px solid var(--border);
    border-radius: 10px;
  }

  .user-menu__link {
    flex-shrink: 0;
    padding: 9px 13px;
  }

  .user-menu__link--danger {
    display: none;
  }

  .user-hero__seal {
    display: none;
  }

  .user-hero h1 {
    font-size: 26px;
  }
}
</style>