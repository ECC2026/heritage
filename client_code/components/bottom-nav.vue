<template>
  <!-- theme="green" 仅用于首页设计稿主题；其他页面保持原来的默认导航外观。 -->
  <view class="bottom-nav" :class="theme === 'green' ? 'bottom-nav--green' : ''">
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
    // 当前页面标识，用于高亮对应导航项并拦截重复点击。
    current: {
      type: String,
      default: 'home'
    },
    // 可选视觉主题。默认值保证没有修改的旧页面不受首页样式影响。
    theme: {
      type: String,
      default: 'default'
    }
  },
  data() {
    return {
      //
      // 底部导航统一配置：
      // key 用于激活判断，url 必须与 pages.json 中的 tabBar 页面保持一致，
      // iconClass 使用项目现有图鸟图标字体，不额外增加图片资源。
      //
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
    // 当前项重复点击时不再次跳转，其余项使用微信 tabBar 专用的 switchTab。
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

/*
 * 首页绿色主题覆盖：贴合设计稿中的通栏浅绿色导航。
 * 使用后代选择器只修改当前组件实例，其他页面的默认主题不受影响。
 */
.bottom-nav--green {
  left: 0;
  right: 0;
  bottom: 0;
  height: 118rpx;
  padding-bottom: env(safe-area-inset-bottom);
  border: none;
  border-top: 1rpx solid rgba(54, 111, 92, 0.12);
  border-radius: 0;
  background: rgba(216, 232, 195, 0.98);
  box-shadow: 0 -5rpx 18rpx rgba(51, 92, 67, 0.08);
}

.bottom-nav--green .bottom-nav__item {
  color: #86a19a;
}

.bottom-nav--green .bottom-nav__item--active {
  color: #285f5c;
}
</style>
