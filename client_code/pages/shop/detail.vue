<template>
  <view class="app-page product-page">
    <page-header title="商品详情" variant="quiet" />

    <content-state
      v-if="loading && !loaded"
      class="detail-state"
      type="loading"
      message="正在打开商品档案…"
    />
    <content-state
      v-else-if="error"
      class="detail-state"
      type="error"
      :message="error"
      :retrying="loading"
      @retry="loadProduct"
    />

    <template v-else-if="loaded">
      <swiper class="detail-swiper" :indicator-dots="imageList.length > 1" circular>
        <swiper-item v-for="(item, index) in imageList" :key="index">
          <image
            :src="detailImage(item, index)"
            mode="aspectFill"
            class="detail-image"
            @error="handleDetailImageError(index)"
          ></image>
        </swiper-item>
      </swiper>

      <view class="product-summary">
        <view class="summary-kicker-row">
          <text class="summary-category">{{ product.category || '非遗好物' }}</text>
          <text class="favorite-action" :class="{ 'favorite-action--active': favorited }" @click="handleFavorite">
            {{ favorited ? '已收藏' : '收藏' }}
          </text>
        </view>
        <view class="product-title">{{ product.name }}</view>
        <view class="product-price">¥{{ formatPrice(product.price) }}</view>
        <view v-if="lowStockText || product.sales" class="product-minor-meta">
          <text v-if="lowStockText" class="low-stock">{{ lowStockText }}</text>
          <text v-if="product.sales" class="sales-copy">已售 {{ product.sales }}</text>
        </view>
      </view>

      <view v-if="product.description" class="content-section">
        <view class="content-section__title">商品介绍</view>
        <view class="product-description">{{ product.description }}</view>
      </view>

      <view v-if="product.category" class="content-section craft-section">
        <view class="content-section__title">文化类别</view>
        <view class="craft-name">{{ product.category }}</view>
        <view class="craft-note">商品所关联的现有平台分类</view>
      </view>

      <view class="purchase-section">
        <view>
          <view class="purchase-label">购买数量</view>
          <view v-if="lowStockText" class="purchase-stock">{{ lowStockText }}</view>
        </view>
        <view class="quantity-stepper">
          <view class="quantity-button" @click="changeQuantity(-1)">−</view>
          <view class="quantity-value">{{ quantity }}</view>
          <view class="quantity-button" @click="changeQuantity(1)">＋</view>
        </view>
      </view>

      <view class="purchase-bar">
        <view class="cart-shortcut" @click="goCart">
          <view class="cart-glyph">
            <view class="cart-glyph__basket"></view>
          </view>
          <text>购物车</text>
        </view>
        <view class="bar-total">
          <text class="bar-total__label">合计</text>
          <text class="bar-total__price">¥{{ formatPrice((product.price || 0) * quantity) }}</text>
        </view>
        <view class="bar-actions">
          <view class="action-button action-button--cart" @click="handleAddCart">加入购物车</view>
          <view class="action-button action-button--buy" @click="buyNow">立即购买</view>
        </view>
      </view>
    </template>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import ContentState from '@/components/content-state.vue'
