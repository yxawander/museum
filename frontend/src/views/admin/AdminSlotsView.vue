<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import dayjs from 'dayjs'
import { adminApi } from '@/api/admin'
import type { BatchSlotForm, Exhibition, Slot, SlotForm } from '@/types'
import AdminPageHeader from '@/components/common/AdminPageHeader.vue'
import { shortTime } from '@/utils/format'

const loading = ref(false)
const list = ref<Slot[]>([])
const total = ref(0)
const page = ref(1)
const size = 12
const exhibitions = ref<Exhibition[]>([])
const filterExhibition = ref<number | undefined>()
const filterDate = ref('')
const filterStatus = ref('')

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const batchVisible = ref(false)
const batchSaving = ref(false)
const batchFormRef = ref<FormInstance>()
const batchRange = ref<[string, string] | null>(null)

const form = reactive<SlotForm>({
  exhibitionId: 0,
  visitDate: '',
  startTime: '09:00:00',
  endTime: '11:00:00',
  capacity: 200,
  status: 'OPEN',
  remark: '',
})

const batchForm = reactive({
  exhibitionId: 0,
  startTimes: ['09:00', '11:00', '13:00'],
  capacity: 200,
  skipMonday: true,
})

const rules: FormRules = {
  exhibitionId: [{ required: true, message: '请选择展览', trigger: 'change' }],
  visitDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
}

function disablePast(date: Date) {
  return dayjs(date).isBefore(dayjs().startOf('day'))
}

const timeOptions = [
  { label: '上午 09:00—11:00', value: '09:00' },
  { label: '中午 11:00—13:00', value: '11:00' },
  { label: '下午 13:00—16:00', value: '13:00' },
]

async function loadExhibitions() {
  const data = await adminApi.exhibitions({ page: 1, size: 100 })
  exhibitions.value = data.records
}

