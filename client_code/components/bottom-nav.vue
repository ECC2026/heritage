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
          activeIconClass: 'tn-icon-home-fill'
        },
        {
          key: 'shop',
          label: '文创',
          url: '/pages/shop/list',
          iconClass: 'tn-icon-shop',
          activeIconClass: 'tn-icon-shop-fill'
        },
        {
          key: 'activity',
          label: '活动',
          url: '/pages/activity/list',
          iconClass: 'tn-icon-activity',
          activeIconClass: 'tn-icon-task-fill'
        },
        {
          key: 'community',
          label: '社区',
          url: '/pages/community/index',
          iconClass: 'tn-icon-chat',
          activeIconClass: 'tn-icon-my-chat-fill'
        },
        {
          key: 'profile',
          label: '我的',
          url: '/pages/profile/index',
          iconClass: 'tn-icon-my',
          activeIconClass: 'tn-icon-my-fill'
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
  left: 20rpx;
  right: 20rpx;
  bottom: calc(18rpx + env(safe-area-inset-bottom));
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14rpx 10rpx;
  border-radius: 32rpx;
  background: rgba(255, 251, 246, 0.98);
  box-shadow: 0 10rpx 30rpx rgba(77, 47, 31, 0.12);
  backdrop-filter: blur(10rpx);
}

.bottom-nav__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 10rpx 0 8rpx;
  color: #8e7769;
  transition: all 0.2s ease;
}

.bottom-nav__item--active {
  color: #a6472d;
}

.bottom-nav__icon {
  font-size: 42rpx;
  line-height: 1;
}

.bottom-nav__text {
  font-size: 22rpx;
  line-height: 1.2;
}
</style>
