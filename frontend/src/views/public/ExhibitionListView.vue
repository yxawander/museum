<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { publicApi } from '@/api/public'
import type { Exhibition } from '@/types'
import PageBanner from '@/components/common/PageBanner.vue'
import ExhibitionCard from '@/components/common/ExhibitionCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { EXHIBITION_CATEGORIES } from '@/utils/constants'

const loading = ref(false)
const list = ref<Exhibition[]>([])
const total = ref(0)
const page = ref(1)
const size = 9
const keyword = ref('')
const category = ref('')

async function load() {
  loading.value = true
  try {
    const data = await publicApi.exhibitions({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      category: category.value || undefined,
    })
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function chooseCategory(value: string) {
  category.value = value
  page.value = 1
  void load()
}

function search() {
  page.value = 1
  void load()
}

function reset() {
  keyword.value = ''
  category.value = ''
  page.value = 1
  void load()
}

onMounted(load)
</script>

<template>
  <div>
    <PageBanner
      title="展览陈列"
      subtitle="从曾侯乙编钟到越王勾践剑，从楚文化到明代藩王珍藏，在文物中读懂荆楚与长江。"
    />

    <section class="section">
      <div class="container">
        <div class="filter-bar">
          <div class="filter-cats">
            <button
              type="button"
              class="cat-btn"
              :class="{ 'is-active': category === '' }"
              @click="chooseCategory('')"
            >
              全部展览
            </button>
            <button
              v-for="item in EXHIBITION_CATEGORIES"
              :key="item"
              type="button"
              class="cat-btn"
              :class="{ 'is-active': category === item }"
              @click="chooseCategory(item)"
            >
              {{ item }}
            </button>
          </div>
          <div class="filter-search">
            <el-input
              v-model="keyword"
              placeholder="搜索展览名称或关键词"
              clearable
              @keyup.enter="search"
              @clear="search"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="search">搜索</el-button>
          </div>
        </div>

        <div v-loading="loading" class="list-wrap">
          <div v-if="list.length" class="exhibition-grid">
            <ExhibitionCard v-for="item in list" :key="item.id" :exhibition="item" />
          </div>
          <EmptyState
            v-else-if="!loading"
            title="没有找到相关展览"
            description="试试更换分类或关键词，也可以查看全部展览。"
          >
            <el-button type="primary" plain class="reset-btn" @click="reset">查看全部展览</el-button>
          </EmptyState>
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
    </section>
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border);
}

.filter-cats {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.cat-btn {
  padding: 8px 18px;
  border: 1px solid transparent;
  border-radius: 2px;
  color: var(--muted);
  background: transparent;
  font-size: 13.5px;
  letter-spacing: 0.06em;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cat-btn:hover {
  color: var(--teal);
}

.cat-btn.is-active {
  border-color: rgba(167, 123, 69, 0.4);
  color: var(--teal);
  background: var(--white);
  box-shadow: var(--shadow-sm);
}

.filter-search {
  display: flex;
  width: min(360px, 100%);
  gap: 8px;
}

.exhibition-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
}

.list-wrap {
  min-height: 320px;
}

.reset-btn {
  margin-top: 18px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

@media (max-width: 900px) {
  .filter-bar {
    align-items: stretch;
    flex-direction: column;
    gap: 14px;
  }

  .filter-search {
    width: 100%;
  }

  .exhibition-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .exhibition-grid {
    grid-template-columns: 1fr;
  }
}
</style>