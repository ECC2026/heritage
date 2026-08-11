<template>
  <view class="bottom-nav">
    <view
      v-for="item in items"
      :key="item.key"
      class="bottom-nav__item"
      :class="current === item.key ? 'bottom-nav__item--active' : ''"
      @tap="handleTap(item)"
    >
      <view
        class="bottom-nav__icon"
        :class="current === item.key ? item.activeIconClass : item.iconClass"
      ></view>
      <text class="bottom-nav__text">{{ item.label }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'BottomNav',
  props: {
    current: {
      type: String,
      default: 'home'
    }
  },
  data() {
    return {
      items: [
        {
          key: 'home',
          label: '首页',
          url: '/pages/index/index',
          iconClass: 'tn-icon-home',
          activeIconClass: 'tn-icon-home'
        },
        {
          key: 'community',
          label: '非遗',
          url: '/pages/community/index',
          iconClass: 'tn-icon-floral',
          activeIconClass: 'tn-icon-floral'
        },
        {
          key: 'shop',
          label: '商城',
          url: '/pages/shop/list',
          iconClass: 'tn-icon-shop',
          activeIconClass: 'tn-icon-shop'
        },
        {
          key: 'activity',
          label: '活动',
          url: '/pages/activity/list',
          iconClass: 'tn-icon-calendar',
          activeIconClass: 'tn-icon-calendar'
        },
        {
          key: 'profile',
          label: '我的',
          url: '/pages/profile/index',
          iconClass: 'tn-icon-my',
          activeIconClass: 'tn-icon-my'
        }
      ]
    }
  },
  methods: {
    handleTap(item) {
      if (!item || item.key === this.current) {
        return
      }
      uni.switchTab({
        url: item.url
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "@/tuniao-ui/iconfont.css";

.bottom-nav {
  position: fixed;
  left: $ichip-space-2;
  right: $ichip-space-2;
  bottom: calc(12rpx + env(safe-area-inset-bottom));
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 108rpx;
  padding: 0 8rpx;
  border: 1rpx solid rgba(100, 121, 110, 0.12);
  border-radius: 24rpx;
  background: $ichip-color-surface;
  box-shadow: 0 6rpx 24rpx rgba(44, 39, 35, 0.06);
}

.bottom-nav__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  height: 100%;
  color: $ichip-color-nav-inactive;
}

.bottom-nav__item--active {
  color: $ichip-color-nav-active;
}

.bottom-nav__icon {
  font-size: 40rpx;
  line-height: 1;
}

.bottom-nav__text {
  font-size: 22rpx;
  line-height: 1.2;
}
</style>
