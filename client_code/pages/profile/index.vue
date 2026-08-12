<template>
  <!--
    “我的”页面只调整视觉层：
    登录状态、用户信息、订单、报名、帖子、收藏、认证与退出登录逻辑全部保留。
  -->
  <view class="app-page profile-page with-bottom-nav">
    <view class="safe-top"></view>

    <!-- 页面标题沿用首页的青绿色、宋体和装饰线语言。 -->
    <view class="profile-heading">
      <text class="profile-heading-en">PERSONAL HERITAGE</text>
      <view class="profile-heading-title"><text>我的</text></view>
      <!-- 青铜兽只作为装饰，不拦截用户点击。图片与首页共用同一静态资源。 -->
      <image class="profile-heading-mascot" :src="mascotImage" mode="aspectFit"></image>
    </view>

    <!-- 个人信息卡保留原点击行为：未登录进入登录页，已登录进入资料编辑页。 -->
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

    <!-- 传承人认证的状态、提示和点击目标保持原实现，仅更新卡片样式。 -->
    <view class="section-card inheritor-section">
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

    <!-- 所有常用入口继续由 quickActions 和 handleQuickAction 统一驱动。 -->
    <view class="section-card quick-section">
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

    <!-- 互动数字继续使用原接口统计结果，不增加或伪造静态业务数据。 -->
    <view class="section-card interaction-section">
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

    <!-- 最近订单保留登录、加载中、空数据和正常列表四种原有状态。 -->
    <view class="section-card order-section">
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
    <!-- 复用首页已经实现的绿色导航主题，不改变 tabBar 路由。 -->
    <bottom-nav current="profile" theme="green" />
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

// 与首页复用同一个青铜兽文件，并通过 data 动态绑定避免静态路径被错误哈希化。
const MASCOT_IMAGE = '/static/home/bronze-beast.png'

