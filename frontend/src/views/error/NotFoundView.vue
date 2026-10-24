<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="notfound-page">
    <div class="notfound-seal">楚</div>
    <span class="notfound-code">404</span>
    <h1>这一页，暂时没有找到</h1>
    <p>您访问的页面可能已被移动或删除。不妨回到首页，继续探索荆楚文明。</p>
    <div class="notfound-actions">
      <el-button type="primary" round @click="router.push('/')">回到首页</el-button>
      <el-button round @click="router.push('/exhibitions')">浏览展览</el-button>
    </div>
  </div>
</template>

<style scoped>
.notfound-page {
  display: flex;
  min-height: 100vh;
  align-items: center;
  flex-direction: column;
  justify-content: center;
  padding: 30px;
  color: var(--text);
  background:
    radial-gradient(circle at 50% 20%, rgba(199, 161, 90, 0.12), transparent 45%),
    var(--paper);
  text-align: center;
}

.notfound-seal {
  display: grid;
  width: 76px;
  height: 76px;
  place-items: center;
  border: 1px solid rgba(167, 123, 69, 0.4);
  color: var(--bronze);
  font-family: var(--font-serif);
  font-size: 32px;
  transform: rotate(-4deg);
}

.notfound-code {
  margin-top: 18px;
  color: var(--teal);
  font-family: var(--font-serif);
  font-size: 92px;
  line-height: 1;
  opacity: 0.85;
}

.notfound-page h1 {
  margin-top: 10px;
  font-size: 28px;
  letter-spacing: 0.1em;
}

.notfound-page p {
  max-width: 440px;
  margin-top: 10px;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.9;
}

.notfound-actions {
  display: flex;
  gap: 12px;
  margin-top: 26px;
}
</style>