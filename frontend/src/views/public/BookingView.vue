<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { publicApi } from '@/api/public'
import { reservationApi } from '@/api/reservation'
import type { Exhibition, Reservation, Slot } from '@/types'
import { formatMoney, isSlotBookable, shortTime, weekdayText } from '@/utils/format'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(true)
const submitting = ref(false)
const step = ref(0)
const exhibitions = ref<Exhibition[]>([])
const exhibition = ref<Exhibition | null>(null)
const selectedExhibitionId = ref<number>()
const slots = ref<Slot[]>([])
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const selectedSlot = ref<Slot | null>(null)
const agreed = ref(false)
const created = ref<Reservation | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  visitorName: '',
  visitorIdCard: '',
  visitorPhone: '',
  ticketCount: 1,
  remark: '',
})

const rules: FormRules = {
  visitorName: [
    { required: true, message: '请输入参观人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为 2-20 个字符', trigger: 'blur' },
  ],
  visitorIdCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '请输入正确的 18 位身份证号', trigger: 'blur' },
  ],
  visitorPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' },
  ],
}

const dateOptions = computed(() => {
  const result: { date: string; day: string; weekday: string; count: number; closed: boolean }[] = []
  for (let i = 0; i < 14; i += 1) {
    const date = dayjs().add(i, 'day').format('YYYY-MM-DD')
    const daySlots = slots.value.filter((slot) => slot.visitDate === date)
    const count = daySlots.filter(
      (slot) => slot.status === 'OPEN' && slot.remaining > 0 && isSlotBookable(date, slot.startTime),
    ).length
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

const totalPrice = computed(() => {
  if (!exhibition.value) return 0
  return exhibition.value.ticketPrice * form.ticketCount
})

const canContinue = computed(() => Boolean(selectedSlot.value && !slotDisabled(selectedSlot.value)))

function slotDisabled(slot: Slot): boolean {
  return slot.status !== 'OPEN' || slot.remaining <= 0 || !isSlotBookable(slot.visitDate, slot.startTime)
}

function slotState(slot: Slot): string {
  if (slot.status !== 'OPEN') return '已关闭'
  if (slot.remaining <= 0) return '已约满'
  if (!isSlotBookable(slot.visitDate, slot.startTime)) return '已停止预约'
  return `余 ${slot.remaining} 人`
}

function pickDate(date: string) {
  selectedDate.value = date
  if (selectedSlot.value && selectedSlot.value.visitDate !== date) {
    selectedSlot.value = null
  }
}

function pickSlot(slot: Slot) {
  if (slotDisabled(slot)) return
  selectedSlot.value = slot
}

async function loadSlots(exhibitionId: number) {
  const startDate = dayjs().format('YYYY-MM-DD')
  const endDate = dayjs().add(13, 'day').format('YYYY-MM-DD')
  slots.value = await publicApi.slots(exhibitionId, { startDate, endDate })
  const first = dateOptions.value.find((item) => item.count > 0)
  if (first) selectedDate.value = first.date
}

async function changeExhibition(id: number) {
  const target = exhibitions.value.find((item) => item.id === id)
  if (!target) return
  exhibition.value = target
  selectedExhibitionId.value = id
  selectedSlot.value = null
  await loadSlots(id)
}

function prefill() {
  const user = auth.user
  if (!user) return
  form.visitorName = user.realName ?? ''
  form.visitorPhone = user.phone ?? ''
  if (user.idCard && !user.idCard.includes('*')) {
    form.visitorIdCard = user.idCard
  }
}

function nextStep() {
  if (!canContinue.value) {
    ElMessage.warning('请先选择参观时段')
    return
  }
  prefill()
  step.value = 1
}

async function submit() {
  if (!selectedSlot.value || !exhibition.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!agreed.value) {
    ElMessage.warning('请先阅读并同意预约须知')
    return
  }
  submitting.value = true
  try {
    created.value = await reservationApi.create({
      slotId: selectedSlot.value.id,
      visitorName: form.visitorName,
      visitorIdCard: form.visitorIdCard,
      visitorPhone: form.visitorPhone,
      ticketCount: form.ticketCount,
      remark: form.remark || undefined,
    })
    step.value = 2
  } finally {
    submitting.value = false
  }
}

function restart() {
  step.value = 0
  created.value = null
  selectedSlot.value = null
  agreed.value = false
  form.remark = ''
}

onMounted(async () => {
  try {
    const list = await publicApi.exhibitions({ page: 1, size: 50 })
    exhibitions.value = list.records
    const queryId = Number(route.query.exhibitionId)
    const target = list.records.find((item) => item.id === queryId) ?? list.records[0]
    if (!target) {
      ElMessage.error('暂无可预约展览')
      return
    }
    exhibition.value = target
    selectedExhibitionId.value = target.id
    await loadSlots(target.id)
    const slotId = Number(route.query.slotId)
    if (slotId) {
      const slot = slots.value.find((item) => item.id === slotId)
      if (slot) {
        selectedDate.value = slot.visitDate
        selectedSlot.value = slot
      }
    }
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="booking-page">
    <section class="booking-head">
      <div class="container">
        <div class="breadcrumb">
          <router-link to="/">首页</router-link>
          <span>/</span>
          <span>预约参观</span>
        </div>
        <p class="eyebrow booking-head__eyebrow">RESERVE A VISIT</p>
        <h1>预约参观</h1>
        <p class="booking-head__desc">选择展览与时段，填写实名信息，即可获得入馆核销码。</p>
      </div>
    </section>

    <div class="container booking-wrap">
      <div class="step-bar">
        <el-steps :active="step" align-center finish-status="success">
          <el-step title="选择时段" description="展览 / 日期 / 时段" />
          <el-step title="填写信息" description="参观人实名信息" />
          <el-step title="预约成功" description="获取核销码" />
        </el-steps>
      </div>

      <div class="booking-grid">
        <div class="booking-main">
          <template v-if="step === 0">
            <section class="panel">
              <div class="panel__head">
                <div class="panel__title">
                  <span class="panel__no">01</span>
                  <h2>选择展览与参观时段</h2>
                </div>
                <el-select
                  v-model="selectedExhibitionId"
                  class="ex-select"
                  placeholder="切换展览"
                  @change="changeExhibition"
                >
                  <el-option
                    v-for="item in exhibitions"
                    :key="item.id"
                    :label="item.title"
                    :value="item.id"
                  />
                </el-select>
              </div>

              <div v-if="exhibition" class="ex-summary">
                <img :src="exhibition.coverUrl || '/images/exhibitions/bells.svg'" :alt="exhibition.title">
                <div>
                  <span class="ex-summary__cat">{{ exhibition.category }}</span>
                  <h3>{{ exhibition.title }}</h3>
                  <p>{{ exhibition.venue }} · {{ exhibition.startDate }} 至 {{ exhibition.endDate }}</p>
                </div>
                <span class="ex-summary__price">{{ formatMoney(exhibition.ticketPrice) }}</span>
              </div>

              <h3 class="sub-title">选择日期</h3>
              <div class="date-scroll">
                <button
                  v-for="item in dateOptions"
                  :key="item.date"
                  type="button"
                  class="date-card"
                  :class="{ 'is-active': selectedDate === item.date, 'is-closed': item.closed }"
                  @click="pickDate(item.date)"
                >
                  <small>{{ item.weekday }}</small>
                  <strong>{{ item.day }}</strong>
                  <span>{{ item.count > 0 ? `${item.count} 个可约` : '暂无余票' }}</span>
                </button>
              </div>

              <h3 class="sub-title">选择入馆时段</h3>
              <div v-if="currentSlots.length" class="slot-grid">
                <button
                  v-for="slot in currentSlots"
                  :key="slot.id"
                  type="button"
                  class="slot-card"
                  :class="{ 'is-active': selectedSlot?.id === slot.id, 'is-disabled': slotDisabled(slot) }"
                  @click="pickSlot(slot)"
                >
                  <span class="slot-card__time">{{ shortTime(slot.startTime) }} — {{ shortTime(slot.endTime) }}</span>
                  <span class="slot-card__state">{{ slotState(slot) }}</span>
                  <span class="slot-card__bar">
                    <i :style="{ width: `${Math.min(100, (slot.bookedCount / slot.capacity) * 100)}%` }" />
                  </span>
                  <small>容量 {{ slot.capacity }} 人</small>
                </button>
              </div>
              <div v-else class="slot-empty">
                <el-icon><Calendar /></el-icon>
                <p>当日暂无参观时段，请选择其他日期。</p>
              </div>

              <div class="panel__actions">
                <el-button type="primary" size="large" :disabled="!canContinue" @click="nextStep">
                  下一步：填写信息
                  <el-icon class="el-icon--right"><Right /></el-icon>
                </el-button>
              </div>
            </section>
          </template>

          <template v-else-if="step === 1">
            <section class="panel">
              <div class="panel__head">
                <div class="panel__title">
                  <span class="panel__no">02</span>
                  <h2>填写参观人信息</h2>
                </div>
                <el-button text @click="step = 0">
                  <el-icon><ArrowLeft /></el-icon> 返回修改时段
                </el-button>
              </div>

              <div class="selected-tip">
                <el-icon><Calendar /></el-icon>
                <span>
                  {{ exhibition?.title }} · {{ selectedSlot?.visitDate }} {{ shortTime(selectedSlot?.startTime) }}—{{ shortTime(selectedSlot?.endTime) }}
                </span>
              </div>

              <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="booking-form">
                <el-form-item label="参观人姓名" prop="visitorName">
                  <el-input v-model="form.visitorName" placeholder="请输入与身份证一致的姓名" maxlength="20" />
                </el-form-item>
                <el-form-item label="身份证号" prop="visitorIdCard">
                  <el-input v-model="form.visitorIdCard" placeholder="请输入 18 位身份证号" maxlength="18" />
                </el-form-item>
                <el-form-item label="手机号" prop="visitorPhone">
                  <el-input v-model="form.visitorPhone" placeholder="用于接收预约通知" maxlength="11" />
                </el-form-item>
                <el-form-item label="预约人数">
                  <el-input-number v-model="form.ticketCount" :min="1" :max="5" />
                  <span class="form-hint">单次最多预约 5 人，含本人</span>
                </el-form-item>
                <el-form-item label="备注">
                  <el-input
                    v-model="form.remark"
                    type="textarea"
                    :rows="3"
                    maxlength="200"
                    show-word-limit
                    placeholder="如有无障碍、团队等特殊需求，可在此说明（选填）"
                  />
                </el-form-item>
              </el-form>

              <div class="notice-box">
                <h4><el-icon><WarningFilled /></el-icon> 预约须知</h4>
                <p>1. 预约实行实名制，请填写真实有效的参观人信息，入馆时需核验身份证件。</p>
                <p>2. 请按预约时段到馆，逾期未入馆视为放弃，预约自动失效。</p>
                <p>3. 参观开始前可在线取消预约，取消后名额将释放给其他观众。</p>
                <p>4. 如遇馆内重大活动或临时闭馆，将通过公告或短信通知，敬请留意。</p>
              </div>

              <el-checkbox v-model="agreed" class="agree-check">
                我已阅读并同意《预约须知》，承诺所填信息真实有效
              </el-checkbox>

              <div class="panel__actions">
                <el-button size="large" @click="step = 0">上一步</el-button>
                <el-button type="primary" size="large" :loading="submitting" @click="submit">
                  确认预约
                </el-button>
              </div>
            </section>
          </template>

          <template v-else>
            <section class="panel success-panel">
              <div class="success-icon">
                <el-icon><Check /></el-icon>
              </div>
              <p class="eyebrow">RESERVATION CONFIRMED</p>
              <h2>预约成功</h2>
              <p class="success-desc">请按预约时段携带有效身份证件到馆，出示核销码入馆。</p>

              <div class="success-card">
                <div class="success-card__row">
                  <span>预约单号</span>
                  <strong>{{ created?.orderNo }}</strong>
                </div>
                <div class="success-card__row">
                  <span>展览</span>
                  <strong>{{ created?.exhibitionTitle }}</strong>
                </div>
                <div class="success-card__row">
                  <span>参观时间</span>
                  <strong>{{ created?.visitDate }} {{ shortTime(created?.startTime) }}—{{ shortTime(created?.endTime) }}</strong>
                </div>
                <div class="success-card__row">
                  <span>参观人数</span>
                  <strong>{{ created?.ticketCount }} 人</strong>
                </div>
                <div class="success-card__row">
                  <span>核销码</span>
                  <strong class="code">{{ created?.ticketCode }}</strong>
                </div>
              </div>

              <div class="success-actions">
                <el-button type="primary" size="large" @click="router.push(`/user/reservations/${created?.id}`)">
                  查看预约凭证
                </el-button>
                <el-button size="large" @click="restart">继续预约</el-button>
              </div>
            </section>
          </template>
        </div>

        <aside class="booking-side">
          <div class="side-card">
            <span class="side-card__eyebrow">ORDER SUMMARY</span>
            <h3>预约摘要</h3>
            <div class="side-card__row">
              <span>展览</span>
              <strong>{{ exhibition?.title || '—' }}</strong>
            </div>
            <div class="side-card__row">
              <span>参观日期</span>
              <strong>{{ selectedSlot?.visitDate || selectedDate }}</strong>
            </div>
            <div class="side-card__row">
              <span>入馆时段</span>
              <strong>
                {{ selectedSlot ? `${shortTime(selectedSlot.startTime)}—${shortTime(selectedSlot.endTime)}` : '未选择' }}
              </strong>
            </div>
            <div class="side-card__row">
              <span>人数</span>
              <strong>{{ form.ticketCount }} 人</strong>
            </div>
            <div class="side-card__price">
              <span>合计</span>
              <strong>{{ totalPrice > 0 ? `¥${totalPrice.toFixed(2)}` : '免费' }}</strong>
            </div>
          </div>

          <div class="side-tips">
            <h4>温馨提示</h4>
            <p>开放时间 09:00—17:00，16:00 停止入馆，周一闭馆。</p>
            <p>请提前 10 分钟到馆安检，旺季建议预留更多时间。</p>
            <p>特展票价仅为现场购票参考，预约本身不收取费用。</p>
            <p>咨询电话：027-86794127</p>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<style scoped>
.booking-page {
  min-height: 70vh;
  padding-bottom: 80px;
}

.booking-head {
  padding: 56px 0 42px;
  color: #f8f2e5;
  background:
    radial-gradient(circle at 78% 20%, rgba(199, 161, 90, 0.24), transparent 42%),
    linear-gradient(120deg, #0a3532, #0e4b47);
}

.booking-head .breadcrumb {
  margin-bottom: 20px;
}

.booking-head__eyebrow {
  color: var(--gold);
}

.booking-head h1 {
  color: #fff9ec;
  font-size: clamp(30px, 4vw, 44px);
  letter-spacing: 0.12em;
}

.booking-head__desc {
  margin-top: 8px;
  color: rgba(248, 242, 229, 0.58);
  font-size: 13.5px;
}

.booking-wrap {
  margin-top: -26px;
}

.step-bar {
  padding: 22px 30px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--white);
  box-shadow: var(--shadow-sm);
}

.step-bar :deep(.el-step__title) {
  font-size: 13px;
}

.step-bar :deep(.el-step__description) {
  font-size: 11px;
}

.booking-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 26px;
  margin-top: 24px;
}

.panel {
  padding: 28px 30px 30px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--white);
  box-shadow: var(--shadow-sm);
}

.panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.panel__title {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.panel__no {
  color: var(--gold);
  font-family: var(--font-serif);
  font-size: 20px;
}

.panel__head h2 {
  font-size: 23px;
  letter-spacing: 0.06em;
}

.ex-select {
  width: 180px;
}

.ex-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: #faf7ef;
}

.ex-summary img {
  width: 96px;
  height: 70px;
  border-radius: 6px;
  object-fit: cover;
}

.ex-summary > div {
  min-width: 0;
  flex: 1;
}

.ex-summary__cat {
  color: var(--bronze);
  font-size: 10.5px;
  letter-spacing: 0.1em;
}

.ex-summary h3 {
  margin-top: 2px;
  font-size: 18px;
}

.ex-summary p {
  overflow: hidden;
  margin-top: 2px;
  color: var(--muted);
  font-size: 11.5px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ex-summary__price {
  color: var(--teal);
  font-family: var(--font-serif);
  font-size: 18px;
  white-space: nowrap;
}

.sub-title {
  margin: 26px 0 12px;
  font-size: 16px;
  letter-spacing: 0.06em;
}

.date-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 6px;
}

.date-card {
  display: flex;
  min-width: 76px;
  align-items: center;
  flex-direction: column;
  gap: 1px;
  padding: 10px 6px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fffdf8;
  color: var(--muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.date-card small {
  font-size: 10px;
}

.date-card strong {
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 21px;
  font-weight: 500;
}

.date-card span {
  font-size: 9px;
  white-space: nowrap;
}

.date-card:hover {
  border-color: rgba(167, 123, 69, 0.4);
}

.date-card.is-active {
  border-color: var(--teal);
  color: var(--teal);
  background: var(--teal-light);
}

.date-card.is-active strong {
  color: var(--teal);
}

.date-card.is-closed {
  opacity: 0.45;
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.slot-card {
  display: flex;
  align-items: flex-start;
  flex-direction: column;
  gap: 2px;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 9px;
  background: #fffdf8;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.slot-card:hover:not(.is-disabled) {
  border-color: var(--teal);
  background: #f3f9f6;
}

.slot-card.is-active {
  border-color: var(--teal);
  background: var(--teal-light);
  box-shadow: inset 0 0 0 1px var(--teal);
}

.slot-card.is-disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.slot-card__time {
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 15px;
}

.slot-card__state {
  color: var(--teal);
  font-size: 11px;
}

.slot-card__bar {
  width: 100%;
  height: 3px;
  margin: 8px 0 3px;
  overflow: hidden;
  border-radius: 2px;
  background: #e8e2d4;
}

.slot-card__bar i {
  display: block;
  height: 100%;
  background: var(--gold);
}

.slot-card small {
  color: var(--muted);
  font-size: 10px;
}

.slot-empty {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 6px;
  padding: 34px;
  border: 1px dashed var(--border);
  border-radius: 10px;
  color: var(--muted);
  font-size: 12px;
}

.slot-empty .el-icon {
  font-size: 24px;
  opacity: 0.5;
}

.panel__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid var(--border);
}

.selected-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 22px;
  padding: 12px 16px;
  border-left: 3px solid var(--gold);
  color: #5d4a2e;
  background: #f5eddc;
  font-size: 13px;
}

.booking-form {
  max-width: 620px;
}

.form-hint {
  margin-left: 10px;
  color: var(--muted);
  font-size: 11.5px;
}

.notice-box {
  margin-top: 8px;
  padding: 18px 20px;
  border: 1px solid rgba(167, 123, 69, 0.22);
  border-radius: 8px;
  background: #faf5e9;
}

.notice-box h4 {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #7a5a2e;
  font-family: var(--font-sans);
  font-size: 13px;
}

.notice-box p {
  margin-top: 6px;
  color: #756a56;
  font-size: 12px;
  line-height: 1.75;
}

.agree-check {
  margin-top: 18px;
}

.success-panel {
  padding: 50px 30px;
  text-align: center;
}

.success-icon {
  display: grid;
  width: 74px;
  height: 74px;
  margin: 0 auto 18px;
  place-items: center;
  border: 1px solid rgba(47, 125, 92, 0.35);
  border-radius: 50%;
  color: #2f7d5c;
  background: #eef7f1;
  font-size: 34px;
}

.success-panel h2 {
  margin-top: 4px;
  font-size: 34px;
  letter-spacing: 0.12em;
}

.success-desc {
  margin-top: 8px;
  color: var(--muted);
  font-size: 13px;
}

.success-card {
  max-width: 520px;
  margin: 28px auto 0;
  padding: 8px 22px;
  border: 1px dashed rgba(167, 123, 69, 0.4);
  border-radius: 10px;
  background: #faf7ef;
  text-align: left;
}

.success-card__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(19, 44, 42, 0.08);
  font-size: 13px;
}

.success-card__row:last-child {
  border-bottom: 0;
}

.success-card__row span {
  color: var(--muted);
}

.success-card__row strong {
  color: var(--ink);
  font-family: var(--font-serif);
  font-weight: 500;
  text-align: right;
}

.success-card__row .code {
  color: var(--teal);
  font-family: monospace;
  letter-spacing: 0.08em;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 28px;
}

.booking-side {
  align-self: start;
  position: sticky;
  top: 126px;
}

.side-card {
  padding: 22px;
  border: 1px solid rgba(19, 44, 42, 0.14);
  border-radius: 12px;
  background: var(--white);
  box-shadow: var(--shadow-sm);
}

.side-card__eyebrow {
  color: var(--bronze);
  font-size: 9.5px;
  letter-spacing: 0.18em;
}

.side-card h3 {
  margin: 4px 0 14px;
  font-size: 20px;
}

.side-card__row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 9px 0;
  border-top: 1px dashed rgba(19, 44, 42, 0.1);
  font-size: 12px;
}

.side-card__row span {
  color: var(--muted);
  flex-shrink: 0;
}

.side-card__row strong {
  color: var(--ink);
  font-weight: 500;
  text-align: right;
}

.side-card__price {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 14px;
  border-top: 1px solid var(--border);
}

.side-card__price span {
  color: var(--muted);
  font-size: 12px;
}

.side-card__price strong {
  color: var(--bronze);
  font-family: var(--font-serif);
  font-size: 26px;
  font-weight: 500;
}

.side-tips {
  margin-top: 16px;
  padding: 20px 22px;
  border-radius: 12px;
  color: rgba(248, 242, 229, 0.62);
  background: #0d3d39;
}

.side-tips h4 {
  margin-bottom: 8px;
  color: var(--gold);
  font-family: var(--font-sans);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.side-tips p {
  margin-bottom: 6px;
  font-size: 11.5px;
  line-height: 1.7;
}

@media (max-width: 960px) {
  .booking-grid {
    grid-template-columns: 1fr;
  }

  .booking-side {
    position: static;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }

  .side-tips {
    margin-top: 0;
  }

  .slot-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .panel {
    padding: 20px 16px;
  }

  .panel__head {
    flex-direction: column;
  }

  .ex-select {
    width: 100%;
  }

  .ex-summary__price {
    display: none;
  }

  .slot-grid {
    grid-template-columns: 1fr;
  }

  .booking-side {
    grid-template-columns: 1fr;
  }

  .step-bar {
    padding: 16px 10px;
  }

  .step-bar :deep(.el-step__description) {
    display: none;
  }

  .success-panel {
    padding: 36px 16px;
  }

  .success-actions {
    flex-direction: column;
  }
}
</style>