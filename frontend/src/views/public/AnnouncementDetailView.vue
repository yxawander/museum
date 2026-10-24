<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { publicApi } from '@/api/public'
import type { Announcement } from '@/types'
import { ANNOUNCEMENT_CATEGORIES } from '@/utils/constants'
import { formatDate } from '@/utils/format'

const route = useRoute()
const loading = ref(true)
const announcement = ref<Announcement | null>(null)
const recent = ref<Announcement[]>([])

const paragraphs = computed(() =>
  (announcement.value?.content ?? '').split(/\n+/).filter((line) => line.trim().length > 0),
)

onMounted(async () => {
  try {
    const id = route.params.id as string
    const [detail, list] = await Promise.all([
      publicApi.announcement(id),
      publicApi.announcements({ page: 1, size: 5 }),
    ])
    announcement.value = detail
    recent.value = list.records.filter((item) => String(item.id) !== id)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="article-page">
    <template v-if="announcement">
      <section class="article-head">
        <div class="container article-head__inner">
          <div class="breadcrumb">
            <router-link to="/">首页</router-link>
            <span>/</span>
            <router-link to="/announcements">公告资讯</router-link>
            <span>/</span>
            <span>正文</span>
          </div>
          <span class="article-head__cat">{{ ANNOUNCEMENT_CATEGORIES[announcement.category] }}</span>
          <h1>{{ announcement.title }}</h1>
          <div class="article-head__meta">
            <span><el-icon><Calendar /></el-icon>{{ formatDate(announcement.publishTime) }}</span>
            <span><el-icon><View /></el-icon>{{ announcement.viewCount }} 次阅读</span>
            <span>湖北省博物馆</span>
          </div>
        </div>
      </section>

      <div class="container article-layout">
        <article class="article">
          <p v-if="announcement.summary" class="article__summary">{{ announcement.summary }}</p>
          <div class="article__body">
            <p v-for="(paragraph, index) in paragraphs" :key="index">{{ paragraph }}</p>
          </div>
          <div class="article__foot">
            <router-link to="/announcements" class="back-link">
              <el-icon><ArrowLeft /></el-icon> 返回公告列表
            </router-link>
            <span>湖北省博物馆 · 宣传教育部</span>
          </div>
        </article>

        <aside class="article-side">
          <div class="side-box">
            <h3>近期公告</h3>
            <router-link
              v-for="item in recent"
              :key="item.id"
              :to="`/announcements/${item.id}`"
              class="recent-item"
            >
              <span>{{ formatDate(item.publishTime).slice(5) }}</span>
              <strong>{{ item.title }}</strong>
            </router-link>
          </div>
          <div class="side-box side-box--dark">
            <span class="side-box__eyebrow">NEED HELP?</span>
            <h3>预约咨询</h3>
            <p>如遇预约问题，请拨打服务电话或到馆咨询。</p>
            <strong>027-86794127</strong>
            <small>服务时间 09:00—17:00</small>
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>

<style scoped>
.article-page {
  min-height: 70vh;
  padding-bottom: 70px;
}

.article-head {
  padding: 56px 0 46px;
  color: #f8f2e5;
  background:
    radial-gradient(circle at 82% 18%, rgba(199, 161, 90, 0.2), transparent 42%),
    linear-gradient(120deg, #0a3532, #0e4b47);
}

.article-head__inner {
  max-width: 900px;
}

.article-head .breadcrumb {
  margin-bottom: 22px;
}

.article-head__cat {
  display: inline-block;
  padding: 3px 11px;
  border: 1px solid rgba(199, 161, 90, 0.55);
  color: var(--gold);
  font-size: 10.5px;
  letter-spacing: 0.12em;
}

.article-head h1 {
  max-width: 820px;
  margin-top: 16px;
  color: #fff9ec;
  font-size: clamp(28px, 4vw, 42px);
  font-weight: 500;
  letter-spacing: 0.06em;
  line-height: 1.45;
}

.article-head__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 18px;
  color: rgba(248, 242, 229, 0.5);
  font-size: 12px;
}

.article-head__meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.article-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 50px;
  padding-top: 42px;
}

.article {
  padding: 38px 42px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--white);
  box-shadow: var(--shadow-sm);
}

.article__summary {
  padding: 16px 20px;
  border-left: 3px solid var(--gold);
  color: #5d4a2e;
  background: #f7f0e1;
  font-size: 14px;
  line-height: 1.8;
}

.article__body {
  margin-top: 26px;
}

.article__body p {
  margin-bottom: 16px;
  color: #3d4744;
  font-size: 15px;
  line-height: 2.05;
  text-align: justify;
}

.article__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-top: 36px;
  padding-top: 20px;
  border-top: 1px solid var(--border);
  color: var(--muted);
  font-size: 11.5px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--teal);
  font-size: 13px;
}

.article-side {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.side-box {
  padding: 24px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
}

.side-box h3 {
  margin-bottom: 14px;
  font-size: 19px;
}

.recent-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 0;
  border-top: 1px dashed rgba(19, 44, 42, 0.12);
}

.recent-item span {
  flex-shrink: 0;
  color: var(--bronze);
  font-size: 10.5px;
}

.recent-item strong {
  display: -webkit-box;
  overflow: hidden;
  color: #3d4744;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.recent-item:hover strong {
  color: var(--teal);
}

.side-box--dark {
  color: rgba(248, 242, 229, 0.62);
  background: #0d3d39;
}

.side-box--dark h3 {
  color: #fff9ec;
}

.side-box--dark p {
  color: rgba(248, 242, 229, 0.55);
  font-size: 12px;
}

.side-box--dark strong {
  display: block;
  margin-top: 12px;
  color: var(--gold);
  font-family: var(--font-serif);
  font-size: 24px;
  font-weight: 500;
}

.side-box--dark small {
  color: rgba(248, 242, 229, 0.4);
  font-size: 10px;
}

.side-box__eyebrow {
  color: var(--gold);
  font-size: 9.5px;
  letter-spacing: 0.18em;
}

@media (max-width: 960px) {
  .article-layout {
    grid-template-columns: 1fr;
  }

  .article-side {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .article {
    padding: 24px 18px;
  }

  .article-side {
    grid-template-columns: 1fr;
  }

  .article__foot {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>