<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { adminApi } from '@/api/admin'
import type { Exhibition, ExhibitionForm } from '@/types'
import AdminPageHeader from '@/components/common/AdminPageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { COVER_PRESETS, EXHIBITION_CATEGORIES } from '@/utils/constants'
import { formatMoney } from '@/utils/format'

const loading = ref(false)
const list = ref<Exhibition[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const category = ref('')
const status = ref('')

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const emptyForm = (): ExhibitionForm => ({
  title: '',
  subtitle: '',
  category: '常设展览',
  coverUrl: '/images/exhibitions/bells.svg',
  description: '',
  venue: '',
  startDate: '',
  endDate: '',
  openTime: '09:00:00',
  closeTime: '17:00:00',
  ticketPrice: 0,
  tips: '',
  status: 'DRAFT',
  sortOrder: 0,
})

const form = reactive<ExhibitionForm>(emptyForm())

const rules: FormRules = {
  title: [{ required: true, message: '请输入展览名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  venue: [{ required: true, message: '请输入展出地点', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  openTime: [{ required: true, message: '请选择开放时间', trigger: 'change' }],
  closeTime: [{ required: true, message: '请选择闭馆时间', trigger: 'change' }],
  ticketPrice: [{ required: true, message: '请输入票价', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    const data = await adminApi.exhibitions({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      category: category.value || undefined,
      status: status.value || undefined,
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
  category.value = ''
  status.value = ''
  page.value = 1
  void load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row: Exhibition) {
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    subtitle: row.subtitle ?? '',
    category: row.category,
    coverUrl: row.coverUrl ?? '',
    description: row.description ?? '',
    venue: row.venue,
    startDate: row.startDate,
    endDate: row.endDate,
    openTime: row.openTime,
    closeTime: row.closeTime,
    ticketPrice: row.ticketPrice,
    tips: row.tips ?? '',
    status: row.status,
    sortOrder: row.sortOrder,
  })
  dialogVisible.value = true
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await adminApi.updateExhibition(editingId.value, { ...form })
      ElMessage.success('展览已更新')
    } else {
      await adminApi.createExhibition({ ...form })
      ElMessage.success('展览已创建')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

function handleMore(row: Exhibition, command: string) {
  if (command === 'preview') {
    void window.open(`/exhibitions/${row.id}`, '_blank')
  } else if (command === 'delete') {
    void remove(row)
  }
}

async function changeStatus(row: Exhibition, next: string) {
  await adminApi.exhibitionStatus(row.id, next)
  ElMessage.success('状态已更新')
  await load()
}

async function remove(row: Exhibition) {
  try {
    await ElMessageBox.confirm(`确定删除展览“${row.title}”吗？已产生预约的展览无法删除。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await adminApi.deleteExhibition(row.id)
    ElMessage.success('展览已删除')
    await load()
  } catch {
    // 取消
  }
}

onMounted(load)
</script>

<template>
  <div class="exhibitions-admin">
    <AdminPageHeader title="展览管理" description="维护展览信息、展期、票价与上架状态。">
      <el-button @click="load">刷新</el-button>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> 新建展览
      </el-button>
    </AdminPageHeader>

    <div class="filter-card">
      <el-input v-model="keyword" placeholder="展览名称 / 地点" clearable class="filter-input" @keyup.enter="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="category" placeholder="全部分类" clearable class="filter-select">
        <el-option v-for="item in EXHIBITION_CATEGORIES" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="status" placeholder="全部状态" clearable class="filter-select">
        <el-option label="已上架" value="PUBLISHED" />
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已下架" value="CLOSED" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="list" size="default" row-key="id">
        <el-table-column label="展览" min-width="200">
          <template #default="{ row }">
            <div class="ex-cell">
              <img :src="row.coverUrl || '/images/exhibitions/bells.svg'" :alt="row.title">
              <div>
                <strong>{{ row.title }}</strong>
                <span>{{ row.subtitle || '—' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="90" />
        <el-table-column prop="venue" label="地点" min-width="120" show-overflow-tooltip />
        <el-table-column label="展期" min-width="175">
          <template #default="{ row }">
            <span class="date-range">{{ row.startDate }} 至 {{ row.endDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="票价" width="76">
          <template #default="{ row }">{{ formatMoney(row.ticketPrice) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="82">
          <template #default="{ row }"><StatusTag :status="row.status" kind="exhibition" /></template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="60" />
        <el-table-column label="操作" width="196" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-dropdown trigger="click" @command="(cmd: string) => changeStatus(row, cmd)">
              <el-button text type="primary" size="small">
                状态 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="PUBLISHED">上架</el-dropdown-item>
                  <el-dropdown-item command="CLOSED">下架</el-dropdown-item>
                  <el-dropdown-item command="DRAFT">设为草稿</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-dropdown trigger="click" @command="(cmd: string) => handleMore(row, cmd)">
              <el-button text size="small">
                更多 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="preview">预览</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-foot">
        <span>共 {{ total }} 个展览</span>
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

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑展览' : '新建展览'"
      width="760px"
      top="6vh"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <div class="form-grid">
          <el-form-item label="展览名称" prop="title" class="span-2">
            <el-input v-model="form.title" maxlength="100" placeholder="如：曾侯乙编钟" />
          </el-form-item>
          <el-form-item label="副标题" class="span-2">
            <el-input v-model="form.subtitle" maxlength="200" placeholder="一句话简介" />
          </el-form-item>
          <el-form-item label="分类" prop="category">
            <el-select v-model="form.category">
              <el-option v-for="item in EXHIBITION_CATEGORIES" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="展出地点" prop="venue">
            <el-input v-model="form.venue" placeholder="如：综合馆一楼" />
          </el-form-item>
          <el-form-item label="开始日期" prop="startDate">
            <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
          </el-form-item>
          <el-form-item label="结束日期" prop="endDate">
            <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
          </el-form-item>
          <el-form-item label="开放时间" prop="openTime">
            <el-time-picker v-model="form.openTime" value-format="HH:mm:ss" placeholder="09:00" />
          </el-form-item>
          <el-form-item label="闭馆时间" prop="closeTime">
            <el-time-picker v-model="form.closeTime" value-format="HH:mm:ss" placeholder="17:00" />
          </el-form-item>
          <el-form-item label="票价（元）" prop="ticketPrice">
            <el-input-number v-model="form.ticketPrice" :min="0" :precision="2" :step="10" />
          </el-form-item>
          <el-form-item label="首页排序">
            <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
          </el-form-item>
          <el-form-item label="封面图" class="span-2">
            <el-select v-model="form.coverUrl" placeholder="选择封面">
              <el-option v-for="item in COVER_PRESETS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <img v-if="form.coverUrl" :src="form.coverUrl" class="cover-preview" alt="封面预览">
          </el-form-item>
          <el-form-item label="展览简介" class="span-2">
            <el-input v-model="form.description" type="textarea" :rows="6" maxlength="5000" show-word-limit />
          </el-form-item>
          <el-form-item label="参观提示" class="span-2">
            <el-input v-model="form.tips" type="textarea" :rows="2" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio value="DRAFT">草稿</el-radio>
              <el-radio value="PUBLISHED">上架</el-radio>
              <el-radio value="CLOSED">下架</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
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

.filter-input {
  width: 220px;
}

.filter-select {
  width: 150px;
}

.table-card {
  padding: 6px 16px 16px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.ex-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ex-cell img {
  width: 58px;
  height: 42px;
  flex-shrink: 0;
  border-radius: 4px;
  object-fit: cover;
}

.ex-cell strong {
  display: block;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 13.5px;
  font-weight: 600;
}

.ex-cell span {
  display: block;
  overflow: hidden;
  max-width: 190px;
  color: var(--muted);
  font-size: 10.5px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.date-range {
  color: #5b6662;
  font-size: 11.5px;
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

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 18px;
}

.span-2 {
  grid-column: span 2;
}

.cover-preview {
  width: 90px;
  height: 58px;
  margin-left: 10px;
  border-radius: 4px;
  object-fit: cover;
}

@media (max-width: 720px) {
  .filter-input,
  .filter-select {
    width: 100%;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: span 1;
  }
}
</style>