<script setup lang="ts">
import PageBanner from '@/components/common/PageBanner.vue'

const venues = [
  { name: '综合馆', desc: '曾侯乙编钟、越王勾践剑、梁庄王珍藏等常设展厅', floor: '1F—3F' },
  { name: '楚文化馆', desc: '楚国八百年、荆楚简牍与楚式青铜器专题陈列', floor: '1F—2F' },
  { name: '编钟馆', desc: '曾侯乙编钟演奏厅与礼乐文明沉浸式展示', floor: '1F' },
  { name: '临时展厅', desc: '引进与原创特展，展期与票价以公告为准', floor: '1F' },
]

function openMap() {
  window.open('https://map.baidu.com/search/湖北省博物馆', '_blank')
}

const faqs = [
  {
    q: '参观需要提前预约吗？',
    a: '需要。观众可通过本系统提前 7 天实名预约，选择参观日期与入馆时段。每个账号单次最多预约 5 人。',
  },
  {
    q: '预约后可以取消吗？',
    a: '可以。在参观开始前，可进入“我的预约”取消订单，名额会实时释放。参观开始后不可取消。',
  },
  {
    q: '入馆需要携带什么证件？',
    a: '请携带预约时填写的本人有效身份证件，按预约时段到馆，经安检与核验后入馆。',
  },
  {
    q: '迟到了还能入馆吗？',
    a: '请尽量按预约时段到馆。超过预约时段结束时间，预约将自动失效。如遇特殊情况，请联系现场工作人员。',
  },
  {
    q: '馆内可以拍照吗？',
    a: '大部分展厅允许非商业性拍照，请关闭闪光灯并遵守现场提示。部分特殊展品禁止拍摄，以展厅标识为准。',
  },
]
</script>

