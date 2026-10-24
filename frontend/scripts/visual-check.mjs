import { chromium } from 'playwright-core'
import { mkdirSync } from 'node:fs'
import { resolve } from 'node:path'

const base = 'http://127.0.0.1:5173'
const outDir = resolve('..', 'docs', 'screenshots')
mkdirSync(outDir, { recursive: true })

const executablePath = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'
const browser = await chromium.launch({ executablePath, headless: true })
const errors = []

async function newPage(width = 1440, height = 900) {
  const context = await browser.newContext({ viewport: { width, height }, locale: 'zh-CN' })
  const page = await context.newPage()
  page.on('console', (msg) => {
    if (msg.type() === 'error') errors.push(`[console] ${msg.text()}`)
  })
  page.on('pageerror', (err) => errors.push(`[pageerror] ${err.message}`))
  return { context, page }
}

async function shot(page, name, fullPage = true) {
  await page.evaluate(() => window.scrollTo(0, 0)).catch(() => {})
  await page.waitForTimeout(600)
  await page.screenshot({ path: resolve(outDir, `${name}.png`), fullPage })
  console.log('saved', name)
}

async function login(page, username, password) {
  await page.goto(`${base}/login`, { waitUntil: 'networkidle' })
  await page.getByPlaceholder('请输入用户名').fill(username)
  await page.getByPlaceholder('请输入密码').fill(password)
  await page.getByRole('button', { name: '登录', exact: true }).click()
  await page.waitForTimeout(1200)
}

// 游客端
{
  const { context, page } = await newPage()
  await page.goto(base, { waitUntil: 'networkidle' })
  await shot(page, '01-home')
  await page.goto(`${base}/exhibitions`, { waitUntil: 'networkidle' })
  await shot(page, '02-exhibitions')
  await page.goto(`${base}/exhibitions/1`, { waitUntil: 'networkidle' })
  await shot(page, '03-exhibition-detail')
  await page.goto(`${base}/announcements`, { waitUntil: 'networkidle' })
  await shot(page, '04-announcements')
  await page.goto(`${base}/about`, { waitUntil: 'networkidle' })
  await shot(page, '05-about')
  await page.goto(`${base}/login`, { waitUntil: 'networkidle' })
  await shot(page, '05b-login')
  await page.goto(`${base}/register`, { waitUntil: 'networkidle' })
  await shot(page, '05c-register')

  await login(page, 'visitor', 'Visitor@123')
  console.log('visitor url', page.url())
  await page.goto(`${base}/booking?exhibitionId=1`, { waitUntil: 'networkidle' })
  await shot(page, '06-booking')
  await page.goto(`${base}/user/profile`, { waitUntil: 'networkidle' })
  await shot(page, '07-profile')
  await page.goto(`${base}/user/reservations`, { waitUntil: 'networkidle' })
  await shot(page, '08-my-reservations')
  const detailLink = page.getByRole('button', { name: '查看凭证' }).first()
  if (await detailLink.count()) {
    await detailLink.click()
    await page.waitForTimeout(800)
    await shot(page, '09-reservation-detail')
  }
  await context.close()
}

// 移动端首页
{
  const { context, page } = await newPage(390, 844)
  await page.goto(base, { waitUntil: 'networkidle' })
  await page.waitForTimeout(500)
  await page.screenshot({ path: resolve(outDir, '10-home-mobile.png') })
  await page.locator('.menu-toggle').click()
  await page.waitForTimeout(500)
  await page.screenshot({ path: resolve(outDir, '10b-home-mobile-menu.png') })
  await page.goto(`${base}/exhibitions/1`, { waitUntil: 'networkidle' })
  await page.waitForTimeout(500)
  await page.screenshot({ path: resolve(outDir, '10c-exhibition-mobile.png') })
  await context.close()
}

// 管理端
{
  const { context, page } = await newPage()
  await login(page, 'admin', 'Admin@123')
  console.log('admin url', page.url())
  await page.goto(`${base}/admin/dashboard`, { waitUntil: 'networkidle' })
  await page.waitForTimeout(1200)
  await shot(page, '11-admin-dashboard')
  await page.goto(`${base}/admin/exhibitions`, { waitUntil: 'networkidle' })
  await shot(page, '12-admin-exhibitions')
  await page.goto(`${base}/admin/slots`, { waitUntil: 'networkidle' })
  await shot(page, '13-admin-slots')
  await page.goto(`${base}/admin/reservations`, { waitUntil: 'networkidle' })
  await shot(page, '14-admin-reservations')
  await page.goto(`${base}/admin/checkin`, { waitUntil: 'networkidle' })
  await shot(page, '15-admin-checkin')
  await page.goto(`${base}/admin/users`, { waitUntil: 'networkidle' })
  await shot(page, '16-admin-users')
  await page.goto(`${base}/admin/announcements`, { waitUntil: 'networkidle' })
  await shot(page, '17-admin-announcements')
  await page.goto(`${base}/admin/feedbacks`, { waitUntil: 'networkidle' })
  await shot(page, '18-admin-feedbacks')
  await context.close()
}

// 检票员
{
  const { context, page } = await newPage()
  await login(page, 'staff', 'Staff@123')
  console.log('staff url', page.url())
  await shot(page, '19-staff-checkin')
  await context.close()
}

await browser.close()
console.log('\n--- errors ---')
console.log(errors.length ? errors.join('\n') : 'no console/page errors')