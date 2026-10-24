<script setup lang="ts">
import type { Exhibition } from '@/types'
import { formatMoney } from '@/utils/format'

defineProps<{
  exhibition: Exhibition
  compact?: boolean
}>()
</script>

<template>
  <router-link :to="`/exhibitions/${exhibition.id}`" class="ex-card" :class="{ 'ex-card--compact': compact }">
    <div class="ex-card__media">
      <img :src="exhibition.coverUrl || '/images/exhibitions/bells.svg'" :alt="exhibition.title" loading="lazy">
      <span class="ex-card__category">{{ exhibition.category }}</span>
      <span class="ex-card__price">{{ formatMoney(exhibition.ticketPrice) }}</span>
    </div>
    <div class="ex-card__body">
      <h3>{{ exhibition.title }}</h3>
      <p v-if="exhibition.subtitle" class="ex-card__subtitle">{{ exhibition.subtitle }}</p>
      <div class="ex-card__meta">
        <span><el-icon><Location /></el-icon>{{ exhibition.venue }}</span>
        <span><el-icon><Calendar /></el-icon>{{ exhibition.startDate }} 起</span>
      </div>
      <div class="ex-card__foot">
        <span class="ex-card__more">查看展览 <el-icon><Right /></el-icon></span>
        <span class="ex-card__views">{{ exhibition.viewCount }} 次浏览</span>
      </div>
    </div>
  </router-link>
</template>

<style scoped>
.ex-card {
  display: flex;
  overflow: hidden;
  flex-direction: column;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--white);
  box-shadow: var(--shadow-sm);
  transition: transform 0.35s ease, box-shadow 0.35s ease;
}

.ex-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-md);
}

.ex-card__media {
  position: relative;
  overflow: hidden;
  aspect-ratio: 4 / 3;
  background: #0b2b28;
}

.ex-card__media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.ex-card:hover .ex-card__media img {
  transform: scale(1.06);
}

.ex-card__category {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 4px 10px;
  border: 1px solid rgba(199, 161, 90, 0.55);
  border-radius: 2px;
  color: #f9f1dc;
  background: rgba(5, 28, 26, 0.55);
  font-size: 11px;
  letter-spacing: 0.1em;
  backdrop-filter: blur(4px);
}

.ex-card__price {
  position: absolute;
  right: 14px;
  bottom: 14px;
  padding: 4px 10px;
  border-radius: 2px;
  color: #fff8e9;
  background: rgba(167, 123, 69, 0.9);
  font-size: 12px;
  letter-spacing: 0.04em;
}

.ex-card__body {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: 20px 20px 16px;
}

.ex-card__body h3 {
  font-size: 22px;
  letter-spacing: 0.06em;
}

.ex-card__subtitle {
  display: -webkit-box;
  overflow: hidden;
  margin-top: 7px;
  color: var(--muted);
  font-size: 13px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.ex-card__meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 14px;
  color: #6b7572;
  font-size: 12px;
}

.ex-card__meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.ex-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 18px;
  padding-top: 12px;
  border-top: 1px dashed rgba(19, 44, 42, 0.12);
  color: var(--muted);
  font-size: 11.5px;
}

.ex-card__more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--teal);
  font-size: 12.5px;
}

.ex-card__views {
  opacity: 0.75;
}

.ex-card--compact .ex-card__body {
  padding: 15px 16px 13px;
}

.ex-card--compact .ex-card__body h3 {
  font-size: 18px;
}

.ex-card--compact .ex-card__subtitle,
.ex-card--compact .ex-card__views {
  display: none;
}
</style>