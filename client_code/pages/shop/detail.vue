<template>
  <view class="app-page detail-page" style="margin-top: 25px;">
    <page-header title="商品详情" />
    <swiper class="detail-swiper" indicator-dots circular>
      <swiper-item v-for="(item, index) in imageList" :key="index">
        <image :src="item" mode="aspectFill" class="detail-image"></image>
      </swiper-item>
    </swiper>

    <view class="section-card detail-card">
      <view class="top-row">
        <view class="soft-pill">{{ product.category || '非遗文创' }}</view>
        <view class="soft-pill collect-pill" :class="{ active: favorited }" @click="handleFavorite">
          {{ favorited ? '已收藏' : '收藏商品' }}
        </view>
      </view>
      <view class="detail-title">{{ product.name }}</view>
      <view class="detail-price">¥{{ formatPrice(product.price) }}</view>
      <view class="detail-line">库存 {{ product.stock || 0 }} · 销量 {{ product.sales || 0 }}</view>
      <view class="detail-desc">{{ product.description || '暂无商品描述' }}</view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">购买数量</text>
        <text class="section-note">下单前可调整数量</text>
      </view>
      <view class="quantity-row">
        <view class="quantity-btn" @click="changeQuantity(-1)">-</view>
        <view class="quantity-value">{{ quantity }}</view>
        <view class="quantity-btn" @click="changeQuantity(1)">+</view>
      </view>
    </view>

    <view class="bottom-bar">
      <view class="bar-price">
        <text>合计</text>
        <text class="bar-price-value">¥{{ formatPrice((product.price || 0) * quantity) }}</text>
      </view>
      <view class="bar-actions">
        <view class="secondary-button mini" @click="handleAddCart">加入购物车</view>
        <view class="primary-button mini" @click="buyNow">立即购买</view>
      </view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { addCart, getFavoriteStatus, getProductDetail, toggleFavorite } from '@/common/request/api.js'
import { isLoggedIn, requireLogin } from '@/common/session.js'
import { formatPrice, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      product: {},
      quantity: 1,
      favorited: false
    }
  },
  computed: {
    imageList() {
      if (this.product.images) {
        const list = String(this.product.images).split(',').filter(Boolean)
        if (list.length) {
          return list.map((item) => normalizeImage(item))
        }
      }
      return [normalizeImage(this.product.cover)]
    }
  },
  onLoad(options) {
    this.loadProduct(options.id)
  },
  methods: {
    formatPrice,
    async loadProduct(id) {
      this.product = await getProductDetail(id)
      this.loadFavoriteStatus()
    },
    async loadFavoriteStatus() {
      if (!isLoggedIn() || !this.product.id) {
        this.favorited = false
        return
      }
      const result = await getFavoriteStatus({
        type: 'product',
        targetId: this.product.id
      })
      this.favorited = !!(result && result.favorited)
    },
    changeQuantity(step) {
      const nextValue = this.quantity + step
      if (nextValue < 1) return
      if (this.product.stock && nextValue > this.product.stock) {
        uni.showToast({ title: '超过库存数量', icon: 'none' })
        return
      }
      this.quantity = nextValue
    },
    async handleAddCart() {
      if (!requireLogin()) return
      await addCart({ productId: this.product.id, quantity: this.quantity })
      uni.showToast({ title: '已加入购物车', icon: 'success' })
    },
    async handleFavorite() {
      if (!requireLogin()) return
      const result = await toggleFavorite({
        type: 'product',
        targetId: this.product.id
      })
      this.favorited = !!(result && result.favorited)
      uni.showToast({
        title: this.favorited ? '已加入收藏' : '已取消收藏',
        icon: 'none'
      })
    },
    buyNow() {
      if (!requireLogin()) return
      uni.setStorageSync('checkoutItems', [
        {
          id: this.product.id,
          quantity: this.quantity,
          product: this.product
        }
      ])
      uni.navigateTo({ url: '/pages/shop/order' })
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-page {
  padding-bottom: 150rpx;
}

.detail-swiper {
  height: 620rpx;
}

.detail-image {
  width: 100%;
  height: 100%;
}

.detail-card {
  margin-top: -26rpx;
  position: relative;
  z-index: 2;
}

.top-row {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.collect-pill.active {
  background: #a6472d;
  color: #fff;
}

.detail-title {
  margin-top: 18rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: #34251f;
}

.detail-price {
  margin-top: 20rpx;
  color: #a6472d;
  font-size: 52rpx;
  font-weight: 700;
}

.detail-line {
  margin-top: 14rpx;
  font-size: 24rpx;
  color: #8a7466;
}

.detail-desc {
  margin-top: 24rpx;
  font-size: 28rpx;
  line-height: 1.8;
  color: #5f4a3f;
}

.quantity-row {
  display: flex;
  align-items: center;
  width: 270rpx;
  border-radius: 999rpx;
  overflow: hidden;
  border: 2rpx solid rgba(166, 71, 45, 0.12);
}

.quantity-btn,
.quantity-value {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 76rpx;
  font-size: 30rpx;
}

.quantity-btn {
  width: 76rpx;
  background: #fff8ef;
}

.quantity-value {
  flex: 1;
  background: #fff;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
  background: rgba(255, 252, 247, 0.98);
  box-shadow: 0 -10rpx 30rpx rgba(77, 53, 39, 0.08);
}

.bar-price {
  display: flex;
  flex-direction: column;
  font-size: 24rpx;
  color: #8a7466;
}

.bar-price-value {
  margin-top: 6rpx;
  color: #a6472d;
  font-size: 34rpx;
  font-weight: 700;
}

.bar-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
  flex: 1;
}

.mini {
  height: 78rpx;
  font-size: 26rpx;
}
</style>
