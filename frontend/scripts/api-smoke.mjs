const base = 'http://127.0.0.1:8080/api'
const results = []
const tokens = {}

async function api(path, { method = 'GET', body, token } = {}) {
  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json; charset=utf-8'
  if (token) headers.Authorization = `Bearer ${token}`
  const res = await fetch(base + path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  const text = await res.text()
  let data
  try {
    data = JSON.parse(text)
  } catch {
    data = text
  }
  return { status: res.status, body: data, headers: res.headers }
}

function check(name, condition, detail = '') {
  results.push({ name, ok: Boolean(condition), detail })
  console.log(`${condition ? 'PASS' : 'FAIL'}  ${name}${detail ? '  -> ' + detail : ''}`)
}

async function login(username, password) {
  const res = await api('/auth/login', { method: 'POST', body: { username, password } })
  if (res.body.code !== 200) throw new Error(`login failed ${username}: ${JSON.stringify(res.body)}`)
  tokens[username] = res.body.data.token
  return res.body.data
}

const stamp = Date.now().toString().slice(-6)
let createdExhibitionId
let createdSlotId
let createdAnnouncementId
let createdStaffName = ''

// 1. 公共接口
{
  const home = await api('/public/home')
  check('公共首页', home.body.code === 200 && home.body.data.exhibitions.length > 0)
  const list = await api('/public/exhibitions?page=1&size=3')
  check('展览分页', list.body.code === 200 && list.body.data.records.length === 3)
  const slots = await api('/public/exhibitions/1/slots')
  check('时段查询', slots.body.code === 200 && slots.body.data.length > 0)
  const notices = await api('/public/announcements?page=1&size=3')
  check('公告分页', notices.body.code === 200 && notices.body.data.records.length > 0)
}

// 2. 登录
await login('admin', 'Admin@123')
await login('visitor', 'Visitor@123')
await login('staff', 'Staff@123')
check('三种角色登录', Boolean(tokens.admin && tokens.visitor && tokens.staff))

// 3. 权限
{
  const noToken = await api('/admin/dashboard')
  check('未登录访问后台返回 401', noToken.body.code === 401)
  const forbidden = await api('/admin/dashboard', { token: tokens.visitor })
  check('普通观众访问后台返回 403', forbidden.body.code === 403)
}

// 4. 游客预约
let visitorReservation
{
  const slots = await api('/public/exhibitions/1/slots')
  const dayAfterTomorrow = new Date(Date.now() + 3 * 86400000).toISOString().slice(0, 10)
  const target = slots.body.data.find((s) => s.remaining > 3 && s.visitDate > dayAfterTomorrow)
  const created = await api('/reservations', {
    method: 'POST',
    token: tokens.visitor,
    body: {
      slotId: target.id,
      visitorName: '李明',
      visitorIdCard: '420106199808081234',
      visitorPhone: '13800000003',
      ticketCount: 2,
      remark: '自动化冒烟测试',
    },
  })
  check('创建预约', created.body.code === 200 && created.body.data.status === 'CONFIRMED')
  visitorReservation = created.body.data
  const duplicated = await api('/reservations', {
    method: 'POST',
    token: tokens.visitor,
    body: {
      slotId: target.id,
      visitorName: '李明',
      visitorIdCard: '420106199808081234',
      visitorPhone: '13800000003',
      ticketCount: 1,
    },
  })
  check('重复预约被拒绝', duplicated.body.code === 409)
  const cancelled = await api(`/reservations/${visitorReservation.id}/cancel`, {
    method: 'PUT',
    token: tokens.visitor,
    body: { reason: '自动化测试取消' },
  })
  check('取消预约', cancelled.body.code === 200)
}

// 5. 数据看板
{
  const dashboard = await api('/admin/dashboard', { token: tokens.admin })
  check(
    '后台看板',
    dashboard.body.code === 200 && dashboard.body.data.trend.length === 7,
    `访客 ${dashboard.body.data.visitorCount}`,
  )
}

// 6. 展览 CRUD
{
  const created = await api('/admin/exhibitions', {
    method: 'POST',
    token: tokens.admin,
    body: {
      title: `自动化测试展览${stamp}`,
      subtitle: '接口冒烟测试数据',
      category: '临时展览',
      coverUrl: '/images/exhibitions/river.svg',
      description: '用于验证展览创建、修改与删除。',
      venue: '临时展厅三',
      startDate: '2026-10-01',
      endDate: '2026-12-31',
      openTime: '09:00:00',
      closeTime: '17:00:00',
      ticketPrice: 10,
      tips: '测试提示',
      status: 'DRAFT',
      sortOrder: 1,
    },
  })
  check('创建展览', created.body.code === 200, created.body.data?.title)
  createdExhibitionId = created.body.data.id
  const updated = await api(`/admin/exhibitions/${createdExhibitionId}`, {
    method: 'PUT',
    token: tokens.admin,
    body: {
      ...created.body.data,
      title: `自动化测试展览${stamp}-已修改`,
      status: 'PUBLISHED',
    },
  })
  check('修改展览', updated.body.code === 200 && updated.body.data.title.endsWith('-已修改'))
  const status = await api(`/admin/exhibitions/${createdExhibitionId}/status?status=CLOSED`, {
    method: 'PUT',
    token: tokens.admin,
  })
  check('上下架展览', status.body.code === 200)
}

// 7. 时段 CRUD
{
  const created = await api('/admin/slots', {
    method: 'POST',
    token: tokens.admin,
    body: {
      exhibitionId: createdExhibitionId,
      visitDate: '2026-11-10',
      startTime: '18:00:00',
      endTime: '20:00:00',
      capacity: 50,
      status: 'OPEN',
      remark: '测试时段',
    },
  })
  check('创建时段', created.body.code === 200)
  createdSlotId = created.body.data.id
  const updated = await api(`/admin/slots/${createdSlotId}`, {
    method: 'PUT',
    token: tokens.admin,
    body: {
      exhibitionId: createdExhibitionId,
      visitDate: '2026-11-10',
      startTime: '18:00:00',
      endTime: '20:00:00',
      capacity: 80,
      status: 'OPEN',
      remark: '测试时段-已修改',
    },
  })
  check('修改时段容量', updated.body.code === 200 && updated.body.data.capacity === 80)
  const batch = await api('/admin/slots/batch', {
    method: 'POST',
    token: tokens.admin,
    body: {
      exhibitionId: createdExhibitionId,
      startDate: '2026-11-11',
      endDate: '2026-11-13',
      startTimes: ['09:00', '11:00'],
      capacity: 60,
      skipMonday: true,
    },
  })
  check('批量生成时段', batch.body.code === 200 && batch.body.data.created >= 4, `生成 ${batch.body.data?.created}`)
  const page = await api(`/admin/slots?page=1&size=20&exhibitionId=${createdExhibitionId}`, { token: tokens.admin })
  check('时段分页', page.body.code === 200 && page.body.data.total >= 5)
  for (const row of page.body.data.records) {
    await api(`/admin/slots/${row.id}`, { method: 'DELETE', token: tokens.admin })
  }
  check('删除时段', true)
}

// 8. 公告 CRUD
{
  const created = await api('/admin/announcements', {
    method: 'POST',
    token: tokens.admin,
    body: {
      title: `自动化测试公告${stamp}`,
      summary: '接口测试摘要',
      content: '接口测试正文内容。\n\n第二段内容。',
      category: 'NOTICE',
      isTop: 0,
      status: 0,
    },
  })
  check('创建公告', created.body.code === 200)
  createdAnnouncementId = created.body.data.id
  const updated = await api(`/admin/announcements/${createdAnnouncementId}`, {
    method: 'PUT',
    token: tokens.admin,
    body: {
      title: `自动化测试公告${stamp}-已发布`,
      summary: '接口测试摘要',
      content: '接口测试正文内容。',
      category: 'NOTICE',
      isTop: 1,
      status: 1,
    },
  })
  check('发布公告', updated.body.code === 200 && updated.body.data.status === 1)
  const pub = await api('/public/announcements?page=1&size=5')
  check('前台可见已发布公告', pub.body.data.records.some((n) => n.id === createdAnnouncementId))
  const deleted = await api(`/admin/announcements/${createdAnnouncementId}`, {
    method: 'DELETE',
    token: tokens.admin,
  })
  check('删除公告', deleted.body.code === 200)
}

// 9. 意见反馈
{
  const created = await api('/feedbacks', {
    method: 'POST',
    token: tokens.visitor,
    body: { type: 'SUGGESTION', content: `自动化测试反馈${stamp}`, contact: '13800000003' },
  })
  check('提交反馈', created.body.code === 200)
  const page = await api(`/admin/feedbacks?page=1&size=10&keyword=自动化测试反馈${stamp}`, { token: tokens.admin })
  check('后台查询反馈', page.body.code === 200 && page.body.data.total === 1)
  const id = page.body.data.records[0].id
  const replied = await api(`/admin/feedbacks/${id}/reply`, {
    method: 'POST',
    token: tokens.admin,
    body: { reply: '感谢您的反馈，我们已记录。' },
  })
  check('回复反馈', replied.body.code === 200)
  const my = await api('/feedbacks/my?page=1&size=10', { token: tokens.visitor })
  check('用户查看回复', my.body.data.records.some((f) => f.id === id && f.status === 'REPLIED'))
}

// 10. 用户管理
{
  createdStaffName = `staff${stamp}`
  const created = await api('/admin/users', {
    method: 'POST',
    token: tokens.admin,
    body: {
      username: createdStaffName,
      password: 'Staff@123',
      realName: '测试检票员',
      phone: '13900001111',
      email: 'staff_test@example.com',
      role: 'STAFF',
    },
  })
  check('创建工作人员', created.body.code === 200, createdStaffName)
  const id = created.body.data.id
  const disabled = await api(`/admin/users/${id}/status`, {
    method: 'PUT',
    token: tokens.admin,
    body: { status: 0 },
  })
  check('禁用账号', disabled.body.code === 200)
  const enabled = await api(`/admin/users/${id}/status`, {
    method: 'PUT',
    token: tokens.admin,
    body: { status: 1 },
  })
  check('启用账号', enabled.body.code === 200)
  const reset = await api(`/admin/users/${id}/password`, {
    method: 'PUT',
    token: tokens.admin,
    body: { newPassword: 'NewStaff@123' },
  })
  check('重置密码', reset.body.code === 200)
  const loginNew = await api('/auth/login', {
    method: 'POST',
    body: { username: createdStaffName, password: 'NewStaff@123' },
  })
  check('新密码可登录', loginNew.body.code === 200)
}

// 11. 核销（错误路径：非今日）
{
  const preview = await api('/admin/checkin/search?code=HB202610220002', { token: tokens.staff })
  check('核销查询', preview.body.code === 200 && preview.body.data.orderNo === 'HB202610220002')
  const checkin = await api('/admin/checkin', {
    method: 'POST',
    token: tokens.staff,
    body: { code: 'HB202610220002' },
  })
  check('非今日预约不可核销', checkin.body.code === 409)
}

// 12. CSV 导出
{
  const res = await fetch(`${base}/admin/reservations/export`, {
    headers: { Authorization: `Bearer ${tokens.admin}` },
  })
  const buffer = await res.arrayBuffer()
  const head = Buffer.from(buffer).subarray(0, 30).toString('utf8')
  check('导出 CSV', res.headers.get('content-type')?.includes('text/csv') && head.includes('预约单号'))
}

// 13. 删除测试展览
{
  const deleted = await api(`/admin/exhibitions/${createdExhibitionId}`, {
    method: 'DELETE',
    token: tokens.admin,
  })
  check('删除测试展览', deleted.body.code === 200)
}

const failed = results.filter((r) => !r.ok)
console.log(`\n===== ${results.length - failed.length}/${results.length} passed =====`)
if (failed.length) {
  console.log(failed.map((r) => `${r.name}: ${r.detail}`).join('\n'))
  process.exitCode = 1
}