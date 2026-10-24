<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { adminApi } from '@/api/admin'
import type { Dashboard } from '@/types'
import AdminPageHeader from '@/components/common/AdminPageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { shortTime } from '@/utils/format'

const loading = ref(true)
const data = ref<Dashboard | null>(null)
const trendRef = ref<HTMLDivElement>()
const pieRef = ref<HTMLDivElement>()
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const cards = [
  { key: 'todayVisitorCount', label: '今日预约人数', unit: '人', icon: 'UserFilled', tone: 'teal' },
  { key: 'todayCheckedCount', label: '今日已入馆', unit: '人', icon: 'CircleCheckFilled', tone: 'green' },
  { key: 'todayReservationCount', label: '今日新增订单', unit: '单', icon: 'Tickets', tone: 'gold' },
  { key: 'pendingFeedbackCount', label: '待处理反馈', unit: '条', icon: 'ChatDotRound', tone: 'red' },
] as const

function cardValue(key: (typeof cards)[number]['key']) {
  return data.value ? data.value[key] : 0
}

function renderTrend() {
  if (!trendRef.value || !data.value) return
  trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    color: ['#0e4b47', '#c7a15a'],
    tooltip: { trigger: 'axis' },
    legend: { right: 10, top: 0, itemWidth: 10, itemHeight: 10, textStyle: { color: '#6f7774', fontSize: 11 } },
    grid: { left: 10, right: 14, top: 38, bottom: 6, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.value.trend.map((item) => item.day),
      axisLine: { lineStyle: { color: 'rgba(19,44,42,.14)' } },
      axisTick: { show: false },
      axisLabel: { color: '#8a918d', fontSize: 10 },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(19,44,42,.07)' } },
      axisLabel: { color: '#8a918d', fontSize: 10 },
    },
    series: [
      {
        name: '预约人数',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        data: data.value.trend.map((item) => item.total),
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(14,75,71,.22)' },
            { offset: 1, color: 'rgba(14,75,71,0)' },
          ]),
        },
      },
      {
        name: '入馆人数',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        data: data.value.trend.map((item) => item.checked),
      },
    ],
  })
}

function renderPie() {
  if (!pieRef.value || !data.value) return
  pieChart = echarts.init(pieRef.value)
  pieChart.setOption({
    color: ['#0e4b47', '#c7a15a', '#7fa79d', '#a94442', '#8a918d'],
    tooltip: { trigger: 'item', formatter: '{b}：{c} 个（{d}%）' },
    legend: { bottom: 0, itemWidth: 9, itemHeight: 9, textStyle: { color: '#6f7774', fontSize: 10 } },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fffdf8', borderWidth: 3 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 13, fontWeight: 600 } },
        data: data.value.categoryDistribution.map((item) => ({ name: item.name, value: item.value })),
      },
    ],
  })
}

