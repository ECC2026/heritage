<template>
  <view class="app-page with-bottom-nav">
    <view class="safe-top"></view>

    <view class="hero-card hero">
      <view class="hero-top">
        <view>
          <view class="soft-pill">无念万艺</view>
          <view class="hero-title">无念万艺</view>
        </view>
        <view class="hero-user" @click="goProfile">
          <image class="hero-avatar" :src="normalizeImage(displayUser.avatar)" mode="aspectFill"></image>
          <text>{{ displayUser.nickname }}</text>
        </view>
      </view>

      <view class="hero-search" @click="goNewsList">
        <text class="hero-search-icon">搜索</text>
        <text class="hero-search-text">查看最新非遗资讯、商品和活动内容</text>
      </view>
    </view>

    <view class="section-card" style="padding: 0; overflow: hidden;">
      <swiper class="banner-swiper" indicator-dots autoplay circular>
        <swiper-item v-for="item in banners" :key="item.id">
          <image class="banner-image" :src="normalizeImage(item.image, '/static/img/lbt1.jpg')" mode="aspectFill" @click="handleBanner(item)"></image>
          <view class="banner-copy">
            <view class="banner-title">{{ item.title || '非遗文化推荐' }}</view>
            <view class="banner-link">{{ item.linkType === 'page' ? '点击查看专题页面' : '点击查看详情' }}</view>
          </view>
        </swiper-item>
      </swiper>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">核心入口</text>
        <text class="section-note">围绕核心业务主链路设计</text>
      </view>
      <view class="quick-grid">
        <view class="quick-item warm" @click="goShop">
          <text class="quick-label">文创商城</text>
          <text class="quick-note">购买非遗文创</text>
        </view>
        <view class="quick-item ink" @click="goActivity">
          <text class="quick-label">活动报名</text>
          <text class="quick-note">参与线下体验</text>
        </view>
        <view class="quick-item teal" @click="goCommunity">
          <text class="quick-label">交流社区</text>
          <text class="quick-note">发布心得帖子</text>
        </view>
        <view class="quick-item sand" @click="goProfile">
          <text class="quick-label">个人中心</text>
          <text class="quick-note">查看订单资料</text>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">热门非遗项目</text>
        <text class="section-note">文化展示重点内容</text>
      </view>
      <scroll-view scroll-x class="heritage-scroll">
        <view class="heritage-list">
          <view class="heritage-item" v-for="item in heritageList" :key="item.id">
            <image class="heritage-cover" :src="normalizeImage(item.cover, '/static/img/logo1.jpg')" mode="aspectFill"></image>
            <view class="heritage-name">{{ item.name }}</view>
            <view class="heritage-meta">{{ item.category }} · {{ item.level }}</view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">非遗资讯</text>
        <text class="section-note" @click="goNewsList">查看更多</text>
      </view>
      <view v-if="newsList.length">
        <view class="news-item" v-for="item in newsList" :key="item.id" @click="goNewsDetail(item.id)">
          <image class="news-cover" :src="normalizeImage(item.cover, '/static/img/logo1.jpg')" mode="aspectFill"></image>
          <view class="news-info">
            <view class="news-title">{{ item.title }}</view>
            <view class="news-summary">{{ shortText(item.summary || item.content, 38) }}</view>
            <view class="news-time">{{ formatDateTime(item.createTime) }}</view>
          </view>
        </view>
      </view>
      <view v-else class="empty-block">暂时还没有资讯内容</view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">精选文创商品</text>
        <text class="section-note" @click="goShop">进入商城</text>
      </view>
      <view class="product-grid">
        <view class="product-card" v-for="item in products" :key="item.id" @click="goProductDetail(item.id)">
          <image class="product-cover" :src="normalizeImage(item.cover, '/static/img/logo1.jpg')" mode="aspectFill"></image>
          <view class="product-name">{{ item.name }}</view>
          <view class="product-price">¥{{ formatPrice(item.price) }}</view>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">近期活动</text>
        <text class="section-note" @click="goActivity">更多活动</text>
      </view>
      <view v-if="activities.length">
        <view class="activity-row" v-for="item in activities" :key="item.id" @click="goActivityDetail(item.id)">
          <view class="activity-main">
            <view class="activity-name">{{ item.title || item.name }}</view>
            <view class="activity-line">{{ formatDateTime(item.startTime) }} · {{ item.location }}</view>
          </view>
          <view class="activity-side">
            <view class="soft-pill">{{ item.statusText || '进行中' }}</view>
            <view class="activity-count">{{ item.signupCount || 0 }}/{{ item.maxParticipants || item.limitCount || 0 }}</view>
          </view>
        </view>
      </view>
      <view v-else class="empty-block">暂时还没有活动内容</view>
    </view>
    <bottom-nav current="home" />
  </view>
