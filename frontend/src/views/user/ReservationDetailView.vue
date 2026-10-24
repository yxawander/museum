<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import QrcodeVue from 'qrcode.vue'
import { reservationApi } from '@/api/reservation'
import type { Reservation } from '@/types'
import StatusTag from '@/components/common/StatusTag.vue'
import { shortTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const reservation = ref<Reservation | null>(null)

const active = computed(() => {
  if (!reservation.value) return 0
  return reservation.value.status === 'CHECKED_IN' ? 2 : reservation.value.status === 'CONFIRMED' ? 1 : 0
})

const qrValue = computed(() => reservation.value?.qrContent ?? '')

async function load() {
  loading.value = true
  try {
    reservation.value = await reservationApi.detail(route.params.id as string)
  } finally {
    loading.value = false
  }
}

async function cancel() {
  if (!reservation.value) return
  try {
    const { value } = await ElMessageBox.prompt('取消后名额将释放，请填写取消原因（选填）。', '取消预约', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      inputPlaceholder: '例如：行程有变',
    })
    await reservationApi.cancel(reservation.value.id, value || '用户取消')
    ElMessage.success('预约已取消')
    await load()
  } catch {
    // 忽略弹窗关闭
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="detail-view">
    <div class="content-head">
      <div>
        <button type="button" class="back-btn" @click="router.push('/user/reservations')">
          <el-icon><ArrowLeft /></el-icon> 返回我的预约
        </button>
        <h2>预约凭证</h2>
        <p>入馆时请出示核销码或本人身份证件。</p>
      </div>
      <StatusTag v-if="reservation" :status="reservation.status" />
    </div>

    <template v-if="reservation">
      <div class="ticket" :class="{ 'is-inactive': reservation.status === 'CANCELLED' || reservation.status === 'EXPIRED' }">
        <div class="ticket__main">
          <div class="ticket__head">
            <div>
              <span class="ticket__eyebrow">HUBEI MUSEUM · ADMISSION TICKET</span>
              <h3>{{ reservation.exhibitionTitle }}</h3>
              <p>{{ reservation.venue }}</p>
            </div>
            <img src="/logo.svg" alt="标识" class="ticket__logo">
          </div>

          <div class="ticket__grid">
            <div>
              <span>参观日期</span>
              <strong>{{ reservation.visitDate }}</strong>
            </div>
            <div>
              <span>入馆时段</span>
              <strong>{{ shortTime(reservation.startTime) }}—{{ shortTime(reservation.endTime) }}</strong>
            </div>
            <div>
              <span>参观人</span>
              <strong>{{ reservation.visitorName }}</strong>
            </div>
            <div>
              <span>人数</span>
              <strong>{{ reservation.ticketCount }} 人</strong>
            </div>
            <div>
              <span>身份证号</span>
              <strong>{{ reservation.visitorIdCard }}</strong>
            </div>
            <div>
              <span>手机号</span>
              <strong>{{ reservation.visitorPhone }}</strong>
            </div>
          </div>

          <div class="ticket__order">
            <span>预约单号</span>
            <strong>{{ reservation.orderNo }}</strong>
          </div>
          <div v-if="reservation.remark" class="ticket__remark">备注：{{ reservation.remark }}</div>
        </div>

        <div class="ticket__stub">
          <div class="ticket__qr">
            <qrcode-vue :value="qrValue" :size="150" level="M" />
          </div>
          <span class="ticket__code">{{ reservation.ticketCode }}</span>
          <small>扫码核验入馆</small>
          <div class="ticket__stub-line" />
          <span class="ticket__status">{{ reservation.statusText }}</span>
        </div>
      </div>

      <div class="detail-grid">
        <section class="panel">
          <h3>预约进度</h3>
          <el-timeline class="timeline">
            <el-timeline-item :timestamp="reservation.createdAt" type="primary" placement="top">
              <strong>预约成功</strong>
              <p>订单已生成，请按预约时段到馆。</p>
            </el-timeline-item>
            <el-timeline-item
              v-if="reservation.status === 'CHECKED_IN'"
              :timestamp="reservation.checkinAt"
              type="success"
              placement="top"
            >
              <strong>已核验入馆</strong>
              <p>欢迎参观，祝您观展愉快。</p>
            </el-timeline-item>
            <el-timeline-item
              v-if="reservation.status === 'CANCELLED'"
              :timestamp="reservation.cancelledAt"
              type="info"
              placement="top"
            >
              <strong>预约已取消</strong>
              <p>{{ reservation.cancelReason || '用户取消' }}</p>
            </el-timeline-item>
            <el-timeline-item
              v-if="reservation.status === 'EXPIRED'"
              :timestamp="reservation.visitDate"
              type="danger"
              placement="top"
            >
              <strong>预约已过期</strong>
              <p>未在预约时段内入馆，订单自动失效。</p>
            </el-timeline-item>
            <el-timeline-item :timestamp="`${reservation.visitDate} ${shortTime(reservation.startTime)}`" placement="top">
              <strong>计划参观</strong>
              <p>请提前 10 分钟到馆安检。</p>
            </el-timeline-item>
          </el-timeline>
        </section>

        <aside class="side">
          <div class="panel">
            <h3>参观须知</h3>
            <ul class="tips">
              <li>请携带预约时使用的有效身份证件。</li>
              <li>按预约时段到馆，逾期预约自动失效。</li>
              <li>参观开始前可在“我的预约”中取消。</li>
              <li>展厅内请勿饮食、喧哗，拍照请关闭闪光灯。</li>
            </ul>
          </div>
          <div class="actions">
            <el-button v-if="reservation.canCancel" type="danger" plain @click="cancel">取消预约</el-button>
            <el-button @click="router.push(`/exhibitions/${reservation.exhibitionId}`)">查看展览</el-button>
            <el-button type="primary" @click="router.push('/booking')">再约一次</el-button>
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>

<style scoped>
.content-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
  padding: 0;
  border: 0;
  color: var(--muted);
  background: transparent;
  font-size: 11.5px;
  cursor: pointer;
}

.back-btn:hover {
  color: var(--teal);
}

.content-head h2 {
  font-size: 27px;
  letter-spacing: 0.08em;
}

.content-head p {
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
}

.ticket {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 240px;
  overflow: hidden;
  border: 1px solid rgba(199, 161, 90, 0.35);
  border-radius: 12px;
  background: var(--white);
  box-shadow: var(--shadow-md);
}

.ticket::before,
.ticket::after {
  position: absolute;
  top: 50%;
  width: 22px;
  height: 22px;
  border: 1px solid rgba(199, 161, 90, 0.35);
  border-radius: 50%;
  content: '';
  background: var(--paper);
  transform: translateY(-50%);
}

.ticket::before {
  right: 228px;
}

.ticket::after {
  right: -12px;
}

.ticket__main {
  padding: 28px 30px;
}

.ticket__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px dashed rgba(19, 44, 42, 0.14);
}

