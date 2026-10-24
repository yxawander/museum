<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    subtitle?: string
    eyebrow?: string
  }>(),
  { subtitle: '', eyebrow: 'HUBEI PROVINCIAL MUSEUM' },
)
</script>

<template>
  <section class="page-banner">
    <div class="container">
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span>/</span>
        <span>{{ title }}</span>
      </div>
      <p class="eyebrow banner-eyebrow">{{ eyebrow }}</p>
      <h1>{{ title }}</h1>
      <p v-if="subtitle">{{ subtitle }}</p>
      <slot />
    </div>
  </section>
</template>

<style scoped>
.banner-eyebrow {
  color: var(--gold);
  opacity: 0.9;
}

.banner-eyebrow::before {
  background: rgba(199, 161, 90, 0.6);
}
</style>