<template>
  <!-- 共享主题仅更换购物车 UI；选择、数量调整、缓存结算数据和跳转逻辑均不变。 -->
  <view class="app-page heritage-subpage cart-page">
    <page-header title="购物车" variant="green" />
    <view class="hero-card">
      <view class="soft-pill">Cart</view>
      <view class="cart-title">我的购物车</view>
      <view class="cart-note">购物车接口当前支持加入和查询，数量调整会在本次下单时一并带入。</view>
    </view>

    <view class="section-card" v-if="cartList.length">
      <view class="select-all" @click="toggleAll">
        <text>{{ isAllSelected ? '取消全选' : '全选商品' }}</text>
        <text class="section-note">{{ selectedIds.length }}/{{ cartList.length }}</text>
      </view>

      <view class="cart-item" v-for="item in cartList" :key="item.id">
        <view class="check-dot" :class="{ active: selectedIds.includes(item.id) }" @click="toggleSelect(item.id)"></view>
        <image class="cart-cover" :src="normalizeImage(item.product.cover, '/static/img/logo1.jpg')" mode="aspectFill"></image>
        <view class="cart-main">
          <view class="cart-name">{{ item.product.name }}</view>
          <view class="cart-meta">{{ item.product.category || '非遗文创' }}</view>
          <view class="cart-bottom">
            <text class="cart-price">¥{{ formatPrice(item.product.price) }}</text>
            <view class="count-box">
              <text class="count-btn" @click="changeQuantity(item, -1)">-</text>
              <text class="count-value">{{ item.quantity }}</text>
              <text class="count-btn" @click="changeQuantity(item, 1)">+</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    <view v-else class="section-card empty-block">购物车还是空的，先去商城挑选喜欢的文创商品吧。</view>

    <view class="cart-footer">
      <view>
        <view class="cart-total-label">已选 {{ selectedIds.length }} 件</view>
        <view class="cart-total-price">¥{{ formatPrice(totalPrice) }}</view>
      </view>
      <view class="primary-button cart-submit" @click="toSettle">去结算</view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getCartList } from '@/common/request/api.js'
import { requireLogin } from '@/common/session.js'
import { formatPrice, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      cartList: [],
      selectedIds: []
    }
  },
  computed: {
    totalPrice() {
      return this.cartList
        .filter((item) => this.selectedIds.includes(item.id))
        .reduce((sum, item) => sum + Number(item.product.price || 0) * Number(item.quantity || 0), 0)
    },
    isAllSelected() {
      return this.cartList.length > 0 && this.selectedIds.length === this.cartList.length
    }
  },
  onShow() {
    this.loadCart()
  },
  methods: {
    formatPrice,
    normalizeImage,
    async loadCart() {
      if (!requireLogin()) return
      this.cartList = await getCartList()
      this.selectedIds = this.cartList.map((item) => item.id)
    },
    toggleSelect(id) {
      const index = this.selectedIds.indexOf(id)
      if (index > -1) {
        this.selectedIds.splice(index, 1)
      } else {
        this.selectedIds.push(id)
      }
    },
    toggleAll() {
      this.selectedIds = this.isAllSelected ? [] : this.cartList.map((item) => item.id)
    },
    changeQuantity(item, step) {
      const nextValue = Number(item.quantity || 1) + step
      if (nextValue < 1) return
      if (item.product.stock && nextValue > item.product.stock) {
        uni.showToast({ title: '超过库存数量', icon: 'none' })
        return
      }
      item.quantity = nextValue
    },
    toSettle() {
      const selectedItems = this.cartList.filter((item) => this.selectedIds.includes(item.id))
      if (!selectedItems.length) {
        uni.showToast({ title: '请先选择商品', icon: 'none' })
        return
      }
      uni.setStorageSync('checkoutItems', selectedItems)
      uni.navigateTo({ url: '/pages/shop/order' })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/heritage-subpage.scss";

.cart-page {
  padding-bottom: calc(168rpx + env(safe-area-inset-bottom));
}

.cart-title {
  margin-top: 20rpx;
  font-size: 48rpx;
  font-weight: 700;
  color: $heritage-green;
  font-family: "STKaiti", "KaiTi", "STSong", serif;
  letter-spacing: 4rpx;
}

.cart-note {
  margin-top: 16rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: $heritage-muted;
}

.select-all {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
  font-size: 28rpx;
  color: $heritage-ink;
}

/* 商品从旧分割线列表改成独立浅绿卡片，循环和商品数据结构不变。 */
.cart-item {
  display: flex;
  align-items: center;
  margin-top: 15rpx;
  padding: 15rpx;
  border: 1rpx solid rgba(75, 122, 98, 0.18);
  border-radius: 15rpx;
  background: $heritage-card;
  box-shadow: 0 4rpx 11rpx rgba(70, 106, 76, 0.08);
}

.cart-item:first-of-type {
  margin-top: 0;
}

.check-dot {
  width: 34rpx;
  height: 34rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(36, 105, 97, 0.36);
  margin-right: 18rpx;
}

.check-dot.active {
  border-color: $heritage-green;
  background: $heritage-green;
  box-shadow: inset 0 0 0 7rpx #f5faef;
}

.cart-cover {
  width: 156rpx;
  height: 156rpx;
  border-radius: 11rpx;
  background: linear-gradient(150deg, #eaf2ef, #bad5d0);
}

.cart-main {
  flex: 1;
  margin-left: 18rpx;
}

.cart-name {
  font-size: 30rpx;
  font-weight: 700;
  color: $heritage-ink;
  line-height: 1.5;
}

.cart-meta {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $heritage-muted;
}

.cart-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 18rpx;
}

.cart-price {
  color: #a54535;
  font-family: Georgia, serif;
  font-size: 30rpx;
  font-weight: 700;
}

.count-box {
  display: flex;
  align-items: center;
  border-radius: 999rpx;
  overflow: hidden;
  border: 1rpx solid rgba(36, 105, 97, 0.2);
  background: rgba(237, 245, 229, 0.78);
}

.count-btn,
.count-value {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  font-size: 28rpx;
}

.count-value {
  width: 72rpx;
  background: rgba(250, 252, 244, 0.94);
  color: $heritage-ink;
}

.cart-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid rgba(36, 105, 97, 0.16);
  background: rgba(231, 241, 216, 0.97);
  box-shadow: 0 -8rpx 24rpx rgba(63, 102, 74, 0.1);
}

.cart-total-label {
  font-size: 24rpx;
  color: $heritage-muted;
}

.cart-total-price {
  margin-top: 8rpx;
  font-size: 36rpx;
  font-weight: 700;
  color: #a54535;
  font-family: Georgia, serif;
}

.cart-submit {
  width: 240rpx;
  height: 84rpx;
}
</style>
