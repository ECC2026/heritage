<template>
  <view class="app-page" style="margin-top: 20px;">
    <page-header title="资讯详情" />
    <image class="cover" :src="normalizeImage(news.cover, '/static/img/lbt1.jpg')" mode="aspectFill"></image>
    <view class="section-card detail-card">
      <view class="head-row">
        <view class="soft-pill">{{ news.category || '资讯' }}</view>
        <view class="soft-pill collect-pill" :class="{ active: favorited }" @click="handleFavorite">
          {{ favorited ? '已收藏' : '收藏' }}
        </view>
      </view>
      <view class="title">{{ news.title }}</view>
      <view class="meta">
        <text>{{ formatDateTime(news.createTime) }}</text>
        <text>{{ news.views || 0 }} 浏览</text>
      </view>
      <rich-text class="article-content" :nodes="news.content || '<p>暂无内容</p>'"></rich-text>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getFavoriteStatus, getNewsDetail, toggleFavorite } from '@/common/request/api.js'
import { isLoggedIn, requireLogin } from '@/common/session.js'
import { formatDateTime, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      news: {},
      favorited: false
    }
  },
  onLoad(options) {
    this.loadNews(options.id)
  },
  methods: {
    formatDateTime,
    normalizeImage,
    async loadNews(id) {
      this.news = await getNewsDetail(id)
      this.loadFavoriteStatus()
    },
    async loadFavoriteStatus() {
      if (!isLoggedIn() || !this.news.id) {
        this.favorited = false
        return
      }
      const result = await getFavoriteStatus({
        type: 'news',
        targetId: this.news.id
      })
      this.favorited = !!(result && result.favorited)
    },
    async handleFavorite() {
      if (!requireLogin()) return
      const result = await toggleFavorite({
        type: 'news',
        targetId: this.news.id
      })
      this.favorited = !!(result && result.favorited)
      uni.showToast({
        title: this.favorited ? '已加入收藏' : '已取消收藏',
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.cover {
  width: 100%;
  height: 420rpx;
  background: #f0e5d8;
}

.detail-card {
  margin-top: -26rpx;
  position: relative;
}

.head-row {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.collect-pill.active {
  background: #a6472d;
  color: #fff;
}

.title {
  margin-top: 18rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: #34251f;
  line-height: 1.5;
}

.meta {
  display: flex;
  justify-content: space-between;
  margin-top: 18rpx;
  font-size: 24rpx;
  color: #8a7466;
}

.article-content {
  display: block;
  margin-top: 28rpx;
  font-size: 30rpx;
  line-height: 1.9;
  color: #4f3e35;
}
</style>
