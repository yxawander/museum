# 前端 · 湖北省博物馆参观预约系统

Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router。

## 启动

```bash
npm install
npm run dev
```

访问 `http://127.0.0.1:5173`。开发服务器将 `/api` 代理到 `http://127.0.0.1:8080`（见 `vite.config.ts`）。

## 构建

```bash
npm run build
```

包含 `vue-tsc` 类型检查与 `vite build`；产物在 `dist/`。

## 目录

```text
src/
├─ api/            Axios 实例与业务接口
├─ assets/         全局样式与主题变量
├─ components/     公共组件、布局组件
├─ layouts/        PublicLayout / UserLayout / AdminLayout
├─ router/         路由与权限守卫
├─ stores/         Pinia（auth）
├─ types/          接口数据类型
├─ utils/          常量与格式化
└─ views/          24 个页面
```

## 测试脚本

```bash
# 接口冒烟（需后端已启动）
node scripts/api-smoke.mjs

# 并发抢名额
node scripts/test-concurrency.mjs

# 页面截图 + 控制台检查（需安装 playwright-core 与 Chrome）
node scripts/visual-check.mjs
```

截图输出到 `../docs/screenshots/`。

## 说明

- 页面视觉使用本地 `public/images/*.svg`，无外网图片依赖。
- 路由守卫按 `meta.requiresAuth`/`meta.roles` 控制访问；真正的权限边界在后端。
- 401 会清除本地登录态并跳转 `/login?redirect=当前页`。