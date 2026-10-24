<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({ username: '', password: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const demoAccounts = [
  { label: '观众', username: 'visitor', password: 'Visitor@123' },
  { label: '管理员', username: 'admin', password: 'Admin@123' },
  { label: '检票员', username: 'staff', password: 'Staff@123' },
]

function fill(account: (typeof demoAccounts)[number]) {
  form.username = account.username
  form.password = account.password
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const user = await auth.login({ username: form.username.trim(), password: form.password })
    ElMessage.success(`欢迎回来，${user.realName || user.username}`)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    if (redirect) {
      await router.push(redirect)
    } else if (user.role === 'ADMIN') {
      await router.push('/admin/dashboard')
    } else if (user.role === 'STAFF') {
      await router.push('/admin/checkin')
    } else {
      await router.push('/')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-visual">
      <img src="/images/museum.svg" alt="湖北省博物馆" class="auth-visual__bg">
      <div class="auth-visual__overlay" />
      <div class="auth-visual__content">
        <router-link to="/" class="auth-brand">
          <img src="/logo.svg" alt="标识">
          <div>
            <strong>湖北省博物馆</strong>
            <span>HUBEI PROVINCIAL MUSEUM</span>
          </div>
        </router-link>
        <div class="auth-quote">
          <p class="eyebrow auth-quote__eyebrow">JINGCHU CIVILIZATION</p>
          <h1>让文物活起来<br>与文明对话</h1>
          <p>从曾侯乙编钟的华夏正音，到越王勾践剑的千年锋芒。提前预约，开启一段与荆楚文明的相遇。</p>
        </div>
        <div class="auth-features">
          <span><el-icon><CircleCheck /></el-icon> 实名预约</span>
          <span><el-icon><CircleCheck /></el-icon> 分时入馆</span>
          <span><el-icon><CircleCheck /></el-icon> 扫码核验</span>
        </div>
      </div>
    </div>

    <div class="auth-form-side">
      <div class="auth-card">
        <div class="auth-card__head">
          <span class="auth-card__eyebrow">WELCOME BACK</span>
          <h2>登录预约系统</h2>
          <p>使用您的账号登录，管理预约与参观信息。</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" size="large" placeholder="请输入用户名" autocomplete="username">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              size="large"
              show-password
              placeholder="请输入密码"
              autocomplete="current-password"
              @keyup.enter="submit"
            >
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>

          <el-button type="primary" size="large" class="auth-submit" :loading="submitting" @click="submit">
            登录
          </el-button>
        </el-form>

        <div class="auth-switch">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
          <router-link to="/" class="auth-back">返回首页</router-link>
        </div>

        <div class="demo-box">
          <span class="demo-box__title">演示账号（点击填充）</span>
          <div class="demo-box__list">
            <button
              v-for="account in demoAccounts"
              :key="account.username"
              type="button"
              class="demo-chip"
              @click="fill(account)"
            >
              <strong>{{ account.label }}</strong>
              <small>{{ account.username }}</small>
            </button>
          </div>
        </div>
      </div>

      <p class="auth-copyright">© {{ new Date().getFullYear() }} 湖北省博物馆参观预约系统 · 课程设计作品</p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: grid;
  min-height: 100vh;
  grid-template-columns: 1.05fr 1fr;
  background: var(--paper);
}

.auth-visual {
  position: relative;
  overflow: hidden;
  min-height: 640px;
  color: #f8f2e5;
  background: #0a2b28;
}

.auth-visual__bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.auth-visual__overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(5, 24, 22, 0.35), rgba(5, 24, 22, 0.86)),
    radial-gradient(circle at 30% 20%, rgba(199, 161, 90, 0.18), transparent 45%);
}

.auth-visual__content {
  position: relative;
  z-index: 1;
  display: flex;
  height: 100%;
  flex-direction: column;
  justify-content: space-between;
  padding: 46px 52px;
}

.auth-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.auth-brand img {
  width: 44px;
  height: 44px;
}

.auth-brand strong {
  display: block;
  color: #fff9ec;
  font-family: var(--font-serif);
  font-size: 20px;
  letter-spacing: 0.12em;
}

.auth-brand span {
  display: block;
  margin-top: 2px;
  color: rgba(248, 242, 229, 0.4);
  font-size: 8.5px;
  letter-spacing: 0.16em;
}

.auth-quote {
  max-width: 460px;
  margin: auto 0;
  padding: 60px 0;
}

.auth-quote__eyebrow {
  color: var(--gold);
}

.auth-quote h1 {
  color: #fff9ec;
  font-size: clamp(36px, 4.4vw, 54px);
  font-weight: 500;
  letter-spacing: 0.14em;
  line-height: 1.4;
}

.auth-quote > p:last-child {
  max-width: 400px;
  margin-top: 20px;
  color: rgba(248, 242, 229, 0.58);
  font-size: 13.5px;
  line-height: 1.9;
}

.auth-features {
  display: flex;
  flex-wrap: wrap;
  gap: 22px;
  color: rgba(248, 242, 229, 0.55);
  font-size: 11.5px;
}

.auth-features span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.auth-features .el-icon {
  color: var(--gold);
}

.auth-form-side {
  display: flex;
  align-items: center;
  flex-direction: column;
  justify-content: center;
  padding: 40px 28px;
}

.auth-card {
  width: min(420px, 100%);
}

.auth-card__head {
  margin-bottom: 30px;
}

.auth-card__eyebrow {
  color: var(--bronze);
  font-size: 10px;
  letter-spacing: 0.22em;
}

.auth-card__head h2 {
  margin-top: 8px;
  font-size: 32px;
  letter-spacing: 0.1em;
}

.auth-card__head p {
  margin-top: 6px;
  color: var(--muted);
  font-size: 13px;
}

.auth-submit {
  width: 100%;
  margin-top: 8px;
  letter-spacing: 0.16em;
}

.auth-switch {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 20px;
  color: var(--muted);
  font-size: 12.5px;
}

.auth-switch a {
  color: var(--teal);
  font-weight: 600;
}

.auth-back {
  margin-left: auto;
  color: var(--muted) !important;
  font-weight: 400 !important;
}

.demo-box {
  margin-top: 30px;
  padding: 16px;
  border: 1px dashed rgba(167, 123, 69, 0.35);
  border-radius: 8px;
  background: #faf6ec;
}

.demo-box__title {
  color: #8a7353;
  font-size: 10.5px;
  letter-spacing: 0.08em;
}

.demo-box__list {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.demo-chip {
  display: flex;
  flex: 1;
  align-items: flex-start;
  flex-direction: column;
  padding: 7px 9px;
  border: 1px solid rgba(19, 44, 42, 0.1);
  border-radius: 6px;
  background: var(--white);
  cursor: pointer;
  transition: all 0.2s ease;
}

.demo-chip:hover {
  border-color: var(--teal);
  background: var(--teal-light);
}

.demo-chip strong {
  color: var(--ink);
  font-size: 11.5px;
}

.demo-chip small {
  color: var(--muted);
  font-size: 9px;
}

.auth-copyright {
  margin-top: 42px;
  color: var(--muted);
  font-size: 10.5px;
  text-align: center;
}

@media (max-width: 900px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-visual {
    display: none;
  }

  .auth-form-side {
    min-height: 100vh;
    padding: 40px 20px;
    background:
      radial-gradient(circle at 80% 0%, rgba(199, 161, 90, 0.12), transparent 40%),
      var(--paper);
  }
}
</style>