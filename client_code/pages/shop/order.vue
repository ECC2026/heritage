<template>
  <view class="app-page order-page">
    <page-header title="确认订单" />
    <view class="hero-card">
      <view class="soft-pill">Checkout</view>
      <view class="order-title">确认订单</view>
      <view class="order-note">填写收货信息后即可提交订单，后端会根据商品数量生成正式订单记录。</view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">收货信息</text>
        <text class="section-note">用于创建订单</text>
      </view>
      <view class="field-group">
        <text class="field-label">收货人</text>
        <input v-model.trim="form.receiverName" class="field-input" placeholder="请输入收货人姓名" />
      </view>
      <view class="field-group">
        <text class="field-label">联系电话</text>
        <input v-model.trim="form.receiverPhone" class="field-input" type="number" maxlength="11" placeholder="请输入联系电话" />
      </view>
      <view class="field-group">
        <text class="field-label">收货地址</text>
        <textarea v-model.trim="form.address" class="field-textarea" placeholder="请输入详细收货地址"></textarea>
      </view>
      <view class="field-group">
        <text class="field-label">订单备注</text>
        <textarea v-model.trim="form.remark" class="field-textarea" placeholder="可选，填写补充说明"></textarea>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">商品清单</text>
        <text class="section-note">{{ checkoutItems.length }} 件商品</text>
      </view>

      <view v-if="checkoutItems.length">
        <view class="checkout-item" v-for="item in checkoutItems" :key="item._key">
          <image class="checkout-cover" :src="normalizeImage(item.product.cover, '/static/img/logo1.jpg')" mode="aspectFill"></image>
          <view class="checkout-info">
            <view class="checkout-name">{{ item.product.name }}</view>
            <view class="checkout-meta">¥{{ formatPrice(item.product.price) }} × {{ item.quantity }}</view>
          </view>
          <view class="checkout-subtotal">¥{{ formatPrice(item.product.price * item.quantity) }}</view>
        </view>
      </view>
      <view v-else class="empty-block">还没有待结算的商品，请先从购物车或商品详情页进入。</view>
    </view>

    <view class="cart-footer">
      <view>
        <view class="cart-total-label">应付金额</view>
        <view class="cart-total-price">¥{{ formatPrice(totalPrice) }}</view>
      </view>
      <view class="primary-button cart-submit" @click="submitOrder">提交订单</view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { createOrder, getUserInfo } from '@/common/request/api.js'
import { requireLogin } from '@/common/session.js'
import { formatPrice, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      checkoutItems: [],
      form: {
        receiverName: '',
        receiverPhone: '',
        address: '',
        remark: ''
      }
    }
  },
  computed: {
    totalPrice() {
      return this.checkoutItems.reduce((sum, item) => {
        return sum + Number(item.product.price || 0) * Number(item.quantity || 0)
      }, 0)
    }
  },
  onShow() {
    this.loadCheckout()
    this.loadUserInfo()
  },
  methods: {
    formatPrice,
    normalizeImage,
    loadCheckout() {
      const checkoutItems = uni.getStorageSync('checkoutItems') || []
      this.checkoutItems = checkoutItems.map((item, index) => ({
        ...item,
        _key: item.id || item.productId || (item.product && item.product.id) || `checkout-${index}`
      }))
    },
    async loadUserInfo() {
      if (!requireLogin()) return
      const user = await getUserInfo()
      this.form.receiverName = this.form.receiverName || user.nickname || user.username || ''
      this.form.receiverPhone = this.form.receiverPhone || user.phone || ''
    },
    async submitOrder() {
      if (!this.checkoutItems.length) {
        uni.showToast({ title: '没有可提交的商品', icon: 'none' })
        return
      }
      if (!this.form.receiverName || !this.form.receiverPhone || !this.form.address) {
        uni.showToast({ title: '请完善收货信息', icon: 'none' })
        return
      }

      await createOrder({
        receiverName: this.form.receiverName,
        receiverPhone: this.form.receiverPhone,
        address: this.form.address,
        remark: this.form.remark,
        items: this.checkoutItems.map((item) => ({
          productId: item.product.id,
          quantity: item.quantity
        }))
      })

      uni.removeStorageSync('checkoutItems')
      uni.showToast({ title: '订单提交成功', icon: 'success' })
      setTimeout(() => {
        uni.switchTab({ url: '/pages/profile/index' })
      }, 400)
    }
  }
}
</script>

<style lang="scss" scoped>
.order-page {
  padding-bottom: 150rpx;
}

.order-title {
  margin-top: 20rpx;
  font-size: 48rpx;
  font-weight: 700;
  color: #34251f;
}

.order-note {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #8a7466;
  line-height: 1.7;
}

.field-group {
  margin-bottom: 22rpx;
}

.checkout-item {
  display: flex;
  align-items: center;
  padding: 18rpx 0;
  border-bottom: 1rpx solid rgba(166, 71, 45, 0.08);
}

.checkout-item:last-child {
  border-bottom: none;
}

.checkout-cover {
  width: 140rpx;
  height: 140rpx;
  border-radius: 18rpx;
  background: #f0e5d8;
}

.checkout-info {
  flex: 1;
  margin-left: 18rpx;
}

.checkout-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #34251f;
}

.checkout-meta {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #8a7466;
}

.checkout-subtotal {
  font-size: 28rpx;
  font-weight: 700;
  color: #a6472d;
}
</style>
