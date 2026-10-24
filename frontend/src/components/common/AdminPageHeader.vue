<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <div class="admin-page-head">
    <div>
      <h3>{{ title }}</h3>
      <p v-if="description">{{ description }}</p>
    </div>
    <div class="admin-page-head__actions">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.admin-page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
}

.admin-page-head h3 {
  font-size: 18px;
  letter-spacing: 0.06em;
}

.admin-page-head p {
  margin-top: 4px;
  color: var(--muted);
  font-size: 12.5px;
}

.admin-page-head__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 640px) {
  .admin-page-head {
    flex-direction: column;
  }
}
</style>