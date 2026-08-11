<template>
  <view class="app-page shop-page with-bottom-nav">
    <view class="safe-top"></view>

    <view class="shop-header">
      <view class="shop-title">商城</view>
      <view class="shop-subtitle">把传统手艺带回日常</view>

      <view class="search-field">
        <text class="search-field__icon" @click="handleSearch">⌕</text>
        <input
          v-model.trim="keyword"
          class="search-field__input"
          placeholder="搜非遗好物、文创、美食"
          confirm-type="search"
          @confirm="handleSearch"
        />
        <text v-if="keyword" class="search-field__clear" @click="clearSearch">×</text>
      </view>
    </view>

    <scroll-view v-if="categoryOptions.length > 1" scroll-x class="category-scroll">
      <view class="category-row">
        <view
          v-for="item in categoryOptions"
          :key="item.value"
          class="category-option"
          :class="{ 'category-option--active': selectedCategory === item.value }"
          @click="selectedCategory = item.value"
        >{{ item.label }}</view>
      </view>
    </scroll-view>

    <view class="goods-section">
      <view class="section-heading">
        <view>
          <view class="section-heading__title">非遗好物</view>
          <view class="section-heading__subtitle">来自传统技艺的当代表达</view>
        </view>
        <view class="cart-entry" @click="goCart">购物车 →</view>
      </view>

      <content-state
        v-if="loading && !loaded"
        type="loading"
        message="正在挑选非遗好物…"
      />
      <content-state
        v-else-if="error"
        type="error"
        :message="error"
        :retrying="loading"
        @retry="loadProducts"
      />
      <view v-else-if="visibleProducts.length" class="goods-grid">
        <view v-for="item in visibleProducts" :key="item.id" class="goods-item" @click="toDetail(item.id)">
          <image
            class="goods-cover"
            :src="productImage(item)"
            mode="aspectFill"
            @error="handleProductImageError(item.id)"
          ></image>
          <view class="goods-name">{{ item.name }}</view>
          <view class="goods-category">{{ item.category || '非遗好物' }}</view>
          <view class="goods-meta">
            <text class="goods-price">¥{{ formatPrice(item.price) }}</text>
            <text v-if="item.sales" class="goods-sales">已售 {{ item.sales }}</text>
          </view>
        </view>
      </view>
      <content-state v-else type="empty" message="没有找到匹配的非遗好物" />
    </view>

    <view v-if="loading && loaded" class="refresh-tip">正在更新…</view>
    <bottom-nav current="shop" />
  </view>
</template>