function resize() {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  try {
    data.value = await adminApi.dashboard()
    await nextTick()
    renderTrend()
    renderPie()
    window.addEventListener('resize', resize)
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <AdminPageHeader title="运营概览" description="实时掌握预约、入馆与内容运营数据。">
      <el-button @click="$router.push('/admin/reservations')">预约管理</el-button>
      <el-button type="primary" @click="$router.push('/admin/checkin')">现场核销</el-button>
    </AdminPageHeader>

    <div class="stat-grid">
      <div v-for="card in cards" :key="card.key" class="stat-card" :class="`tone-${card.tone}`">
        <div class="stat-card__icon">
          <el-icon><component :is="card.icon" /></el-icon>
        </div>
        <div>
          <span>{{ card.label }}</span>
          <strong>{{ cardValue(card.key) }}<small>{{ card.unit }}</small></strong>
        </div>
        <span class="stat-card__line" />
      </div>
    </div>

    <div class="chart-grid">
      <section class="panel panel--wide">
        <div class="panel__head">
          <div>
            <h3>近 7 天参观趋势</h3>
            <p>按参观日期统计预约人数与实际入馆人数</p>
          </div>
          <span class="panel__badge">实时</span>
        </div>
        <div ref="trendRef" class="chart chart--line" />
      </section>

      <section class="panel">
        <div class="panel__head">
          <div>
            <h3>展览分类占比</h3>
            <p>当前已上架展览</p>
          </div>
        </div>
        <div ref="pieRef" class="chart chart--pie" />
      </section>
    </div>

    <div class="bottom-grid">
      <section class="panel">
        <div class="panel__head">
          <div>
            <h3>今日预约容量</h3>
            <p>容量 {{ data?.todayCapacity ?? 0 }} 人，已预约 {{ data?.todayBooked ?? 0 }} 人</p>
          </div>
          <div class="rate">
            <small>核销率</small>
            <strong>{{ data?.checkinRate ?? 0 }}%</strong>
          </div>
        </div>
        <el-progress
          :percentage="data && data.todayCapacity ? Math.min(100, Math.round((data.todayBooked / data.todayCapacity) * 100)) : 0"
          :stroke-width="12"
          color="#0e4b47"
        />
        <div class="progress-labels">
          <span>已预约 {{ data?.todayBooked ?? 0 }} 人</span>
          <span>剩余 {{ data ? Math.max(0, data.todayCapacity - data.todayBooked) : 0 }} 人</span>
        </div>

        <div class="top-list">
          <h4>热门展览 TOP5</h4>
          <div v-for="(item, index) in data?.topExhibitions ?? []" :key="item.name" class="top-item">
            <span class="top-item__no">{{ index + 1 }}</span>
            <span class="top-item__name">{{ item.name }}</span>
            <span class="top-item__bar">
              <i :style="{ width: `${Math.min(100, (item.value / Math.max(1, data?.topExhibitions[0]?.value ?? 1)) * 100)}%` }" />
            </span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
      </section>

      <section class="panel panel--wide">
        <div class="panel__head">
          <div>
            <h3>最新预约</h3>
            <p>最近 8 条预约记录</p>
          </div>
          <el-button text type="primary" @click="$router.push('/admin/reservations')">查看全部</el-button>
        </div>
        <el-table :data="data?.recentReservations ?? []" size="small" class="recent-table">
          <el-table-column prop="orderNo" label="预约单号" min-width="150" show-overflow-tooltip />
          <el-table-column prop="exhibitionTitle" label="展览" min-width="120" show-overflow-tooltip />
          <el-table-column prop="visitorName" label="参观人" width="90" />
          <el-table-column label="参观时段" min-width="145">
            <template #default="{ row }">
              {{ row.visitDate }} {{ shortTime(row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <StatusTag :status="row.status" />
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </div>
</template>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}

.stat-card {
  position: relative;
  display: flex;
  overflow: hidden;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.stat-card__icon {
  display: grid;
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  place-items: center;
  border-radius: 10px;
  color: var(--teal);
  background: var(--teal-light);
  font-size: 21px;
}

.stat-card span {
  display: block;
  color: var(--muted);
  font-size: 11px;
}

.stat-card strong {
  display: block;
  margin-top: 2px;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 30px;
  font-weight: 500;
  line-height: 1.2;
}

.stat-card strong small {
  margin-left: 3px;
  color: var(--muted);
  font-family: var(--font-sans);
  font-size: 10px;
  font-weight: 400;
}

.stat-card__line {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: var(--teal);
  opacity: 0.75;
}

.tone-green .stat-card__icon {
  color: #2f7d5c;
  background: #eaf6ef;
}

.tone-green .stat-card__line {
  background: #2f7d5c;
}

.tone-gold .stat-card__icon {
  color: #a77b45;
  background: #f7efdf;
}

.tone-gold .stat-card__line {
  background: #c7a15a;
}

.tone-red .stat-card__icon {
  color: #a94442;
  background: #f9eceb;
}

.tone-red .stat-card__line {
  background: #a94442;
}

.chart-grid {
  display: grid;
  grid-template-columns: 1.55fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1.35fr;
  gap: 16px;
}

.panel {
  padding: 20px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.panel__head h3 {
  font-size: 16px;
  letter-spacing: 0.04em;
}

.panel__head p {
  margin-top: 2px;
  color: var(--muted);
  font-size: 10.5px;
}

.panel__badge {
  padding: 1px 8px;
  border: 1px solid rgba(47, 125, 92, 0.35);
  color: #2f7d5c;
  font-size: 9px;
}

.rate {
  display: flex;
  align-items: flex-end;
  flex-direction: column;
  gap: 1px;
}

.rate small {
  color: var(--muted);
  font-size: 9px;
  letter-spacing: 0.08em;
}

.rate strong {
  color: var(--teal);
  font-family: var(--font-serif);
  font-size: 22px;
  font-weight: 500;
}

.chart {
  width: 100%;
}

.chart--line {
  height: 260px;
}

.chart--pie {
  height: 260px;
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  color: var(--muted);
  font-size: 10.5px;
}

.top-list {
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid var(--border);
}

.top-list h4 {
  margin-bottom: 12px;
  font-family: var(--font-sans);
  font-size: 12px;
  letter-spacing: 0.06em;
}

.top-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 11px;
}

.top-item__no {
  display: grid;
  width: 16px;
  height: 16px;
  place-items: center;
  border-radius: 3px;
  color: var(--muted);
  background: #f1ede3;
  font-size: 9px;
}

.top-item:nth-child(2) .top-item__no {
  color: #fff;
  background: var(--teal);
}

.top-item__name {
  width: 96px;
  overflow: hidden;
  color: #4d5754;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.top-item__bar {
  flex: 1;
  height: 5px;
  overflow: hidden;
  border-radius: 4px;
  background: #f0ece2;
}

.top-item__bar i {
  display: block;
  height: 100%;
  border-radius: 4px;
  background: linear-gradient(90deg, var(--teal), #4e8f86);
}

.top-item strong {
  width: 26px;
  color: var(--teal);
  font-size: 11px;
  text-align: right;
}

.recent-table {
  width: 100%;
}

@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: 1fr 1fr;
  }

  .chart-grid,
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>