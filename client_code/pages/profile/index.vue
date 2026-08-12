<template>
  <view class="app-page profile-page with-bottom-nav" style="margin-top: 25px;">
    <view class="safe-top"></view>

    <view class="hero-card profile-hero">
      <view class="hero-top">
        <view class="hero-user" @click="handleProfileAction">
          <image :src="userCard.avatar" class="hero-avatar" mode="aspectFill" />
          <view class="hero-copy">
            <view class="hero-name-row">
              <text class="hero-name">{{ userCard.name }}</text>
              <view v-if="showInheritorMark" class="inheritor-mark">传承人</view>
            </view>
            <text class="hero-meta">{{ userCard.subTitle }}</text>
            <text v-if="showInheritorMark" class="hero-tip">已通过传承人认证，正在展示非遗传承身份</text>
          </view>
        </view>
        <view class="hero-status">{{ userCard.statusText }}</view>
      </view>

      <view class="hero-stats">
        <view class="stat-item">
          <text class="stat-value">{{ orderStats.total }}</text>
          <text class="stat-label">累计订单</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ orderStats.active }}</text>
          <text class="stat-label">进行中</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ orderStats.finished }}</text>
          <text class="stat-label">已完成</text>
        </view>
      </view>

      <view class="hero-actions">
        <view class="soft-pill action-pill" @click="goToEdit">编辑资料</view>
        <view class="soft-pill action-pill secondary" @click="goToOrders">查看订单</view>
        <view v-if="loggedIn" class="soft-pill action-pill ghost" @click="handleLogout">退出登录</view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">传承人认证</text>
        <text class="section-note">{{ inheritorStatusText() }}</text>
      </view>
      <view v-if="loggedIn" class="inheritor-panel" @click="goToInheritor">
        <view class="inheritor-copy">
          <text class="inheritor-title">
            {{ inheritorApplication.id ? '查看认证申请' : '申请成为传承人' }}
          </text>
          <text class="inheritor-desc">{{ inheritorStatusDescription() }}</text>
        </view>
        <view v-if="!inheritorApplication.id" class="inheritor-badge badge-empty">去申请</view>
        <view v-else-if="Number(inheritorApplication.auditStatus) === 1" class="inheritor-badge badge-success">已通过</view>
        <view v-else-if="Number(inheritorApplication.auditStatus) === 2" class="inheritor-badge badge-danger">未通过</view>
        <view v-else class="inheritor-badge badge-pending">待审核</view>
      </view>
      <view v-else class="empty-block compact-empty">
        <text>登录后可提交传承人认证申请，完善技艺资料并等待后台审核。</text>
        <button class="primary-button empty-button" @click="goToLogin">立即登录</button>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">常用入口</text>
        <text class="section-note">覆盖答辩演示主流程</text>
      </view>
      <view class="quick-grid">
        <view
          v-for="item in quickActions"
          :key="item.key"
          class="quick-card"
          @click="handleQuickAction(item.key)"
        >
          <text class="quick-icon">{{ item.icon }}</text>
          <text class="quick-name">{{ item.label }}</text>
          <text class="quick-note">{{ item.note }}</text>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">互动概览</text>
        <text class="section-note">报名、帖子、收藏</text>
      </view>
      <view class="interaction-grid">
        <view class="interaction-card">
          <text class="interaction-value">{{ engagementSummary.signups }}</text>
          <text class="interaction-label">我的报名</text>
        </view>
        <view class="interaction-card">
          <text class="interaction-value">{{ engagementSummary.posts }}</text>
          <text class="interaction-label">我的帖子</text>
        </view>
        <view class="interaction-card">
          <text class="interaction-value">{{ engagementSummary.favorites }}</text>
          <text class="interaction-label">我的收藏</text>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">最近订单</text>
        <text class="section-note" @click="goToOrders">全部订单</text>
      </view>

      <view v-if="!loggedIn" class="empty-block">
        <text>登录后可查看订单、资料和报名记录。</text>
        <button class="primary-button empty-button" @click="goToLogin">立即登录</button>
      </view>

      <view v-else-if="loading" class="empty-block">
        <text>正在同步你的订单信息...</text>
      </view>

      <view v-else-if="recentOrders.length">
        <view
          v-for="order in recentOrders"
          :key="order.id"
          class="order-card"
        >
          <view class="order-head">
            <text class="order-no">{{ order.orderNo }}</text>
            <text class="order-status">{{ getOrderStatusText(order.status) }}</text>
          </view>
          <text class="order-product">{{ getOrderPreview(order) }}</text>
          <view class="order-meta">
            <text>{{ formatPrice(order.totalPrice || 0) }}</text>
            <text>{{ formatDateTime(order.createTime) }}</text>
          </view>
        </view>
      </view>

      <view v-else class="empty-block">
        <text>还没有订单记录，可以先去商城挑选文创商品。</text>
        <button class="secondary-button empty-button" @click="goToShop">前往商城</button>
      </view>
    </view>

    <view v-if="loggedIn" class="section-card logout-card">
      <view class="logout-button" @click="handleLogout">退出登录</view>
    </view>
    <bottom-nav current="profile" />
  </view>
