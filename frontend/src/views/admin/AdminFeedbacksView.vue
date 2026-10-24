<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import type { Feedback } from '@/types'
import AdminPageHeader from '@/components/common/AdminPageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { FEEDBACK_TYPES } from '@/utils/constants'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const list = ref<Feedback[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const status = ref('')
const type = ref('')
const keyword = ref('')

const dialogVisible = ref(false)
const saving = ref(false)
const current = ref<Feedback | null>(null)
const reply = ref('')

async function load() {
  loading.value = true
  try {
    const data = await adminApi.feedbacks({
      page: page.value,
      size,
      status: status.value || undefined,
      type: type.value || undefined,
      keyword: keyword.value || undefined,
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
  status.value = ''
  type.value = ''
  keyword.value = ''
  page.value = 1
  void load()
}

function openReply(row: Feedback) {
  current.value = row
  reply.value = row.reply ?? ''
  dialogVisible.value = true
}

async function submitReply() {
  if (!current.value) return
  if (!reply.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  saving.value = true
  try {
    await adminApi.replyFeedback(current.value.id, reply.value.trim())
    ElMessage.success('回复已发送')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="feedbacks-admin">
    <AdminPageHeader title="意见反馈" description="查看观众意见建议，及时回复并跟踪处理状态。">
      <el-button @click="load">刷新</el-button>
    </AdminPageHeader>

    <div class="filter-card">
      <el-input v-model="keyword" placeholder="反馈内容关键词" clearable class="filter-keyword" @keyup.enter="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="type" placeholder="全部类型" clearable class="filter-select">
        <el-option v-for="(label, value) in FEEDBACK_TYPES" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="status" placeholder="全部状态" clearable class="filter-select">
        <el-option label="待处理" value="PENDING" />
        <el-option label="已回复" value="REPLIED" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="list" row-key="id">
        <el-table-column label="提交人" width="130">
          <template #default="{ row }">
            <div class="user-cell">
              <strong>{{ row.realName || row.username }}</strong>
              <span>{{ row.contact || '未留联系方式' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <span class="type-text">{{ FEEDBACK_TYPES[row.type] }}</span>
          </template>
        </el-table-column>
        <el-table-column label="反馈内容" min-width="320">
          <template #default="{ row }">
            <p class="content-text">{{ row.content }}</p>
            <p v-if="row.reply" class="reply-text">回复：{{ row.reply }}</p>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><StatusTag :status="row.status" kind="feedback" /></template>
        </el-table-column>
        <el-table-column label="提交时间" min-width="140">
          <template #default="{ row }">{{ formatDate(row.createdAt, true) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openReply(row)">
              {{ row.status === 'REPLIED' ? '修改回复' : '回复' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-foot">
        <span>共 {{ total }} 条反馈</span>
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

    <el-dialog v-model="dialogVisible" title="回复反馈" width="560px">
      <div v-if="current" class="feedback-quote">
        <div class="feedback-quote__head">
          <span>{{ current.realName || current.username }}</span>
          <span>{{ formatDate(current.createdAt, true) }}</span>
        </div>
        <p>{{ current.content }}</p>
      </div>
      <el-input
        v-model="reply"
        type="textarea"
        :rows="6"
        maxlength="1000"
        show-word-limit
        placeholder="请输入回复内容，回复后将展示给用户。"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReply">发送回复</el-button>
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

.user-cell strong {
  display: block;
  color: var(--ink);
  font-size: 12.5px;
  font-weight: 500;
}

.user-cell span {
  display: block;
  color: var(--muted);
  font-size: 10px;
}

.type-text {
  color: var(--bronze);
  font-size: 11.5px;
}

.content-text {
  color: #3d4744;
  font-size: 12px;
  line-height: 1.7;
}

.reply-text {
  margin-top: 4px;
  padding-left: 8px;
  border-left: 2px solid rgba(199, 161, 90, 0.5);
  color: #7a6a50;
  font-size: 11px;
  line-height: 1.6;
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

.feedback-quote {
  margin-bottom: 16px;
  padding: 14px 16px;
  border-left: 3px solid var(--gold);
  background: #faf5e9;
}

.feedback-quote__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: var(--muted);
  font-size: 10.5px;
}

.feedback-quote p {
  margin-top: 6px;
  color: #4d4436;
  font-size: 12.5px;
  line-height: 1.8;
}

@media (max-width: 720px) {
  .filter-keyword,
  .filter-select {
    width: 100%;
  }
}
</style>