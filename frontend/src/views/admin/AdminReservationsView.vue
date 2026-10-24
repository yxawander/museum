<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { adminApi } from '@/api/admin'
import type { Exhibition, Reservation } from '@/types'
import AdminPageHeader from '@/components/common/AdminPageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { shortTime } from '@/utils/format'

const loading = ref(false)
const exporting = ref(false)
const list = ref<Reservation[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const status = ref('')
const visitDate = ref('')
const exhibitionId = ref<number | undefined>()
const exhibitions = ref<Exhibition[]>([])
const drawerVisible = ref(false)
const current = ref<Reservation | null>(null)

const today = dayjs().format('YYYY-MM-DD')

async function loadExhibitions() {
  const data = await adminApi.exhibitions({ page: 1, size: 100 })
  exhibitions.value = data.records
}

async function load() {
  loading.value = true
  try {
    const data = await adminApi.reservations({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      status: status.value || undefined,
      visitDate: visitDate.value || undefined,
      exhibitionId: exhibitionId.value,
    })
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  void load()
}

function resetFilters() {
  keyword.value = ''
  status.value = ''
  visitDate.value = ''
  exhibitionId.value = undefined
  page.value = 1
  void load()
}

function openDetail(row: Reservation) {
  current.value = row
  drawerVisible.value = true
}

async function checkin(row: Reservation) {
  try {
    await ElMessageBox.confirm(`确认为“${row.visitorName}”核销入馆吗？`, '核销确认', {
      type: 'info',
      confirmButtonText: '确认核销',
      cancelButtonText: '取消',
    })
    await adminApi.checkinReservation(row.id)
    ElMessage.success('核销成功')
    await load()
    if (current.value?.id === row.id) {
      current.value = null
      drawerVisible.value = false
    }
  } catch {
    // 取消
  }
}

async function cancel(row: Reservation) {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因（选填）。', '取消预约', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      inputPlaceholder: '例如：观众电话取消',
    })
    await adminApi.cancelReservation(row.id, value || '管理员取消')
    ElMessage.success('预约已取消')
    await load()
    drawerVisible.value = false
  } catch {
    // 取消
  }
}

