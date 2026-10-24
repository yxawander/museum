<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { authApi } from '@/api/auth'
import { reservationApi } from '@/api/reservation'
import { useAuthStore } from '@/stores/auth'
import { ROLE_TEXT } from '@/utils/constants'
import { formatDate } from '@/utils/format'

const auth = useAuthStore()
const formRef = ref<FormInstance>()
const saving = ref(false)
const loading = ref(true)
const totalReservations = ref(0)
const upcomingReservations = ref(0)

const form = reactive({
  realName: '',
  phone: '',
  email: '',
  idCard: '',
})

const rules: FormRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

const avatarText = computed(() => (auth.user?.realName || auth.user?.username || '观').slice(0, 1))
const roleText = computed(() => ROLE_TEXT[auth.user?.role ?? 'VISITOR'] ?? '普通观众')

async function load() {
  loading.value = true
  try {
    const user = await auth.fetchMe()
    if (user) {
      form.realName = user.realName
      form.phone = user.phone
      form.email = user.email ?? ''
      form.idCard = user.idCard?.includes('*') ? '' : user.idCard ?? ''
    }
    const [all, upcoming] = await Promise.all([
      reservationApi.list({ page: 1, size: 1 }),
      reservationApi.list({ page: 1, size: 1, status: 'CONFIRMED' }),
    ])
    totalReservations.value = all.total
    upcomingReservations.value = upcoming.total
  } finally {
    loading.value = false
  }
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const user = await authApi.updateProfile({
      realName: form.realName.trim(),
      phone: form.phone.trim(),
      email: form.email || undefined,
      idCard: form.idCard && !form.idCard.includes('*') ? form.idCard : undefined,
    })
    auth.updateUser(user)
    ElMessage.success('个人资料已更新')
    form.idCard = user.idCard?.includes('*') ? '' : user.idCard ?? ''
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="profile-view">
    <div class="content-head">
      <div>
        <span class="content-head__eyebrow">MY ACCOUNT</span>
        <h2>个人资料</h2>
        <p>完善个人信息，预约时可自动带入，减少重复填写。</p>
      </div>
      <el-button type="primary" plain @click="$router.push('/user/password')">修改密码</el-button>
    </div>

    <div class="profile-stats">
      <div class="mini-stat">
        <span>累计预约</span>
        <strong>{{ totalReservations }}</strong>
        <small>次订单</small>
      </div>
      <div class="mini-stat">
        <span>待参观</span>
        <strong>{{ upcomingReservations }}</strong>
        <small>个行程</small>
      </div>
      <div class="mini-stat">
        <span>账号角色</span>
        <strong class="mini-stat__text">{{ roleText }}</strong>
        <small>当前身份</small>
      </div>
    </div>

    <div class="profile-grid">
      <section class="panel">
        <div class="panel__head">
          <h3>基本信息</h3>
          <span>注册于 {{ formatDate(auth.user?.createdAt) }}</span>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="profile-form">
          <el-form-item label="用户名">
            <el-input :model-value="auth.user?.username" disabled />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" maxlength="20" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" maxlength="11" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱（选填）" />
          </el-form-item>
          <el-form-item label="身份证号">
            <el-input
              v-model="form.idCard"
              maxlength="18"
              :placeholder="auth.user?.idCard?.includes('*') ? '已认证，如需修改请输入完整身份证号' : '请输入身份证号（选填）'"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="save">保存修改</el-button>
            <el-button @click="load">重置</el-button>
          </el-form-item>
        </el-form>
      </section>

      <aside class="profile-side">
        <div class="account-card">
          <div class="account-card__avatar">{{ avatarText }}</div>
          <strong>{{ auth.displayName }}</strong>
          <span class="account-card__role">{{ roleText }}</span>
          <p>@{{ auth.user?.username }}</p>
          <div class="account-card__line" />
          <div class="account-card__row">
            <span>账号状态</span>
            <strong class="is-ok">正常</strong>
          </div>
          <div class="account-card__row">
            <span>最近登录</span>
            <strong>{{ formatDate(auth.user?.lastLoginAt, true) }}</strong>
          </div>
        </div>

        <div class="security-card">
          <h4>账号安全</h4>
          <p>建议定期修改密码，不要与他人共享账号。</p>
          <el-button text type="primary" @click="$router.push('/user/password')">
            前往修改密码 <el-icon><Right /></el-icon>
          </el-button>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.content-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 22px;
}

.content-head__eyebrow {
  color: var(--bronze);
  font-size: 10px;
  letter-spacing: 0.2em;
}

.content-head h2 {
  margin-top: 4px;
  font-size: 27px;
  letter-spacing: 0.08em;
}

.content-head p {
  margin-top: 4px;
  color: var(--muted);
  font-size: 12.5px;
}

.profile-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-bottom: 20px;
}

.mini-stat {
  padding: 18px 20px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.mini-stat span {
  display: block;
  color: var(--muted);
  font-size: 11px;
  letter-spacing: 0.08em;
}

.mini-stat strong {
  display: inline-block;
  margin-top: 3px;
  color: var(--teal);
  font-family: var(--font-serif);
  font-size: 30px;
  font-weight: 500;
  line-height: 1.2;
}

.mini-stat small {
  margin-left: 4px;
  color: var(--muted);
  font-size: 10px;
}

.mini-stat__text {
  font-size: 20px !important;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 20px;
}

.panel {
  padding: 24px 26px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--border);
}

.panel__head h3 {
  font-size: 19px;
}

.panel__head span {
  color: var(--muted);
  font-size: 10.5px;
}

.profile-form {
  max-width: 520px;
}

.profile-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.account-card {
  padding: 24px;
  border: 1px solid rgba(199, 161, 90, 0.25);
  border-radius: 10px;
  color: rgba(248, 242, 229, 0.65);
  background: linear-gradient(150deg, #0a3532, #0d4843);
}

.account-card__avatar {
  display: grid;
  width: 58px;
  height: 58px;
  place-items: center;
  border: 1px solid rgba(199, 161, 90, 0.5);
  border-radius: 50%;
  color: var(--gold);
  font-family: var(--font-serif);
  font-size: 26px;
}

.account-card strong {
  display: block;
  margin-top: 12px;
  color: #fff9ec;
  font-family: var(--font-serif);
  font-size: 19px;
}

.account-card__role {
  display: inline-block;
  margin-top: 3px;
  padding: 1px 9px;
  border: 1px solid rgba(199, 161, 90, 0.4);
  color: var(--gold);
  font-size: 10px;
}

.account-card p {
  margin-top: 4px;
  color: rgba(248, 242, 229, 0.4);
  font-size: 11px;
}

.account-card__line {
  height: 1px;
  margin: 16px 0 8px;
  background: rgba(255, 255, 255, 0.1);
}

.account-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 6px 0;
  font-size: 11.5px;
}

.account-card__row strong {
  margin: 0;
  color: rgba(248, 242, 229, 0.78);
  font-family: var(--font-sans);
  font-size: 11px;
  font-weight: 400;
}

.account-card__row .is-ok {
  color: #8fd0ae;
}

.security-card {
  padding: 20px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.security-card h4 {
  font-size: 14px;
  letter-spacing: 0.06em;
}

.security-card p {
  margin-top: 6px;
  color: var(--muted);
  font-size: 11.5px;
  line-height: 1.7;
}

@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-side {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .content-head {
    flex-direction: column;
  }

  .profile-stats {
    grid-template-columns: 1fr;
  }

  .profile-side {
    grid-template-columns: 1fr;
  }

  .panel {
    padding: 18px 16px;
  }
}
</style>