</template>

<script>
import BottomNav from '@/components/bottom-nav.vue'
import tabbarPageMixin from '@/mixins/tabbar-page.js'
import {
  getMyInheritorApplication,
  getFavoriteStats,
  getMyPosts,
  getMySignups,
  getOrders,
  getUserInfo as fetchUserInfo
} from '@/common/request/api.js'
import { clearAuth, getUserInfo, isLoggedIn, setUserInfo } from '@/common/session.js'
import { formatDateTime, formatPrice, normalizeImage } from '@/common/utils.js'

const DEFAULT_AVATAR = '/static/img/logo.png'

export default {
  components: {
    BottomNav
  },
  mixins: [tabbarPageMixin],
  data() {
    return {
      loading: false,
      loggedIn: false,
      userInfo: {},
      recentOrders: [],
      inheritorApplication: {},
      orderSummary: {
        total: 0,
        active: 0,
        finished: 0
      },
      engagementSummary: {
        signups: 0,
        posts: 0,
        favorites: 0
      },
      quickActions: [
        { key: 'orders', label: '我的订单', note: '查看购买进度', icon: '单' },
        { key: 'signups', label: '我的报名', note: '跟进活动审核', icon: '报' },
        { key: 'posts', label: '我的帖子', note: '管理社区内容', icon: '帖' },
        { key: 'favorites', label: '我的收藏', note: '沉淀感兴趣内容', icon: '藏' },
        { key: 'inheritor', label: '传承人认证', note: '提交资料申请审核', icon: '承' },
        { key: 'cart', label: '购物车', note: '整理待下单商品', icon: '购' },
        { key: 'bookings', label: '我的预约', note: '管理服务预约', icon: '约' },
        { key: 'activities', label: '文化活动', note: '报名线下体验', icon: '活' },
        { key: 'news', label: '最新资讯', note: '关注非遗动态', icon: '讯' }
      ]
    }
  },
  computed: {
    userCard() {
      if (!this.loggedIn) {
        return {
          avatar: DEFAULT_AVATAR,
          name: '游客模式',
          subTitle: '登录后同步个人资料、订单和报名信息',
          statusText: '未登录'
        }
      }

      const nickname = this.userInfo.nickname || this.userInfo.username || '非遗用户'
      const phone = this.userInfo.phone || '未填写手机号'
      return {
        avatar: normalizeImage(this.userInfo.avatar) || DEFAULT_AVATAR,
        name: nickname,
        subTitle: phone,
        statusText: this.userInfo.status === 0 ? '已停用' : '正常'
      }
    },
    orderStats() {
      return this.orderSummary
    },
    showInheritorMark() {
      return this.loggedIn && this.inheritorApplication && Number(this.inheritorApplication.auditStatus) === 1
    }
  },
  onShow() {
    this.initializePage()
  },
  onPullDownRefresh() {
    this.initializePage(true)
  },
  methods: {
    formatDateTime,
    formatPrice,
    async initializePage(fromRefresh) {
      this.loggedIn = isLoggedIn()
      this.userInfo = getUserInfo()

      if (!this.loggedIn) {
        this.recentOrders = []
        this.inheritorApplication = {}
        this.orderSummary = {
          total: 0,
          active: 0,
          finished: 0
        }
        this.engagementSummary = {
          signups: 0,
          posts: 0,
          favorites: 0
        }
        if (fromRefresh) {
          uni.stopPullDownRefresh()
        }
        return
      }

      this.loading = true
      try {
        const [userInfo, orderResult, signupsResult, postsResult, favoriteStats, inheritorResult] = await Promise.all([
          fetchUserInfo(),
          getOrders({ page: 1, size: 50 }),
          getMySignups({ page: 1, size: 1 }),
          getMyPosts({ page: 1, size: 1 }),
          getFavoriteStats(),
          getMyInheritorApplication()
        ])
        const orderList = orderResult && orderResult.list ? orderResult.list : []
        this.userInfo = userInfo || {}
        this.inheritorApplication = inheritorResult || {}
        setUserInfo(this.userInfo)
        this.recentOrders = orderList.slice(0, 4)
        this.orderSummary = {
          total: Number(orderResult && orderResult.total ? orderResult.total : orderList.length),
          active: orderList.filter((item) => [0, 1, 2].indexOf(Number(item.status)) !== -1).length,
          finished: orderList.filter((item) => Number(item.status) === 3).length
        }
        this.engagementSummary = {
          signups: Number(signupsResult && signupsResult.total ? signupsResult.total : 0),
          posts: Number(postsResult && postsResult.total ? postsResult.total : 0),
          favorites: Number(favoriteStats && favoriteStats.total ? favoriteStats.total : 0)
        }
      } catch (error) {
        this.userInfo = getUserInfo()
        this.inheritorApplication = {}
        this.orderSummary = {
          total: 0,
          active: 0,
          finished: 0
        }
        this.engagementSummary = {
          signups: 0,
          posts: 0,
          favorites: 0
        }
      } finally {
        this.loading = false
        if (fromRefresh) {
          uni.stopPullDownRefresh()
        }
      }
    },
    handleProfileAction() {
      if (!this.loggedIn) {
        this.goToLogin()
        return
      }
      this.goToEdit()
    },
    handleQuickAction(key) {
      if (key === 'orders') {
        this.goToOrders()
        return
      }
      if (key === 'cart') {
        uni.navigateTo({ url: '/pages/shop/cart' })
        return
      }
      if (key === 'signups') {
        uni.navigateTo({ url: '/pages/profile/signups' })
        return
      }
      if (key === 'posts') {
        uni.navigateTo({ url: '/pages/profile/posts' })
        return
      }
      if (key === 'favorites') {
        uni.navigateTo({ url: '/pages/profile/favorites' })
        return
      }
      if (key === 'inheritor') {
        this.goToInheritor()
        return
      }
      if (key === 'bookings') {
        if (!this.loggedIn) {
          this.goToLogin()
          return
        }
        uni.navigateTo({ url: '/pages/service/my-bookings' })
        return
      }
      if (key === 'activities') {
        uni.switchTab({ url: '/pages/activity/list' })
        return
      }
      if (key === 'news') {
        uni.navigateTo({ url: '/pages/news/list' })
      }
    },
    goToLogin() {
      uni.navigateTo({ url: '/pages/login/login' })
    },
    goToEdit() {
      if (!this.loggedIn) {
        this.goToLogin()
        return
      }
      uni.navigateTo({ url: '/pages/profile/edit' })
    },
    goToOrders() {
      if (!this.loggedIn) {
        this.goToLogin()
        return
      }
      uni.navigateTo({ url: '/pages/profile/orders' })
    },
    goToShop() {
      uni.switchTab({ url: '/pages/shop/list' })
    },
    goToInheritor() {
      if (!this.loggedIn) {
        this.goToLogin()
        return
      }
      uni.navigateTo({ url: '/pages/profile/inheritor' })
    },
    getOrderPreview(order) {
      if (order.items && order.items.length) {
        return order.items.map((item) => `${item.productName} x${item.quantity}`).join(' / ')
      }
      return order.productName || '文创商品订单'
    },
    getOrderStatusText(status) {
      const map = {
        0: '待支付',
        1: '已支付',
        2: '已发货',
        3: '已完成',
        4: '已取消'
      }
      return map[status] || '处理中'
    },
    inheritorStatusText() {
      if (!this.loggedIn) {
        return '未登录'
      }
      if (!this.inheritorApplication || !this.inheritorApplication.id) {
        return '去申请'
      }
      const map = {
        0: '待审核',
        1: '已通过',
        2: '未通过'
      }
      return map[this.inheritorApplication.auditStatus] || '处理中'
    },
    inheritorStatusDescription() {
      if (!this.inheritorApplication || !this.inheritorApplication.id) {
        return '完善技艺方向、从业经历和证明材料，提交后可进入后台审核流程。'
      }
      if (Number(this.inheritorApplication.auditStatus) === 1) {
        return '你的传承人认证已通过，可以在这里查看已提交的认证资料。'
      }
      if (Number(this.inheritorApplication.auditStatus) === 2) {
        return this.inheritorApplication.auditRemark || '本次申请未通过，可补充资料后重新提交。'
      }
      return '申请资料已提交，当前正在等待后台审核，请留意审核结果。'
    },
    handleLogout() {
      uni.showModal({
        title: '退出登录',
        content: '确认退出当前账号吗？',
        success: (res) => {
          if (!res.confirm) {
            return
          }
          clearAuth()
          this.loggedIn = false
          this.userInfo = {}
          this.recentOrders = []
          this.inheritorApplication = {}
          this.orderSummary = {
            total: 0,
            active: 0,
            finished: 0
          }
          this.engagementSummary = {
            signups: 0,
            posts: 0,
            favorites: 0
          }
          uni.showToast({
            title: '已退出登录',
            icon: 'success'
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
  padding: 24rpx;
  padding-bottom: 48rpx;
  background:
    radial-gradient(circle at top right, rgba(166, 71, 45, 0.16), transparent 34%),
    linear-gradient(180deg, #f8efe7 0%, #f5f1ec 32%, #f7f4ef 100%);
}

.profile-hero {
  margin-bottom: 24rpx;
}

.hero-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24rpx;
}

.hero-user {
  display: flex;
  align-items: center;
  flex: 1;
}

.hero-avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  margin-right: 24rpx;
}

.hero-copy {
  flex: 1;
}

.hero-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.hero-name {
  display: block;
  color: #3a241c;
  font-size: 36rpx;
  font-weight: 700;
}

.inheritor-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #b6853e 0%, #d7b06a 100%);
  color: #fffaf0;
  font-size: 20rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
  box-shadow: 0 8rpx 18rpx rgba(182, 133, 62, 0.16);
}

.hero-meta {
  display: block;
  margin-top: 10rpx;
  color: #7c6154;
  font-size: 24rpx;
  line-height: 1.6;
}

.hero-tip {
  display: block;
  margin-top: 8rpx;
  color: #9b6d1f;
  font-size: 22rpx;
  line-height: 1.6;
}

.hero-status {
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(166, 71, 45, 0.12);
  color: #8b381f;
  font-size: 22rpx;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18rpx;
  margin-top: 30rpx;
}

.stat-item {
  padding: 20rpx 12rpx;
  border-radius: 24rpx;
  background: rgba(255, 247, 241, 0.14);
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #3a241c;
}

.stat-label {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #8c6e62;
}

.hero-actions {
  display: flex;
  gap: 18rpx;
  margin-top: 28rpx;
}

.action-pill {
  min-width: 180rpx;
  justify-content: center;
}

.action-pill.secondary {
  background: rgba(255, 246, 240, 0.12);
}

.action-pill.ghost {
  background: rgba(122, 68, 50, 0.08);
  color: #7a4432;
}

.compact-empty {
  padding-top: 12rpx;
}

.inheritor-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 26rpx 24rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #fffaf5 0%, #f7ede2 100%);
}

.inheritor-copy {
  flex: 1;
}

.inheritor-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #34221c;
}

.inheritor-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: #8e6c61;
}

.inheritor-badge {
  flex-shrink: 0;
  min-width: 124rpx;
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  text-align: center;
  font-size: 22rpx;
  font-weight: 700;
}

.badge-empty {
  background: rgba(166, 71, 45, 0.12);
  color: #a6472d;
}

.badge-pending {
  background: rgba(197, 141, 26, 0.14);
  color: #b27a12;
}

.badge-success {
  background: rgba(46, 145, 82, 0.14);
  color: #2e9152;
}

.badge-danger {
  background: rgba(178, 74, 60, 0.14);
  color: #b24a3c;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.quick-card {
  padding: 26rpx 24rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #fffaf5 0%, #f8eee5 100%);
}

.quick-icon {
  display: inline-flex;
  width: 62rpx;
  height: 62rpx;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  background: #a6472d;
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
}

.quick-name {
  display: block;
  margin-top: 18rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #34221c;
}

.quick-note {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #8e6c61;
}

.interaction-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18rpx;
}

.interaction-card {
  padding: 24rpx 12rpx;
  border-radius: 22rpx;
  text-align: center;
  background: linear-gradient(180deg, #fffaf4 0%, #f7ede4 100%);
}

.interaction-value {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #2f1f18;
}

.interaction-label {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #8d6f63;
}

.order-card {
  padding: 24rpx 0;
  border-top: 1rpx solid #f0e1d8;
}

.order-card:first-child {
  border-top: none;
  padding-top: 0;
}

.order-head,
.order-meta {
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
}

.order-no {
  flex: 1;
  font-size: 24rpx;
  color: #7f6357;
}

.order-status {
  font-size: 22rpx;
  color: #a6472d;
}

.order-product {
  display: block;
  margin: 14rpx 0;
  font-size: 28rpx;
  line-height: 1.6;
  color: #2c1d18;
}

.order-meta {
  font-size: 22rpx;
  color: #9c7d70;
}

.empty-button {
  margin-top: 24rpx;
}

.logout-card {
  padding: 0;
}

.logout-button {
  padding: 28rpx 24rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
  color: #b53d35;
}
</style>