async function load() {
  loading.value = true
  try {
    const data = await adminApi.slots({
      page: page.value,
      size,
      exhibitionId: filterExhibition.value,
      date: filterDate.value || undefined,
      status: filterStatus.value || undefined,
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
  filterExhibition.value = undefined
  filterDate.value = ''
  filterStatus.value = ''
  page.value = 1
  void load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    exhibitionId: exhibitions.value[0]?.id ?? 0,
    visitDate: dayjs().add(1, 'day').format('YYYY-MM-DD'),
    startTime: '09:00:00',
    endTime: '11:00:00',
    capacity: 200,
    status: 'OPEN',
    remark: '',
  })
  dialogVisible.value = true
}

function openEdit(row: Slot) {
  editingId.value = row.id
  Object.assign(form, {
    exhibitionId: row.exhibitionId,
    visitDate: row.visitDate,
    startTime: row.startTime,
    endTime: row.endTime,
    capacity: row.capacity,
    status: row.status,
    remark: row.remark ?? '',
  })
  dialogVisible.value = true
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await adminApi.updateSlot(editingId.value, { ...form })
      ElMessage.success('时段已更新')
    } else {
      await adminApi.createSlot({ ...form })
      ElMessage.success('时段已创建')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

function openBatch() {
  Object.assign(batchForm, {
    exhibitionId: exhibitions.value[0]?.id ?? 0,
    startTimes: ['09:00', '11:00', '13:00'],
    capacity: 200,
    skipMonday: true,
  })
  batchRange.value = [
    dayjs().add(1, 'day').format('YYYY-MM-DD'),
    dayjs().add(7, 'day').format('YYYY-MM-DD'),
  ]
  batchVisible.value = true
}

async function generateBatch() {
  if (!batchForm.exhibitionId) {
    ElMessage.warning('请选择展览')
    return
  }
  if (!batchRange.value || batchRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  if (!batchForm.startTimes.length) {
    ElMessage.warning('请至少选择一个时段')
    return
  }
  batchSaving.value = true
  try {
    const payload: BatchSlotForm = {
      exhibitionId: batchForm.exhibitionId,
      startDate: batchRange.value[0],
      endDate: batchRange.value[1],
      startTimes: batchForm.startTimes,
      capacity: batchForm.capacity,
      skipMonday: batchForm.skipMonday,
    }
    const result = await adminApi.batchSlots(payload)
    ElMessage.success(`成功生成 ${result.created} 个时段`)
    batchVisible.value = false
    await load()
  } finally {
    batchSaving.value = false
  }
}

async function toggleStatus(row: Slot) {
  const next = row.status === 'OPEN' ? 'CLOSED' : 'OPEN'
  await adminApi.slotStatus(row.id, next)
  ElMessage.success(next === 'OPEN' ? '时段已开放' : '时段已关闭')
  await load()
}

async function remove(row: Slot) {
  try {
    await ElMessageBox.confirm(
      `确定删除 ${row.visitDate} ${shortTime(row.startTime)} 的时段吗？已有预约的时段无法删除。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
    await adminApi.deleteSlot(row.id)
    ElMessage.success('时段已删除')
    await load()
  } catch {
    // 取消
  }
}

onMounted(async () => {
  await loadExhibitions()
  await load()
})
</script>

<template>
  <div class="slots-admin">
    <AdminPageHeader title="时段管理" description="按展览维护开放日期、入馆时段与库存容量。">
      <el-button @click="openBatch">
        <el-icon><Calendar /></el-icon> 批量生成
      </el-button>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> 新增时段
      </el-button>
    </AdminPageHeader>

    <div class="filter-card">
      <el-select v-model="filterExhibition" placeholder="全部展览" clearable class="filter-select filter-select--wide">
        <el-option v-for="item in exhibitions" :key="item.id" :label="item.title" :value="item.id" />
      </el-select>
      <el-date-picker
        v-model="filterDate"
        type="date"
        value-format="YYYY-MM-DD"
        placeholder="参观日期"
        class="filter-date"
      />
      <el-select v-model="filterStatus" placeholder="全部状态" clearable class="filter-select">
        <el-option label="开放" value="OPEN" />
        <el-option label="关闭" value="CLOSED" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="list" row-key="id">
        <el-table-column prop="exhibitionTitle" label="展览" min-width="150" show-overflow-tooltip />
        <el-table-column prop="visitDate" label="参观日期" width="115" />
        <el-table-column label="时段" width="130">
          <template #default="{ row }">
            {{ shortTime(row.startTime) }} — {{ shortTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column prop="bookedCount" label="已预约" width="80" />
        <el-table-column label="剩余" width="80">
          <template #default="{ row }">
            <span :class="{ 'is-low': row.remaining <= 20 }">{{ row.remaining }}</span>
          </template>
        </el-table-column>
        <el-table-column label="使用率" width="130">
          <template #default="{ row }">
            <el-progress
              :percentage="row.capacity ? Math.min(100, Math.round((row.bookedCount / row.capacity) * 100)) : 0"
              :stroke-width="6"
              :show-text="false"
              :color="row.bookedCount / row.capacity > 0.8 ? '#a94442' : '#0e4b47'"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 'OPEN'"
              inline-prompt
              active-text="开放"
              inactive-text="关闭"
              @change="toggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger" size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-foot">
        <span>共 {{ total }} 个时段</span>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑时段' : '新增时段'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="所属展览" prop="exhibitionId">
          <el-select v-model="form.exhibitionId" placeholder="选择展览">
            <el-option v-for="item in exhibitions" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="参观日期" prop="visitDate">
          <el-date-picker v-model="form.visitDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="form.startTime" value-format="HH:mm:ss" placeholder="09:00" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker v-model="form.endTime" value-format="HH:mm:ss" placeholder="11:00" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="5000" :step="50" />
          <span class="hint">含已预约人数，不能小于 {{ editingId ? '当前预约数' : 1 }}</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="OPEN">开放</el-radio>
            <el-radio value="CLOSED">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" maxlength="255" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchVisible" title="批量生成参观时段" width="620px">
      <el-form ref="batchFormRef" :model="batchForm" label-width="100px">
        <el-form-item label="所属展览">
          <el-select v-model="batchForm.exhibitionId" placeholder="选择展览">
            <el-option v-for="item in exhibitions" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="batchRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :disabled-date="disablePast"
          />
        </el-form-item>
        <el-form-item label="时段">
          <el-checkbox-group v-model="batchForm.startTimes">
            <el-checkbox v-for="item in timeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="每场容量">
          <el-input-number v-model="batchForm.capacity" :min="1" :max="5000" :step="50" />
        </el-form-item>
        <el-form-item label="跳过周一">
          <el-switch v-model="batchForm.skipMonday" />
          <span class="hint">周一为闭馆日，建议跳过</span>
        </el-form-item>
        <el-alert
          title="已存在的相同时段会自动跳过，不会重复生成。单次最多生成 31 天。"
          type="info"
          :closable="false"
          show-icon
        />
      </el-form>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSaving" @click="generateBatch">生成时段</el-button>
      </template>
    </el-dialog>
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

.filter-select {
  width: 150px;
}

.filter-select--wide {
  width: 210px;
}

.filter-date {
  width: 160px;
}

.table-card {
  padding: 6px 16px 16px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.is-low {
  color: #a94442;
  font-weight: 600;
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

.hint {
  margin-left: 10px;
  color: var(--muted);
  font-size: 10.5px;
}

@media (max-width: 720px) {
  .filter-select,
  .filter-select--wide,
  .filter-date {
    width: 100%;
  }
}
</style>