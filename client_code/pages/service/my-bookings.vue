<template>
  <view class="app-page bookings-page" style="margin-top: 20px;">
    <page-header title="我的预约" />

    <view class="section-card">
      <view v-if="loading" class="empty-block">
        <text>正在加载预约记录...</text>
      </view>

      <view v-else-if="bookings.length">
        <view v-for="item in bookings" :key="item.id" class="booking-card">
          <view class="booking-head">
            <text class="booking-name">{{ item.serviceName || '非遗服务' }}</text>
            <text class="booking-status" :class="`status-${item.status}`">{{ item.statusText || statusText(item.status) }}</text>
          </view>
          <text class="booking-meta">场次：{{ formatDateTime(item.scheduleStartTime) }}</text>
          <text v-if="item.scheduleEndTime" class="booking-meta">结束：{{ formatDateTime(item.scheduleEndTime) }}</text>
          <text v-if="item.serviceProvider" class="booking-meta">服务方：{{ item.serviceProvider }}</text>
          <text class="booking-meta">数量：{{ item.quantity }} 位</text>
          <view class="booking-actions">
            <view
              v-if="canCancel(item.status)"
              class="soft-pill action-pill danger"
              @click="handleCancel(item)"
            >取消预约</view>
          </view>
        </view>
      </view>

      <view v-else class="empty-block">
        <text>你还没有服务预约，去挑一个感兴趣的体验吧。</text>
        <button class="primary-button empty-button" @click="goToServices">去预约</button>
      </view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { cancelServiceBooking, getMyServiceBookings } from '@/common/request/api.js'
import { requireLogin } from '@/common/session.js'
import { formatDateTime } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      loading: false,
      bookings: []
    }
  },
  onShow() {
    if (!requireLogin()) {
      return
    }
    this.loadBookings()
  },
  onPullDownRefresh() {
    this.loadBookings(true)
  },
  methods: {
    formatDateTime,
    async loadBookings(fromRefresh) {
      this.loading = true
      try {
        const result = await getMyServiceBookings({ page: 1, size: 50 })
        this.bookings = result && result.list ? result.list : []
      } catch (error) {
        this.bookings = []
      } finally {
        this.loading = false
        if (fromRefresh) {
          uni.stopPullDownRefresh()
        }
      }
    },
    statusText(status) {
      const map = {
        0: '已取消',
        1: '已预约',
        2: '已取消'
      }
      return map[status] || '处理中'
    },
    canCancel(status) {
      return Number(status) === 1
    },
    handleCancel(item) {
      uni.showModal({
        title: '取消预约',
        content: `确认取消“${item.serviceName || '当前服务'}”的预约吗？`,
        success: async (res) => {
          if (!res.confirm) {
            return
          }
          try {
            await cancelServiceBooking(item.id)
            uni.showToast({ title: '已取消预约', icon: 'success' })
            this.loadBookings()
          } catch (error) {
            uni.showToast({ title: (error && error.message) || '取消失败', icon: 'none' })
          }
        }
      })
    },
    goToServices() {
      uni.navigateTo({ url: '/pages/service/list' })
    }
  }
}
</script>

<style lang="scss" scoped>
.bookings-page {
  padding: 24rpx;
  padding-bottom: 48rpx;
  background:
    radial-gradient(circle at top right, rgba(166, 71, 45, 0.14), transparent 30%),
    linear-gradient(180deg, #f8efe7 0%, #f4f1ec 100%);
}

.booking-card {
  padding: 24rpx 0;
  border-top: 1rpx solid #f0e1d8;
}

.booking-card:first-child {
  padding-top: 0;
  border-top: none;
}

.booking-head {
  display: flex;
  justify-content: space-between;
  gap: 14rpx;
}

.booking-name {
  flex: 1;
  font-size: 30rpx;
  font-weight: 700;
  color: #2f1f18;
}

.booking-status {
  font-size: 22rpx;
}

.status-1 {
  color: #2e9152;
}

.status-2 {
  color: #b24a3c;
}

.booking-meta {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #8d7063;
}

.booking-actions {
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
