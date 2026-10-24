<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import QrcodeVue from 'qrcode.vue'
import { reservationApi } from '@/api/reservation'
import type { Reservation } from '@/types'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { shortTime } from '@/utils/format'

const loading = ref(false)
const list = ref<Reservation[]>([])
const total = ref(0)
const page = ref(1)
const size = 6
const tab = ref('ALL')
const qrVisible = ref(false)
const current = ref<Reservation | null>(null)

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '待参观', value: 'CONFIRMED' },
  { label: '已入馆', value: 'CHECKED_IN' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已过期', value: 'EXPIRED' },
]

async function load() {
  loading.value = true
  try {
    const data = await reservationApi.list({
      page: page.value,
      size,
      status: tab.value === 'ALL' ? undefined : tab.value,
    })
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function switchTab(value: string) {
  tab.value = value
  page.value = 1
  void load()
}

function showQr(item: Reservation) {
  current.value = item
  qrVisible.value = true
}

async function cancel(item: Reservation) {
  try {
    const { value } = await ElMessageBox.prompt('取消后名额将释放，请填写取消原因（选填）。', '取消预约', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      inputPlaceholder: '例如：行程有变',
      inputValidator: () => true,
    })
    await reservationApi.cancel(item.id, value || '用户取消')
    ElMessage.success('预约已取消')
    await load()
  } catch {
    // 用户主动关闭弹窗时不处理
  }
}

onMounted(load)
</script>

<template>
  <div class="reservations-view">
    <div class="content-head">
      <div>
        <span class="content-head__eyebrow">MY RESERVATIONS</span>
        <h2>我的预约</h2>
        <p>查看预约状态与入馆核销码，参观开始前可在线取消。</p>
      </div>
      <el-button type="primary" @click="$router.push('/booking')">新建预约</el-button>
    </div>

    <div class="tabs">
      <button
        v-for="item in tabs"
        :key="item.value"
        type="button"
        class="tab"
        :class="{ 'is-active': tab === item.value }"
        @click="switchTab(item.value)"
      >
        {{ item.label }}
      </button>
    </div>

    <div v-loading="loading" class="reservation-list">
      <article v-for="item in list" :key="item.id" class="reservation-card">
        <div class="reservation-card__cover">
          <img :src="item.coverUrl || '/images/exhibitions/bells.svg'" :alt="item.exhibitionTitle">
          <span class="reservation-card__status">
            <StatusTag :status="item.status" />
          </span>
        </div>
        <div class="reservation-card__body">
          <div class="reservation-card__top">
            <div>
              <h3>{{ item.exhibitionTitle }}</h3>
              <p>{{ item.venue }}</p>
            </div>
            <span class="order-no">{{ item.orderNo }}</span>
          </div>
          <div class="reservation-card__info">
            <div>
              <span>参观日期</span>
              <strong>{{ item.visitDate }}</strong>
            </div>
            <div>
              <span>入馆时段</span>
              <strong>{{ shortTime(item.startTime) }}—{{ shortTime(item.endTime) }}</strong>
            </div>
            <div>
              <span>参观人</span>
              <strong>{{ item.visitorName }} · {{ item.ticketCount }} 人</strong>
            </div>
            <div>
              <span>预约时间</span>
              <strong>{{ item.createdAt?.slice(0, 16) }}</strong>
            </div>
          </div>
          <div v-if="item.status === 'CANCELLED'" class="reservation-card__note">
            取消原因：{{ item.cancelReason || '用户取消' }}
          </div>
          <div v-if="item.status === 'CHECKED_IN'" class="reservation-card__note is-success">
            已于 {{ item.checkinAt?.slice(0, 16) }} 核验入馆，祝您参观愉快。
          </div>

          <div class="reservation-card__actions">
            <el-button text type="primary" @click="$router.push(`/user/reservations/${item.id}`)">
              查看凭证
            </el-button>
            <el-button
              v-if="item.status === 'CONFIRMED' || item.status === 'CHECKED_IN'"
              text
              type="primary"
              @click="showQr(item)"
            >
              出示核销码
            </el-button>
            <el-button v-if="item.canCancel" text type="danger" @click="cancel(item)">取消预约</el-button>
            <el-button text @click="$router.push(`/exhibitions/${item.exhibitionId}`)">查看展览</el-button>
          </div>
        </div>
      </article>

      <EmptyState
        v-if="!loading && list.length === 0"
        title="还没有预约记录"
        description="选一个感兴趣的展览，开启您的博物馆之旅。"
      >
        <el-button type="primary" class="empty-btn" @click="$router.push('/exhibitions')">
          去逛展览
        </el-button>
      </EmptyState>
    </div>

    <div v-if="total > size" class="pager">
      <el-pagination
        v-model:current-page="page"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        @current-change="load"
      />
    </div>

    <el-dialog v-model="qrVisible" title="入馆核销码" width="360px" align-center>
      <div v-if="current" class="qr-dialog">
        <p class="qr-dialog__ex">{{ current.exhibitionTitle }}</p>
        <div class="qr-dialog__code">
          <qrcode-vue :value="current.qrContent" :size="190" level="M" />
        </div>
        <p class="qr-dialog__no">{{ current.orderNo }}</p>
        <p class="qr-dialog__time">
          {{ current.visitDate }} {{ shortTime(current.startTime) }}—{{ shortTime(current.endTime) }}
        </p>
        <el-alert
          title="请向工作人员出示此码，或直接出示身份证件核验入馆"
          type="info"
          :closable="false"
          show-icon
        />
      </div>
    </el-dialog>
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

.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}

