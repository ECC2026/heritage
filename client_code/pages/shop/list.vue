<template>
  <view class="app-page with-bottom-nav">
    <view class="safe-top"></view>
    <view class="hero-card shop-hero">
      <view class="soft-pill">Heritage Shop</view>
      <view class="shop-title">文创商城</view>
      <view class="shop-subtitle">把非遗元素转化为可购买、可收藏、可分享的文创商品。</view>

      <view class="shop-search">
        <input v-model.trim="keyword" class="field-input" placeholder="搜索商品名称" confirm-type="search" @confirm="handleSearch" />
      </view>
      <view class="shop-actions">
        <view class="secondary-button" @click="resetSearch">重置</view>
        <view class="primary-button" @click="handleSearch">搜索商品</view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">精选商品</text>
        <text class="section-note" @click="goCart">购物车</text>
      </view>

      <view v-if="products.length" class="goods-grid">
        <view class="goods-card" v-for="item in products" :key="item.id" @click="toDetail(item.id)">
          <image class="goods-cover" :src="normalizeImage(item.cover, '/static/img/logo1.jpg')" mode="aspectFill"></image>
          <view class="goods-name">{{ item.name }}</view>
          <view class="goods-desc">{{ item.category || '非遗文创' }}</view>
          <view class="goods-foot">
            <text class="goods-price">¥{{ formatPrice(item.price) }}</text>
            <text class="goods-sales">已售 {{ item.sales || 0 }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-block">没有找到匹配的商品</view>
    </view>
    <bottom-nav current="shop" />
  </view>
</template>

<script>
import BottomNav from '@/components/bottom-nav.vue'
import tabbarPageMixin from '@/mixins/tabbar-page.js'
import { getProducts } from '@/common/request/api.js'
import { formatPrice, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    BottomNav
  },
  mixins: [tabbarPageMixin],
  data() {
    return {
      keyword: '',
      products: []
    }
  },
  onShow() {
    this.loadProducts()
  },
  onPullDownRefresh() {
    this.loadProducts().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    formatPrice,
    normalizeImage,
    async loadProducts() {
      const result = await getProducts({
        page: 1,
        size: 30,
        status: 1,
        name: this.keyword
      })
      this.products = result.list || []
    },
    handleSearch() {
      this.loadProducts()
    },
    resetSearch() {
      this.keyword = ''
      this.loadProducts()
    },
    toDetail(id) {
      uni.navigateTo({ url: `/pages/shop/detail?id=${id}` })
    },
    goCart() {
      uni.navigateTo({ url: '/pages/shop/cart' })
    }
  }
}
</script>

<style lang="scss" scoped>
.shop-hero {
  margin-top: 12rpx;
}

.shop-title {
  margin-top: 20rpx;
  font-size: 48rpx;
  font-weight: 700;
  color: #34251f;
}

.shop-subtitle {
  margin-top: 14rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: #8a7466;
}

.shop-search {
  margin-top: 28rpx;
}

.shop-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
  margin-top: 18rpx;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.goods-card {
  padding: 18rpx;
  border-radius: 24rpx;
  background: #fff;
}

.goods-cover {
  width: 100%;
  height: 240rpx;
  border-radius: 18rpx;
  background: #f0e5d8;
}

.goods-name {
  margin-top: 14rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #34251f;
  line-height: 1.5;
}

.goods-desc {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #8a7466;
}

.goods-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.goods-price {
  color: #a6472d;
  font-size: 30rpx;
  font-weight: 700;
}

.goods-sales {
  font-size: 22rpx;
  color: #8a7466;
}
</style>
