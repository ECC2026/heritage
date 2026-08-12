<template>
  <view class="app-page shop-page with-bottom-nav">
    <view class="safe-top"></view>

    <!--
      商城页主视觉：复用首页已经归档的水墨山景静态图，只承担视觉装饰。
      图片通过 data 动态绑定，避免微信小程序编译器把根路径误写成临时 assets 地址。
      本区域没有加入青铜兽，也没有改变搜索框原有的 v-model、确认搜索和清空事件。
    -->
    <view class="shop-header">
      <image class="shop-header__background" :src="pageVisualBackground" mode="aspectFill"></image>
      <view class="shop-header__kicker">HERITAGE GOODS</view>
      <view class="shop-title">非遗商城</view>
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
    <!-- 仅切换公共底部导航的视觉主题，current 值及原有页面切换逻辑保持不变。 -->
    <bottom-nav current="shop" theme="green" />
  </view>
</template>

<script>
import BottomNav from '@/components/bottom-nav.vue'
import ContentState from '@/components/content-state.vue'
import tabbarPageMixin from '@/mixins/tabbar-page.js'
import { getProducts } from '@/common/request/api.js'
import { formatPrice, normalizeImage } from '@/common/utils.js'

// 商城与首页共用同一份静态山水背景，避免相同素材在页面目录中重复存储。
const PAGE_VISUAL_BACKGROUND = '/static/home/feature-side-bg.png'

