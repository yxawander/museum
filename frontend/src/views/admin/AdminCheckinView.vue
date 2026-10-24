<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { adminApi } from '@/api/admin'
import type { Reservation } from '@/types'
import AdminPageHeader from '@/components/common/AdminPageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { shortTime } from '@/utils/format'

const code = ref('')
const searching = ref(false)
const checking = ref(false)
const preview = ref<Reservation | null>(null)
const result = ref<Reservation | null>(null)
const todayList = ref<Reservation[]>([])
const pendingCount = ref(0)
const checkedCount = ref(0)
const loadingList = ref(false)
const today = dayjs().format('YYYY-MM-DD')

const canCheckin = computed(() => {
  const item = preview.value
  return Boolean(item && item.status === 'CONFIRMED' && item.visitDate === today)
})

async function search() {
  const value = code.value.trim()
  if (!value) {
    ElMessage.warning('请输入预约单号或核销码')
    return
  }
  searching.value = true
  result.value = null
  preview.value = null
  try {
    preview.value = await adminApi.checkinSearch(value)
  } finally {
    searching.value = false
  }
}

async function doCheckin() {
  const value = code.value.trim()
  if (!value) return
  checking.value = true
  try {
    result.value = await adminApi.checkin(value)
    preview.value = null
    code.value = ''
    ElMessage.success('核销成功，请提醒观众有序入馆')
    await loadToday()
  } finally {
    checking.value = false
  }
}

async function checkinRow(row: Reservation) {
  code.value = row.orderNo
  await doCheckin()
}

async function loadToday() {
  loadingList.value = true
  try {
    const [pending, checked, list] = await Promise.all([
      adminApi.reservations({ page: 1, size: 1, visitDate: today, status: 'CONFIRMED' }),
      adminApi.reservations({ page: 1, size: 1, visitDate: today, status: 'CHECKED_IN' }),
      adminApi.reservations({ page: 1, size: 6, visitDate: today, status: 'CONFIRMED' }),
    ])
    pendingCount.value = pending.total
    checkedCount.value = checked.total
    todayList.value = list.records
  } finally {
    loadingList.value = false
  }
}

function clearPreview() {
  preview.value = null
  result.value = null
  code.value = ''
}

onMounted(loadToday)
</script>

<template>
  <div class="checkin-view">
    <AdminPageHeader title="现场核销" description="输入预约单号或核销码，核验观众预约信息并完成入馆。">
      <el-tag type="success" effect="plain">今日 {{ today }}</el-tag>
    </AdminPageHeader>

    <div class="checkin-stats">
      <div class="checkin-stat">
        <span>今日待核销</span>
        <strong>{{ pendingCount }}</strong>
        <small>单</small>
      </div>
      <div class="checkin-stat">
        <span>今日已入馆</span>
        <strong>{{ checkedCount }}</strong>
        <small>单</small>
      </div>
      <div class="checkin-stat">
        <span>核销方式</span>
        <strong class="text">扫码 / 单号</strong>
        <small>支持身份证核验</small>
      </div>
    </div>

    <div class="checkin-grid">
      <section class="scan-panel">
        <div class="scan-panel__head">
          <span class="scan-panel__icon"><el-icon><FullScreen /></el-icon></span>
          <div>
            <h3>核销码查询</h3>
            <p>支持预约单号（HB 开头）或 22 位核销码</p>
          </div>
        </div>

        <div class="scan-input">
          <el-input
            v-model="code"
            size="large"
            placeholder="扫描二维码或输入预约单号 / 核销码"
            clearable
            @keyup.enter="search"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" size="large" :loading="searching" @click="search">查询</el-button>
        </div>

        <transition name="page-fade">
          <div v-if="preview" class="preview-card">
            <div class="preview-card__head">
              <StatusTag :status="preview.status" />
              <span>{{ preview.orderNo }}</span>
            </div>
            <h4>{{ preview.exhibitionTitle }}</h4>
            <div class="preview-grid">
              <div><span>参观人</span><strong>{{ preview.visitorName }}</strong></div>
              <div><span>人数</span><strong>{{ preview.ticketCount }} 人</strong></div>
              <div><span>参观日期</span><strong>{{ preview.visitDate }}</strong></div>
              <div><span>入馆时段</span><strong>{{ shortTime(preview.startTime) }}—{{ shortTime(preview.endTime) }}</strong></div>
              <div><span>身份证号</span><strong>{{ preview.visitorIdCard }}</strong></div>
              <div><span>手机号</span><strong>{{ preview.visitorPhone }}</strong></div>
            </div>
            <div v-if="canCheckin" class="preview-actions">
              <el-button type="primary" size="large" :loading="checking" @click="doCheckin">确认核销入馆</el-button>
              <el-button size="large" @click="clearPreview">取消</el-button>
            </div>
            <el-alert
              v-else
              :title="preview.status !== 'CONFIRMED'
                ? '该预约当前状态不可核销'
                : '该预约不是今日参观，无法核销'"
              type="warning"
              :closable="false"
              show-icon
            />
          </div>
        </transition>

        <transition name="page-fade">
          <div v-if="result" class="success-card">
            <div class="success-card__icon"><el-icon><Check /></el-icon></div>
            <h3>核销成功</h3>
            <p>{{ result.visitorName }} · {{ result.exhibitionTitle }}</p>
            <span>核销时间 {{ result.checkinAt }}</span>
            <el-button type="primary" plain @click="clearPreview">继续核销下一位</el-button>
          </div>
        </transition>

        <div v-if="!preview && !result" class="scan-hint">
          <el-icon><Aim /></el-icon>
          <p>将观众出示的二维码对准扫码设备，或在左侧输入预约单号。</p>
          <span>核销成功后，预约状态将变更为“已入馆”。</span>
        </div>
      </section>

      <aside class="today-panel">
        <div class="panel__head">
          <div>
            <h3>今日待核销</h3>
            <p>按预约时间排序，点击可直接核销</p>
          </div>
          <el-button text type="primary" size="small" @click="loadToday">刷新</el-button>
        </div>
        <div v-loading="loadingList" class="today-list">
          <article v-for="item in todayList" :key="item.id" class="today-item">
            <div>
              <strong>{{ item.visitorName }}</strong>
              <span>{{ item.exhibitionTitle }}</span>
            </div>
            <div class="today-item__time">
              {{ shortTime(item.startTime) }}
              <small>{{ item.ticketCount }} 人</small>
            </div>
            <el-button size="small" type="primary" plain @click="checkinRow(item)">核销</el-button>
          </article>
          <EmptyState
            v-if="!loadingList && todayList.length === 0"
            title="今日暂无待核销预约"
            description="所有预约均已核销或今日没有新的预约。"
          />
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.checkin-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}

