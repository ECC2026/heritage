<template>
  <view class="app-page orders-page" style="margin-top: 20px;">
    <page-header title="我的订单" />

    <view class="section-card summary-card">
      <view class="section-head">
        <text class="section-title">我的订单</text>
        <text class="section-note">文创商城下单记录</text>
      </view>
      <view class="summary-grid">
        <view class="summary-item">
          <text class="summary-value">{{ summary.total }}</text>
          <text class="summary-label">全部订单</text>
        </view>
        <view class="summary-item">
          <text class="summary-value">{{ summary.shipping }}</text>
          <text class="summary-label">待收货</text>
        </view>
        <view class="summary-item">
          <text class="summary-value">{{ summary.finished }}</text>
          <text class="summary-label">已完成</text>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="filter-row">
        <view
          v-for="item in filters"
          :key="item.value"
          class="soft-pill filter-pill"
          :class="{ active: currentStatus === item.value }"
          @click="switchFilter(item.value)"
        >
          {{ item.label }}
        </view>
      </view>

      <view v-if="loading" class="empty-block">
        <text>正在加载订单列表...</text>
      </view>

      <view v-else-if="filteredOrders.length">
        <view v-for="item in filteredOrders" :key="item.id" class="order-card">
          <view class="order-head">
            <text class="order-no">{{ item.orderNo }}</text>
            <text class="order-status">{{ orderStatusText(item.status) }}</text>
          </view>
          <text class="order-title">{{ orderPreview(item) }}</text>
          <text class="order-address">{{ item.address || '待补充收货地址' }}</text>
          <view class="order-meta">
            <text>{{ formatDateTime(item.createTime) }}</text>
            <text>{{ formatPrice(item.totalPrice || 0) }}</text>
          </view>
          <view v-if="item.status === 2" class="order-actions">
            <button class="receipt-button" @click="confirmReceipt(item)">确认收货</button>
          </view>
        </view>
      </view>

      <view v-else class="empty-block">
        <text>当前筛选下还没有订单，先去商城看看吧。</text>
        <button class="primary-button empty-button" @click="goToShop">前往商城</button>
      </view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getOrders, updateOrderStatus } from '@/common/request/api.js'
import { requireLogin } from '@/common/session.js'
import { formatDateTime, formatPrice } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      loading: false,
      currentStatus: 'all',
      orders: [],
      filters: [
        { label: '全部', value: 'all' },
        { label: '待支付', value: 0 },
        { label: '已支付', value: 1 },
        { label: '已发货', value: 2 },
        { label: '已完成', value: 3 }
      ]
    }
  },
  computed: {
    filteredOrders() {
      if (this.currentStatus === 'all') {
        return this.orders
      }
      return this.orders.filter((item) => Number(item.status) === Number(this.currentStatus))
    },
    summary() {
      return {
        total: this.orders.length,
        shipping: this.orders.filter((item) => [1, 2].indexOf(Number(item.status)) !== -1).length,
        finished: this.orders.filter((item) => Number(item.status) === 3).length
      }
    }
  },
  onShow() {
    if (!requireLogin()) {
      return
    }
    this.loadOrders()
  },
  onPullDownRefresh() {
    this.loadOrders(true)
  },
  methods: {
    formatDateTime,
    formatPrice,
    async loadOrders(fromRefresh) {
      this.loading = true
      try {
        const result = await getOrders({ page: 1, size: 50 })
        this.orders = result && result.list ? result.list : []
      } catch (error) {
        this.orders = []
      } finally {
        this.loading = false
        if (fromRefresh) {
          uni.stopPullDownRefresh()
        }
      }
    },
    switchFilter(status) {
      this.currentStatus = status
    },
    orderPreview(order) {
      if (order.items && order.items.length) {
        return order.items.map((item) => `${item.productName} x${item.quantity}`).join(' / ')
      }
      return order.productName || '文创商品订单'
    },
    orderStatusText(status) {
      const map = {
        0: '待支付',
        1: '已支付',
        2: '已发货',
        3: '已完成',
        4: '已取消'
      }
      return map[status] || '处理中'
    },
    async confirmReceipt(order) {
      uni.showModal({
        title: '确认收货',
        content: '确定已收到商品吗？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            await updateOrderStatus(order.id, { status: 3 })
            uni.showToast({ title: '已确认收货', icon: 'success' })
            this.loadOrders()
          } catch (e) {
            uni.showToast({ title: '操作失败，请重试', icon: 'none' })
          }
        }
      })
    },
    goToShop() {
      uni.switchTab({ url: '/pages/shop/list' })
    }
  }
}
</script>

<style lang="scss" scoped>
.orders-page {
  padding: 24rpx;
  padding-bottom: 48rpx;
  background:
    radial-gradient(circle at top right, rgba(166, 71, 45, 0.14), transparent 28%),
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

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-bottom: 10rpx;
}

.filter-pill.active {
  background: #a6472d;
  color: #fff;
}

.order-card {
  padding: 24rpx 0;
  border-top: 1rpx solid #f0e1d8;
}

.order-card:first-child {
  border-top: none;
}

.order-head,
.order-meta {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.order-no {
  flex: 1;
  font-size: 24rpx;
  color: #7f6357;
}

.order-status {
  font-size: 22rpx;
  color: #a6472d;
}

.order-title {
  display: block;
  margin-top: 12rpx;
  font-size: 28rpx;
  color: #2c1d18;
  line-height: 1.6;
}

.order-address {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #9b7d71;
  line-height: 1.6;
}

.order-meta {
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #9b7d71;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16rpx;
}

.receipt-button {
  padding: 10rpx 28rpx;
  font-size: 24rpx;
  color: #fff;
  background: #a6472d;
  border: none;
  border-radius: 32rpx;
  line-height: 1.5;
}

.empty-button {
  margin-top: 24rpx;
}
</style>