export default {
  components: {
    BottomNav,
    ContentState
  },
  mixins: [tabbarPageMixin],
  data() {
    return {
      // 只用于页眉 UI；不参与商品查询、筛选或路由参数。
      pageVisualBackground: PAGE_VISUAL_BACKGROUND,
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
/*
 * 商城页绿色主题色板与首页保持一致。
 * 颜色只在当前 scoped 页面中生效，不会影响商品详情、购物车或其他页面。
 */
$page-bg: #edf3e7;
$theme-green: #087d79;
$theme-deep: #285f5c;
$theme-ink: #24423f;
$theme-muted: #66807a;
$theme-line: rgba(36, 105, 97, 0.22);
$theme-card: rgba(249, 252, 242, 0.94);

/* 宣纸感背景与首页一致，并预留固定底部导航和设备安全区空间。 */
.shop-page {
  position: relative;
  min-height: 100vh;
  padding-bottom: calc(152rpx + env(safe-area-inset-bottom));
  overflow-x: hidden;
  background:
    radial-gradient(circle at 16% 10%, rgba(255, 255, 255, 0.78) 0, rgba(255, 255, 255, 0) 25%),
    linear-gradient(180deg, #eef4e8 0%, #f5f7ef 52%, $page-bg 100%);
  color: $theme-ink;
}

.shop-page::before {
  position: fixed;
  inset: 0;
  z-index: 0;
  opacity: 0.14;
  background-image:
    linear-gradient(45deg, rgba(34, 102, 94, 0.05) 25%, transparent 25%),
    linear-gradient(-45deg, rgba(34, 102, 94, 0.04) 25%, transparent 25%);
  background-size: 20rpx 20rpx;
  content: '';
  pointer-events: none;
}

/* 页面真实内容统一抬到纹理层上方，避免伪元素影响触控。 */
.safe-top,
.shop-header,
.category-scroll,
.goods-section,
.refresh-tip {
  position: relative;
  z-index: 1;
}

/* 水墨页眉复用首页图片；overflow 裁切让山形自然收在圆角卡片中。 */
.shop-header {
  min-height: 252rpx;
  margin: 8rpx 28rpx 0;
  overflow: hidden;
  padding: 28rpx 28rpx 24rpx;
  border: 1rpx solid $theme-line;
  border-radius: 22rpx;
  background: #f5f8ea;
  box-shadow: 0 8rpx 22rpx rgba(63, 102, 74, 0.12);
}

.shop-header__background {
  position: absolute;
  inset: 0;
  z-index: 0;
  display: block;
  width: 100%;
  height: 100%;
  opacity: 0.86;
  pointer-events: none;
}

.shop-header__kicker,
.shop-title,
.shop-subtitle,
.search-field {
  position: relative;
  z-index: 1;
}

.shop-header__kicker {
  color: rgba(40, 95, 92, 0.68);
  font-family: Georgia, serif;
  font-size: 16rpx;
  letter-spacing: 6rpx;
}

.shop-title {
  margin-top: 6rpx;
  color: $theme-green;
  font-family: "STKaiti", "KaiTi", "STSong", serif;
  font-size: 42rpx;
  font-weight: 600;
  letter-spacing: 7rpx;
}

.shop-subtitle {
  margin-top: 8rpx;
  color: $theme-muted;
  font-size: 21rpx;
  letter-spacing: 2rpx;
}

/* 搜索框保留原 input 和事件，仅改为首页同款半透明圆角形态。 */
.search-field {
  display: flex;
  align-items: center;
  height: 68rpx;
  margin-top: 24rpx;
  padding: 0 22rpx;
  border: 1rpx solid rgba(55, 112, 99, 0.16);
  border-radius: 34rpx;
  background: rgba(244, 248, 236, 0.9);
  box-shadow: 0 3rpx 10rpx rgba(63, 98, 72, 0.08);
}

.search-field__icon {
  margin-right: 14rpx;
  color: $theme-deep;
  font-size: 32rpx;
}

.search-field__input {
  flex: 1;
  height: 100%;
  color: $theme-ink;
  font-size: 23rpx;
}

.search-field__clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44rpx;
  height: 44rpx;
  color: $theme-muted;
  font-size: 34rpx;
}

/* 动态商品分类仍使用原数据源和点击赋值，只把横向文字改为圆角筛选标签。 */
.category-scroll {
  width: 100%;
  margin-top: 24rpx;
  white-space: nowrap;
}

.category-row {
  display: inline-flex;
  gap: 14rpx;
  padding: 0 28rpx 10rpx;
}

.category-option {
  padding: 10rpx 24rpx;
  border: 1rpx solid rgba(46, 110, 100, 0.16);
  border-radius: 999rpx;
  background: rgba(248, 251, 241, 0.75);
  color: $theme-muted;
  font-size: 21rpx;
}

.category-option--active {
  border-color: $theme-green;
  background: $theme-green;
  color: #f7fbf1;
  font-weight: 600;
  box-shadow: 0 5rpx 12rpx rgba(8, 125, 121, 0.17);
}

.goods-section {
  padding: 30rpx 28rpx 0;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

/* 左侧短线是纯 CSS 装饰，不新增图标资源。 */
.section-heading__title {
  position: relative;
  padding-left: 24rpx;
  color: $theme-green;
  font-family: "STKaiti", "KaiTi", "STSong", serif;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 3rpx;
}

.section-heading__title::before {
  position: absolute;
  top: 50%;
  left: 0;
  width: 14rpx;
  height: 3rpx;
  border-radius: 3rpx;
  background: $theme-green;
  content: '';
}

.section-heading__subtitle {
  margin-top: 7rpx;
  color: $theme-muted;
  font-size: 20rpx;
}

.cart-entry {
  padding: 10rpx 17rpx;
  border: 1rpx solid $theme-line;
  border-radius: 999rpx;
  background: rgba(248, 251, 241, 0.78);
  color: $theme-deep;
  font-size: 20rpx;
}

/* 商品网格仍为原来的两列结构，卡片化后与首页推荐区保持同一视觉语言。 */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20rpx 16rpx;
}

.goods-item {
  min-width: 0;
  overflow: hidden;
  padding-bottom: 16rpx;
  border: 1rpx solid rgba(75, 122, 98, 0.2);
  border-radius: 14rpx;
  background: $theme-card;
  box-shadow: 0 5rpx 12rpx rgba(70, 106, 76, 0.12);
}

.goods-cover {
  display: block;
  width: 100%;
  height: 300rpx;
  background: linear-gradient(150deg, #eaf2ef, #bad5d0);
}

.goods-name {
  display: -webkit-box;
  margin: 14rpx 15rpx 0;
  overflow: hidden;
  color: $theme-ink;
  font-size: 25rpx;
  font-weight: 600;
  line-height: 1.42;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.goods-category {
  margin: 7rpx 15rpx 0;
  overflow: hidden;
  color: $theme-muted;
  font-size: 19rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin: 11rpx 15rpx 0;
}

.goods-price {
  color: #a54535;
  font-family: Georgia, serif;
  font-size: 28rpx;
  font-weight: 700;
}

.goods-sales {
  color: #82928d;
  font-size: 18rpx;
}

/* 刷新提示延续首页深绿色胶囊样式，不改变原显示条件。 */
.refresh-tip {
  position: fixed;
  top: calc(24rpx + env(safe-area-inset-top));
  right: 24rpx;
  z-index: 50;
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(23, 91, 84, 0.9);
  color: #fff;
  font-size: 20rpx;
}
</style>
