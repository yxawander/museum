<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    description?: string
  }>(),
  { title: '暂无数据', description: '换个条件试试，或稍后再来看看。' },
)
</script>

<template>
  <div class="empty-state">
    <div class="empty-state__icon">
      <el-icon><Box /></el-icon>
    </div>
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
    <slot />
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  align-items: center;
  flex-direction: column;
  padding: 54px 20px;
  text-align: center;
}

.empty-state__icon {
  display: grid;
  width: 62px;
  height: 62px;
  place-items: center;
  border: 1px dashed rgba(167, 123, 69, 0.5);
  border-radius: 50%;
  color: var(--bronze);
  font-size: 26px;
}

.empty-state h3 {
  margin-top: 16px;
  font-size: 17px;
  letter-spacing: 0.08em;
}

.empty-state p {
  margin-top: 6px;
  color: var(--muted);
  font-size: 13px;
}
</style>