async function exportCsv() {
  exporting.value = true
  try {
    const blob = await adminApi.exportReservations({
      keyword: keyword.value || undefined,
      status: status.value || undefined,
      visitDate: visitDate.value || undefined,
      exhibitionId: exhibitionId.value,
    })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `预约记录_${dayjs().format('YYYYMMDD')}.csv`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

onMounted(async () => {
  await loadExhibitions()
  await load()
})
</script>

<template>
  <div class="reservations-admin">
    <AdminPageHeader title="预约管理" description="查询全部预约订单，执行核销、取消与数据导出。">
      <el-button :loading="exporting" @click="exportCsv">
        <el-icon><Download /></el-icon> 导出 CSV
      </el-button>
      <el-button type="primary" @click="$router.push('/admin/checkin')">
        <el-icon><CircleCheck /></el-icon> 现场核销
      </el-button>
    </AdminPageHeader>

    <div class="filter-card">
      <el-input v-model="keyword" placeholder="单号 / 姓名 / 手机号 / 核销码" clearable class="filter-keyword" @keyup.enter="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="exhibitionId" placeholder="全部展览" clearable class="filter-select filter-select--wide">
        <el-option v-for="item in exhibitions" :key="item.id" :label="item.title" :value="item.id" />
      </el-select>
      <el-date-picker v-model="visitDate" type="date" value-format="YYYY-MM-DD" placeholder="参观日期" class="filter-date" />
      <el-select v-model="status" placeholder="全部状态" clearable class="filter-select">
        <el-option label="待参观" value="CONFIRMED" />
        <el-option label="已入馆" value="CHECKED_IN" />
        <el-option label="已取消" value="CANCELLED" />
        <el-option label="已过期" value="EXPIRED" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="list" row-key="id">
        <el-table-column prop="orderNo" label="预约单号" min-width="165" show-overflow-tooltip />
        <el-table-column prop="exhibitionTitle" label="展览" min-width="120" show-overflow-tooltip />
        <el-table-column label="参观人" min-width="130">
          <template #default="{ row }">
            <div class="visitor-cell">
              <strong>{{ row.visitorName }}</strong>
              <span>{{ row.visitorPhone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="参观时段" min-width="165">
          <template #default="{ row }">
            {{ row.visitDate }} {{ shortTime(row.startTime) }}—{{ shortTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="ticketCount" label="人数" width="66" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="预约时间" min-width="145">
          <template #default="{ row }">{{ row.createdAt?.slice(0, 16) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="185" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 'CONFIRMED' && row.visitDate === today"
              text
              type="success"
              size="small"
              @click="checkin(row)"
            >
              核销
            </el-button>
            <el-button v-if="row.status === 'CONFIRMED'" text type="danger" size="small" @click="cancel(row)">
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-foot">
        <span>共 {{ total }} 条预约</span>
        <el-pagination
          v-model:current-page="page"
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          @current-change="load"
        />
      </div>
    </div>

    <el-drawer v-model="drawerVisible" title="预约详情" size="440px">
      <template v-if="current">
        <div class="drawer-head">
          <StatusTag :status="current.status" />
          <span class="drawer-head__no">{{ current.orderNo }}</span>
        </div>
        <div class="drawer-ex">
          <img :src="current.coverUrl || '/images/exhibitions/bells.svg'" :alt="current.exhibitionTitle">
          <div>
            <strong>{{ current.exhibitionTitle }}</strong>
            <span>{{ current.venue }}</span>
          </div>
        </div>
        <el-descriptions :column="1" border size="small" class="drawer-desc">
          <el-descriptions-item label="参观日期">{{ current.visitDate }}</el-descriptions-item>
          <el-descriptions-item label="入馆时段">
            {{ shortTime(current.startTime) }}—{{ shortTime(current.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="参观人">{{ current.visitorName }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ current.visitorIdCard }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ current.visitorPhone }}</el-descriptions-item>
          <el-descriptions-item label="预约人数">{{ current.ticketCount }} 人</el-descriptions-item>
          <el-descriptions-item label="核销码">{{ current.ticketCode }}</el-descriptions-item>
          <el-descriptions-item label="下单账号">{{ current.username }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ current.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ current.remark || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="current.cancelReason" label="取消原因">
            {{ current.cancelReason }}
          </el-descriptions-item>
          <el-descriptions-item v-if="current.checkinAt" label="核销时间">
            {{ current.checkinAt }}
          </el-descriptions-item>
        </el-descriptions>
        <div class="drawer-actions">
          <el-button
            v-if="current.status === 'CONFIRMED' && current.visitDate === today"
            type="success"
            @click="checkin(current)"
          >
            核销入馆
          </el-button>
          <el-button v-if="current.status === 'CONFIRMED'" type="danger" plain @click="cancel(current)">
            取消预约
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.filter-card {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.filter-keyword {
  width: 240px;
}

.filter-select {
  width: 140px;
}

.filter-select--wide {
  width: 190px;
}

.filter-date {
  width: 155px;
}

.table-card {
  padding: 6px 16px 16px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.visitor-cell strong {
  display: block;
  color: var(--ink);
  font-size: 12.5px;
  font-weight: 500;
}

.visitor-cell span {
  color: var(--muted);
  font-size: 10.5px;
}

.table-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding-top: 14px;
}

.table-foot > span {
  color: var(--muted);
  font-size: 11px;
}

.drawer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 16px;
}

.drawer-head__no {
  color: var(--muted);
  font-family: monospace;
  font-size: 11px;
}

.drawer-ex {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
  padding: 12px;
  border-radius: 8px;
  background: #faf7ef;
}

.drawer-ex img {
  width: 80px;
  height: 56px;
  border-radius: 5px;
  object-fit: cover;
}

.drawer-ex strong {
  display: block;
  font-family: var(--font-serif);
  font-size: 15px;
}

.drawer-ex span {
  display: block;
  color: var(--muted);
  font-size: 10.5px;
}

.drawer-desc {
  margin-top: 4px;
}

.drawer-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

@media (max-width: 720px) {
  .filter-keyword,
  .filter-select,
  .filter-select--wide,
  .filter-date {
    width: 100%;
  }
}
</style>