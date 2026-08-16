<template>
  <view class="app-page signups-page" style="margin-top: 20px;">
    <page-header title="我的报名" />

    <view class="section-card summary-card">
      <view class="section-head">
        <text class="section-title">我的报名</text>
        <text class="section-note">活动参与记录</text>
      </view>
      <view class="summary-grid">
        <view class="summary-item">
          <text class="summary-value">{{ summary.total }}</text>
          <text class="summary-label">全部记录</text>
        </view>
        <view class="summary-item">
          <text class="summary-value">{{ summary.pending }}</text>
          <text class="summary-label">待审核</text>
        </view>
        <view class="summary-item">
          <text class="summary-value">{{ summary.approved }}</text>
          <text class="summary-label">已通过</text>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view v-if="loading" class="empty-block">
        <text>正在加载报名记录...</text>
      </view>

      <view v-else-if="signups.length">
        <view v-for="item in signups" :key="item.id" class="signup-card">
          <image :src="normalizeImage(item.activityCover)" class="signup-cover" mode="aspectFill"></image>
          <view class="signup-body">
            <view class="signup-head">
              <text class="signup-name">{{ item.activityName || '非遗文化活动' }}</text>
              <text v-if="Number(item.status) === 0" class="signup-status status-0">{{ statusText(item.status) }}</text>
              <text v-else-if="Number(item.status) === 1" class="signup-status status-1">{{ statusText(item.status) }}</text>
              <text v-else class="signup-status status-2">{{ statusText(item.status) }}</text>
            </view>
            <text class="signup-meta">{{ item.activityLocation || '地点待更新' }}</text>
            <text class="signup-meta">{{ formatDateTime(item.activityStartTime) }}</text>
            <text v-if="item.remark" class="signup-note">备注：{{ item.remark }}</text>
            <view class="signup-actions">
              <view class="soft-pill action-pill" @click="goToActivity(item.activityId)">查看活动</view>
              <view
                v-if="canCancel(item.status)"
                class="soft-pill action-pill danger"
                @click="handleCancel(item)"
              >取消报名</view>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty-block">
        <text>你还没有报名活动，可以去活动页看看最近的非遗体验。</text>
        <button class="primary-button empty-button" @click="goToActivities">去报名</button>
      </view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { cancelSignup, getMySignups } from '@/common/request/api.js'
import { requireLogin } from '@/common/session.js'
import { formatDateTime, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      loading: false,
      signups: []
    }
  },
  computed: {
    summary() {
      return {
        total: this.signups.length,
        pending: this.signups.filter((item) => Number(item.status) === 0).length,
        approved: this.signups.filter((item) => Number(item.status) === 1).length
      }
    }
  },
  onShow() {
    if (!requireLogin()) {
      return
    }
    this.loadSignups()
  },
  onPullDownRefresh() {
    this.loadSignups(true)
  },
  methods: {
    formatDateTime,
    normalizeImage,
    async loadSignups(fromRefresh) {
      this.loading = true
      try {
        const result = await getMySignups({ page: 1, size: 50 })
        this.signups = result && result.list ? result.list : []
      } catch (error) {
        this.signups = []
      } finally {
        this.loading = false
        if (fromRefresh) {
          uni.stopPullDownRefresh()
        }
      }
    },
    statusText(status) {
      const map = {
        0: '待审核',
        1: '已通过',
        2: '已拒绝',
        3: '已取消'
      }
      return map[status] || '处理中'
    },
    canCancel(status) {
      return [0, 1].indexOf(Number(status)) !== -1
    },
    handleCancel(item) {
      uni.showModal({
        title: '取消报名',
        content: `确认取消“${item.activityName || '当前活动'}”的报名吗？`,
        success: async (res) => {
          if (!res.confirm) {
            return
          }
          await cancelSignup(item.id)
          uni.showToast({
            title: '已取消报名',
            icon: 'success'
          })
          this.loadSignups()
        }
      })
    },
    goToActivity(id) {
      if (!id) {
        return
      }
      uni.navigateTo({ url: `/pages/activity/detail?id=${id}` })
    },
    goToActivities() {
      uni.switchTab({ url: '/pages/activity/list' })
    }
  }
}
</script>

<style lang="scss" scoped>
.signups-page {
  padding: 24rpx;
  padding-bottom: 48rpx;
  background:
    radial-gradient(circle at top right, rgba(166, 71, 45, 0.14), transparent 30%),
    linear-gradient(180deg, #f8efe7 0%, #f4f1ec 100%);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18rpx;
}

.summary-item {
  padding: 22rpx 12rpx;
  border-radius: 22rpx;
  text-align: center;
  background: linear-gradient(180deg, #fff9f4 0%, #f7ede4 100%);
}

.summary-value {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #2f1f18;
}

.summary-label {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #8e6d61;
}

.signup-card {
  display: flex;
  gap: 18rpx;
  padding: 24rpx 0;
  border-top: 1rpx solid #f0e1d8;
}

.signup-card:first-child {
  padding-top: 0;
  border-top: none;
}

.signup-cover {
  width: 180rpx;
  height: 180rpx;
  border-radius: 20rpx;
  background: #f1e4d7;
  flex-shrink: 0;
}

.signup-body {
  flex: 1;
}

.signup-head {
  display: flex;
  justify-content: space-between;
  gap: 14rpx;
}

.signup-name {
  flex: 1;
  font-size: 30rpx;
  font-weight: 700;
  color: #2f1f18;
}

.signup-status {
  font-size: 22rpx;
}

.status-0 {
  color: #c58d1a;
}

.status-1 {
  color: #2e9152;
}

.status-2 {
  color: #b24a3c;
}

.signup-meta,
.signup-note {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #8d7063;
}

.signup-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 18rpx;
  flex-wrap: wrap;
}

.action-pill {
  min-width: 148rpx;
  justify-content: center;
}

.action-pill.danger {
  color: #b24a3c;
}

.empty-button {
  margin-top: 24rpx;
}
</style>
