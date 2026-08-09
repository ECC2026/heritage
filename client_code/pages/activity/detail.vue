<template>
  <view class="app-page detail-page" style="margin-top: 25px;">
    <page-header title="活动详情" />
    <image class="cover" :src="normalizeImage(activity.cover, '/static/img/lbt2.jpg')" mode="aspectFill"></image>

    <view class="section-card detail-card">
      <view class="head-row">
        <view class="soft-pill">{{ activity.statusText || '进行中' }}</view>
        <view class="soft-pill collect-pill" :class="{ active: favorited }" @click="handleFavorite">
          {{ favorited ? '已收藏' : '收藏活动' }}
        </view>
      </view>
      <view class="title">{{ activity.title || activity.name }}</view>
      <view class="info-grid">
        <text>时间：{{ formatDateTime(activity.startTime) }}</text>
        <text>地点：{{ activity.location || '待定' }}</text>
        <text>组织方：{{ activity.organizer || activity.organizerName || '平台推荐' }}</text>
        <text>报名：{{ activity.signupCount || 0 }}/{{ activity.maxParticipants || activity.limitCount || 0 }}</text>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">活动介绍</text>
        <text class="section-note">报名后由后台审核</text>
      </view>
      <view class="desc">{{ activity.description || '暂无活动详情' }}</view>
    </view>

    <view v-if="mySignup" class="section-card">
      <view class="section-head">
        <text class="section-title">我的报名状态</text>
        <text class="section-note">{{ signupStatusText(mySignup.status) }}</text>
      </view>
      <view class="desc">提交时间：{{ formatDateTime(mySignup.createTime) }}</view>
      <view v-if="mySignup.remark" class="desc signup-remark">报名备注：{{ mySignup.remark }}</view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">报名备注</text>
        <text class="section-note">选填</text>
      </view>
      <textarea v-model.trim="remark" class="field-textarea" placeholder="可填写参与动机、人数说明或其他备注"></textarea>
    </view>

    <view class="bottom-wrap">
      <view class="primary-button" :class="{ disabled: signupDisabled }" @click="handleSignup">{{ signupButtonText }}</view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getActivityDetail, getFavoriteStatus, getMySignups, signupActivity, toggleFavorite } from '@/common/request/api.js'
import { isLoggedIn, requireLogin } from '@/common/session.js'
import { formatDateTime, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      activity: {},
      remark: '',
      favorited: false,
      mySignup: null
    }
  },
  computed: {
    signupDisabled() {
      return !!(this.mySignup && [0, 1].indexOf(Number(this.mySignup.status)) !== -1)
    },
    signupButtonText() {
      if (!this.mySignup) {
        return '提交报名'
      }
      return this.signupStatusText(this.mySignup.status)
    }
  },
  onLoad(options) {
    this.loadActivity(options.id)
  },
  methods: {
    formatDateTime,
    normalizeImage,
    async loadActivity(id) {
      this.activity = await getActivityDetail(id)
      this.loadFavoriteStatus()
      this.loadMySignup()
    },
    async loadFavoriteStatus() {
      if (!isLoggedIn() || !this.activity.id) {
        this.favorited = false
        return
      }
      const result = await getFavoriteStatus({
        type: 'activity',
        targetId: this.activity.id
      })
      this.favorited = !!(result && result.favorited)
    },
    async loadMySignup() {
      if (!isLoggedIn() || !this.activity.id) {
        this.mySignup = null
        return
      }
      const result = await getMySignups({ page: 1, size: 50 })
      const list = result && result.list ? result.list : []
      this.mySignup = list.find((item) => Number(item.activityId) === Number(this.activity.id)) || null
    },
    signupStatusText(status) {
      const map = {
        0: '待审核中',
        1: '已通过',
        2: '审核未通过',
        3: '已取消'
      }
      return map[status] || '提交报名'
    },
    async handleFavorite() {
      if (!requireLogin()) return
      const result = await toggleFavorite({
        type: 'activity',
        targetId: this.activity.id
      })
      this.favorited = !!(result && result.favorited)
      uni.showToast({
        title: this.favorited ? '已加入收藏' : '已取消收藏',
        icon: 'none'
      })
    },
    async handleSignup() {
      if (!requireLogin()) return
      if (this.signupDisabled) {
        uni.showToast({ title: this.signupButtonText, icon: 'none' })
        return
      }
      await signupActivity({
        activityId: this.activity.id,
        remark: this.remark
      })
      uni.showToast({ title: '报名已提交', icon: 'success' })
      this.loadMySignup()
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-page {
  padding-bottom: 140rpx;
}

.cover {
  width: 100%;
  height: 460rpx;
  background: #f0e5d8;
}

.detail-card {
  margin-top: -28rpx;
  position: relative;
  z-index: 2;
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
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-top: 24rpx;
  font-size: 26rpx;
  color: #6f5a4c;
  line-height: 1.7;
}

.desc {
  font-size: 28rpx;
  color: #4f3e35;
  line-height: 1.8;
}

.signup-remark {
  margin-top: 12rpx;
}

.bottom-wrap {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
  background: rgba(255, 252, 247, 0.98);
  box-shadow: 0 -10rpx 30rpx rgba(77, 53, 39, 0.08);
}

.disabled {
  opacity: 0.78;
}
</style>
