<template>
  <div class="export-action">
    <div class="export-copy">
      <div class="export-title">{{ title }}</div>
      <div class="export-note">{{ description }}</div>
      <div class="export-meta">
        <span>当前结果: {{ total }}</span>
        <span v-if="lastExportAt">上次导出: {{ lastExportAt }}</span>
      </div>
    </div>

    <div class="export-buttons">
      <el-button :loading="loading" type="primary" @click="$emit('export', 'filtered')">
        导出当前筛选
      </el-button>
      <el-button :loading="loading" plain @click="$emit('export', 'all')">
        导出全部
      </el-button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '导出数据'
  },
  description: {
    type: String,
    default: '支持导出当前筛选结果或全部数据。'
  },
  total: {
    type: Number,
    default: 0
  },
  loading: {
    type: Boolean,
    default: false
  },
  lastExportAt: {
    type: String,
    default: ''
  }
})

defineEmits(['export'])
</script>
