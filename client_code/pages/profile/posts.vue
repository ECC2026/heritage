<template>
  <!-- 共享主题仅更新 UI；帖子查询、删除确认和发布跳转均保持不变。 -->
  <view class="app-page heritage-subpage posts-page">
    <page-header title="我的帖子" variant="green" />

    <view class="section-card summary-card">
      <view class="section-head">
        <text class="section-title">我的帖子</text>
        <text class="section-note">社区分享记录</text>
      </view>
      <view class="summary-row">
        <text>累计发布 {{ posts.length }} 篇内容</text>
        <view class="primary-button mini-button" @click="goToPublish">继续发帖</view>
      </view>
    </view>

    <view class="section-card">
      <view v-if="loading" class="empty-block">
        <text>正在加载帖子列表...</text>
      </view>

      <view v-else-if="posts.length">
        <view v-for="item in posts" :key="item.id" class="post-card">
          <view class="post-head">
            <view>
              <text class="post-title">{{ item.title || '社区动态' }}</text>
              <text class="post-meta">{{ item.category || '交流讨论' }} · {{ formatDateTime(item.createTime) }}</text>
            </view>
            <view class="post-delete" @click="handleDelete(item)">删除</view>
          </view>
          <text class="post-content">{{ shortText(item.content, 88) }}</text>
          <view v-if="getImages(item).length" class="post-images">
            <image
              v-for="img in getImages(item)"
              :key="img.key"
              :src="img.url"
              class="post-image"
              mode="aspectFill"
            ></image>
          </view>
          <view class="post-footer">
            <text>点赞 {{ item.likes || 0 }}</text>
            <text>评论 {{ item.comments || 0 }}</text>
            <text>浏览 {{ item.views || 0 }}</text>
          </view>
        </view>
      </view>

      <view v-else class="empty-block">
        <text>你还没有发布内容，可以去社区分享体验和心得。</text>
        <button class="primary-button empty-button" @click="goToPublish">发布第一篇</button>
      </view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { deletePost, getMyPosts } from '@/common/request/api.js'
import { requireLogin } from '@/common/session.js'
import { formatDateTime, normalizeImage, shortText } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      loading: false,
      posts: []
    }
  },
  onShow() {
    if (!requireLogin()) {
      return
    }
    this.loadPosts()
  },
  onPullDownRefresh() {
    this.loadPosts(true)
  },
  methods: {
    formatDateTime,
    shortText,
    async loadPosts(fromRefresh) {
      this.loading = true
      try {
        const result = await getMyPosts({ page: 1, size: 50 })
        this.posts = result && result.list ? result.list : []
      } catch (error) {
        this.posts = []
      } finally {
        this.loading = false
        if (fromRefresh) {
          uni.stopPullDownRefresh()
        }
      }
    },
    getImages(item) {
      if (!item || !item.images) {
        return []
      }
      return String(item.images)
        .split(',')
        .filter(Boolean)
        .map((url, index) => ({
          key: `post-image-${item.id || 'default'}-${index}`,
          url: normalizeImage(url)
        }))
    },
    handleDelete(item) {
      uni.showModal({
        title: '删除帖子',
        content: `确认删除“${item.title || '当前帖子'}”吗？`,
        success: async (res) => {
          if (!res.confirm) {
            return
          }
          await deletePost(item.id)
          uni.showToast({
            title: '已删除',
            icon: 'success'
          })
          this.loadPosts()
        }
      })
    },
    goToPublish() {
      uni.navigateTo({ url: '/pages/community/post' })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/heritage-subpage.scss";

.posts-page {
  padding-bottom: calc(64rpx + env(safe-area-inset-bottom));
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
  font-size: 24rpx;
  color: $heritage-muted;
}

.mini-button {
  width: 168rpx;
  height: 68rpx;
  font-size: 24rpx;
}

.post-card {
  margin-top: 16rpx;
  padding: 20rpx;
  border: 1rpx solid rgba(75, 122, 98, 0.18);
  border-radius: 15rpx;
  background: $heritage-card;
  box-shadow: 0 4rpx 11rpx rgba(70, 106, 76, 0.08);
}

.post-card:first-child {
  margin-top: 0;
}

.post-head {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.post-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: $heritage-ink;
}

.post-meta {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $heritage-muted;
}

.post-delete {
  font-size: 22rpx;
  color: $heritage-danger;
}

.post-content {
  display: block;
  margin-top: 16rpx;
  font-size: 28rpx;
  line-height: 1.7;
  color: #4e6660;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.post-image {
  width: 188rpx;
  height: 188rpx;
  border-radius: 12rpx;
  background: linear-gradient(150deg, #eaf2ef, #bad5d0);
}

.post-footer {
  display: flex;
  gap: 24rpx;
  margin-top: 18rpx;
  font-size: 22rpx;
  color: $heritage-muted;
}

.empty-button {
  margin-top: 24rpx;
}
</style>