export default {
  components: {
    BottomNav
  },
  mixins: [tabbarPageMixin],
  data() {
    return {
      loading: false,
      loggedIn: false,
      // 仅用于页面标题装饰，不参与任何个人中心业务逻辑。
      mascotImage: MASCOT_IMAGE,
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

<style lang="scss" scoped>
/*
 * “我的”页面绿色主题覆盖。
 * 仅修改布局、色彩、边框、阴影和字体，不修改模板中的点击事件或脚本业务逻辑。
 * 色值与首页保持同源，后续若调整品牌色可在本段变量中统一修改。
 */
$profile-green: #087d79;
$profile-deep: #285f5c;
$profile-ink: #24423f;
$profile-muted: #67817a;
$profile-paper: rgba(249, 252, 242, 0.94);
$profile-line: rgba(38, 105, 97, 0.22);
$profile-pale: #dcebd1;

/* 页面背景使用与首页一致的浅绿渐变和轻微纸张纹理，并为固定导航预留空间。 */
.profile-page {
  position: relative;
  min-height: 100vh;
  padding: 0 0 calc(154rpx + env(safe-area-inset-bottom));
  overflow-x: hidden;
  background:
    radial-gradient(circle at 12% 8%, rgba(255, 255, 255, 0.82) 0, transparent 28%),
    linear-gradient(180deg, #edf4e8 0%, #f5f7ef 52%, #edf3e7 100%);
  color: $profile-ink;
}

.profile-page::before {
  position: fixed;
  inset: 0;
  z-index: 0;
  opacity: 0.14;
  background-image:
    linear-gradient(45deg, rgba(35, 105, 96, 0.05) 25%, transparent 25%),
    linear-gradient(-45deg, rgba(35, 105, 96, 0.04) 25%, transparent 25%);
  background-size: 20rpx 20rpx;
  content: '';
  pointer-events: none;
}

/* 页面所有内容位于纹理层上方；底部导航组件自身仍使用其固定层级。 */
.safe-top,
.profile-heading,
.profile-hero,
.section-card {
  position: relative;
  z-index: 1;
}

/* 标题区域沿用首页“居中中文标题 + 英文小字 + 青铜兽”的品牌表达。 */
.profile-heading {
  height: 144rpx;
  margin: 0 32rpx 18rpx;
  text-align: center;
}

.profile-heading-en {
  display: block;
  color: rgba(40, 95, 92, 0.58);
  font-family: Georgia, serif;
  font-size: 14rpx;
  letter-spacing: 7rpx;
}

.profile-heading-title {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 11rpx;
  color: $profile-green;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 42rpx;
  font-weight: 600;
  letter-spacing: 9rpx;
}

.profile-heading-title::before,
.profile-heading-title::after {
  width: 48rpx;
  height: 1rpx;
  margin: 0 18rpx;
  background: $profile-green;
  box-shadow: 7rpx -6rpx 0 -0.5rpx rgba(8, 125, 121, 0.45), -7rpx 6rpx 0 -0.5rpx rgba(8, 125, 121, 0.45);
  content: '';
}

.profile-heading-mascot {
  position: absolute;
  right: -9rpx;
  bottom: -31rpx;
  z-index: 3;
  width: 111rpx;
  height: 132rpx;
  pointer-events: none;
}

/*
 * 个人信息主卡：浅色玉纹背景和双层边框替换旧暖棕色卡片。
 * overflow 保持可见，使标题区青铜兽可以自然压住卡片右上角。
 */
.profile-hero {
  margin: 0 32rpx 24rpx;
  padding: 32rpx 27rpx 27rpx;
  overflow: visible;
  border: 1rpx solid $profile-line;
  border-radius: 22rpx;
  background:
    radial-gradient(ellipse at 100% 0, rgba(151, 201, 163, 0.42), transparent 43%),
    linear-gradient(145deg, rgba(250, 253, 244, 0.98), rgba(224, 241, 215, 0.93));
  box-shadow: 0 9rpx 22rpx rgba(61, 101, 71, 0.12);
}

.profile-hero::after {
  position: absolute;
  right: 18rpx;
  bottom: 15rpx;
  width: 218rpx;
  height: 87rpx;
  border: 2rpx solid rgba(38, 135, 112, 0.11);
  border-radius: 50%;
  content: '';
  pointer-events: none;
}

.hero-top,
.hero-user,
.hero-name-row,
.hero-actions,
.order-head,
.order-meta {
  display: flex;
  align-items: center;
}

.hero-top,
.order-head,
.order-meta {
  justify-content: space-between;
}

.hero-top {
  gap: 20rpx;
}

.hero-user {
  min-width: 0;
  flex: 1;
}

.hero-avatar {
  width: 112rpx;
  height: 112rpx;
  margin-right: 22rpx;
  flex-shrink: 0;
  border: 6rpx double rgba(40, 95, 92, 0.72);
  border-radius: 50%;
  background: #f8fbf2;
  box-shadow: 0 7rpx 16rpx rgba(48, 94, 75, 0.14);
}

.hero-copy {
  min-width: 0;
  flex: 1;
}

.hero-name-row {
  flex-wrap: wrap;
  gap: 10rpx;
}

.hero-name {
  color: $profile-ink;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 37rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
}

.inheritor-mark {
  padding: 6rpx 14rpx;
  border: 1rpx solid rgba(107, 89, 47, 0.3);
  border-radius: 999rpx;
  background: linear-gradient(135deg, #8b815e, #c9b986);
  color: #fffbe9;
  font-size: 18rpx;
  box-shadow: none;
}

.hero-meta,
.hero-tip {
  display: block;
  margin-top: 8rpx;
  color: $profile-muted;
  font-size: 21rpx;
  line-height: 1.5;
}

.hero-tip {
  color: #667b53;
}

.hero-status {
  flex-shrink: 0;
  padding: 8rpx 16rpx;
  border: 1rpx solid rgba(40, 95, 92, 0.18);
  border-radius: 999rpx;
  background: rgba(244, 249, 237, 0.78);
  color: $profile-deep;
  font-size: 19rpx;
}

/* 订单统计保持原来的三个数据字段，只改变为通透的三栏玉色统计。 */
.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rpx;
  margin-top: 28rpx;
  overflow: hidden;
  border: 1rpx solid rgba(39, 106, 97, 0.14);
  border-radius: 15rpx;
  background: rgba(39, 106, 97, 0.14);
}

.stat-item {
  padding: 18rpx 10rpx;
  border-radius: 0;
  background: rgba(248, 251, 242, 0.9);
  text-align: center;
}

.stat-value {
  display: block;
  color: $profile-green;
  font-family: Georgia, serif;
  font-size: 34rpx;
  font-weight: 600;
}

.stat-label {
  display: block;
  margin-top: 5rpx;
  color: $profile-muted;
  font-size: 19rpx;
}

/* 操作按钮保留三项原功能，统一为青绿色描边胶囊。 */
.hero-actions {
  position: relative;
  z-index: 2;
  gap: 12rpx;
  margin-top: 22rpx;
}

.action-pill {
  min-width: 0;
  flex: 1;
  justify-content: center;
  padding: 11rpx 8rpx;
  border: 1rpx solid rgba(40, 95, 92, 0.2);
  border-radius: 999rpx;
  background: rgba(232, 243, 222, 0.8);
  color: $profile-deep;
  font-size: 20rpx;
}

.action-pill.secondary,
.action-pill.ghost {
  background: rgba(250, 252, 245, 0.76);
  color: $profile-deep;
}

/* 各业务分区统一成白绿色纸张卡片，标题样式与首页栏目标题呼应。 */
.section-card {
  margin: 22rpx 32rpx;
  padding: 26rpx;
  border: 1rpx solid rgba(64, 118, 91, 0.17);
  border-radius: 19rpx;
  background: $profile-paper;
  box-shadow: 0 7rpx 17rpx rgba(61, 101, 71, 0.09);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid rgba(44, 112, 102, 0.12);
}

.section-title {
  color: $profile-green;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 31rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
}

.section-note {
  color: $profile-muted;
  font-size: 19rpx;
}

/* 认证卡突出身份申请信息，但状态文字与点击逻辑仍由原脚本控制。 */
.inheritor-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  padding: 22rpx;
  border: 1rpx solid rgba(71, 126, 93, 0.18);
  border-radius: 15rpx;
  background:
    radial-gradient(ellipse at 100% 100%, rgba(174, 211, 166, 0.4), transparent 48%),
    linear-gradient(135deg, #f8fbed, #e8f3dc);
}

.inheritor-copy {
  min-width: 0;
  flex: 1;
}

.inheritor-title {
  display: block;
  color: $profile-ink;
  font-size: 27rpx;
  font-weight: 600;
}

.inheritor-desc {
  display: block;
  margin-top: 8rpx;
  color: $profile-muted;
  font-size: 20rpx;
  line-height: 1.6;
}

.inheritor-badge {
  min-width: 106rpx;
  padding: 10rpx 14rpx;
  flex-shrink: 0;
  border-radius: 999rpx;
  font-size: 19rpx;
  font-weight: 600;
  text-align: center;
}

.badge-empty,
.badge-pending {
  background: rgba(221, 234, 196, 0.9);
  color: $profile-deep;
}

.badge-success {
  background: rgba(57, 139, 90, 0.14);
  color: #28794c;
}

.badge-danger {
  background: rgba(178, 74, 60, 0.12);
  color: #9e4439;
}

/*
 * 常用入口由两列改为四列，更接近首页“六大体系”的入口语言。
 * 入口名称和说明文字仍完整保留，图标继续使用 quickActions 中的原字符。
 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24rpx 8rpx;
}

.quick-card {
  min-width: 0;
  padding: 4rpx 2rpx 8rpx;
  border-radius: 0;
  background: transparent;
  text-align: center;
}

.quick-icon {
  display: inline-flex;
  width: 72rpx;
  height: 68rpx;
  align-items: center;
  justify-content: center;
  -webkit-clip-path: polygon(25% 3%, 75% 3%, 100% 50%, 75% 97%, 25% 97%, 0 50%);
  clip-path: polygon(25% 3%, 75% 3%, 100% 50%, 75% 97%, 25% 97%, 0 50%);
  background: linear-gradient(145deg, #4d8273, $profile-deep);
  color: #f7faec;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 28rpx;
  font-weight: 600;
  filter: drop-shadow(0 4rpx 4rpx rgba(32, 91, 77, 0.13));
}

.quick-name {
  display: block;
  margin-top: 11rpx;
  overflow: hidden;
  color: $profile-deep;
  font-size: 20rpx;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quick-note {
  display: -webkit-box;
  min-height: 39rpx;
  margin-top: 5rpx;
  overflow: hidden;
  color: #789088;
  font-size: 15rpx;
  line-height: 1.35;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

/* 互动概览继续展示报名、帖子、收藏三个原有统计值。 */
.interaction-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.interaction-card {
  padding: 22rpx 8rpx;
  border: 1rpx solid rgba(58, 116, 93, 0.15);
  border-radius: 14rpx;
  background: linear-gradient(150deg, #fbfdf5, #eaf4e0);
  text-align: center;
}

.interaction-value {
  display: block;
  color: $profile-green;
  font-family: Georgia, serif;
  font-size: 34rpx;
  font-weight: 600;
}

.interaction-label {
  display: block;
  margin-top: 7rpx;
  color: $profile-muted;
  font-size: 19rpx;
}

/* 最近订单每一条成为独立浅色卡片，字段内容、数量和状态映射保持不变。 */
.order-card {
  margin-top: 13rpx;
  padding: 20rpx;
  border: 1rpx solid rgba(54, 112, 91, 0.13);
  border-radius: 13rpx;
  background: linear-gradient(145deg, rgba(253, 254, 249, 0.98), rgba(237, 246, 228, 0.88));
}

.order-card:first-child {
  margin-top: 0;
  padding-top: 20rpx;
  border-top: 1rpx solid rgba(54, 112, 91, 0.13);
}

.order-head,
.order-meta {
  gap: 12rpx;
}

.order-no {
  min-width: 0;
  flex: 1;
  overflow: hidden;
  color: $profile-muted;
  font-size: 20rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-status {
  color: $profile-green;
  font-size: 20rpx;
  font-weight: 600;
}

.order-product {
  display: block;
  margin: 12rpx 0;
  color: $profile-ink;
  font-size: 25rpx;
  line-height: 1.5;
}

.order-meta {
  color: #789088;
  font-size: 19rpx;
}

/* 登录、空订单按钮沿用原事件，只覆盖成当前绿色主题。 */
.profile-page .primary-button,
.profile-page .secondary-button {
  height: 76rpx;
  border: 1rpx solid rgba(40, 95, 92, 0.22);
  border-radius: 999rpx;
  background: $profile-deep;
  color: #f9fbed;
  font-size: 25rpx;
}

.profile-page .secondary-button {
  background: #f7faef;
  color: $profile-deep;
}

.empty-block {
  padding: 38rpx 16rpx;
  color: $profile-muted;
  font-size: 22rpx;
  line-height: 1.6;
  text-align: center;
}

.compact-empty {
  padding-top: 12rpx;
}

.empty-button {
  margin-top: 20rpx;
}

/* 退出登录保留原确认弹窗，仅弱化为页面末尾的危险操作入口。 */
.logout-card {
  padding: 0;
  overflow: hidden;
  background: rgba(250, 252, 245, 0.76);
}

.logout-button {
  padding: 24rpx;
  color: #9c5147;
  font-size: 24rpx;
  font-weight: 600;
  text-align: center;
}

/* 小屏设备降低入口文字尺寸，确保四列入口不挤压或换行错位。 */
@media screen and (max-width: 350px) {
  .profile-heading-mascot {
    width: 96rpx;
    height: 114rpx;
  }

  .quick-name {
    font-size: 18rpx;
  }

  .quick-note {
    font-size: 14rpx;
  }
}
</style>
