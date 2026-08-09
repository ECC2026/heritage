<template>
  <view class="app-page with-bottom-nav">
    <view class="safe-top"></view>
    <view class="hero-card community-hero">
      <view class="hero-head">
        <view>
          <view class="soft-pill">Community</view>
          <view class="community-title">交流社区</view>
          <view class="community-subtitle">在这里分享观展感受、活动心得与非遗学习经验。</view>
        </view>
        <view class="secondary-button publish-btn" @click="toPublish">发帖</view>
      </view>

      <scroll-view scroll-x class="category-scroll">
        <view class="category-list">
          <view
            v-for="item in categories"
            :key="item.value"
            class="category-pill"
            :class="{ active: currentCategory === item.value }"
            @click="changeCategory(item.value)"
          >
            {{ item.label }}
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="section-card">
      <view v-if="posts.length">
        <view class="post-card" v-for="item in posts" :key="item.id">
          <view class="post-user">
            <image class="post-avatar" :src="normalizeImage(item.userAvatar)" mode="aspectFill"></image>
            <view class="post-user-copy">
              <view class="post-user-name">{{ item.userName }}</view>
              <view class="post-user-time">{{ formatDateTime(item.createTime) }}</view>
            </view>
          </view>

          <view class="post-title" @click="openDetail(item.id)">{{ item.title || '社区动态' }}</view>
          <view class="post-content" @click="openDetail(item.id)">{{ item.content }}</view>

          <view class="post-images" v-if="getImages(item).length">
            <image
              v-for="(img, idx) in getImages(item)"
              :key="idx"
              :src="img"
              class="post-image"
              mode="aspectFill"
            ></image>
          </view>

          <view class="post-actions">
            <text @click="likePost(item)">{{ item.liked ? '已赞' : '赞' }} {{ item.likes || 0 }}</text>
            <text @click="toggleComment(item)">{{ activePostId === item.id ? '收起评论' : `评论 ${item.comments || 0}` }}</text>
          </view>

          <view v-if="activePostId === item.id" class="comment-box">
            <view v-if="commentMap[item.id] && commentMap[item.id].length" class="comment-list">
              <view v-for="comment in commentMap[item.id]" :key="comment.id" class="comment-item">
                <image class="comment-avatar" :src="normalizeImage(comment.userAvatar)" mode="aspectFill"></image>
                <view class="comment-body">
                  <text class="comment-name">{{ comment.userName }}</text>
                  <text class="comment-content">{{ comment.content }}</text>
                  <text class="comment-time">{{ formatDateTime(comment.createTime) }}</text>
                </view>
              </view>
            </view>
            <view v-else class="comment-empty">还没有评论，来抢个沙发吧。</view>
            <textarea v-model.trim="commentDraft" class="field-textarea" placeholder="写下你的评论..."></textarea>
            <view class="primary-button comment-submit" @click="submitComment(item)">提交评论</view>
          </view>
        </view>
      </view>
      <view v-else class="empty-block">还没有社区内容，快来发布第一篇帖子吧。</view>
    </view>
    <bottom-nav current="community" />
  </view>
</template>

