<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { publicApi } from '@/api/public'
import type { Announcement } from '@/types'
import PageBanner from '@/components/common/PageBanner.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { ANNOUNCEMENT_CATEGORIES } from '@/utils/constants'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const list = ref<Announcement[]>([])
const total = ref(0)
const page = ref(1)
const size = 8
const category = ref('')
const keyword = ref('')

const tabs = [
  { label: '全部', value: '' },
  { label: '通知公告', value: 'NOTICE' },
  { label: '馆内新闻', value: 'NEWS' },
  { label: '社教活动', value: 'ACTIVITY' },
]

async function load() {
  loading.value = true
  try {
    const data = await publicApi.announcements({
      page: page.value,
      size,
      category: category.value || undefined,
      keyword: keyword.value || undefined,
    })
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function choose(value: string) {
  category.value = value
  page.value = 1
  void load()
}

function search() {
  page.value = 1
  void load()
}

onMounted(load)
</script>

<template>
  <div>
    <PageBanner title="公告资讯" subtitle="展览动态、开放通知、社教活动与参观须知，第一时间了解博物馆最新信息。" />

    <section class="section">
      <div class="container notice-layout">
        <div class="notice-main">
          <div class="tabs">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              type="button"
              class="tab"
              :class="{ 'is-active': category === tab.value }"
              @click="choose(tab.value)"
            >
              {{ tab.label }}
            </button>
            <el-input
              v-model="keyword"
              class="search"
              placeholder="搜索公告标题"
              clearable
              @keyup.enter="search"
              @clear="search"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>

          <div v-loading="loading" class="notice-list">
            <router-link
              v-for="item in list"
              :key="item.id"
              :to="`/announcements/${item.id}`"
              class="notice-item"
            >
              <span class="notice-item__date">
                <strong>{{ item.publishTime ? item.publishTime.slice(8, 10) : '—' }}</strong>
                <small>{{ item.publishTime ? item.publishTime.slice(0, 7) : '' }}</small>
              </span>
              <span class="notice-item__body">
                <span class="notice-item__meta">
                  <span class="notice-item__tag">{{ ANNOUNCEMENT_CATEGORIES[item.category] }}</span>
                  <span v-if="item.isTop === 1" class="notice-item__top">置顶</span>
                  <span class="notice-item__views">{{ item.viewCount }} 次阅读</span>
                </span>
                <strong>{{ item.title }}</strong>
                <small>{{ item.summary }}</small>
              </span>
              <el-icon class="notice-item__arrow"><Right /></el-icon>
            </router-link>

            <EmptyState
              v-if="!loading && list.length === 0"
              title="暂无相关公告"
              description="可以切换分类，或稍后再来看看。"
            />
          </div>

          <div v-if="total > size" class="pager">
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

        <aside class="notice-side">
          <div class="side-box">
            <span class="side-box__eyebrow">VISIT NOTICE</span>
            <h3>参观须知</h3>
            <p>开放时间：周二至周日 09:00—17:00</p>
            <p>停止入馆：16:00（周一闭馆，法定节假日除外）</p>
            <p>预约方式：提前 7 天实名预约，单次最多 5 人</p>
            <p>入馆方式：携带身份证件，按预约时段核验入馆</p>
            <el-button type="primary" plain class="side-box__btn" @click="$router.push('/about')">
              查看完整指南
            </el-button>
          </div>

          <div class="side-box side-box--dark">
            <span class="side-box__eyebrow">CONTACT</span>
            <h3>联系我们</h3>
            <p>咨询电话：027-86794127</p>
            <p>服务时间：09:00—17:00</p>
            <p>地址：武汉市武昌区东湖路160号</p>
            <p>邮箱：service@hbmuseum.cn</p>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
.notice-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 46px;
}

.tabs {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 22px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}

.tab {
  padding: 7px 16px;
  border: 1px solid transparent;
  border-radius: 2px;
  color: var(--muted);
  background: transparent;
  font-size: 13.5px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab:hover {
  color: var(--teal);
}

.tab.is-active {
  border-color: rgba(167, 123, 69, 0.4);
  color: var(--teal);
  background: var(--white);
}

.search {
  width: 220px;
  margin-left: auto;
}

.notice-list {
  border-top: 1px solid var(--border);
  min-height: 320px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 22px;
  padding: 24px 6px;
  border-bottom: 1px solid var(--border);
  transition: padding 0.2s ease, background 0.2s ease;
}

.notice-item:hover {
  padding-left: 14px;
  background: rgba(255, 253, 248, 0.75);
}

.notice-item__date {
  display: flex;
  width: 58px;
  flex-shrink: 0;
  flex-direction: column;
  align-items: center;
  padding-right: 18px;
  border-right: 1px solid var(--border);
}

.notice-item__date strong {
  color: var(--teal);
  font-family: var(--font-serif);
  font-size: 30px;
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

.notice-item__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.notice-item__tag {
  color: var(--bronze);
  font-size: 10.5px;
  letter-spacing: 0.1em;
}

.notice-item__top {
  padding: 0 5px;
  border: 1px solid rgba(169, 68, 66, 0.4);
  color: #a94442;
  font-size: 9px;
}

.notice-item__views {
  color: var(--muted);
  font-size: 10px;
}

.notice-item__body strong {
  overflow: hidden;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 18px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-item__body small {
  display: -webkit-box;
  overflow: hidden;
  margin-top: 4px;
  color: var(--muted);
  font-size: 12.5px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.notice-item__arrow {
  margin-left: auto;
  color: var(--bronze);
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 34px;
}

.notice-side {
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

.side-box__eyebrow {
  color: var(--bronze);
  font-size: 9.5px;
  letter-spacing: 0.18em;
}

.side-box h3 {
  margin: 5px 0 12px;
  font-size: 20px;
}

.side-box p {
  margin-bottom: 7px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.7;
}

.side-box__btn {
  width: 100%;
  margin-top: 12px;
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
}

.side-box--dark .side-box__eyebrow {
  color: var(--gold);
}

@media (max-width: 960px) {
  .notice-layout {
    grid-template-columns: 1fr;
  }

  .notice-side {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .search {
    width: 100%;
    margin-left: 0;
  }

  .notice-item__date {
    width: 48px;
    padding-right: 12px;
  }

  .notice-item__body small {
    display: none;
  }

  .notice-side {
    grid-template-columns: 1fr;
  }
}
</style>