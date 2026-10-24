<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { publicApi } from '@/api/public'
import type { Exhibition, Slot } from '@/types'
import { formatMoney, isSlotBookable, shortTime, weekdayText } from '@/utils/format'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(true)
const exhibition = ref<Exhibition | null>(null)
const slots = ref<Slot[]>([])
const selectedDate = ref('')
const booking = ref(false)

const dateOptions = computed(() => {
  const result: { date: string; day: string; weekday: string; count: number; closed: boolean }[] = []
  for (let i = 0; i < 7; i += 1) {
    const date = dayjs().add(i, 'day').format('YYYY-MM-DD')
    const daySlots = slots.value.filter((slot) => slot.visitDate === date)
    const count = daySlots.filter((slot) => slot.status === 'OPEN' && slot.remaining > 0 && isSlotBookable(date, slot.startTime)).length
    result.push({
      date,
      day: dayjs(date).format('DD'),
      weekday: i === 0 ? '今天' : weekdayText(date),
      count,
      closed: dayjs(date).day() === 1 || daySlots.length === 0,
    })
  }
  return result
})

const currentSlots = computed(() => slots.value.filter((slot) => slot.visitDate === selectedDate.value))

const paragraphs = computed(() =>
  (exhibition.value?.description ?? '').split(/\n+/).filter((item) => item.trim().length > 0),
)

function slotState(slot: Slot): { text: string; disabled: boolean } {
  if (slot.status === 'CLOSED') return { text: '已关闭', disabled: true }
  if (slot.remaining <= 0) return { text: '已约满', disabled: true }
  if (!isSlotBookable(slot.visitDate, slot.startTime)) return { text: '已停止预约', disabled: true }
  return { text: `余 ${slot.remaining} 人`, disabled: false }
}

function chooseDate(date: string) {
  selectedDate.value = date
}

function chooseSlot(slot: Slot) {
  if (slotState(slot).disabled) return
  if (!auth.isLoggedIn) {
    void router.push({ path: '/login', query: { redirect: `/booking?exhibitionId=${slot.exhibitionId}&slotId=${slot.id}` } })
    return
  }
  booking.value = true
  void router.push({
    path: '/booking',
    query: { exhibitionId: String(slot.exhibitionId), slotId: String(slot.id) },
  })
}

function goBooking() {
  if (!exhibition.value) return
  if (!auth.isLoggedIn) {
    void router.push({ path: '/login', query: { redirect: `/booking?exhibitionId=${exhibition.value.id}` } })
    return
  }
  void router.push({ path: '/booking', query: { exhibitionId: String(exhibition.value.id) } })
}