</template>

<script>
import BottomNav from '@/components/bottom-nav.vue'
import tabbarPageMixin from '@/mixins/tabbar-page.js'
import { getActivities, getBanners, getHeritageProjects, getNewsList, getProducts } from '@/common/request/api.js'
import { getUserInfo, isLoggedIn } from '@/common/session.js'
import { formatDateTime, formatPrice, normalizeImage, shortText } from '@/common/utils.js'

export default {
  components: {
    BottomNav
  },
  mixins: [tabbarPageMixin],
  data() {
    return {
      banners: [],
      heritageList: [],
      newsList: [],
      products: [],
      activities: [],
      displayUser: {
        nickname: '去登录',
        avatar: '/static/img/logo.png'
      }
    }
  },
  onShow() {
    this.updateDisplayUser()
  },
  onLoad() {
    this.loadData()
    this.updateDisplayUser()
  },
  onPullDownRefresh() {
    this.loadData().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    formatDateTime,
    formatPrice,
    normalizeImage,
    shortText,
    async loadData() {
      const [banners, projects, news, products, activities] = await Promise.all([
        getBanners().catch(() => []),
        getHeritageProjects().catch(() => []),
        getNewsList({ page: 1, size: 3, status: 1 }).catch(() => ({ list: [] })),
        getProducts({ page: 1, size: 4, status: 1 }).catch(() => ({ list: [] })),
        getActivities({ page: 1, size: 3 }).catch(() => ({ list: [] }))
      ])

      this.banners = banners || []
      this.heritageList = projects || []
      this.newsList = news.list || []
      this.products = products.list || []
      this.activities = activities.list || []
    },
    updateDisplayUser() {
      if (!isLoggedIn()) {
        this.displayUser = {
          nickname: '去登录',
          avatar: '/static/img/logo.png'
        }
        return
      }
      const user = getUserInfo()
      this.displayUser = {
        nickname: user.nickname || user.username || '我的',
        avatar: normalizeImage(user.avatar) || '/static/img/logo.png'
      }
    },
    handleBanner(item) {
      if (!item || !item.link) return
      if (item.linkType === 'page' && item.link.indexOf('/pages/') === 0) {
        uni.navigateTo({ url: item.link })
        return
      }
      if (item.link.indexOf('/pages/') === 0) {
        uni.navigateTo({ url: item.link })
      }
    },
    goNewsList() {
      uni.navigateTo({ url: '/pages/news/list' })
    },
    goNewsDetail(id) {
      uni.navigateTo({ url: `/pages/news/detail?id=${id}` })
    },
    goProductDetail(id) {
      uni.navigateTo({ url: `/pages/shop/detail?id=${id}` })
    },
    goActivityDetail(id) {
      uni.navigateTo({ url: `/pages/activity/detail?id=${id}` })
    },
    goShop() {
      uni.switchTab({ url: '/pages/shop/list' })
    },
    goActivity() {
      uni.switchTab({ url: '/pages/activity/list' })
    },
    goCommunity() {
      uni.switchTab({ url: '/pages/community/index' })
    },
    goProfile() {
      uni.switchTab({ url: '/pages/profile/index' })
    }
  }
}
</script>

