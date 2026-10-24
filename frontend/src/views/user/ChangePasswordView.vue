<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { authApi } from '@/api/auth'

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

function validateConfirm(_rule: unknown, value: string, callback: (error?: Error) => void) {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
  ],
  confirmPassword: [{ validator: validateConfirm, trigger: 'blur' }],
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await authApi.changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword,
    })
    ElMessage.success('密码修改成功，请牢记新密码')
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
    formRef.value?.clearValidate()
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="password-view">
    <div class="content-head">
      <div>
        <span class="content-head__eyebrow">SECURITY</span>
        <h2>修改密码</h2>
        <p>为了账号安全，建议使用字母、数字与符号组合的密码。</p>
      </div>
    </div>

    <div class="password-grid">
      <section class="panel">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="password-form">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入当前密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="form.newPassword" type="password" show-password placeholder="6-20 位字符" />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
          </el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">确认修改</el-button>
        </el-form>
      </section>

      <aside class="security-tips">
        <div class="security-tips__icon">
          <el-icon><Lock /></el-icon>
        </div>
        <h3>密码安全建议</h3>
        <ul>
          <li>长度至少 8 位，建议包含大小写字母、数字与符号。</li>
          <li>不要使用生日、手机号、用户名等易猜测信息。</li>
          <li>不要与其他网站使用相同密码。</li>
          <li>如发现账号异常，请及时联系博物馆工作人员。</li>
        </ul>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.content-head {
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

.password-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 20px;
}

.panel {
  padding: 26px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.password-form {
  max-width: 420px;
}

.security-tips {
  align-self: start;
  padding: 24px;
  border: 1px solid rgba(199, 161, 90, 0.28);
  border-radius: 10px;
  color: rgba(248, 242, 229, 0.62);
  background: linear-gradient(150deg, #0a3532, #0d4843);
}

.security-tips__icon {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  border: 1px solid rgba(199, 161, 90, 0.45);
  border-radius: 50%;
  color: var(--gold);
  font-size: 20px;
}

.security-tips h3 {
  margin-top: 14px;
  color: #fff9ec;
  font-size: 18px;
}

.security-tips li {
  position: relative;
  margin-top: 8px;
  padding-left: 13px;
  font-size: 11.5px;
  line-height: 1.7;
}

.security-tips li::before {
  position: absolute;
  top: 9px;
  left: 0;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  content: '';
  background: var(--gold);
}

@media (max-width: 900px) {
  .password-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .panel {
    padding: 18px 16px;
  }
}
</style>