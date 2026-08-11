<template>
  <view class="page-header" :class="{ 'page-header--quiet': variant === 'quiet' }">
    <view class="page-header__safe"></view>
    <view class="page-header__bar">
      <view class="page-header__action" @tap="handleBack">
        <text class="page-header__back-icon">{{ variant === 'quiet' ? '←' : '返回' }}</text>
      </view>
      <text class="page-header__title">{{ title }}</text>
      <view class="page-header__placeholder"></view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'PageHeader',
  props: {
    title: {
      type: String,
      default: '页面'
    },
    variant: {
      type: String,
      default: 'default'
    }
  },
  methods: {
    handleBack() {
      const pages = getCurrentPages()
      if (pages && pages.length > 1) {
        uni.navigateBack()
        return
      }
      uni.switchTab({
        url: '/pages/index/index'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.page-header {
  position: relative;
  z-index: 20;
  padding: 0 24rpx 12rpx;
}

.page-header__safe {
  padding-top: calc(12rpx + env(safe-area-inset-top));
}

.page-header__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 84rpx;
}

.page-header__action,
.page-header__placeholder {
  width: 84rpx;
  height: 84rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-header__action {
  border-radius: 50%;
  background: rgba(255, 251, 246, 0.9);
  box-shadow: 0 8rpx 20rpx rgba(77, 47, 31, 0.08);
}

.page-header__back-icon {
  font-size: 24rpx;
  color: #3c2a22;
  line-height: 1;
  font-weight: 600;
}

.page-header__title {
  flex: 1;
  text-align: center;
  padding: 0 16rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #2f1f18;
}

.page-header--quiet {
  padding-bottom: 8rpx;
  border-bottom: 1rpx solid $ichip-color-line;
  background: $ichip-color-surface;
}

.page-header--quiet .page-header__action,
.page-header--quiet .page-header__placeholder {
  width: 164rpx;
}

.page-header--quiet .page-header__action {
  justify-content: flex-start;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.page-header--quiet .page-header__back-icon {
  color: $ichip-color-nav-active;
  font-size: 38rpx;
  font-weight: $ichip-weight-regular;
}

.page-header--quiet .page-header__title {
  color: $ichip-color-ink;
  font-size: 29rpx;
  font-weight: $ichip-weight-medium;
}
</style>
