<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { feedbackApi } from '@/api/reservation'
import type { Feedback } from '@/types'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { FEEDBACK_TYPES } from '@/utils/constants'
import { formatDate } from '@/utils/format'

const formRef = ref<FormInstance>()
const submitting = ref(false)
const loading = ref(false)
const list = ref<Feedback[]>([])
const total = ref(0)
const page = ref(1)
const size = 5

const form = reactive({
  type: 'SUGGESTION',
  content: '',
  contact: '',
})

const rules: FormRules = {
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  content: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
    { min: 5, max: 1000, message: '反馈内容为 5-1000 个字符', trigger: 'blur' },
  ],
}

async function load() {
  loading.value = true
  try {
    const data = await feedbackApi.my({ page: page.value, size })
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await feedbackApi.create({
      type: form.type,
      content: form.content.trim(),
      contact: form.contact || undefined,
    })
    ElMessage.success('反馈已提交，感谢您的建议')
    form.content = ''
    form.contact = ''
    page.value = 1
    await load()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="feedback-view">
    <div class="content-head">
      <div>
        <span class="content-head__eyebrow">FEEDBACK</span>
        <h2>意见反馈</h2>
        <p>您的建议会帮助我们改进服务，我们会尽快回复。</p>
      </div>
    </div>

    <div class="feedback-grid">
      <section class="panel">
        <div class="panel__head">
          <h3>提交反馈</h3>
          <span>请文明表达，便于我们准确处理</span>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="反馈类型" prop="type">
            <el-radio-group v-model="form.type">
              <el-radio-button v-for="(label, value) in FEEDBACK_TYPES" :key="value" :value="value">
                {{ label }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="反馈内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="6"
              maxlength="1000"
              show-word-limit
              placeholder="请描述您遇到的问题、建议或想表扬的服务……"
            />
          </el-form-item>
          <el-form-item label="联系方式（选填）">
            <el-input v-model="form.contact" maxlength="100" placeholder="手机号或邮箱，便于我们联系您" />
          </el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">提交反馈</el-button>
        </el-form>
      </section>

      <aside class="panel feedback-history">
        <div class="panel__head">
          <h3>我的反馈</h3>
          <span>共 {{ total }} 条</span>
        </div>

        <div v-loading="loading" class="feedback-list">
          <article v-for="item in list" :key="item.id" class="feedback-item">
            <div class="feedback-item__head">
              <span class="feedback-item__type">{{ FEEDBACK_TYPES[item.type] }}</span>
              <StatusTag :status="item.status" kind="feedback" />
            </div>
            <p class="feedback-item__content">{{ item.content }}</p>
            <div v-if="item.reply" class="feedback-item__reply">
              <strong>博物馆回复</strong>
              <p>{{ item.reply }}</p>
            </div>
            <span class="feedback-item__time">{{ formatDate(item.createdAt, true) }}</span>
          </article>

          <EmptyState
            v-if="!loading && list.length === 0"
            title="还没有反馈记录"
            description="有建议或疑问？在左侧告诉我们。"
          />
        </div>

        <div v-if="total > size" class="pager">
          <el-pagination
            v-model:current-page="page"
            small
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="size"
            @current-change="load"
          />
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.content-head {
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

.feedback-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 20px;
}

.panel {
  padding: 24px 26px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--border);
}

.panel__head h3 {
  font-size: 19px;
}

.panel__head span {
  color: var(--muted);
  font-size: 10.5px;
}

.feedback-history {
  align-self: start;
}

.feedback-list {
  min-height: 160px;
}

.feedback-item {
  padding: 14px 0;
  border-bottom: 1px dashed var(--border);
}

.feedback-item:first-child {
  padding-top: 0;
}

.feedback-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.feedback-item__type {
  color: var(--bronze);
  font-size: 10.5px;
  letter-spacing: 0.08em;
}

.feedback-item__content {
  display: -webkit-box;
  overflow: hidden;
  margin-top: 6px;
  color: #3d4744;
  font-size: 12.5px;
  line-height: 1.75;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.feedback-item__reply {
  margin-top: 8px;
  padding: 10px 12px;
  border-left: 2px solid var(--gold);
  background: #faf5e9;
}

.feedback-item__reply strong {
  color: #7a5a2e;
  font-size: 10.5px;
}

.feedback-item__reply p {
  margin-top: 3px;
  color: #6b5f4c;
  font-size: 11.5px;
  line-height: 1.7;
}

.feedback-item__time {
  display: block;
  margin-top: 6px;
  color: #a49b89;
  font-size: 9.5px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

@media (max-width: 960px) {
  .feedback-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .panel {
    padding: 18px 16px;
  }
}
</style>