import { addCart, getFavoriteStatus, getProductDetail, toggleFavorite } from '@/common/request/api.js'
import { isLoggedIn, requireLogin } from '@/common/session.js'
import { formatPrice, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    PageHeader,
    ContentState
  },
  data() {
    return {
      productId: null,
      product: {},
      quantity: 1,
      favorited: false,
      loading: false,
      loaded: false,
      error: '',
      failedDetailImages: {}
    }
  },
  computed: {
    imageList() {
      if (this.product.images) {
        const list = String(this.product.images).split(',').filter(Boolean)
        if (list.length) return list.map(item => normalizeImage(item))
      }
      return [normalizeImage(this.product.cover, '/static/img/logo1.jpg')]
    },
    lowStockText() {
      if (this.product.stock === undefined || this.product.stock === null) return ''
      const stock = Number(this.product.stock)
      if (Number.isNaN(stock)) return ''
      if (stock <= 0) return '暂时缺货'
      if (stock <= 5) return `仅余 ${stock} 件`
      return ''
    }
  },
  onLoad(options) {
    this.productId = options.id
    this.loadProduct()
  },
  methods: {
    formatPrice,
    async loadProduct() {
      if (!this.productId || this.loading) return
      this.loading = true
      this.error = ''
      try {
        this.product = await getProductDetail(this.productId)
        this.loaded = true
        this.loadFavoriteStatus().catch(() => {
          this.favorited = false
        })
      } catch (error) {
        this.error = error && error.message ? error.message : '商品详情加载失败，请稍后重试'
      } finally {
        this.loading = false
      }
    },
    detailImage(item, index) {
      if (this.failedDetailImages[index]) return '/static/img/logo1.jpg'
      return item
    },
    handleDetailImageError(index) {
      this.failedDetailImages[index] = true
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
    goCart() {
      uni.navigateTo({ url: '/pages/shop/cart' })
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
.product-page {
  min-height: 100vh;
  padding-bottom: calc(176rpx + env(safe-area-inset-bottom));
  background: $ichip-color-page;
  color: $ichip-color-ink;
}

.detail-state {
  min-height: 520rpx;
}

.detail-swiper {
  width: 100%;
  height: 750rpx;
  background: #ddd5ca;
}

.detail-image {
  width: 100%;
  height: 100%;
}

.product-summary {
  padding: $ichip-space-4 28rpx $ichip-space-section;
}

.summary-kicker-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.summary-category {
  color: $ichip-color-nav-active;
  font-size: 21rpx;
  letter-spacing: 2rpx;
}

.favorite-action {
  padding: 8rpx 12rpx;
  border-bottom: 1rpx solid $ichip-color-line;
  color: $ichip-color-muted;
  font-size: 21rpx;
}

.favorite-action--active {
  border-color: rgba($ichip-color-brand, 0.4);
  color: $ichip-color-brand;
}

.product-title {
  margin-top: 18rpx;
  color: $ichip-color-ink;
  font-family: "STSong", "Songti SC", serif;
  font-size: 40rpx;
  font-weight: $ichip-weight-medium;
  line-height: 1.42;
}

.product-price {
  margin-top: 18rpx;
  color: $ichip-color-brand;
  font-size: 34rpx;
  font-weight: $ichip-weight-medium;
}

.product-minor-meta {
  display: flex;
  gap: $ichip-space-3;
  margin-top: 12rpx;
  color: $ichip-color-faint;
  font-size: 20rpx;
}

.low-stock {
  color: $ichip-color-gold;
}

.content-section {
  margin: 0 28rpx;
  padding: $ichip-space-section 0;
  border-top: 1rpx solid $ichip-color-line;
}

.content-section__title,
.purchase-label {
  color: $ichip-color-ink;
  font-family: "STSong", "Songti SC", serif;
  font-size: 31rpx;
  font-weight: $ichip-weight-medium;
  letter-spacing: 2rpx;
}

.product-description {
  margin-top: $ichip-space-3;
  color: $ichip-color-muted;
  font-size: 26rpx;
  line-height: 1.82;
  white-space: pre-wrap;
}

.craft-name {
  margin-top: $ichip-space-3;
  color: $ichip-color-nav-active;
  font-size: 28rpx;
  font-weight: $ichip-weight-medium;
}

.craft-note {
  margin-top: 8rpx;
  color: $ichip-color-faint;
  font-size: 21rpx;
}

.purchase-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 28rpx;
  padding: 36rpx 0;
  border-top: 1rpx solid $ichip-color-line;
}

.purchase-stock {
  margin-top: 8rpx;
  color: $ichip-color-gold;
  font-size: 20rpx;
}

.quantity-stepper {
  display: flex;
  align-items: center;
  overflow: hidden;
  border: 1rpx solid $ichip-color-line;
  border-radius: $ichip-radius-sm;
  background: $ichip-color-surface;
}

.quantity-button,
.quantity-value {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 62rpx;
  color: $ichip-color-ink;
  font-size: 26rpx;
}

.quantity-button {
  width: 64rpx;
  color: $ichip-color-nav-active;
}

.quantity-value {
  width: 68rpx;
  border-right: 1rpx solid $ichip-color-line;
  border-left: 1rpx solid $ichip-color-line;
  font-size: 24rpx;
}

.purchase-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid $ichip-color-line;
  background: $ichip-color-surface;
  box-shadow: 0 -4rpx 18rpx rgba(44, 39, 35, 0.035);
}

.cart-shortcut {
  display: flex;
  width: 68rpx;
  flex-shrink: 0;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  color: $ichip-color-muted;
  font-size: 18rpx;
}

.cart-glyph {
  position: relative;
  width: 36rpx;
  height: 30rpx;
}

.cart-glyph::before {
  position: absolute;
  top: 2rpx;
  left: 0;
  width: 9rpx;
  height: 2rpx;
  background: $ichip-color-nav-active;
  content: "";
}

.cart-glyph__basket {
  position: absolute;
  top: 5rpx;
  right: 1rpx;
  width: 28rpx;
  height: 16rpx;
  border: 2rpx solid $ichip-color-nav-active;
  border-top-width: 1rpx;
  transform: skewX(-7deg);
}

.cart-glyph__basket::before,
.cart-glyph__basket::after {
  position: absolute;
  bottom: -8rpx;
  width: 4rpx;
  height: 4rpx;
  border: 1rpx solid $ichip-color-nav-active;
  border-radius: 50%;
  background: $ichip-color-surface;
  content: "";
}

.cart-glyph__basket::before {
  left: 3rpx;
}

.cart-glyph__basket::after {
  right: 3rpx;
}

.bar-total {
  display: flex;
  width: 114rpx;
  flex-shrink: 0;
  flex-direction: column;
}

.bar-total__label {
  color: $ichip-color-faint;
  font-size: 18rpx;
}

.bar-total__price {
  margin-top: 3rpx;
  color: $ichip-color-brand;
  font-size: 25rpx;
  font-weight: $ichip-weight-medium;
}

.bar-actions {
  display: grid;
  min-width: 0;
  flex: 1;
  grid-template-columns: 1fr 1fr;
  gap: 12rpx;
}

.action-button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 76rpx;
  border-radius: $ichip-radius-md;
  font-size: 23rpx;
  font-weight: $ichip-weight-medium;
}

.action-button--cart {
  border: 1rpx solid $ichip-color-nav-active;
  background: transparent;
  color: $ichip-color-nav-active;
}

.action-button--buy {
  background: $ichip-color-nav-active;
  color: #fff;
}
</style>