<style lang="scss" scoped>
.hero {
  margin-top: 12rpx;
}

.hero-top {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.hero-title {
  margin-top: 20rpx;
  font-size: 48rpx;
  font-weight: 700;
  color: #31231d;
}

.hero-subtitle {
  margin-top: 16rpx;
  color: #7b6558;
  font-size: 26rpx;
  line-height: 1.7;
}

.hero-user {
  width: 144rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  font-size: 24rpx;
  color: #6d5446;
}

.hero-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #fff;
}

.hero-search {
  display: flex;
  align-items: center;
  margin-top: 26rpx;
  padding: 22rpx 24rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.84);
}

.hero-search-icon {
  margin-right: 16rpx;
  font-size: 24rpx;
  color: #8b381f;
}

.hero-search-text {
  font-size: 26rpx;
  color: #8b7467;
}

.banner-swiper {
  height: 340rpx;
}

.banner-image {
  width: 100%;
  height: 100%;
}

.banner-copy {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 24rpx;
  background: linear-gradient(180deg, transparent, rgba(25, 17, 12, 0.6));
}

.banner-title {
  color: #fff;
  font-size: 34rpx;
  font-weight: 700;
}

.banner-link {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.84);
  font-size: 24rpx;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.quick-item {
  padding: 26rpx;
  border-radius: 24rpx;
  color: #fff;
}

.quick-item.warm { background: linear-gradient(135deg, #a6472d, #d88b45); }
.quick-item.ink { background: linear-gradient(135deg, #3b4c63, #6b7a8f); }
.quick-item.teal { background: linear-gradient(135deg, #22756e, #48a99c); }
.quick-item.sand { background: linear-gradient(135deg, #85614e, #bc936a); }

.quick-label {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
}

.quick-note {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.82);
}

.heritage-scroll {
  white-space: nowrap;
}

.heritage-list {
  display: inline-flex;
  gap: 20rpx;
}

.heritage-item {
  width: 220rpx;
}

.heritage-cover {
  width: 220rpx;
  height: 220rpx;
  border-radius: 24rpx;
  background: #f0e5d8;
}

.heritage-name {
  margin-top: 16rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #34251f;
}

.heritage-meta {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #8a7466;
}

.news-item {
  display: flex;
  padding: 18rpx 0;
  border-bottom: 1rpx solid rgba(166, 71, 45, 0.08);
}

.news-item:last-child {
  border-bottom: none;
}

.news-cover {
  width: 176rpx;
  height: 132rpx;
  border-radius: 18rpx;
  background: #f0e5d8;
}

.news-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin-left: 18rpx;
}

.news-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #34251f;
  line-height: 1.5;
}

.news-summary,
.news-time {
  font-size: 24rpx;
  color: #8a7466;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.product-card {
  padding: 18rpx;
  border-radius: 24rpx;
  background: #fff;
}

.product-cover {
  width: 100%;
  height: 240rpx;
  border-radius: 18rpx;
  background: #f0e5d8;
}

.product-name {
  margin-top: 14rpx;
  font-size: 28rpx;
  color: #34251f;
  line-height: 1.5;
}

.product-price {
  margin-top: 10rpx;
  color: #a6472d;
  font-size: 30rpx;
  font-weight: 700;
}

.activity-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 0;
  border-bottom: 1rpx solid rgba(166, 71, 45, 0.08);
}

.activity-row:last-child {
  border-bottom: none;
}

.activity-main {
  flex: 1;
  padding-right: 20rpx;
}

.activity-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #34251f;
}

.activity-line {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #8a7466;
}

.activity-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10rpx;
}

.activity-count {
  font-size: 22rpx;
  color: #8a7466;
}
</style>