.ticket__eyebrow {
  color: var(--bronze);
  font-size: 9.5px;
  letter-spacing: 0.16em;
}

.ticket__head h3 {
  margin-top: 5px;
  font-size: 28px;
  letter-spacing: 0.06em;
}

.ticket__head p {
  margin-top: 3px;
  color: var(--muted);
  font-size: 11.5px;
}

.ticket__logo {
  width: 44px;
  height: 44px;
  opacity: 0.75;
}

.ticket__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  padding: 20px 0;
}

.ticket__grid span {
  display: block;
  color: var(--muted);
  font-size: 9.5px;
  letter-spacing: 0.08em;
}

.ticket__grid strong {
  display: block;
  margin-top: 3px;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 15px;
  font-weight: 500;
}

.ticket__order {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 14px;
  border-top: 1px dashed rgba(19, 44, 42, 0.14);
  font-size: 11.5px;
}

.ticket__order span {
  color: var(--muted);
}

.ticket__order strong {
  color: var(--teal);
  font-family: monospace;
  font-size: 12px;
}

.ticket__remark {
  margin-top: 8px;
  color: var(--muted);
  font-size: 11px;
}

.ticket__stub {
  display: flex;
  align-items: center;
  flex-direction: column;
  justify-content: center;
  padding: 24px 18px;
  border-left: 1px dashed rgba(199, 161, 90, 0.4);
  background: #faf6ec;
  text-align: center;
}

.ticket__qr {
  padding: 8px;
  border: 1px solid var(--border);
  background: #fff;
}

.ticket__code {
  margin-top: 10px;
  color: var(--ink);
  font-family: monospace;
  font-size: 10px;
  letter-spacing: 0.06em;
  word-break: break-all;
}

.ticket__stub small {
  margin-top: 2px;
  color: var(--muted);
  font-size: 9px;
}

.ticket__stub-line {
  width: 70%;
  height: 1px;
  margin: 14px 0;
  background: rgba(19, 44, 42, 0.12);
}

.ticket__status {
  color: var(--teal);
  font-family: var(--font-serif);
  font-size: 18px;
  letter-spacing: 0.1em;
}

.ticket.is-inactive {
  opacity: 0.72;
  filter: grayscale(0.4);
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 20px;
  margin-top: 22px;
}

.panel {
  padding: 22px 24px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.panel h3 {
  margin-bottom: 16px;
  font-size: 18px;
}

.timeline {
  padding-left: 4px;
}

.timeline strong {
  color: var(--ink);
  font-size: 13px;
}

.timeline p {
  margin-top: 3px;
  color: var(--muted);
  font-size: 11.5px;
}

.side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tips li {
  position: relative;
  margin-bottom: 8px;
  padding-left: 14px;
  color: var(--muted);
  font-size: 11.5px;
  line-height: 1.7;
}

.tips li::before {
  position: absolute;
  top: 9px;
  left: 0;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  content: '';
  background: var(--gold);
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.actions .el-button {
  width: 100%;
  margin-left: 0;
}

@media (max-width: 900px) {
  .ticket {
    grid-template-columns: 1fr;
  }

  .ticket::before,
  .ticket::after {
    display: none;
  }

  .ticket__stub {
    border-top: 1px dashed rgba(199, 161, 90, 0.4);
    border-left: 0;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .ticket__main {
    padding: 20px 18px;
  }

  .ticket__grid {
    grid-template-columns: 1fr 1fr;
  }

  .ticket__head h3 {
    font-size: 22px;
  }
}
</style>