.tab {
  padding: 6px 16px;
  border: 1px solid transparent;
  border-radius: 2px;
  color: var(--muted);
  background: transparent;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab:hover {
  color: var(--teal);
}

.tab.is-active {
  border-color: rgba(167, 123, 69, 0.4);
  color: var(--teal);
  background: var(--white);
}

.reservation-list {
  display: flex;
  min-height: 280px;
  flex-direction: column;
  gap: 14px;
}

.reservation-card {
  display: flex;
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
  transition: box-shadow 0.25s ease;
}

.reservation-card:hover {
  box-shadow: var(--shadow-sm);
}

.reservation-card__cover {
  position: relative;
  width: 170px;
  flex-shrink: 0;
  background: #0b2b28;
}

.reservation-card__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.reservation-card__status {
  position: absolute;
  top: 10px;
  left: 10px;
}

.reservation-card__body {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  padding: 18px 22px;
}

.reservation-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.reservation-card__top h3 {
  font-size: 20px;
  letter-spacing: 0.05em;
}

.reservation-card__top p {
  margin-top: 2px;
  color: var(--muted);
  font-size: 11px;
}

.order-no {
  color: #99917f;
  font-family: monospace;
  font-size: 10.5px;
  white-space: nowrap;
}

.reservation-card__info {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-top: 14px;
  padding: 12px 0;
  border-top: 1px dashed var(--border);
  border-bottom: 1px dashed var(--border);
}

.reservation-card__info span {
  display: block;
  color: var(--muted);
  font-size: 9.5px;
  letter-spacing: 0.06em;
}

.reservation-card__info strong {
  display: block;
  margin-top: 2px;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 13px;
  font-weight: 500;
}

.reservation-card__note {
  margin-top: 10px;
  padding: 7px 10px;
  color: #8a6b42;
  background: #f8f1e2;
  font-size: 11px;
}

.reservation-card__note.is-success {
  color: #2f6d52;
  background: #edf7f1;
}

.reservation-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  margin-top: auto;
  padding-top: 10px;
}

.empty-btn {
  margin-top: 18px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.qr-dialog {
  text-align: center;
}

.qr-dialog__ex {
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 18px;
}

.qr-dialog__code {
  display: inline-block;
  margin: 16px 0 10px;
  padding: 12px;
  border: 1px solid var(--border);
  background: #fff;
}

.qr-dialog__no {
  color: var(--muted);
  font-family: monospace;
  font-size: 11.5px;
}

.qr-dialog__time {
  margin: 4px 0 16px;
  color: var(--teal);
  font-size: 12.5px;
}

@media (max-width: 860px) {
  .reservation-card {
    flex-direction: column;
  }

  .reservation-card__cover {
    width: 100%;
    height: 150px;
  }

  .reservation-card__info {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .content-head {
    flex-direction: column;
  }

  .reservation-card__body {
    padding: 14px 16px;
  }

  .reservation-card__top {
    flex-direction: column;
    gap: 4px;
  }
}
</style>