onMounted(async () => {
  try {
    const id = route.params.id as string
    const startDate = dayjs().format('YYYY-MM-DD')
    const endDate = dayjs().add(13, 'day').format('YYYY-MM-DD')
    const [detail, slotList] = await Promise.all([
      publicApi.exhibition(id),
      publicApi.slots(id, { startDate, endDate }),
    ])
    exhibition.value = detail
    slots.value = slotList
    const firstBookable = dateOptions.value.find((item) => item.count > 0)
    selectedDate.value = firstBookable?.date ?? startDate
  } catch {
    ElMessage.error('展览加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="detail-page">
    <template v-if="exhibition">
      <section class="detail-hero">
        <div class="detail-hero__bg">
          <img :src="exhibition.coverUrl || '/images/exhibitions/bells.svg'" :alt="exhibition.title">
        </div>
        <div class="container detail-hero__inner">
          <div class="breadcrumb">
            <router-link to="/">首页</router-link>
            <span>/</span>
            <router-link to="/exhibitions">展览陈列</router-link>
            <span>/</span>
            <span>{{ exhibition.title }}</span>
          </div>
          <span class="detail-hero__cat">{{ exhibition.category }}</span>
          <h1>{{ exhibition.title }}</h1>
          <p v-if="exhibition.subtitle" class="detail-hero__sub">{{ exhibition.subtitle }}</p>
          <div class="detail-hero__meta">
            <span><el-icon><Location /></el-icon>{{ exhibition.venue }}</span>
            <span><el-icon><Calendar /></el-icon>{{ exhibition.startDate }} — {{ exhibition.endDate }}</span>
            <span><el-icon><Clock /></el-icon>{{ shortTime(exhibition.openTime) }}—{{ shortTime(exhibition.closeTime) }}</span>
            <span><el-icon><Ticket /></el-icon>{{ formatMoney(exhibition.ticketPrice) }}</span>
          </div>
        </div>
      </section>

      <div class="container detail-grid">
        <article class="detail-main">
          <section class="detail-block">
            <p class="eyebrow">EXHIBITION STORY</p>
            <h2>展览简介</h2>
            <div class="detail-text">
              <p v-for="(paragraph, index) in paragraphs" :key="index">{{ paragraph }}</p>
            </div>
          </section>

          <section class="detail-block">
            <p class="eyebrow">VISITOR TIPS</p>
            <h2>参观提示</h2>
            <div class="tips-box">
              <el-icon><InfoFilled /></el-icon>
              <p>{{ exhibition.tips || '请遵守馆内参观秩序，勿触摸展品，拍照请关闭闪光灯。' }}</p>
            </div>
            <ul class="detail-list">
              <li>请携带预约时使用的有效身份证件，按预约时段到馆。</li>
              <li>展厅内请勿饮食、奔跑与大声喧哗，保持安静参观环境。</li>
              <li>1.2 米以下儿童、行动不便者需由成人陪同入馆。</li>
              <li>如遇客流高峰，现场将采取限流措施，请听从工作人员引导。</li>
            </ul>
          </section>

          <section class="detail-block">
            <p class="eyebrow">EXHIBITION INFO</p>
            <h2>展览信息</h2>
            <div class="info-grid">
              <div class="info-item">
                <span>展出地点</span>
                <strong>{{ exhibition.venue }}</strong>
              </div>
              <div class="info-item">
                <span>展期</span>
                <strong>{{ exhibition.startDate }} 至 {{ exhibition.endDate }}</strong>
              </div>
              <div class="info-item">
                <span>开放时间</span>
                <strong>{{ shortTime(exhibition.openTime) }}—{{ shortTime(exhibition.closeTime) }}（周一闭馆）</strong>
              </div>
              <div class="info-item">
                <span>票价</span>
                <strong>{{ formatMoney(exhibition.ticketPrice) }}</strong>
              </div>
            </div>
          </section>
        </article>

        <aside class="booking-panel">
          <div class="booking-panel__head">
            <span class="booking-panel__eyebrow">RESERVE A VISIT</span>
            <h3>选择参观时段</h3>
            <p>可预约未来 7 天，请按预约时段到馆。</p>
          </div>

          <div class="date-tabs">
            <button
              v-for="item in dateOptions"
              :key="item.date"
              type="button"
              class="date-tab"
              :class="{ 'is-active': selectedDate === item.date, 'is-closed': item.closed }"
              @click="chooseDate(item.date)"
            >
              <small>{{ item.weekday }}</small>
              <strong>{{ item.day }}</strong>
              <span>{{ item.count > 0 ? `${item.count} 个时段` : '闭馆/约满' }}</span>
            </button>
          </div>

          <div class="slot-list">
            <button
              v-for="slot in currentSlots"
              :key="slot.id"
              type="button"
              class="slot-item"
              :class="{ 'is-disabled': slotState(slot).disabled }"
              @click="chooseSlot(slot)"
            >
              <span class="slot-item__time">{{ shortTime(slot.startTime) }} — {{ shortTime(slot.endTime) }}</span>
              <span class="slot-item__state">{{ slotState(slot).text }}</span>
              <el-icon class="slot-item__arrow"><Right /></el-icon>
            </button>
            <div v-if="!currentSlots.length" class="slot-empty">
              <el-icon><Calendar /></el-icon>
              <p>当日暂无开放时段</p>
            </div>
          </div>

          <el-button type="primary" size="large" class="booking-panel__btn" :loading="booking" @click="goBooking">
            进入预约流程
          </el-button>

          <div class="booking-panel__foot">
            <span><el-icon><CircleCheck /></el-icon> 实名预约</span>
            <span><el-icon><CircleCheck /></el-icon> 免费取消</span>
            <span><el-icon><CircleCheck /></el-icon> 扫码入馆</span>
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>

<style scoped>
.detail-page {
  min-height: 70vh;
}

.detail-hero {
  position: relative;
  overflow: hidden;
  min-height: 460px;
  color: #f8f2e5;
  background: #0a2b28;
}

.detail-hero__bg {
  position: absolute;
  inset: 0;
}

.detail-hero__bg::after {
  position: absolute;
  inset: 0;
  content: '';
  background: linear-gradient(90deg, rgba(5, 24, 22, 0.92), rgba(5, 24, 22, 0.42) 65%, rgba(5, 24, 22, 0.55));
}

.detail-hero__bg img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-hero__inner {
  position: relative;
  z-index: 1;
  display: flex;
  max-width: 1200px;
  min-height: 460px;
  flex-direction: column;
  justify-content: center;
  padding: 70px 0;
}

.detail-hero__cat {
  align-self: flex-start;
  padding: 4px 12px;
  border: 1px solid rgba(199, 161, 90, 0.55);
  color: var(--gold);
  font-size: 11px;
  letter-spacing: 0.16em;
}

.detail-hero h1 {
  margin-top: 18px;
  color: #fff9ec;
  font-size: clamp(40px, 5vw, 64px);
  font-weight: 500;
  letter-spacing: 0.12em;
}

.detail-hero__sub {
  margin-top: 12px;
  color: rgba(248, 242, 229, 0.72);
  font-family: var(--font-serif);
  font-size: 18px;
  letter-spacing: 0.08em;
}

.detail-hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 22px;
  margin-top: 26px;
  color: rgba(248, 242, 229, 0.62);
  font-size: 13px;
}

.detail-hero__meta span {
  display: flex;
  align-items: center;
  gap: 7px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 56px;
  padding: 56px 0 80px;
}

.detail-block {
  padding-bottom: 40px;
  border-bottom: 1px solid var(--border);
}

.detail-block + .detail-block {
  padding-top: 38px;
}

.detail-block h2 {
  margin-top: 2px;
  font-size: 28px;
  letter-spacing: 0.08em;
}

.detail-text {
  margin-top: 20px;
}

.detail-text p {
  margin-bottom: 14px;
  color: #4d5754;
  font-size: 14.5px;
  line-height: 2;
  text-align: justify;
}

.tips-box {
  display: flex;
  gap: 12px;
  margin-top: 18px;
  padding: 18px 20px;
  border-left: 3px solid var(--gold);
  color: #5d4a2e;
  background: #f5eddc;
}

.tips-box .el-icon {
  margin-top: 3px;
  color: var(--bronze);
  font-size: 18px;
}

.tips-box p {
  font-size: 13.5px;
  line-height: 1.8;
}

.detail-list {
  margin-top: 18px;
}

.detail-list li {
  position: relative;
  margin-bottom: 8px;
  padding-left: 16px;
  color: var(--muted);
  font-size: 13px;
}

.detail-list li::before {
  position: absolute;
  top: 11px;
  left: 0;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  content: '';
  background: var(--gold);
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1px;
  margin-top: 20px;
  overflow: hidden;
  border: 1px solid var(--border);
  background: var(--border);
}

.info-item {
  padding: 18px 20px;
  background: var(--white);
}

.info-item span {
  display: block;
  color: var(--muted);
  font-size: 11px;
  letter-spacing: 0.12em;
}

.info-item strong {
  display: block;
  margin-top: 4px;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 15px;
  font-weight: 500;
}

.booking-panel {
  align-self: start;
  position: sticky;
  top: 126px;
  overflow: hidden;
  border: 1px solid rgba(19, 44, 42, 0.14);
  border-radius: 12px;
  background: var(--white);
  box-shadow: var(--shadow-md);
}

.booking-panel__head {
  padding: 24px 22px 18px;
  color: #f8f2e5;
  background: linear-gradient(135deg, #0a3532, #0e4b47);
}

.booking-panel__eyebrow {
  color: var(--gold);
  font-size: 10px;
  letter-spacing: 0.2em;
}

.booking-panel__head h3 {
  margin-top: 6px;
  color: #fff9ec;
  font-size: 22px;
  letter-spacing: 0.06em;
}

.booking-panel__head p {
  margin-top: 5px;
  color: rgba(248, 242, 229, 0.55);
  font-size: 11.5px;
}

.date-tabs {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
  padding: 16px 16px 8px;
}

.date-tab {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 1px;
  padding: 8px 4px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: #f6f2e9;
  color: var(--muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.date-tab small {
  font-size: 10px;
}

.date-tab strong {
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 18px;
  font-weight: 500;
}

.date-tab span {
  font-size: 9px;
  white-space: nowrap;
}

.date-tab:hover {
  border-color: rgba(167, 123, 69, 0.35);
}

.date-tab.is-active {
  border-color: var(--teal);
  color: var(--teal);
  background: var(--teal-light);
}

.date-tab.is-active span {
  color: var(--teal);
}

.date-tab.is-closed {
  opacity: 0.5;
}

.slot-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 16px 4px;
}

.slot-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 13px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fffdf8;
  cursor: pointer;
  transition: all 0.2s ease;
}

.slot-item:hover:not(.is-disabled) {
  border-color: var(--teal);
  background: #f2f8f5;
}

.slot-item__time {
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 14px;
}

.slot-item__state {
  margin-left: auto;
  color: var(--teal);
  font-size: 11.5px;
}

.slot-item__arrow {
  color: var(--bronze);
  font-size: 12px;
}

.slot-item.is-disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.slot-item.is-disabled .slot-item__state {
  color: var(--muted);
}

.slot-empty {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 6px;
  padding: 22px;
  color: var(--muted);
  font-size: 12px;
}

.slot-empty .el-icon {
  font-size: 22px;
  opacity: 0.5;
}

.booking-panel__btn {
  width: calc(100% - 32px);
  margin: 16px 16px 10px;
}

.booking-panel__foot {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  padding: 0 16px 18px;
  color: var(--muted);
  font-size: 10px;
}

.booking-panel__foot span {
  display: flex;
  align-items: center;
  gap: 3px;
}

.booking-panel__foot .el-icon {
  color: var(--teal);
}

@media (max-width: 960px) {
  .detail-grid {
    grid-template-columns: 1fr;
    gap: 34px;
  }

  .booking-panel {
    position: static;
    order: -1;
  }
}

@media (max-width: 640px) {
  .detail-hero,
  .detail-hero__inner {
    min-height: 380px;
  }

  .detail-hero h1 {
    font-size: 36px;
  }

  .detail-hero__meta {
    gap: 12px;
    font-size: 11.5px;
  }

  .detail-grid {
    padding: 34px 0 54px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>