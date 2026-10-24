<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

function goBack() {
  if (auth.isAdmin) {
    void router.push('/admin/dashboard')
  } else if (auth.isStaff) {
    void router.push('/admin/checkin')
  } else {
    void router.push('/')
  }
}
</script>

<template>
  <div class="error-page">
    <div class="error-inner">
      <img src="/logo.svg" alt="标识" class="error-logo">
      <span class="error-code">403</span>
      <h1>抱歉，您没有访问权限</h1>
      <p>该页面仅向具备相应角色的账号开放。如需访问，请联系博物馆系统管理员。</p>
      <div class="error-actions">
        <el-button type="primary" round @click="goBack">返回工作台</el-button>
        <el-button round @click="router.push('/')">回到首页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.error-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 30px;
  color: #f8f2e5;
  background:
    radial-gradient(circle at 50% 30%, rgba(199, 161, 90, 0.18), transparent 45%),
    #0a2b28;
}

.error-inner {
  max-width: 520px;
  text-align: center;
}

.error-logo {
  width: 56px;
  height: 56px;
  margin: 0 auto 18px;
}

.error-code {
  display: block;
  color: var(--gold);
  font-family: var(--font-serif);
  font-size: 92px;
  line-height: 1;
  opacity: 0.9;
}

.error-inner h1 {
  margin-top: 12px;
  color: #fff9ec;
  font-size: 28px;
  letter-spacing: 0.1em;
}

.error-inner p {
  margin-top: 10px;
  color: rgba(248, 242, 229, 0.55);
  font-size: 13px;
  line-height: 1.9;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 26px;
}
</style>