<template>
  <div>
    <PageBanner
      title="参观指南"
      subtitle="开放时间、预约流程、交通路线与馆内服务，出发前请先读一读这份指南。"
    />

    <section class="section">
      <div class="container intro-grid">
        <div class="intro-media">
          <img src="/images/museum.svg" alt="湖北省博物馆建筑意象">
          <div class="intro-media__badge">
            <strong>1953</strong>
            <span>建馆年份</span>
          </div>
        </div>
        <div class="intro-copy">
          <p class="eyebrow">ABOUT THE MUSEUM</p>
          <h2>收藏荆楚，讲述长江</h2>
          <p>
            湖北省博物馆坐落于武汉东湖之滨，是湖北省规模最大的综合性博物馆，也是国家一级博物馆。
            馆藏文物二十余万件（套），以曾侯乙编钟、越王勾践剑、郧县人头骨化石、元青花四爱图梅瓶等最为著名。
          </p>
          <p>
            博物馆以“荆楚文化”为核心，设有综合馆、楚文化馆、编钟馆与临时展厅，通过常设陈列、
            专题特展、编钟演奏与社教活动，让观众在文物与当代生活之间建立连接。
          </p>
          <div class="intro-stats">
            <div><strong>24万+</strong><span>馆藏文物（套）</span></div>
            <div><strong>6</strong><span>常设与特展</span></div>
            <div><strong>65</strong><span>曾侯乙编钟件数</span></div>
          </div>
        </div>
      </div>
    </section>

    <section class="visit-band">
      <div class="container visit-band__inner">
        <div class="visit-item">
          <el-icon><Clock /></el-icon>
          <div>
            <span>开放时间</span>
            <strong>09:00—17:00</strong>
            <small>16:00 停止入馆，周一闭馆</small>
          </div>
        </div>
        <div class="visit-item">
          <el-icon><Location /></el-icon>
          <div>
            <span>馆址</span>
            <strong>武汉市武昌区东湖路160号</strong>
            <small>东湖之滨，省博湖北日报站旁</small>
          </div>
        </div>
        <div class="visit-item">
          <el-icon><Phone /></el-icon>
          <div>
            <span>咨询电话</span>
            <strong>027-86794127</strong>
            <small>服务时间 09:00—17:00</small>
          </div>
        </div>
      </div>
    </section>

    <section class="section section--paper">
      <div class="container">
        <div class="section-head">
          <div class="section-head__main">
            <p class="eyebrow">FLOOR GUIDE</p>
            <h2 class="section-title">场馆分布</h2>
            <p class="section-desc">建议从综合馆开始，预留 2—3 小时，慢慢看。</p>
          </div>
        </div>
        <div class="venue-grid">
          <div v-for="(venue, index) in venues" :key="venue.name" class="venue-card">
            <span class="venue-card__no">0{{ index + 1 }}</span>
            <div>
              <h3>{{ venue.name }}</h3>
              <p>{{ venue.desc }}</p>
            </div>
            <span class="venue-card__floor">{{ venue.floor }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container transport-grid">
        <div class="transport-main">
          <p class="eyebrow">GETTING HERE</p>
          <h2 class="section-title">交通路线</h2>
          <div class="transport-list">
            <div class="transport-item">
              <span class="transport-item__icon"><el-icon><Van /></el-icon></span>
              <div>
                <h3>地铁</h3>
                <p>乘坐地铁 8 号线至“省博湖北日报站”，从 A 出口出站，步行约 500 米即到。</p>
              </div>
            </div>
            <div class="transport-item">
              <span class="transport-item__icon"><el-icon><Van /></el-icon></span>
              <div>
                <h3>公交</h3>
                <p>14 路、402 路、411 路、552 路、578 路、701 路、709 路、810 路至“省博物馆站”下车。</p>
              </div>
            </div>
            <div class="transport-item">
              <span class="transport-item__icon"><el-icon><Guide /></el-icon></span>
              <div>
                <h3>自驾</h3>
                <p>导航至“湖北省博物馆停车场”。节假日车位紧张，建议优先选择公共交通。</p>
              </div>
            </div>
            <div class="transport-item">
              <span class="transport-item__icon"><el-icon><Place /></el-icon></span>
              <div>
                <h3>火车站 / 机场</h3>
                <p>武汉站、武昌站可乘地铁 4 号线转 8 号线；天河机场乘地铁 2 号线转 8 号线。</p>
              </div>
            </div>
          </div>

          <div class="entry-flow">
            <h3>入馆流程</h3>
            <div class="entry-flow__steps">
              <span>出示预约码</span><i />
              <span>安全检查</span><i />
              <span>身份证核验</span><i />
              <span>入馆参观</span>
            </div>
          </div>
        </div>

        <aside class="map-card">
          <div class="map-card__canvas">
            <div class="map-road map-road--1" />
            <div class="map-road map-road--2" />
            <div class="map-lake" />
            <div class="map-pin">
              <el-icon><LocationFilled /></el-icon>
              <strong>湖北省博物馆</strong>
            </div>
            <span class="map-label map-label--lake">东湖</span>
            <span class="map-label map-label--road">东湖路</span>
            <span class="map-label map-label--metro">8 号线</span>
          </div>
          <div class="map-card__foot">
            <div>
              <span>地址</span>
              <strong>武汉市武昌区东湖路160号</strong>
            </div>
            <el-button text type="primary" @click="openMap">
              打开地图
            </el-button>
          </div>
        </aside>
      </div>
    </section>

    <section class="section faq-section">
      <div class="container faq-grid">
        <div>
          <p class="eyebrow">FAQ</p>
          <h2 class="section-title">常见问题</h2>
          <p class="section-desc">如果没有找到答案，欢迎拨打 027-86794127 咨询。</p>
        </div>
        <el-collapse class="faq-collapse">
          <el-collapse-item v-for="(item, index) in faqs" :key="index" :title="item.q" :name="index">
            <p>{{ item.a }}</p>
          </el-collapse-item>
        </el-collapse>
      </div>
    </section>
  </div>
</template>

<style scoped>
.intro-grid {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 56px;
  align-items: center;
}

.intro-media {
  position: relative;
}

.intro-media img {
  width: 100%;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: var(--shadow-md);
}

.intro-media__badge {
  position: absolute;
  right: -18px;
  bottom: -18px;
  display: flex;
  width: 100px;
  height: 100px;
  align-items: center;
  flex-direction: column;
  justify-content: center;
  border: 1px solid rgba(199, 161, 90, 0.5);
  color: #f8f2e5;
  background: #0d3d39;
  box-shadow: var(--shadow-md);
}

.intro-media__badge strong {
  font-family: var(--font-serif);
  font-size: 28px;
  font-weight: 500;
}

.intro-media__badge span {
  color: rgba(248, 242, 229, 0.55);
  font-size: 10px;
  letter-spacing: 0.1em;
}

.intro-copy h2 {
  margin-top: 4px;
  font-size: clamp(28px, 3vw, 38px);
  letter-spacing: 0.08em;
}

.intro-copy p {
  margin-top: 16px;
  color: #4d5754;
  font-size: 14.5px;
  line-height: 2;
  text-align: justify;
}

.intro-stats {
  display: flex;
  gap: 36px;
  margin-top: 28px;
  padding-top: 22px;
  border-top: 1px solid var(--border);
}

.intro-stats strong {
  display: block;
  color: var(--teal);
  font-family: var(--font-serif);
  font-size: 28px;
  font-weight: 500;
}

.intro-stats span {
  color: var(--muted);
  font-size: 11px;
}

.visit-band {
  padding: 0;
  color: #f8f2e5;
  background: #0a2b28;
}

.visit-band__inner {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
}

.visit-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 30px 32px;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.visit-item:last-child {
  border-right: 0;
}

.visit-item .el-icon {
  margin-top: 4px;
  color: var(--gold);
  font-size: 20px;
}

.visit-item span {
  display: block;
  color: rgba(248, 242, 229, 0.45);
  font-size: 10px;
  letter-spacing: 0.12em;
}

.visit-item strong {
  display: block;
  margin-top: 4px;
  color: #fff9ec;
  font-family: var(--font-serif);
  font-size: 18px;
  font-weight: 500;
}

.visit-item small {
  display: block;
  margin-top: 2px;
  color: rgba(248, 242, 229, 0.45);
  font-size: 11px;
}

.venue-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.venue-card {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  padding: 24px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--white);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.venue-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-sm);
}

