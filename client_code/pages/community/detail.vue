<template>
  <view class="app-page detail-page">
    <page-header title="帖子详情" />

    <view class="section-card detail-card">
      <view class="detail-head">
        <view class="soft-pill">{{ post.category || '社区帖子' }}</view>
        <view class="soft-pill">{{ post.views || 0 }} 浏览</view>
      </view>
      <view class="detail-title">{{ post.title || '社区动态' }}</view>

      <view class="author-row">
        <image class="author-avatar" :src="normalizeImage(post.userAvatar)" mode="aspectFill"></image>
        <view class="author-copy">
          <text class="author-name">{{ post.userName || '匿名用户' }}</text>
          <text class="author-time">{{ formatDateTime(post.createTime) }}</text>
        </view>
      </view>

      <view class="detail-content">{{ post.content || '暂无内容' }}</view>

      <view v-if="images.length" class="image-grid">
        <image v-for="(item, index) in images" :key="index" :src="item" class="detail-image" mode="aspectFill"></image>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">评论区</text>
        <text class="section-note">{{ comments.length }} 条评论</text>
      </view>

      <view v-if="comments.length">
        <view v-for="item in comments" :key="item.id" class="comment-item">
          <image class="comment-avatar" :src="normalizeImage(item.userAvatar)" mode="aspectFill"></image>
          <view class="comment-body">
            <text class="comment-name">{{ item.userName }}</text>
            <text class="comment-content">{{ item.content }}</text>
            <text class="comment-time">{{ formatDateTime(item.createTime) }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-block">还没有评论，来留下第一条交流内容吧。</view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getComments, getPostDetail } from '@/common/request/api.js'
import { formatDateTime, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      post: {},
      comments: []
    }
  },
  computed: {
    images() {
      if (!this.post.images) {
        return []
      }
      return String(this.post.images)
        .split(',')
        .filter(Boolean)
        .map((item) => normalizeImage(item))
    }
  },
  onLoad(options) {
    this.loadData(options.id)
  },
  methods: {
    formatDateTime,
    normalizeImage,
    async loadData(id) {
      const [post, comments] = await Promise.all([
        getPostDetail(id),
        getComments({ postId: id, page: 1, size: 50 })
      ])
      this.post = post || {}
      this.comments = comments && comments.list ? comments.list : []
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-page {
  padding: 24rpx;
  padding-bottom: 48rpx;
  background:
    radial-gradient(circle at top left, rgba(166, 71, 45, 0.14), transparent 28%),
    linear-gradient(180deg, #f8efe7 0%, #f4f1ec 100%);
}

.detail-head {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.detail-title {
  margin-top: 18rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: #34251f;
  line-height: 1.5;
}

.author-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 22rpx;
}

.author-avatar,
.comment-avatar {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  background: #f0e5d8;
  flex-shrink: 0;
}

.author-copy,
.comment-body {
  flex: 1;
}

.author-name,
.comment-name {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #34251f;
}

.author-time,
.comment-time {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #8a7466;
}

.detail-content {
  margin-top: 26rpx;
  font-size: 30rpx;
  line-height: 1.9;
  color: #4f3e35;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 22rpx;
}

.detail-image {
  width: 210rpx;
  height: 210rpx;
  border-radius: 20rpx;
  background: #f0e5d8;
}

.comment-item {
  display: flex;
  gap: 14rpx;
  padding: 20rpx 0;
  border-top: 1rpx solid #f0e1d8;
}

.comment-item:first-child {
  padding-top: 0;
  border-top: none;
}

.comment-content {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  line-height: 1.7;
  color: #5b473d;
}
</style>
