<template>
  <view class="app-page book-page" style="margin-top: 20px;">
    <page-header title="提交预约" />

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">选择场次</text>
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
          @click="selectedScheduleId = item.id"
        >
          <view class="schedule-body">
            <view class="schedule-time">{{ formatDateTime(item.startTime) }} — {{ formatDateTime(item.endTime) }}</view>
            <view class="schedule-remain">余 {{ item.remaining || 0 }} 位</view>
          </view>
          <view v-if="selectedScheduleId === item.id" class="schedule-check">✓</view>
        </view>
      </view>
      <view v-else class="empty-block">
        <text>暂无可预约场次。</text>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">预约数量</text>
      </view>
      <view class="stepper-row">
        <view class="stepper-btn" @click="changeQuantity(-1)">−</view>
        <text class="stepper-value">{{ quantity }}</text>
        <view class="stepper-btn" @click="changeQuantity(1)">＋</view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">联系人信息</text>
        <text class="section-note">用于确认预约</text>
      </view>
      <view class="field-row">
        <text class="field-label">联系人</text>
        <input v-model.trim="contactName" class="field-input" placeholder="请输入联系人姓名" />
      </view>
      <view class="field-row">
        <text class="field-label">手机号</text>
        <input v-model.trim="contactPhone" class="field-input" type="number" maxlength="11" placeholder="请输入手机号" />
      </view>
    </view>

    <view class="bottom-wrap">
      <view class="primary-button" :class="{ disabled: submitting }" @click="handleSubmit">
        {{ submitting ? '提交中…' : '确认预约' }}
      </view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { bookService, getServiceDetail, getServiceSchedules } from '@/common/request/api.js'
import { requireLogin } from '@/common/session.js'
import { formatDateTime } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      serviceId: '',
      service: {},
      schedules: [],
      loadingSchedules: false,
      selectedScheduleId: null,
      quantity: 1,
      contactName: '',
      contactPhone: '',
      submitting: false
    }
  },
  onLoad(options) {
    this.serviceId = options.serviceId
    this.selectedScheduleId = options.scheduleId || null
    this.loadService()
    this.loadSchedules()
  },
  methods: {
    formatDateTime,
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
        if (!this.selectedScheduleId && this.schedules.length) {
          this.selectedScheduleId = this.schedules[0].id
        }
      } catch (error) {
        this.schedules = []
      } finally {
        this.loadingSchedules = false
      }
    },
    changeQuantity(delta) {
      const next = this.quantity + delta
      if (next < 1 || next > 99) {
        return
      }
      this.quantity = next
    },
    async handleSubmit() {
      if (!requireLogin()) {
        return
      }
      if (!this.selectedScheduleId) {
        uni.showToast({ title: '请选择场次', icon: 'none' })
        return
      }
      if (!this.contactName) {
        uni.showToast({ title: '请填写联系人姓名', icon: 'none' })
        return
      }
      if (!/^1\d{10}$/.test(this.contactPhone)) {
        uni.showToast({ title: '请填写正确的手机号', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        await bookService(this.serviceId, {
          scheduleId: this.selectedScheduleId,
          quantity: this.quantity,
          contactName: this.contactName,
          contactPhone: this.contactPhone
        })
        uni.showToast({ title: '预约成功', icon: 'success' })
        setTimeout(() => {
          uni.redirectTo({ url: '/pages/service/my-bookings' })
        }, 600)
      } catch (error) {
        uni.showToast({ title: (error && error.message) || '预约失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.book-page {
  padding: 24rpx;
  padding-bottom: 140rpx;
  background:
    radial-gradient(circle at top right, rgba(166, 71, 45, 0.14), transparent 30%),
    linear-gradient(180deg, #f8efe7 0%, #f4f1ec 100%);
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

.stepper-row {
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.stepper-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  background: rgba(166, 71, 45, 0.12);
  color: #a6472d;
  font-size: 34rpx;
  line-height: 64rpx;
  text-align: center;
}

.stepper-value {
  min-width: 64rpx;
  text-align: center;
  font-size: 34rpx;
  font-weight: 700;
  color: #34251f;
}

.field-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 22rpx 0;
  border-top: 1rpx solid #f0e1d8;
}

.field-row:first-child {
  border-top: none;
  padding-top: 0;
}

.field-label {
  width: 120rpx;
  flex-shrink: 0;
  font-size: 26rpx;
  color: #6f5a4c;
}

.field-input {
  flex: 1;
  font-size: 28rpx;
  color: #34251f;
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