.venue-card__no {
  color: var(--gold);
  font-family: var(--font-serif);
  font-size: 24px;
}

.venue-card h3 {
  font-size: 19px;
}

.venue-card p {
  margin-top: 5px;
  color: var(--muted);
  font-size: 12.5px;
  line-height: 1.7;
}

.venue-card__floor {
  margin-left: auto;
  padding: 2px 8px;
  border: 1px solid rgba(167, 123, 69, 0.35);
  color: var(--bronze);
  font-size: 10px;
  white-space: nowrap;
}

.transport-grid {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 46px;
}

.transport-list {
  margin-top: 20px;
}

.transport-item {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}

.transport-item__icon {
  display: grid;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  place-items: center;
  border: 1px solid rgba(167, 123, 69, 0.3);
  border-radius: 50%;
  color: var(--bronze);
}

.transport-item h3 {
  font-size: 16px;
}

.transport-item p {
  margin-top: 4px;
  color: var(--muted);
  font-size: 12.5px;
  line-height: 1.8;
}

.entry-flow {
  margin-top: 28px;
  padding: 20px 22px;
  border-left: 3px solid var(--gold);
  background: #f5eddc;
}

.entry-flow h3 {
  font-size: 15px;
  letter-spacing: 0.08em;
}

.entry-flow__steps {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
  color: #6b5738;
  font-size: 12px;
}

.entry-flow__steps i {
  width: 18px;
  height: 1px;
  background: rgba(167, 123, 69, 0.5);
}

.map-card {
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--white);
  box-shadow: var(--shadow-sm);
}

.map-card__canvas {
  position: relative;
  overflow: hidden;
  height: 320px;
  background:
    linear-gradient(90deg, rgba(19, 44, 42, 0.04) 1px, transparent 1px),
    linear-gradient(rgba(19, 44, 42, 0.04) 1px, transparent 1px),
    #eef0e6;
  background-size: 32px 32px;
}

.map-road {
  position: absolute;
  background: #d6d0bf;
}

.map-road--1 {
  top: 38%;
  left: -10%;
  width: 120%;
  height: 12px;
  transform: rotate(-8deg);
}

.map-road--2 {
  top: -10%;
  left: 60%;
  width: 10px;
  height: 120%;
  transform: rotate(6deg);
}

.map-lake {
  position: absolute;
  right: -40px;
  bottom: -50px;
  width: 260px;
  height: 210px;
  border-radius: 50% 40% 45% 55%;
  background: #bcd8d0;
  opacity: 0.85;
}

.map-pin {
  position: absolute;
  top: 40%;
  left: 48%;
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 4px;
  color: #a94442;
  transform: translate(-50%, -50%);
}

.map-pin .el-icon {
  font-size: 30px;
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.2));
}

.map-pin strong {
  padding: 3px 8px;
  border-radius: 3px;
  color: #fff;
  background: #a94442;
  font-size: 10px;
  white-space: nowrap;
}

.map-label {
  position: absolute;
  color: #7b8a86;
  font-size: 10px;
  letter-spacing: 0.08em;
}

.map-label--lake {
  right: 60px;
  bottom: 40px;
  color: #5d8c82;
  font-size: 13px;
}

.map-label--road {
  top: 30%;
  left: 10%;
  transform: rotate(-8deg);
}

.map-label--metro {
  top: 12%;
  left: 63%;
  color: #2f7d5c;
  transform: rotate(6deg);
}

.map-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
}

.map-card__foot span {
  display: block;
  color: var(--muted);
  font-size: 10px;
}

.map-card__foot strong {
  display: block;
  margin-top: 2px;
  color: var(--ink);
  font-size: 12.5px;
  font-weight: 500;
}

.faq-section {
  background: #f2ede2;
}

.faq-grid {
  display: grid;
  grid-template-columns: 0.7fr 1.3fr;
  gap: 50px;
}

.faq-collapse {
  border-top: 1px solid var(--border);
}

.faq-collapse :deep(.el-collapse-item__header) {
  height: auto;
  padding: 16px 0;
  color: var(--ink);
  background: transparent;
  font-family: var(--font-serif);
  font-size: 15px;
  line-height: 1.6;
}

.faq-collapse :deep(.el-collapse-item__wrap) {
  background: transparent;
}

.faq-collapse :deep(.el-collapse-item__content) {
  color: var(--muted);
  font-size: 13px;
  line-height: 1.9;
}

@media (max-width: 960px) {
  .intro-grid,
  .transport-grid,
  .faq-grid {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .intro-media__badge {
    right: 14px;
    bottom: 14px;
  }

  .visit-band__inner {
    grid-template-columns: 1fr;
  }

  .visit-item {
    border-right: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
}

@media (max-width: 640px) {
  .venue-grid {
    grid-template-columns: 1fr;
  }

  .intro-stats {
    gap: 20px;
  }

  .entry-flow__steps {
    gap: 6px;
  }
}
</style>