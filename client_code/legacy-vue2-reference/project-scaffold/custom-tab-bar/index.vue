<template>
  <view class="custom-tabbar">
    <view
      v-for="(item, index) in list"
      :key="item.pagePath"
      class="custom-tabbar__item"
      :class="selected === index ? 'custom-tabbar__item--active' : ''"
      @tap="switchTab(index)"
    >
      <view v-if="selected === index" class="custom-tabbar__icon" :class="item.activeIconClass"></view>
      <view v-else class="custom-tabbar__icon" :class="item.iconClass"></view>
      <text class="custom-tabbar__text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      selected: 0,
      list: [
        {
          pagePath: '/pages/index/index',
          text: '首页',
          iconClass: 'tn-icon-home',
          activeIconClass: 'tn-icon-home-fill'
        },
        {
          pagePath: '/pages/shop/list',
          text: '文创',
          iconClass: 'tn-icon-shop',
          activeIconClass: 'tn-icon-shop-fill'
        },
        {
          pagePath: '/pages/activity/list',
          text: '活动',
          iconClass: 'tn-icon-activity',
          activeIconClass: 'tn-icon-task-fill'
        },
        {
          pagePath: '/pages/community/index',
          text: '社区',
          iconClass: 'tn-icon-chat',
          activeIconClass: 'tn-icon-my-chat-fill'
        },
        {
          pagePath: '/pages/profile/index',
          text: '我的',
          iconClass: 'tn-icon-my',
          activeIconClass: 'tn-icon-my-fill'
        }
      ]
    }
  },
  created() {
    this.syncSelected()
  },
  methods: {
    syncSelected() {
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      const route = currentPage && currentPage.route ? `/${currentPage.route}` : ''
      const index = this.list.findIndex((item) => item.pagePath === route)
      if (index !== -1) {
        this.selected = index
      }
    },
    switchTab(index) {
      if (this.selected === index) {
        return
      }
      this.selected = index
      uni.switchTab({
        url: this.list[index].pagePath
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "@/tuniao-ui/iconfont.css";

.custom-tabbar {
  position: fixed;
  left: 18rpx;
  right: 18rpx;
  bottom: calc(18rpx + env(safe-area-inset-bottom));
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14rpx 10rpx;
  border-radius: 32rpx;
  background: rgba(255, 250, 244, 0.98);
  box-shadow: 0 -8rpx 28rpx rgba(82, 54, 38, 0.08);
}

.custom-tabbar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 10rpx 0 6rpx;
  color: #8f7b70;
}

.custom-tabbar__item--active {
  color: #a6472d;
}

.custom-tabbar__icon {
  font-size: 42rpx;
  line-height: 1;
}

.custom-tabbar__text {
  font-size: 22rpx;
  line-height: 1.2;
}
</style>
