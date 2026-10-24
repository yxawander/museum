<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { publicApi } from '@/api/public'
import type { HomeData } from '@/types'
import ExhibitionCard from '@/components/common/ExhibitionCard.vue'
import { ANNOUNCEMENT_CATEGORIES } from '@/utils/constants'
import { formatDate } from '@/utils/format'

const loading = ref(true)
const home = ref<HomeData | null>(null)

onMounted(async () => {
  try {
    home.value = await publicApi.home()
  } finally {
    loading.value = false
  }
})

const steps = [
  { no: '01', title: '选择展览与日期', desc: '浏览当前展览，选择参观日期与入馆时段。' },
  { no: '02', title: '实名填写信息', desc: '填写参观人姓名、身份证号与手机号，单次最多 5 人。' },
  { no: '03', title: '获取预约码', desc: '预约成功后生成专属核销码，可在“我的预约”中查看。' },
  { no: '04', title: '按时到馆核验', desc: '携带有效身份证件，按预约时段到馆，出示核销码入馆。' },
]

const services = [
  { icon: 'Headset', title: '讲解服务', desc: '每日定时公益讲解，亦可预约团队讲解与语音导览。' },
  { icon: 'Reading', title: '社教活动', desc: '面向青少年与家庭观众的节气、非遗、考古主题活动。' },
  { icon: 'ShoppingBag', title: '文创商店', desc: '以编钟、勾践剑、楚凤为灵感的原创文创与联名好物。' },
]

const notices = [
  { label: '开放时间', value: '周二至周日 09:00—17:00', note: '16:00 停止入馆，周一闭馆' },
  { label: '预约规则', value: '提前 7 天实名预约', note: '每个账号单次最多预约 5 人' },
  { label: '入馆方式', value: '身份证 + 预约核销码', note: '请按预约时段到馆，逾时作废' },
]
</script>

