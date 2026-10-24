<script setup lang="ts">
const year = new Date().getFullYear()

const quickLinks = [
  { label: '展览陈列', path: '/exhibitions' },
  { label: '预约参观', path: '/booking' },
  { label: '公告资讯', path: '/announcements' },
  { label: '参观指南', path: '/about' },
]
</script>

<template>
  <footer class="site-footer">
    <div class="container footer-grid">
      <div class="footer-brand">
        <div class="footer-brand__head">
          <img src="/logo.svg" alt="湖北省博物馆标识" class="footer-brand__logo">
          <div>
            <strong>湖北省博物馆</strong>
            <span>HUBEI PROVINCIAL MUSEUM</span>
          </div>
        </div>
        <p>
          荆楚文化的重要收藏、研究、展示与教育中心。让文物活起来，让历史走进生活。
        </p>
        <div class="footer-seal">楚</div>
      </div>

      <div class="footer-col">
        <h4>快速导航</h4>
        <router-link v-for="item in quickLinks" :key="item.path" :to="item.path" class="footer-link">
          {{ item.label }}
        </router-link>
      </div>

      <div class="footer-col">
        <h4>参观信息</h4>
        <p>开放时间：周二至周日 09:00—17:00</p>
        <p>停止入馆：16:00（周一闭馆）</p>
        <p>地址：武汉市武昌区东湖路160号</p>
        <p>咨询电话：027-86794127</p>
      </div>

      <div class="footer-col">
        <h4>交通指引</h4>
        <p>地铁 8 号线省博湖北日报站 A 口</p>
        <p>公交 14 / 402 / 411 / 552 / 578 路</p>
        <p>省博物馆站下车，步行约 5 分钟</p>
        <p class="footer-note">建议绿色出行，错峰参观</p>
      </div>
    </div>

    <div class="container footer-bottom">
      <span>© {{ year }} 湖北省博物馆参观预约系统 · 课程设计作品</span>
      <span>鄂ICP备 00000000 号 · 网站安全检测</span>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  padding: 64px 0 24px;
  color: rgba(247, 244, 236, 0.72);
  background:
    radial-gradient(circle at 80% 0%, rgba(199, 161, 90, 0.16), transparent 40%),
    #0a2b28;
}

.footer-grid {
  display: grid;
  grid-template-columns: 1.5fr 0.8fr 1fr 1fr;
  gap: 46px;
  padding-bottom: 42px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-brand__head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.footer-brand__logo {
  width: 46px;
  height: 46px;
}

.footer-brand__head strong {
  display: block;
  color: #f7f1e3;
  font-family: var(--font-serif);
  font-size: 20px;
  letter-spacing: 0.1em;
}

.footer-brand__head span {
  display: block;
  margin-top: 2px;
  color: rgba(247, 241, 227, 0.45);
  font-size: 9px;
  letter-spacing: 0.12em;
}

.footer-brand p {
  max-width: 320px;
  margin-top: 18px;
  font-size: 13.5px;
  line-height: 1.9;
}

.footer-seal {
  display: grid;
  width: 44px;
  height: 44px;
  margin-top: 20px;
  place-items: center;
  border: 1px solid rgba(199, 161, 90, 0.5);
  color: var(--gold);
  font-family: var(--font-serif);
  font-size: 20px;
  opacity: 0.8;
}

.footer-col h4 {
  margin-bottom: 16px;
  color: #f7f1e3;
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.16em;
}

.footer-col p,
.footer-link {
  display: block;
  margin-bottom: 9px;
  color: rgba(247, 244, 236, 0.62);
  font-size: 13px;
  line-height: 1.8;
}

.footer-link {
  transition: color 0.2s ease, transform 0.2s ease;
}

.footer-link:hover {
  color: var(--gold);
  transform: translateX(3px);
}

.footer-note {
  color: var(--gold) !important;
  opacity: 0.8;
}

.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding-top: 20px;
  color: rgba(247, 244, 236, 0.38);
  font-size: 12px;
}

@media (max-width: 960px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr;
    gap: 34px;
  }
}

@media (max-width: 600px) {
  .site-footer {
    padding-top: 44px;
  }

  .footer-grid {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .footer-bottom {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }
}
</style>