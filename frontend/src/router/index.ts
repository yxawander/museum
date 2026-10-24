import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/PublicLayout.vue'),
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/public/HomeView.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'exhibitions',
        name: 'exhibitions',
        component: () => import('@/views/public/ExhibitionListView.vue'),
        meta: { title: '展览陈列' },
      },
      {
        path: 'exhibitions/:id',
        name: 'exhibition-detail',
        component: () => import('@/views/public/ExhibitionDetailView.vue'),
        meta: { title: '展览详情' },
      },
      {
        path: 'booking',
        name: 'booking',
        component: () => import('@/views/public/BookingView.vue'),
        meta: { title: '预约参观', requiresAuth: true },
      },
      {
        path: 'announcements',
        name: 'announcements',
        component: () => import('@/views/public/AnnouncementListView.vue'),
        meta: { title: '公告资讯' },
      },
      {
        path: 'announcements/:id',
        name: 'announcement-detail',
        component: () => import('@/views/public/AnnouncementDetailView.vue'),
        meta: { title: '公告详情' },
      },
      {
        path: 'about',
        name: 'about',
        component: () => import('@/views/public/AboutView.vue'),
        meta: { title: '参观指南' },
      },
    ],
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', guestOnly: true },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { title: '注册', guestOnly: true },
  },
  {
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/user/profile' },
      {
        path: 'profile',
        name: 'user-profile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { title: '个人资料', requiresAuth: true },
      },
      {
        path: 'reservations',
        name: 'my-reservations',
        component: () => import('@/views/user/MyReservationsView.vue'),
        meta: { title: '我的预约', requiresAuth: true },
      },
      {
        path: 'reservations/:id',
        name: 'reservation-detail',
        component: () => import('@/views/user/ReservationDetailView.vue'),
        meta: { title: '预约详情', requiresAuth: true },
      },
      {
        path: 'feedback',
        name: 'my-feedback',
        component: () => import('@/views/user/FeedbackView.vue'),
        meta: { title: '意见反馈', requiresAuth: true },
      },
      {
        path: 'password',
        name: 'change-password',
        component: () => import('@/views/user/ChangePasswordView.vue'),
        meta: { title: '修改密码', requiresAuth: true },
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '数据看板', requiresAuth: true, roles: ['ADMIN'] },
      },
      {
        path: 'exhibitions',
        name: 'admin-exhibitions',
        component: () => import('@/views/admin/AdminExhibitionsView.vue'),
        meta: { title: '展览管理', requiresAuth: true, roles: ['ADMIN'] },
      },
      {
        path: 'slots',
        name: 'admin-slots',
        component: () => import('@/views/admin/AdminSlotsView.vue'),
        meta: { title: '时段管理', requiresAuth: true, roles: ['ADMIN'] },
      },
      {
        path: 'reservations',
        name: 'admin-reservations',
        component: () => import('@/views/admin/AdminReservationsView.vue'),
        meta: { title: '预约管理', requiresAuth: true, roles: ['ADMIN'] },
      },
      {
        path: 'checkin',
        name: 'admin-checkin',
        component: () => import('@/views/admin/AdminCheckinView.vue'),
        meta: { title: '现场核销', requiresAuth: true, roles: ['ADMIN', 'STAFF'] },
      },
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('@/views/admin/AdminUsersView.vue'),
        meta: { title: '用户管理', requiresAuth: true, roles: ['ADMIN'] },
      },
      {
        path: 'announcements',
        name: 'admin-announcements',
        component: () => import('@/views/admin/AdminAnnouncementsView.vue'),
        meta: { title: '公告管理', requiresAuth: true, roles: ['ADMIN'] },
      },
      {
        path: 'feedbacks',
        name: 'admin-feedbacks',
        component: () => import('@/views/admin/AdminFeedbacksView.vue'),
        meta: { title: '意见反馈', requiresAuth: true, roles: ['ADMIN'] },
      },
    ],
  },
  {
    path: '/403',
    name: 'forbidden',
    component: () => import('@/views/error/ForbiddenView.vue'),
    meta: { title: '无权访问' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/error/NotFoundView.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (auth.token && !auth.user) {
    await auth.fetchMe()
  }

  if (to.meta.requiresAuth && !auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.guestOnly && auth.token) {
    return auth.isAdmin ? '/admin/dashboard' : auth.isStaff ? '/admin/checkin' : '/'
  }

  if (to.meta.roles && auth.user && !to.meta.roles.includes(auth.user.role)) {
    return '/403'
  }

  if (to.path === '/admin' && auth.user?.role === 'STAFF') {
    return '/admin/checkin'
  }

  const suffix = '湖北省博物馆参观预约系统'
  document.title = to.meta.title ? `${to.meta.title} · ${suffix}` : suffix
  return true
})

export default router