<template>
  <div class="home">
    <section class="hero">
      <div class="hero__bg">
        <img src="/images/hero.svg" alt="曾侯乙编钟意象">
      </div>
      <div class="container hero__inner">
        <div class="hero__copy">
          <p class="eyebrow hero__eyebrow">HUBEI PROVINCIAL MUSEUM</p>
          <h1>与两千年前的文明<br><em>在此相遇</em></h1>
          <p class="hero__desc">
            从曾侯乙编钟的华夏正音，到越王勾践剑的千年锋芒。走进荆楚，读懂长江。
          </p>
          <div class="hero__actions">
            <el-button type="primary" size="large" round @click="$router.push('/booking')">
              立即预约参观
              <el-icon class="el-icon--right"><Right /></el-icon>
            </el-button>
            <el-button size="large" round class="hero__ghost" @click="$router.push('/exhibitions')">
              浏览全部展览
            </el-button>
          </div>
          <div class="hero__meta">
            <span><el-icon><Clock /></el-icon> 09:00—17:00</span>
            <span><el-icon><Location /></el-icon> 武汉市武昌区东湖路160号</span>
            <span><el-icon><Phone /></el-icon> 027-86794127</span>
          </div>
        </div>

        <div class="hero__card">
          <span class="hero__card-label">TODAY · 今日余票</span>
          <strong v-if="home">{{ home.stats.todayRemaining }}</strong>
          <strong v-else>—</strong>
          <span class="hero__card-unit">张</span>
          <p>今日开放 3 个入馆时段，建议提前预约，错峰参观。</p>
          <el-button type="primary" plain round @click="$router.push('/booking')">查看时段</el-button>
          <div class="hero__card-line" />
          <small>开放时间 09:00—17:00 · 周一闭馆</small>
        </div>
      </div>
      <div class="hero__scroll">
        <span>向下探索</span>
        <i />
      </div>
    </section>

    <section class="stats-strip">
      <div class="container stats-strip__inner">
        <div class="stat">
          <strong>{{ home?.stats.exhibitionCount ?? '—' }}</strong>
          <span>在展展览</span>
        </div>
        <div class="stat">
          <strong>{{ home?.stats.todayVisitorCount ?? '—' }}</strong>
          <span>今日预约人数</span>
        </div>
        <div class="stat">
          <strong>{{ home?.stats.totalReservationCount ?? '—' }}</strong>
          <span>累计预约订单</span>
        </div>
        <div class="stat">
          <strong>65</strong>
          <span>曾侯乙编钟件数</span>
        </div>
        <div class="stat stat--note">
          <span class="stat__tag">免费开放</span>
          <p>常设展览免费，特展按展览公告收费</p>
        </div>
      </div>
    </section>

    <section class="section section--paper">
      <div class="container">
        <div class="section-head">
          <div class="section-head__main">
            <p class="eyebrow">CURRENT EXHIBITIONS</p>
            <h2 class="section-title">馆藏展览</h2>
            <p class="section-desc">六大常设与特展，从青铜礼乐到明代藩王珍藏，铺陈荆楚文明的长卷。</p>
          </div>
          <router-link to="/exhibitions" class="link-more">全部展览 <el-icon><Right /></el-icon></router-link>
        </div>

        <div v-loading="loading" class="exhibition-grid">
          <ExhibitionCard v-for="item in home?.exhibitions ?? []" :key="item.id" :exhibition="item" />
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container">
        <div class="section-head">
          <div class="section-head__main">
            <p class="eyebrow">VISIT GUIDE</p>
            <h2 class="section-title">四步完成预约</h2>
            <p class="section-desc">实名预约、分时入馆。请提前规划行程，按预约时段到馆参观。</p>
          </div>
          <router-link to="/about" class="link-more">完整参观指南 <el-icon><Right /></el-icon></router-link>
        </div>
        <div class="steps">
          <div v-for="step in steps" :key="step.no" class="step">
            <span class="step__no">{{ step.no }}</span>
            <h3>{{ step.title }}</h3>
            <p>{{ step.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="section notices-section">
      <div class="container notices-grid">
        <div class="notices-main">
          <div class="section-head">
            <div class="section-head__main">
              <p class="eyebrow">NEWS & NOTICE</p>
              <h2 class="section-title">公告资讯</h2>
            </div>
            <router-link to="/announcements" class="link-more">更多资讯 <el-icon><Right /></el-icon></router-link>
          </div>
          <div class="notice-list">
            <router-link
              v-for="item in home?.announcements ?? []"
              :key="item.id"
              :to="`/announcements/${item.id}`"
              class="notice-item"
            >
              <span class="notice-item__date">
                <strong>{{ item.publishTime ? item.publishTime.slice(8, 10) : '—' }}</strong>
                <small>{{ item.publishTime ? item.publishTime.slice(0, 7) : '' }}</small>
              </span>
              <span class="notice-item__body">
                <span class="notice-item__tag">{{ ANNOUNCEMENT_CATEGORIES[item.category] }}</span>
                <strong>{{ item.title }}</strong>
                <small>{{ item.summary }}</small>
              </span>
              <el-icon class="notice-item__arrow"><Right /></el-icon>
            </router-link>
          </div>
        </div>

        <aside class="visit-card">
          <p class="eyebrow">VISIT INFO</p>
          <h3>参观信息</h3>
          <div v-for="row in notices" :key="row.label" class="visit-card__row">
            <span>{{ row.label }}</span>
            <strong>{{ row.value }}</strong>
            <small>{{ row.note }}</small>
          </div>
          <el-button type="primary" round class="visit-card__btn" @click="$router.push('/about')">
            查看交通与入馆须知
          </el-button>
          <div class="visit-card__mark">楚</div>
        </aside>
      </div>
    </section>

    <section class="section section--paper">
      <div class="container">
        <div class="section-head">
          <div class="section-head__main">
            <p class="eyebrow">MUSEUM SERVICES</p>
            <h2 class="section-title">更多博物馆体验</h2>
          </div>
        </div>
        <div class="service-grid">
          <div v-for="item in services" :key="item.title" class="service">
            <div class="service__icon">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="cta">
      <div class="container cta__inner">
        <div>
          <p class="eyebrow cta__eyebrow">PLAN YOUR VISIT</p>
          <h2>让下一次到馆，从容而有序</h2>
          <p>提前预约、按时间段入馆，给自己留出与文物安静相处的时间。</p>
        </div>
        <el-button type="primary" size="large" round @click="$router.push('/booking')">
          预约参观
          <el-icon class="el-icon--right"><Right /></el-icon>
        </el-button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.hero {
  position: relative;
  overflow: hidden;
  min-height: 620px;
  color: #f8f2e5;
  background: #0a2b28;
}

.hero__bg {
  position: absolute;
  inset: 0;
}

.hero__bg::after {
  position: absolute;
  inset: 0;
  content: '';
  background: linear-gradient(90deg, rgba(5, 24, 22, 0.92) 0%, rgba(5, 24, 22, 0.55) 46%, rgba(5, 24, 22, 0.18) 100%);
}

.hero__bg img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  animation: heroZoom 18s ease-out both;
}

@keyframes heroZoom {
  from {
    transform: scale(1.08);
  }
  to {
    transform: scale(1);
  }
}

.hero__inner {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 620px;
  gap: 40px;
  padding: 80px 0 96px;
}

.hero__copy {
  max-width: 680px;
}

.hero__eyebrow {
  color: var(--gold);
}

.hero__copy h1 {
  margin-top: 8px;
  color: #fff9ec;
  font-size: clamp(38px, 4.2vw, 58px);
  font-weight: 500;
  letter-spacing: 0.1em;
  line-height: 1.36;
  white-space: nowrap;
}

.hero__copy h1 em {
  color: var(--gold);
  font-style: normal;
}

.hero__desc {
  max-width: 480px;
  margin-top: 22px;
  color: rgba(248, 242, 229, 0.7);
  font-size: 16px;
  letter-spacing: 0.04em;
}

.hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 34px;
}