<script>
import BottomNav from '@/components/bottom-nav.vue'
import ContentState from '@/components/content-state.vue'
import tabbarPageMixin from '@/mixins/tabbar-page.js'
import { getProducts } from '@/common/request/api.js'
import { formatPrice, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    BottomNav,
    ContentState
  },
  mixins: [tabbarPageMixin],
  data() {
    return {
      keyword: '',
      products: [],
      selectedCategory: 'all',
      loading: false,
      loaded: false,
      error: '',
      requestSerial: 0,
      failedProductImages: {}
    }
  },
  computed: {
    categoryOptions() {
      const categories = this.products
        .map(item => item.category)
        .filter(Boolean)
        .filter((item, index, list) => list.indexOf(item) === index)
      return [
        { label: '精选', value: 'all' },
        ...categories.map(item => ({ label: item, value: item }))
      ]
    },
    visibleProducts() {
      if (this.selectedCategory === 'all') return this.products
      return this.products.filter(item => item.category === this.selectedCategory)
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
    async loadProducts() {
      const requestSerial = ++this.requestSerial
      this.loading = true
      this.error = ''
      try {
        const result = await getProducts({
          page: 1,
          size: 30,
          status: 1,
          name: this.keyword
        })
        if (requestSerial !== this.requestSerial) return
        this.products = result && Array.isArray(result.list) ? result.list : []
        if (!this.categoryOptions.some(item => item.value === this.selectedCategory)) {
          this.selectedCategory = 'all'
        }
        this.loaded = true
      } catch (error) {
        if (requestSerial !== this.requestSerial) return
        this.error = error && error.message ? error.message : '商品加载失败，请稍后重试'
      } finally {
        if (requestSerial === this.requestSerial) this.loading = false
      }
    },
    handleSearch() {
      this.selectedCategory = 'all'
      this.loadProducts()
    },
    clearSearch() {
      this.keyword = ''
      this.selectedCategory = 'all'
      this.loadProducts()
    },
    productImage(item) {
      if (this.failedProductImages[item.id]) return '/static/img/logo1.jpg'
      return normalizeImage(item.cover, '/static/img/logo1.jpg')
    },
    handleProductImageError(id) {
      this.failedProductImages[id] = true
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
.shop-page {
  background: $ichip-color-page;
  color: $ichip-color-ink;
}

.shop-header {
  padding: $ichip-space-2 28rpx 0;
}

.shop-title {
  font-family: "STSong", "Songti SC", serif;
  font-size: 42rpx;
  font-weight: $ichip-weight-medium;
  letter-spacing: 6rpx;
}

.shop-subtitle {
  margin-top: 10rpx;
  color: $ichip-color-muted;
  font-size: $ichip-font-body;
  letter-spacing: 2rpx;
}

.search-field {
  display: flex;
  align-items: center;
  height: 78rpx;
  margin-top: $ichip-space-4;
  padding: 0 $ichip-space-3;
  border: 1rpx solid $ichip-color-line;
  border-radius: $ichip-radius-sm;
  background: rgba($ichip-color-surface, 0.72);
}

.search-field__icon {
  margin-right: 14rpx;
  color: $ichip-color-nav-active;
  font-size: 32rpx;
}

.search-field__input {
  flex: 1;
  height: 100%;
  color: $ichip-color-ink;
  font-size: 24rpx;
}

.search-field__clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44rpx;
  height: 44rpx;
  color: $ichip-color-faint;
  font-size: 34rpx;
}

.category-scroll {
  width: 100%;
  margin-top: $ichip-space-4;
  white-space: nowrap;
}

.category-row {
  display: inline-flex;
  gap: 30rpx;
  padding: 0 28rpx 14rpx;
}

.category-option {
  position: relative;
  padding-bottom: 12rpx;
  color: $ichip-color-muted;
  font-size: 23rpx;
}

.category-option--active {
  color: $ichip-color-nav-active;
  font-weight: $ichip-weight-medium;
}

.category-option--active::after {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 28rpx;
  height: 3rpx;
  border-radius: 3rpx;
  background: $ichip-color-nav-active;
  content: "";
  transform: translateX(-50%);
}

.goods-section {
  padding: $ichip-space-section 28rpx 0;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: $ichip-space-4;
}

.section-heading__title {
  font-family: "STSong", "Songti SC", serif;
  font-size: 32rpx;
  font-weight: $ichip-weight-medium;
  letter-spacing: 2rpx;
}

.section-heading__subtitle {
  margin-top: 8rpx;
  color: $ichip-color-muted;
  font-size: 21rpx;
}

.cart-entry {
  color: $ichip-color-nav-active;
  font-size: 22rpx;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: $ichip-space-5 $ichip-space-3;
}

.goods-item {
  min-width: 0;
}

.goods-cover {
  width: 100%;
  height: 335rpx;
  border-radius: $ichip-radius-md;
  background: #ddd5ca;
}

.goods-name {
  display: -webkit-box;
  margin-top: 16rpx;
  overflow: hidden;
  color: $ichip-color-ink;
  font-size: 28rpx;
  font-weight: $ichip-weight-medium;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.goods-category {
  margin-top: 8rpx;
  overflow: hidden;
  color: $ichip-color-muted;
  font-size: 22rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 12rpx;
}

.goods-price {
  color: $ichip-color-brand;
  font-size: 29rpx;
  font-weight: $ichip-weight-medium;
}

.goods-sales {
  color: $ichip-color-faint;
  font-size: 19rpx;
}

.refresh-tip {
  position: fixed;
  top: calc(24rpx + env(safe-area-inset-top));
  right: $ichip-space-3;
  z-index: 50;
  padding: 10rpx 16rpx;
  border-radius: $ichip-radius-tag;
  background: rgba($ichip-color-ink, 0.86);
  color: #fff;
  font-size: 19rpx;
}
</style>
