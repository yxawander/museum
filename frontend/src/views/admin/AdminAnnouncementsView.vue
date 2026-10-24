<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { adminApi } from '@/api/admin'
import type { Announcement, AnnouncementForm } from '@/types'
import AdminPageHeader from '@/components/common/AdminPageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { ANNOUNCEMENT_CATEGORIES } from '@/utils/constants'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const list = ref<Announcement[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const category = ref('')
const status = ref<number | undefined>()

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const emptyForm = (): AnnouncementForm => ({
  title: '',
  summary: '',
  content: '',
  category: 'NOTICE',
  coverUrl: '',
  isTop: 0,
  status: 0,
})

const form = reactive<AnnouncementForm>(emptyForm())

const rules: FormRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告正文', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

async function load() {
  loading.value = true
  try {
    const data = await adminApi.announcements({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      category: category.value || undefined,
      status: status.value,
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
  status.value = undefined
  page.value = 1
  void load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row: Announcement) {
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    summary: row.summary ?? '',
    content: row.content ?? '',
    category: row.category,
    coverUrl: row.coverUrl ?? '',
    isTop: row.isTop,
    status: row.status,
  })
  dialogVisible.value = true
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await adminApi.updateAnnouncement(editingId.value, { ...form })
      ElMessage.success('公告已更新')
    } else {
      await adminApi.createAnnouncement({ ...form })
      ElMessage.success('公告已创建')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function togglePublish(row: Announcement) {
  const nextStatus = row.status === 1 ? 0 : 1
  await adminApi.updateAnnouncement(row.id, {
    title: row.title,
    summary: row.summary,
    content: row.content ?? '',
    category: row.category,
    coverUrl: row.coverUrl,
    isTop: row.isTop,
    status: nextStatus,
  })
  ElMessage.success(nextStatus === 1 ? '公告已发布' : '公告已转为草稿')
  await load()
}

async function remove(row: Announcement) {
  try {
    await ElMessageBox.confirm(`确定删除公告“${row.title}”吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await adminApi.deleteAnnouncement(row.id)
    ElMessage.success('公告已删除')
    await load()
  } catch {
    // 取消
  }
}

onMounted(load)
</script>

<template>
  <div class="announcements-admin">
    <AdminPageHeader title="公告管理" description="发布开放通知、馆内新闻与社教活动，支持置顶与草稿。">
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> 新建公告
      </el-button>
    </AdminPageHeader>

    <div class="filter-card">
      <el-input v-model="keyword" placeholder="公告标题" clearable class="filter-keyword" @keyup.enter="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="category" placeholder="全部分类" clearable class="filter-select">
        <el-option v-for="(label, value) in ANNOUNCEMENT_CATEGORIES" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="status" placeholder="全部状态" clearable class="filter-select">
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="list" row-key="id">
        <el-table-column label="标题" min-width="280">
          <template #default="{ row }">
            <div class="title-cell">
              <span v-if="row.isTop === 1" class="top-tag">置顶</span>
              <strong>{{ row.title }}</strong>
              <span>{{ row.summary || '—' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100">
          <template #default="{ row }">{{ ANNOUNCEMENT_CATEGORIES[row.category] }}</template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }"><StatusTag :status="row.status" kind="announcement" /></template>
        </el-table-column>
        <el-table-column label="发布时间" min-width="145">
          <template #default="{ row }">{{ row.publishTime ? formatDate(row.publishTime, true) : '—' }}</template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读量" width="85" />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button text type="primary" size="small" @click="togglePublish(row)">
              {{ row.status === 1 ? '转草稿' : '发布' }}
            </el-button>
            <el-button text type="danger" size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-foot">
        <span>共 {{ total }} 条公告</span>
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
      :title="editingId ? '编辑公告' : '新建公告'"
      width="760px"
      top="6vh"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-radio-group v-model="form.category">
            <el-radio v-for="(label, value) in ANNOUNCEMENT_CATEGORIES" :key="value" :value="value">
              {{ label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" maxlength="255" show-word-limit placeholder="列表页展示的简短摘要" />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="12" maxlength="10000" show-word-limit />
        </el-form-item>
        <el-form-item label="发布设置">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" active-text="置顶" />
          <el-radio-group v-model="form.status" class="status-radio">
            <el-radio :value="0">保存草稿</el-radio>
            <el-radio :value="1">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
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

.filter-keyword {
  width: 230px;
}

.filter-select {
  width: 140px;
}

.table-card {
  padding: 6px 16px 16px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 7px;
}

.title-cell strong {
  overflow: hidden;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 13.5px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.title-cell > span:last-child {
  display: none;
}

.top-tag {
  flex-shrink: 0;
  padding: 0 5px;
  border: 1px solid rgba(169, 68, 66, 0.4);
  color: #a94442;
  font-size: 9px;
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

.status-radio {
  margin-left: 24px;
}

@media (max-width: 720px) {
  .filter-keyword,
  .filter-select {
    width: 100%;
  }

  .status-radio {
    margin-left: 0;
  }
}
</style>