.hero__ghost {
  border-color: rgba(248, 242, 229, 0.32);
  color: #f8f2e5;
  background: rgba(255, 255, 255, 0.04);
}

.hero__ghost:hover {
  border-color: var(--gold);
  color: var(--gold);
  background: rgba(255, 255, 255, 0.06);
}

.hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 22px;
  margin-top: 42px;
  color: rgba(248, 242, 229, 0.55);
  font-size: 12.5px;
}

.hero__meta span {
  display: flex;
  align-items: center;
  gap: 7px;
}

.hero__card {
  display: flex;
  width: 240px;
  flex-direction: column;
  align-items: flex-start;
  padding: 24px;
  border: 1px solid rgba(199, 161, 90, 0.35);
  background: rgba(7, 35, 32, 0.55);
  backdrop-filter: blur(12px);
}

.hero__card-label {
  color: var(--gold);
  font-size: 10px;
  letter-spacing: 0.18em;
}

.hero__card strong {
  display: inline-block;
  margin-top: 12px;
  color: #fff9ec;
  font-family: var(--font-serif);
  font-size: 48px;
  font-weight: 500;
  line-height: 1;
}

.hero__card-unit {
  margin-top: 4px;
  color: rgba(248, 242, 229, 0.5);
  font-size: 12px;
}

.hero__card p {
  margin-top: 12px;
  color: rgba(248, 242, 229, 0.6);
  font-size: 12px;
  line-height: 1.8;
}

.hero__card .el-button {
  margin-top: 16px;
  border-color: rgba(199, 161, 90, 0.55);
  color: var(--gold);
  background: transparent;
}

.hero__card-line {
  width: 100%;
  height: 1px;
  margin: 18px 0 10px;
  background: rgba(199, 161, 90, 0.25);
}

.hero__card small {
  color: rgba(248, 242, 229, 0.38);
  font-size: 10.5px;
}

.hero__scroll {
  position: absolute;
  bottom: 26px;
  left: 50%;
  z-index: 2;
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 8px;
  transform: translateX(-50%);
  color: rgba(248, 242, 229, 0.4);
  font-size: 10px;
  letter-spacing: 0.2em;
}

.hero__scroll i {
  width: 1px;
  height: 36px;
  background: linear-gradient(rgba(199, 161, 90, 0.6), transparent);
}

.stats-strip {
  border-bottom: 1px solid var(--border);
  background: var(--white);
}

.stats-strip__inner {
  display: grid;
  grid-template-columns: repeat(4, 1fr) 1.3fr;
  align-items: center;
  min-height: 116px;
}

.stat {
  padding: 20px 26px;
  border-right: 1px solid var(--border);
}

.stat:first-child {
  padding-left: 0;
}

.stat strong {
  display: block;
  color: var(--teal);
  font-family: var(--font-serif);
  font-size: 32px;
  font-weight: 500;
  line-height: 1.2;
}

.stat span {
  color: var(--muted);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.stat--note {
  border-right: 0;
}

.stat--note p {
  margin-top: 6px;
  color: var(--muted);
  font-size: 11.5px;
  line-height: 1.6;
}

.stat__tag {
  display: inline-block;
  padding: 1px 8px;
  border: 1px solid rgba(167, 123, 69, 0.4);
  color: var(--bronze);
  font-size: 10px;
  letter-spacing: 0.12em;
}

.exhibition-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
  min-height: 200px;
}

.steps {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1px;
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--border);
}

.step {
  position: relative;
  padding: 30px 26px 28px;
  background: var(--white);
}

.step__no {
  color: var(--gold);
  font-family: var(--font-serif);
  font-size: 26px;
  opacity: 0.9;
}

.step h3 {
  margin-top: 18px;
  font-size: 18px;
  letter-spacing: 0.05em;
}

.step p {
  margin-top: 8px;
  color: var(--muted);
  font-size: 12.5px;
  line-height: 1.8;
}

.notices-section {
  background: #f2ede2;
}

.notices-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(280px, 0.8fr);
  gap: 50px;
}