.checkin-stat {
  padding: 18px 20px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.checkin-stat span {
  display: block;
  color: var(--muted);
  font-size: 11px;
}

.checkin-stat strong {
  display: inline-block;
  margin-top: 2px;
  color: var(--teal);
  font-family: var(--font-serif);
  font-size: 32px;
  font-weight: 500;
}

.checkin-stat strong.text {
  font-size: 21px;
}

.checkin-stat small {
  margin-left: 4px;
  color: var(--muted);
  font-size: 10px;
}

.checkin-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(300px, 0.7fr);
  gap: 18px;
}

.scan-panel,
.today-panel {
  padding: 24px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.scan-panel__head {
  display: flex;
  align-items: center;
  gap: 13px;
  margin-bottom: 20px;
}

.scan-panel__icon {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  border: 1px solid rgba(167, 123, 69, 0.35);
  border-radius: 10px;
  color: var(--bronze);
  font-size: 22px;
}

.scan-panel__head h3 {
  font-size: 19px;
}

.scan-panel__head p {
  margin-top: 2px;
  color: var(--muted);
  font-size: 10.5px;
}

.scan-input {
  display: flex;
  gap: 10px;
}

.scan-input .el-input {
  flex: 1;
}

.preview-card {
  margin-top: 22px;
  padding: 20px;
  border: 1px solid rgba(14, 75, 71, 0.3);
  border-radius: 10px;
  background: #f6faf8;
}

.preview-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.preview-card__head span {
  color: var(--muted);
  font-family: monospace;
  font-size: 10.5px;
}

.preview-card h4 {
  margin-top: 10px;
  font-size: 22px;
  letter-spacing: 0.05em;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px dashed rgba(19, 44, 42, 0.12);
}

.preview-grid span {
  display: block;
  color: var(--muted);
  font-size: 9.5px;
}

.preview-grid strong {
  display: block;
  margin-top: 2px;
  color: var(--ink);
  font-size: 12.5px;
  font-weight: 500;
}

.preview-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.success-card {
  margin-top: 22px;
  padding: 30px 20px;
  border: 1px solid rgba(47, 125, 92, 0.3);
  border-radius: 10px;
  background: #eef8f2;
  text-align: center;
}

.success-card__icon {
  display: grid;
  width: 58px;
  height: 58px;
  margin: 0 auto 10px;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: #2f7d5c;
  font-size: 28px;
}

.success-card h3 {
  color: #245f46;
  font-size: 24px;
}

.success-card p {
  margin-top: 5px;
  color: #3f6b55;
  font-size: 13px;
}

.success-card > span {
  display: block;
  margin: 4px 0 16px;
  color: #6b8a78;
  font-size: 11px;
}

.scan-hint {
  display: flex;
  align-items: center;
  flex-direction: column;
  padding: 50px 20px;
  color: var(--muted);
  text-align: center;
}

.scan-hint .el-icon {
  color: #b7ad99;
  font-size: 44px;
}

.scan-hint p {
  margin-top: 12px;
  font-size: 12.5px;
}

.scan-hint span {
  margin-top: 4px;
  font-size: 10.5px;
  opacity: 0.7;
}

.panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 10px;
}

.panel__head h3 {
  font-size: 17px;
}

.panel__head p {
  margin-top: 2px;
  color: var(--muted);
  font-size: 10px;
}

.today-list {
  min-height: 200px;
}

.today-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px dashed var(--border);
}

.today-item > div:first-child {
  min-width: 0;
  flex: 1;
}

.today-item strong {
  display: block;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 14px;
}

.today-item span {
  display: block;
  overflow: hidden;
  color: var(--muted);
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.today-item__time {
  color: var(--teal);
  font-family: var(--font-serif);
  font-size: 13px;
  text-align: right;
}

.today-item__time small {
  display: block;
  color: var(--muted);
  font-family: var(--font-sans);
  font-size: 9px;
}

@media (max-width: 1000px) {
  .checkin-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .checkin-stats {
    grid-template-columns: 1fr;
  }

  .scan-input {
    flex-direction: column;
  }

  .preview-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>