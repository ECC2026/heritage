<template>
  <!-- 资讯页仅接入统一绿色视觉；资讯加载、下拉刷新和详情跳转保持原实现。 -->
  <view class="app-page heritage-subpage news-page">
    <page-header title="非遗资讯" variant="green" />
    <view class="hero-card news-hero">
      <view class="soft-pill">Heritage News</view>
      <view class="news-title">非遗资讯</view>
      <view class="news-subtitle">聚合平台资讯内容，用于展示非遗动态、传承人风采与活动回顾。</view>
    </view>

    <view class="section-card">
      <view v-if="newsList.length">
        <view class="news-card" v-for="item in newsList" :key="item.id" @click="toDetail(item.id)">
          <image class="news-cover" :src="normalizeImage(item.cover, '/static/img/lbt1.jpg')" mode="aspectFill"></image>
          <view class="news-content">
            <view class="news-card-title">{{ item.title }}</view>
            <view class="news-summary">{{ shortText(item.summary || item.content, 54) }}</view>
            <view class="news-meta">
              <text>{{ formatDateTime(item.createTime) }}</text>
              <text>{{ item.views || 0 }} 浏览</text>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-block">暂时还没有资讯内容</view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getNewsList } from '@/common/request/api.js'
import { formatDateTime, normalizeImage, shortText } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      newsList: []
    }
  },
  onShow() {
    this.loadNews()
  },
  onPullDownRefresh() {
    this.loadNews().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    formatDateTime,
    normalizeImage,
    shortText,
    async loadNews() {
      const result = await getNewsList({ page: 1, size: 20, status: 1 })
      this.newsList = result.list || []
    },
    toDetail(id) {
      uni.navigateTo({ url: `/pages/news/detail?id=${id}` })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/heritage-subpage.scss";

.news-page {
  padding-bottom: calc(64rpx + env(safe-area-inset-bottom));
}

.news-hero {
  margin-top: 12rpx;
}

.news-title {
  margin-top: 20rpx;
  font-size: 48rpx;
  font-weight: 700;
  color: $heritage-green;
  font-family: "STKaiti", "KaiTi", "STSong", serif;
  letter-spacing: 5rpx;
}

.news-subtitle {
  margin-top: 14rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: $heritage-muted;
}

/* 资讯循环与点击事件不变，仅把旧分割线列表改为独立卡片。 */
.news-card {
  display: flex;
  margin-top: 16rpx;
  padding: 15rpx;
  border: 1rpx solid rgba(75, 122, 98, 0.18);
  border-radius: 15rpx;
  background: $heritage-card;
  box-shadow: 0 4rpx 11rpx rgba(70, 106, 76, 0.08);
}

.news-card:first-child {
  margin-top: 0;
}

.news-cover {
  width: 220rpx;
  height: 168rpx;
  border-radius: 11rpx;
  background: linear-gradient(150deg, #eaf2ef, #bad5d0);
}

.news-content {
  flex: 1;
  margin-left: 18rpx;
}

.news-card-title {
  font-size: 30rpx;
  font-weight: 700;
  color: $heritage-ink;
  line-height: 1.6;
}

.news-summary {
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: $heritage-muted;
}

.news-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: $heritage-green;
}
</style>
