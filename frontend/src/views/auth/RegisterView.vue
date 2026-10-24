<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  idCard: '',
  password: '',
  confirmPassword: '',
})

function validateConfirm(_rule: unknown, value: string, callback: (error?: Error) => void) {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '4-20 位字母、数字或下划线', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  idCard: [{ pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
  ],
  confirmPassword: [{ validator: validateConfirm, trigger: 'blur' }],
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const user = await auth.register({
      username: form.username.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword,
      realName: form.realName.trim(),
      phone: form.phone.trim(),
      email: form.email || undefined,
      idCard: form.idCard || undefined,
    })
    ElMessage.success(`注册成功，欢迎您，${user.realName || user.username}`)
    await router.push('/')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-visual">
      <img src="/images/hero.svg" alt="曾侯乙编钟意象" class="register-visual__bg">
      <div class="register-visual__overlay" />
      <div class="register-visual__content">
        <router-link to="/" class="register-brand">
          <img src="/logo.svg" alt="标识">
          <div>
            <strong>湖北省博物馆</strong>
            <span>HUBEI PROVINCIAL MUSEUM</span>
          </div>
        </router-link>
        <div class="register-quote">
          <p class="eyebrow register-quote__eyebrow">CREATE AN ACCOUNT</p>
          <h1>一次注册<br>开启荆楚之旅</h1>
          <p>注册后可预约展览、管理订单、获取电子核销码，并及时接收博物馆公告。</p>
        </div>
        <ul class="register-benefits">
          <li><el-icon><CircleCheck /></el-icon> 提前 7 天预约，热门展览不错过</li>
          <li><el-icon><CircleCheck /></el-icon> 订单与核销码集中管理</li>
          <li><el-icon><CircleCheck /></el-icon> 参观后反馈建议，帮助我们做得更好</li>
        </ul>
      </div>
    </div>

    <div class="register-form-side">
      <div class="register-card">
        <div class="register-card__head">
          <span class="register-card__eyebrow">JOIN US</span>
          <h2>注册新账号</h2>
          <p>请填写真实信息，便于预约与入馆核验。</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-row">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="4-20 位字母、数字或下划线" />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" maxlength="11" placeholder="11 位手机号" />
            </el-form-item>
            <el-form-item label="邮箱（选填）" prop="email">
              <el-input v-model="form.email" placeholder="用于接收通知" />
            </el-form-item>
          </div>

          <el-form-item label="身份证号（选填）" prop="idCard">
            <el-input v-model="form.idCard" maxlength="18" placeholder="填写后预约时可自动带入" />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="设置密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="6-20 位" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" />
            </el-form-item>
          </div>

          <el-button type="primary" size="large" class="register-submit" :loading="submitting" @click="submit">
            注册并登录
          </el-button>
        </el-form>

        <div class="register-switch">
          已有账号？
          <router-link to="/login">直接登录</router-link>
          <router-link to="/" class="register-back">返回首页</router-link>
        </div>

        <p class="register-agreement">
          注册即表示您同意《用户服务协议》与《隐私政策》，我们仅将您的信息用于预约与入馆核验。
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  display: grid;
  min-height: 100vh;
  grid-template-columns: 1fr 1.05fr;
  background: var(--paper);
}

.register-visual {
  position: relative;
  overflow: hidden;
  min-height: 680px;
  color: #f8f2e5;
  background: #0a2b28;
}

.register-visual__bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.register-visual__overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(5, 24, 22, 0.45), rgba(5, 24, 22, 0.88)),
    radial-gradient(circle at 26% 24%, rgba(199, 161, 90, 0.18), transparent 45%);
}

.register-visual__content {
  position: relative;
  z-index: 1;
  display: flex;
  height: 100%;
  flex-direction: column;
  justify-content: space-between;
  padding: 46px 52px;
}

.register-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.register-brand img {
  width: 44px;
  height: 44px;
}

.register-brand strong {
  display: block;
  color: #fff9ec;
  font-family: var(--font-serif);
  font-size: 20px;
  letter-spacing: 0.12em;
}

.register-brand span {
  display: block;
  margin-top: 2px;
  color: rgba(248, 242, 229, 0.4);
  font-size: 8.5px;
  letter-spacing: 0.16em;
}

.register-quote {
  max-width: 460px;
  margin: auto 0;
  padding: 50px 0;
}

.register-quote__eyebrow {
  color: var(--gold);
}

.register-quote h1 {
  color: #fff9ec;
  font-size: clamp(36px, 4.4vw, 54px);
  font-weight: 500;
  letter-spacing: 0.14em;
  line-height: 1.4;
}

.register-quote > p:last-child {
  max-width: 400px;
  margin-top: 18px;
  color: rgba(248, 242, 229, 0.58);
  font-size: 13.5px;
  line-height: 1.9;
}

.register-benefits li {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-bottom: 8px;
  color: rgba(248, 242, 229, 0.55);
  font-size: 12px;
}

.register-benefits .el-icon {
  color: var(--gold);
}

.register-form-side {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 44px 32px;
}

.register-card {
  width: min(560px, 100%);
}

.register-card__head {
  margin-bottom: 24px;
}

.register-card__eyebrow {
  color: var(--bronze);
  font-size: 10px;
  letter-spacing: 0.22em;
}

.register-card__head h2 {
  margin-top: 6px;
  font-size: 30px;
  letter-spacing: 0.1em;
}

.register-card__head p {
  margin-top: 4px;
  color: var(--muted);
  font-size: 12.5px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.register-submit {
  width: 100%;
  margin-top: 4px;
  letter-spacing: 0.14em;
}

.register-switch {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 18px;
  color: var(--muted);
  font-size: 12.5px;
}

.register-switch a {
  color: var(--teal);
  font-weight: 600;
}

.register-back {
  margin-left: auto;
  color: var(--muted) !important;
  font-weight: 400 !important;
}

.register-agreement {
  margin-top: 30px;
  color: #99917f;
  font-size: 10.5px;
  line-height: 1.7;
  text-align: center;
}

@media (max-width: 960px) {
  .register-page {
    grid-template-columns: 1fr;
  }

  .register-visual {
    display: none;
  }

  .register-form-side {
    min-height: 100vh;
    padding: 36px 20px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>