.notice-list {
  border-top: 1px solid rgba(19, 44, 42, 0.12);
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 6px;
  border-bottom: 1px solid rgba(19, 44, 42, 0.1);
  transition: background 0.2s ease, padding 0.2s ease;
}

.notice-item:hover {
  padding-left: 12px;
  background: rgba(255, 253, 248, 0.7);
}

.notice-item__date {
  display: flex;
  width: 54px;
  flex-shrink: 0;
  flex-direction: column;
  align-items: center;
  padding-right: 16px;
  border-right: 1px solid rgba(19, 44, 42, 0.12);
}

.notice-item__date strong {
  color: var(--teal);
  font-family: var(--font-serif);
  font-size: 26px;
  font-weight: 500;
  line-height: 1;
}

.notice-item__date small {
  margin-top: 4px;
  color: var(--muted);
  font-size: 9px;
}

.notice-item__body {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.notice-item__tag {
  color: var(--bronze);
  font-size: 10.5px;
  letter-spacing: 0.1em;
}

.notice-item__body strong {
  overflow: hidden;
  margin-top: 2px;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 16px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-item__body small {
  overflow: hidden;
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-item__arrow {
  margin-left: auto;
  color: var(--bronze);
}

.visit-card {
  position: relative;
  overflow: hidden;
  align-self: start;
  padding: 30px 28px;
  border: 1px solid rgba(199, 161, 90, 0.28);
  color: rgba(248, 242, 229, 0.75);
  background: linear-gradient(150deg, #0a3532, #0d4843);
}

.visit-card h3 {
  margin: 4px 0 20px;
  color: #fff9ec;
  font-size: 24px;
}

.visit-card .eyebrow {
  color: var(--gold);
}

.visit-card__row {
  padding: 14px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.visit-card__row span {
  display: block;
  color: rgba(248, 242, 229, 0.45);
  font-size: 10.5px;
  letter-spacing: 0.12em;
}

.visit-card__row strong {
  display: block;
  margin-top: 3px;
  color: #f8f2e5;
  font-family: var(--font-serif);
  font-size: 16px;
  font-weight: 500;
}

.visit-card__row small {
  display: block;
  margin-top: 2px;
  color: rgba(248, 242, 229, 0.45);
  font-size: 11px;
}

.visit-card__btn {
  width: 100%;
  margin-top: 20px;
}

.visit-card__mark {
  position: absolute;
  right: -6px;
  bottom: -32px;
  color: rgba(199, 161, 90, 0.08);
  font-family: var(--font-serif);
  font-size: 150px;
  line-height: 1;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.service {
  padding: 30px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--white);
}

.service__icon {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border: 1px solid rgba(167, 123, 69, 0.35);
  border-radius: 50%;
  color: var(--bronze);
  font-size: 20px;
}

.service h3 {
  margin-top: 18px;
  font-size: 19px;
}

.service p {
  margin-top: 8px;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.8;
}

.cta {
  padding: 70px 0;
  color: #f8f2e5;
  background:
    radial-gradient(circle at 82% 30%, rgba(199, 161, 90, 0.2), transparent 40%),
    #0a2b28;
}

.cta__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
}

.cta__eyebrow {
  color: var(--gold);
}

.cta h2 {
  color: #fff9ec;
  font-size: clamp(28px, 3vw, 38px);
  letter-spacing: 0.08em;
}

.cta p:last-child {
  margin-top: 10px;
  color: rgba(248, 242, 229, 0.55);
  font-size: 13px;
}

@media (max-width: 1080px) {
  .hero__card {
    display: none;
  }

  .stats-strip__inner {
    grid-template-columns: repeat(4, 1fr);
  }

  .stat--note {
    display: none;
  }

  .notices-grid {
    grid-template-columns: 1fr;
    gap: 30px;
  }
}

@media (max-width: 900px) {
  .hero,
  .hero__inner {
    min-height: 500px;
  }

  .hero__inner {
    align-items: flex-start;
    padding: 54px 0 54px;
  }

  .exhibition-grid,
  .service-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .steps {
    grid-template-columns: repeat(2, 1fr);
  }

  .cta__inner {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .hero__copy h1 {
    font-size: clamp(30px, 8.6vw, 40px);
    white-space: normal;
  }

  .hero__meta {
    display: none;
  }

  .stats-strip__inner {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat {
    padding: 16px;
  }

  .stat:nth-child(2n) {
    border-right: 0;
  }

  .stat strong {
    font-size: 26px;
  }

  .exhibition-grid,
  .service-grid,
  .steps {
    grid-template-columns: 1fr;
  }

  .notice-item__body small {
    display: none;
  }

  .hero__scroll {
    display: none;
  }
}
</style>