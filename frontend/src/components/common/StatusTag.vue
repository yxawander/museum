<script setup lang="ts">
import { computed } from 'vue'
import { EXHIBITION_STATUS, RESERVATION_STATUS } from '@/utils/constants'

const props = withDefaults(
  defineProps<{
    status: string | number
    kind?: 'reservation' | 'exhibition' | 'feedback' | 'announcement'
  }>(),
  { kind: 'reservation' },
)

const tag = computed(() => {
  if (props.kind === 'reservation') {
    const item = RESERVATION_STATUS[String(props.status)]
    return { text: item?.text ?? String(props.status), type: item?.type ?? 'info' }
  }
  if (props.kind === 'exhibition') {
    const item = EXHIBITION_STATUS[String(props.status)]
    return { text: item?.text ?? String(props.status), type: item?.type ?? 'info' }
  }
  if (props.kind === 'feedback') {
    return String(props.status) === 'REPLIED'
      ? { text: '已回复', type: 'success' as const }
      : { text: '待处理', type: 'warning' as const }
  }
  return Number(props.status) === 1
    ? { text: '已发布', type: 'success' as const }
    : { text: '草稿', type: 'info' as const }
})
</script>

<template>
  <el-tag :type="tag.type" effect="light" round size="small">
    {{ tag.text }}
  </el-tag>
</template>