<template>
  <view class="content-state" :class="`content-state--${type}`">
    <view v-if="type === 'loading'" class="content-state__spinner"></view>
    <text class="content-state__message">{{ displayMessage }}</text>
    <button
      v-if="type === 'error'"
      class="content-state__retry"
      :loading="retrying"
      :disabled="retrying"
      @click="$emit('retry')"
    >
      {{ retrying ? '正在重试' : '重新加载' }}
    </button>
  </view>
</template>

<script>
export default {
  name: 'ContentState',
  props: {
    type: {
      type: String,
      default: 'empty'
    },
    message: {
      type: String,
      default: ''
    },
    retrying: {
      type: Boolean,
      default: false
    }
  },
  emits: ['retry'],
  computed: {
    displayMessage() {
      if (this.message) return this.message
      if (this.type === 'loading') return '正在加载内容…'
      if (this.type === 'error') return '内容加载失败，请稍后重试'
      return '暂无内容'
    }
  }
}
</script>

<style lang="scss" scoped>
.content-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 152rpx;
  padding: 28rpx 20rpx;
  text-align: center;
}

.content-state__spinner {
  width: 42rpx;
  height: 42rpx;
  margin-bottom: 20rpx;
  border: 5rpx solid rgba(100, 121, 110, 0.14);
  border-top-color: $ichip-color-nav-active;
  border-radius: 50%;
  animation: content-state-spin 0.8s linear infinite;
}

.content-state__message {
  color: $ichip-color-muted;
  font-size: 25rpx;
  line-height: 1.6;
}

.content-state__retry {
  height: 68rpx;
  margin-top: 22rpx;
  padding: 0 32rpx;
  border-radius: $ichip-radius-sm;
  background: $ichip-color-nav-active;
  color: #fff;
  font-size: 25rpx;
  line-height: 68rpx;
}

.content-state__retry[disabled] {
  opacity: 0.65;
}

@keyframes content-state-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
