<template>
  <view class="app-page detail-page" style="margin-top: 25px;">
    <page-header title="服务详情" />
    <image class="cover" :src="normalizeImage(service.cover, '/static/img/lbt2.jpg')" mode="aspectFill"></image>

    <view class="section-card detail-card">
      <view class="head-row">
        <view class="soft-pill">{{ service.productSystem || '特色服务' }}</view>
        <view class="soft-pill collect-pill">{{ service.statusText || '可预约' }}</view>
      </view>
      <view class="title">{{ service.name }}</view>
      <view class="info-grid">
        <text>价格：¥{{ formatPrice(service.price) }}<text v-if="service.unit">/{{ service.unit }}</text></text>
        <text>服务方：{{ service.providerName || '平台直营' }}</text>
        <text v-if="service.location">地点：{{ service.location }}</text>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">服务简介</text>
      </view>
      <view class="desc">{{ service.summary || service.description || '暂无服务介绍' }}</view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">可选场次</text>
        <text class="section-note">共 {{ schedules.length }} 个场次</text>
      </view>
      <view v-if="loadingSchedules" class="empty-block compact">
        <text>正在加载场次...</text>
      </view>
      <view v-else-if="schedules.length">
        <view
          v-for="item in schedules"
          :key="item.id"
          class="schedule-card"
          :class="{ selected: selectedScheduleId === item.id }"
          @click="selectSchedule(item.id)"
        >
          <view class="schedule-body">
            <view class="schedule-time">{{ formatDateTime(item.startTime) }} — {{ formatDateTime(item.endTime) }}</view>
            <view class="schedule-remain">余 {{ item.remaining || 0 }} 位</view>
          </view>
          <view v-if="selectedScheduleId === item.id" class="schedule-check">✓</view>
        </view>
      </view>
      <view v-else class="empty-block">
        <text>暂无可预约场次，敬请期待。</text>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">服务内容</text>
      </view>
      <view class="desc">{{ service.description || service.summary || '暂无详细说明' }}</view>
    </view>

    <view class="bottom-wrap">
      <view
        class="primary-button"
        :class="{ disabled: !schedules.length }"
        @click="goBook"
      >{{ schedules.length ? '立即预约' : '暂无可约场次' }}</view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getServiceDetail, getServiceSchedules } from '@/common/request/api.js'
import { requireLogin } from '@/common/session.js'
import { formatDateTime, formatPrice, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      service: {},
      schedules: [],
      loadingSchedules: false,
      selectedScheduleId: null
    }
  },
  onLoad(options) {
    this.serviceId = options.id
    this.loadService()
    this.loadSchedules()
  },
  methods: {
    formatDateTime,
    formatPrice,
    normalizeImage,
    async loadService() {
      try {
        this.service = await getServiceDetail(this.serviceId)
      } catch (error) {
        this.service = {}
      }
    },
    async loadSchedules() {
      this.loadingSchedules = true
      try {
        const result = await getServiceSchedules(this.serviceId)
        this.schedules = Array.isArray(result) ? result : []
        if (this.schedules.length) {
          this.selectedScheduleId = this.schedules[0].id
        }
      } catch (error) {
        this.schedules = []
      } finally {
        this.loadingSchedules = false
      }
    },
    selectSchedule(id) {
      this.selectedScheduleId = id
    },
    goBook() {
      if (!this.schedules.length) {
        uni.showToast({ title: '暂无可约场次', icon: 'none' })
        return
      }
      if (!requireLogin()) {
        return
      }
      uni.navigateTo({
        url: `/pages/service/book?serviceId=${this.service.id}&scheduleId=${this.selectedScheduleId || this.schedules[0].id}`
      })
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

.schedule-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 18rpx;
  padding: 22rpx 24rpx;
  border-radius: 20rpx;
  background: rgba(247, 238, 230, 0.9);
}

.schedule-card.selected {
  border: 2rpx solid #a6472d;
  background: rgba(166, 71, 45, 0.08);
}

.schedule-time {
  font-size: 27rpx;
  font-weight: 700;
  color: #34251f;
}

.schedule-remain {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #8d7063;
}

.schedule-check {
  flex-shrink: 0;
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #a6472d;
  color: #fff;
  font-size: 26rpx;
  line-height: 44rpx;
  text-align: center;
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

.compact {
  padding: 10rpx 0;
}
</style>
