<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { adminApi } from '@/api/admin'
import type { User } from '@/types'
import AdminPageHeader from '@/components/common/AdminPageHeader.vue'
import { ROLE_TEXT } from '@/utils/constants'
import { formatDate } from '@/utils/format'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const list = ref<User[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const role = ref('')
const status = ref<number | undefined>()

const createVisible = ref(false)
const createSaving = ref(false)
const createRef = ref<FormInstance>()
const createForm = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  role: 'STAFF',
})
const createRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '4-20 位字母、数字或下划线', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

const passwordVisible = ref(false)
const passwordSaving = ref(false)
const passwordTarget = ref<User | null>(null)
const newPassword = ref('')

async function load() {
  loading.value = true
  try {
    const data = await adminApi.users({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      role: role.value || undefined,
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
  role.value = ''
  status.value = undefined
  page.value = 1
  void load()
}

async function toggleStatus(row: User) {
  if (row.id === auth.user?.id) {
    ElMessage.warning('不能修改自己的账号状态')
    return
  }
  const next = row.status === 1 ? 0 : 1
  try {
    await adminApi.userStatus(row.id, next)
    ElMessage.success(next === 1 ? '账号已启用' : '账号已禁用')
    await load()
  } catch {
    await load()
  }
}

function openCreate() {
  Object.assign(createForm, {
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    role: 'STAFF',
  })
  createVisible.value = true
}

async function createUser() {
  const valid = await createRef.value?.validate().catch(() => false)
  if (!valid) return
  createSaving.value = true
  try {
    await adminApi.createUser({ ...createForm, email: createForm.email || undefined })
    ElMessage.success('账号已创建')
    createVisible.value = false
    await load()
  } finally {
    createSaving.value = false
  }
}

function openPassword(row: User) {
  passwordTarget.value = row
  newPassword.value = ''
  passwordVisible.value = true
}

async function resetPassword() {
  if (!passwordTarget.value) return
  if (newPassword.value.length < 6) {
    ElMessage.warning('密码长度至少 6 位')
    return
  }
  passwordSaving.value = true
  try {
    await adminApi.resetPassword(passwordTarget.value.id, newPassword.value)
    ElMessage.success('密码已重置')
    passwordVisible.value = false
  } finally {
    passwordSaving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="users-admin">
    <AdminPageHeader title="用户管理" description="管理观众与工作人员账号，支持启用、禁用与密码重置。">
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> 新建工作人员
      </el-button>
    </AdminPageHeader>

    <div class="filter-card">
      <el-input v-model="keyword" placeholder="用户名 / 姓名 / 手机号" clearable class="filter-keyword" @keyup.enter="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="role" placeholder="全部角色" clearable class="filter-select">
        <el-option label="普通观众" value="VISITOR" />
        <el-option label="检票员" value="STAFF" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <el-select v-model="status" placeholder="全部状态" clearable class="filter-select">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="list" row-key="id">
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <span class="user-cell__avatar">{{ (row.realName || row.username).slice(0, 1) }}</span>
              <div>
                <strong>{{ row.realName }}</strong>
                <span>@{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="125" />
        <el-table-column prop="email" label="邮箱" min-width="170" show-overflow-tooltip />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'STAFF' ? 'warning' : 'info'" effect="plain" size="small">
              {{ ROLE_TEXT[row.role] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :disabled="row.id === auth.user?.id"
              inline-prompt
              active-text="启用"
              inactive-text="禁用"
              @change="toggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="最近登录" min-width="145">
          <template #default="{ row }">
            <span class="muted">{{ row.lastLoginAt ? formatDate(row.lastLoginAt, true) : '从未登录' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="120">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-foot">
        <span>共 {{ total }} 位用户</span>
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

    <el-dialog v-model="createVisible" title="新建工作人员账号" width="520px">
      <el-form ref="createRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="4-20 位字母、数字或下划线" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input v-model="createForm.password" type="password" show-password placeholder="6-20 位" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="createForm.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="createForm.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="createForm.role">
            <el-radio value="STAFF">检票员</el-radio>
            <el-radio value="ADMIN">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createSaving" @click="createUser">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordVisible" title="重置密码" width="420px">
      <p class="dialog-tip">正在为“{{ passwordTarget?.realName }}（{{ passwordTarget?.username }}）”重置密码。</p>
      <el-input v-model="newPassword" type="password" show-password placeholder="请输入新密码（6-20 位）" />
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSaving" @click="resetPassword">确认重置</el-button>
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

.user-cell {
  display: flex;
  align-items: center;
  gap: 9px;
}

.user-cell__avatar {
  display: grid;
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  place-items: center;
  border-radius: 50%;
  color: #fff9ec;
  background: linear-gradient(135deg, var(--teal), #1c6b63);
  font-family: var(--font-serif);
  font-size: 13px;
}

.user-cell strong {
  display: block;
  color: var(--ink);
  font-size: 12.5px;
  font-weight: 500;
}

.user-cell span {
  color: var(--muted);
  font-size: 10px;
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

.dialog-tip {
  margin-bottom: 14px;
  color: var(--muted);
  font-size: 12px;
}

@media (max-width: 720px) {
  .filter-keyword,
  .filter-select {
    width: 100%;
  }
}
</style>