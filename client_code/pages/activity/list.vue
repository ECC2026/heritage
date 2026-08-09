<template>
  <view class="app-page with-bottom-nav">
    <view class="safe-top"></view>
    <view class="hero-card activity-hero">
      <view class="soft-pill">Activity Plaza</view>
      <view class="activity-title">活动广场</view>
      <view class="activity-subtitle">查看近期非遗活动安排，报名参与体验课程、表演和文化交流活动。</view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">近期活动</text>
        <text class="section-note">共 {{ activities.length }} 场</text>
      </view>

      <view v-if="activities.length">
        <view class="activity-card" v-for="item in activities" :key="item.id" @click="toDetail(item.id)">
          <image class="activity-cover" :src="normalizeImage(item.cover, '/static/img/lbt2.jpg')" mode="aspectFill"></image>
          <view class="activity-content">
            <view class="activity-name">{{ item.title || item.name }}</view>
            <view class="activity-line">{{ formatDateTime(item.startTime) }}</view>
            <view class="activity-line">{{ item.location || '地点待定' }}</view>
            <view class="activity-foot">
              <text class="soft-pill">{{ item.statusText || '进行中' }}</text>
              <text class="activity-count">{{ item.signupCount || 0 }}/{{ item.maxParticipants || item.limitCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-block">暂无可报名活动</view>
    </view>
    <bottom-nav current="activity" />
  </view>
</template>

<script>
import BottomNav from '@/components/bottom-nav.vue'
import tabbarPageMixin from '@/mixins/tabbar-page.js'
import { getActivities } from '@/common/request/api.js'
import { formatDateTime, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    BottomNav
  },
  mixins: [tabbarPageMixin],
  data() {
    return {
      activities: []
    }
  },
  onShow() {
    this.loadActivities()
  },
  onPullDownRefresh() {
    this.loadActivities().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    formatDateTime,
    normalizeImage,
    async loadActivities() {
      const result = await getActivities({ page: 1, size: 20 })
      this.activities = result.list || []
    },
    toDetail(id) {
      uni.navigateTo({ url: `/pages/activity/detail?id=${id}` })
    }
  }
}
</script>

<style lang="scss" scoped>
.activity-hero {
  margin-top: 12rpx;
}

.activity-title {
  margin-top: 20rpx;
  font-size: 48rpx;
  font-weight: 700;
  color: #34251f;
}

.activity-subtitle {
  margin-top: 14rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: #8a7466;
}

.activity-card {
  overflow: hidden;
  margin-bottom: 24rpx;
  border-radius: 26rpx;
  background: #fff;
}

.activity-card:last-child {
  margin-bottom: 0;
}

.activity-cover {
  width: 100%;
  height: 280rpx;
  background: #f0e5d8;
}

.activity-content {
  padding: 24rpx;
}

.activity-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #34251f;
}

.activity-line {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #8a7466;
}

.activity-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 18rpx;
}

.activity-count {
  font-size: 24rpx;
  color: #8a7466;
}
</style>
