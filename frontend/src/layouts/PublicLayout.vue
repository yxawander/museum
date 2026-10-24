<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import SiteHeader from '@/components/layout/SiteHeader.vue'
import SiteFooter from '@/components/layout/SiteFooter.vue'

const showTop = ref(false)

function onScroll() {
  showTop.value = window.scrollY > 600
}

function toTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>

<template>
  <div class="public-layout">
    <SiteHeader />
    <main class="page-main">
      <router-view />
    </main>
    <SiteFooter />
    <transition name="page-fade">
      <button v-if="showTop" type="button" class="back-top" aria-label="回到顶部" @click="toTop">
        <el-icon><Top /></el-icon>
      </button>
    </transition>
  </div>
</template>

<style scoped>
.public-layout {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
}

.page-main {
  flex: 1;
}

.back-top {
  position: fixed;
  right: 28px;
  bottom: 32px;
  z-index: 40;
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border: 1px solid rgba(199, 161, 90, 0.5);
  border-radius: 50%;
  color: var(--gold);
  background: rgba(10, 43, 40, 0.9);
  box-shadow: var(--shadow-md);
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease;
}

.back-top:hover {
  transform: translateY(-3px);
  background: var(--teal);
}

@media (max-width: 640px) {
  .back-top {
    right: 16px;
    bottom: 20px;
    width: 40px;
    height: 40px;
  }
}
</style>