<script>
import BottomNav from '@/components/bottom-nav.vue'
import tabbarPageMixin from '@/mixins/tabbar-page.js'
import { commentPost, getComments, getPosts, togglePostLike } from '@/common/request/api.js'
import { requireLogin } from '@/common/session.js'
import { formatDateTime, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    BottomNav
  },
  mixins: [tabbarPageMixin],
  data() {
    return {
      posts: [],
      currentCategory: '',
      activePostId: null,
      commentDraft: '',
      commentMap: {},
      categories: [
        { label: '全部', value: '' },
        { label: '交流讨论', value: '交流讨论' },
        { label: '经验分享', value: '经验分享' },
        { label: '活动招募', value: '活动招募' },
        { label: '问题求助', value: '问题求助' },
        { label: '技艺交流', value: '技艺交流' }
      ]
    }
  },
  onShow() {
    this.loadPosts()
  },
  onPullDownRefresh() {
    this.loadPosts().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    formatDateTime,
    normalizeImage,
    async loadPosts() {
      const result = await getPosts({
        page: 1,
        size: 20,
        category: this.currentCategory
      })
      this.posts = (result.list || []).map((item) => ({
        ...item,
        liked: false
      }))
    },
    changeCategory(category) {
      this.currentCategory = category
      this.activePostId = null
      this.commentDraft = ''
      this.loadPosts()
    },
    toPublish() {
      uni.navigateTo({ url: '/pages/community/post' })
    },
    openDetail(id) {
      uni.navigateTo({ url: `/pages/community/detail?id=${id}` })
    },
    getImages(item) {
      if (!item || !item.images) {
        return []
      }
      return String(item.images)
        .split(',')
        .filter(Boolean)
        .map((url) => normalizeImage(url))
    },
    async likePost(item) {
      if (!requireLogin()) return
      const result = await togglePostLike(item.id)
      item.likes = result && result.likes !== undefined ? result.likes : item.likes
      item.liked = !!(result && result.liked)
    },
    async toggleComment(item) {
      this.activePostId = this.activePostId === item.id ? null : item.id
      this.commentDraft = ''
      if (this.activePostId) {
        const result = await getComments({
          postId: item.id,
          page: 1,
          size: 20
        })
        this.commentMap[item.id] = result && result.list ? result.list : []
      }
    },
    async submitComment(item) {
      if (!requireLogin()) return
      if (!this.commentDraft) {
        uni.showToast({ title: '请输入评论内容', icon: 'none' })
        return
      }
      const content = this.commentDraft
      await commentPost({
        postId: item.id,
        content
      })
      uni.showToast({ title: '评论成功', icon: 'success' })
      this.commentDraft = ''
      const list = this.commentMap[item.id] || []
      this.commentMap[item.id] = [
        ...list,
        {
          id: `temp-${Date.now()}`,
          userName: '我',
          userAvatar: '',
          content,
          createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
        }
      ]
      item.comments = (item.comments || 0) + 1
      const result = await getComments({
        postId: item.id,
        page: 1,
        size: 20
      })
      this.commentMap[item.id] = result && result.list ? result.list : []
    }
  }
}
</script>

<style lang="scss" scoped>
.community-hero {
  margin-top: 12rpx;
}

.hero-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
}

.community-title {
  margin-top: 20rpx;
  font-size: 48rpx;
  font-weight: 700;
  color: #34251f;
}

.community-subtitle {
  margin-top: 14rpx;
  font-size: 26rpx;
  color: #8a7466;
  line-height: 1.7;
}

.publish-btn {
  width: 168rpx;
  height: 72rpx;
  font-size: 26rpx;
}

.category-scroll {
  margin-top: 28rpx;
  white-space: nowrap;
}

.category-list {
  display: inline-flex;
  gap: 14rpx;
}

.category-pill {
  padding: 14rpx 24rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.76);
  color: #7d6558;
  font-size: 24rpx;
}

.category-pill.active {
  background: #a6472d;
  color: #fff;
}

.post-card {
  padding: 26rpx 0;
  border-bottom: 1rpx solid rgba(166, 71, 45, 0.08);
}

.post-card:first-child {
  padding-top: 0;
}

.post-card:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.post-user {
  display: flex;
  align-items: center;
}

.post-avatar {
  width: 82rpx;
  height: 82rpx;
  border-radius: 50%;
  background: #f0e5d8;
}

.post-user-copy {
  margin-left: 16rpx;
}

.post-user-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #34251f;
}

.post-user-time {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #8a7466;
}

.post-title {
  margin-top: 18rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #34251f;
}

.post-content {
  margin-top: 14rpx;
  font-size: 28rpx;
  line-height: 1.8;
  color: #5b473d;
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
  border-radius: 18rpx;
  background: #f0e5d8;
}

.post-actions {
  display: flex;
  gap: 28rpx;
  margin-top: 18rpx;
  font-size: 24rpx;
  color: #8a7466;
}

.comment-box {
  margin-top: 18rpx;
}

.comment-list {
  margin-bottom: 18rpx;
}

.comment-item {
  display: flex;
  gap: 12rpx;
  padding: 16rpx 0;
  border-top: 1rpx solid rgba(166, 71, 45, 0.08);
}

.comment-item:first-child {
  border-top: none;
  padding-top: 0;
}

.comment-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #f0e5d8;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-name,
.comment-content,
.comment-time,
.comment-empty {
  display: block;
}

.comment-name {
  font-size: 24rpx;
  font-weight: 700;
  color: #34251f;
}

.comment-content {
  margin-top: 6rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: #5b473d;
}

.comment-time,
.comment-empty {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #8a7466;
}

.comment-submit {
  margin-top: 14rpx;
}
</style>
