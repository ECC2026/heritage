<template>
  <!-- 共享主题不影响收藏筛选、详情跳转或取消收藏请求。 -->
  <view class="app-page heritage-subpage favorites-page">
    <page-header title="我的收藏" variant="green" />

    <view class="section-card summary-card">
      <view class="section-head">
        <text class="section-title">我的收藏</text>
        <text class="section-note">常看内容快速直达</text>
      </view>
      <view class="filter-row">
        <view
          v-for="item in filters"
          :key="item.value"
          class="soft-pill filter-pill"
          :class="{ active: currentType === item.value }"
          @click="switchType(item.value)"
        >
          {{ item.label }}
        </view>
      </view>
    </view>

    <view class="section-card">
      <view v-if="loading" class="empty-block">
        <text>正在加载收藏内容...</text>
      </view>

      <view v-else-if="favorites.length">
        <view v-for="item in favorites" :key="item.id" class="favorite-card">
          <image :src="normalizeImage(item.cover)" class="favorite-cover" mode="aspectFill"></image>
          <view class="favorite-body">
            <view class="favorite-tag">{{ typeText(item.type) }}</view>
            <text class="favorite-title" @click="openFavorite(item)">{{ item.title }}</text>
            <text class="favorite-meta">{{ item.subTitle || '已收藏内容' }}</text>
            <text class="favorite-summary">{{ shortText(item.summary, 52) || '点击查看详情' }}</text>
            <text class="favorite-time">收藏于 {{ formatDateTime(item.createTime) }}</text>
            <view class="favorite-actions">
              <view class="soft-pill action-pill" @click="openFavorite(item)">查看详情</view>
              <view class="soft-pill action-pill danger" @click="removeFavorite(item)">取消收藏</view>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty-block">
        <text>当前分类下还没有收藏内容。</text>
      </view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getMyFavorites, toggleFavorite } from '@/common/request/api.js'
import { requireLogin } from '@/common/session.js'
import { formatDateTime, normalizeImage, shortText } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      loading: false,
      currentType: '',
      favorites: [],
      filters: [
        { label: '全部', value: '' },
        { label: '资讯', value: 'news' },
        { label: '商品', value: 'product' },
        { label: '活动', value: 'activity' },
        { label: '帖子', value: 'post' }
      ]
    }
  },
  onShow() {
    if (!requireLogin()) {
      return
    }
    this.loadFavorites()
  },
  methods: {
    formatDateTime,
    normalizeImage,
    shortText,
    async loadFavorites() {
      this.loading = true
      try {
        const result = await getMyFavorites({
          page: 1,
          size: 50,
          type: this.currentType
        })
        this.favorites = result && result.list ? result.list : []
      } catch (error) {
        this.favorites = []
      } finally {
        this.loading = false
      }
    },
    switchType(type) {
      this.currentType = type
      this.loadFavorites()
    },
    typeText(type) {
      const map = {
        news: '资讯',
        product: '商品',
        activity: '活动',
        post: '帖子'
      }
      return map[type] || '内容'
    },
    openFavorite(item) {
      const routeMap = {
        news: `/pages/news/detail?id=${item.targetId}`,
        product: `/pages/shop/detail?id=${item.targetId}`,
        activity: `/pages/activity/detail?id=${item.targetId}`
      }
      if (item.type === 'post') {
        uni.navigateTo({ url: `/pages/community/detail?id=${item.targetId}` })
        return
      }
      const url = routeMap[item.type]
      if (url) {
        uni.navigateTo({ url })
      }
    },
    async removeFavorite(item) {
      await toggleFavorite({
        type: item.type,
        targetId: item.targetId
      })
      uni.showToast({
        title: '已取消收藏',
        icon: 'none'
      })
      this.loadFavorites()
    }
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/heritage-subpage.scss";

.favorites-page {
  padding-bottom: calc(64rpx + env(safe-area-inset-bottom));
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.filter-pill.active {
  border-color: $heritage-green;
  background: $heritage-green;
  color: #f7fbf1;
}

.favorite-card {
  display: flex;
  gap: 18rpx;
  margin-top: 16rpx;
  padding: 16rpx;
  border: 1rpx solid rgba(75, 122, 98, 0.18);
  border-radius: 15rpx;
  background: $heritage-card;
  box-shadow: 0 4rpx 11rpx rgba(70, 106, 76, 0.08);
}

.favorite-card:first-child {
  margin-top: 0;
}

.favorite-cover {
  width: 176rpx;
  height: 176rpx;
  border-radius: 11rpx;
  background: linear-gradient(150deg, #eaf2ef, #bad5d0);
  flex-shrink: 0;
}

.favorite-body {
  flex: 1;
}

.favorite-tag {
  display: inline-flex;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  border: 1rpx solid rgba(8, 125, 121, 0.13);
  background: rgba(220, 235, 209, 0.66);
  font-size: 22rpx;
  color: $heritage-deep;
}

.favorite-title {
  display: block;
  margin-top: 12rpx;
  font-size: 30rpx;
  font-weight: 700;
  color: $heritage-ink;
  line-height: 1.5;
}

.favorite-meta,
.favorite-summary,
.favorite-time {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: $heritage-muted;
}

.favorite-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
  flex-wrap: wrap;
}

.action-pill {
  min-width: 148rpx;
  justify-content: center;
}

.action-pill.danger {
  color: $heritage-danger;
}
</style>
