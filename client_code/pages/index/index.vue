<template>
  <!--
    首页页面结构说明：
    1. 静态视觉素材统一存放在 /static/home/；
    2. 首页业务列表仍然来自统一 API 服务层，不在模板中硬编码正式数据；
    3. 视觉占位文字只用于还原设计稿，不代表对应功能已经接入。
  -->
  <view class="app-page home-page with-bottom-nav">
    <view class="safe-top"></view>

    <!-- 顶部品牌标题和搜索入口。搜索框点击后进入项目现有搜索页。 -->
    <view class="brand-heading">無念万艺</view>

    <view class="search-entry" @tap="goSearch">
      <text class="search-placeholder">搜索非遗项目、传承人、商品或课程</text>
      <text class="search-more">⋮</text>
      <view class="search-symbol"></view>
    </view>

    <!-- 首页主视觉：当前瓶器和山雾由 CSS 绘制，后续可整体替换为 /static/home/ 下的设计图。 -->
    <view class="hero-art">
      <view class="hero-copy">
        <text class="hero-caption">数字非遗 · 东方生活美学</text>
        <text class="hero-en">CHINA</text>
        <text class="hero-year">HERITAGE ARCHIVE</text>
      </view>
      <view class="hero-mist hero-mist--one"></view>
      <view class="hero-mist hero-mist--two"></view>
      <view class="vase vase--far"><view class="vase-neck"></view></view>
      <view class="vase vase--left"><view class="vase-neck"></view></view>
      <view class="vase vase--main"><view class="vase-neck"></view><view class="vase-branch"></view></view>
      <view class="vase vase--right"><view class="vase-neck"></view></view>
      <!-- 使用运行时绑定，防止 UniApp 将 /static 路径改写成未生成的 /assets 哈希路径。 -->
      <image class="hero-mascot" :src="mascotImage" mode="aspectFit"></image>
    </view>

    <!-- 首次进入显示加载状态；请求失败时提供明确错误提示和重新加载按钮。 -->
    <view v-if="loading && !loaded" class="page-state-card">
      <content-state type="loading" message="正在加载首页内容…" />
    </view>

    <view v-else-if="loadError" class="page-state-card">
      <content-state type="error" :message="loadError" :retrying="loading" @retry="loadHome" />
    </view>

    <template v-else>
      <!-- 六大非遗体系：分类名称来自接口，六边形边框和分类图标属于首页静态视觉。 -->
      <view class="system-section">
        <view class="ornament-title"><text>六大非遗体系</text></view>
        <view v-if="homeData.categories.length" class="category-row">
          <view
            v-for="item in homeData.categories.slice(0, 6)"
            :key="item.id"
            class="category-item"
            @tap="goCommunity"
          >
            <view class="category-hex">
              <view class="category-hex__gap">
                <view class="category-hex__line">
                  <view class="category-hex__content">
                    <image
                      v-if="categoryIconSource(item.name)"
                      class="category-glyph"
                      :src="categoryIconSource(item.name)"
                      mode="aspectFit"
                    ></image>
                    <text v-else class="category-fallback">艺</text>
                  </view>
                </view>
              </view>
            </view>
            <text class="category-name">{{ item.name }}</text>
          </view>
        </view>
        <content-state v-else type="empty" message="暂无非遗分类" />
      </view>

      <!-- 三张主题入口卡片。AI 卡片当前只保留设计稿文字和视觉，不绑定 AI 业务。 -->
      <view class="feature-grid">
        <view class="feature-card feature-card--knowledge">
          <view class="feature-copy">
            <text class="feature-title">AI虚拟体验专区</text>
            <text class="feature-desc">AI数字人讲解｜非遗互动语音问答</text>
            <text class="feature-link">查看更多 ›</text>
          </view>
          <!-- 与主视觉复用同一青铜兽文件，避免项目内保存重复图片。 -->
          <image class="feature-mascot" :src="mascotImage" mode="aspectFit"></image>
        </view>

        <view class="feature-stack">
          <view class="feature-card feature-card--city">
            <view class="feature-copy" @tap="goActivity">
              <text class="feature-title">同城 · {{ selectedCityName }}非遗</text>
              <text class="feature-desc">发现身边的手艺与活动</text>
              <text v-if="cityError" class="feature-error">{{ cityError }}</text>
            </view>
            <picker
              class="city-picker"
              :range="cities"
              range-key="name"
              :value="cityIndex"
              :disabled="loading || !cities.length"
              @change.stop="handleCityChange"
            >
              <text class="city-change">切换 ›</text>
            </picker>
          </view>

          <view class="feature-card feature-card--inheritor" @tap="goCommunity">
            <view class="feature-copy">
              <text class="feature-title">热门传承人</text>
              <text class="feature-desc">听见守艺人的传承故事</text>
              <text class="feature-link">查看更多 ›</text>
            </view>
            <view class="feature-emblem feature-emblem--small"><text>承</text></view>
          </view>
        </view>
      </view>

      <!-- 推荐区域：标签只切换当前首页数据的展示方式，不重复请求接口。 -->
      <view class="recommendation-section">
        <view class="recommend-tabs">
          <view
            v-for="tab in recommendationTabs"
            :key="tab.key"
            class="recommend-tab"
            :class="activeRecommendation === tab.key ? 'recommend-tab--active' : ''"
            @tap="activeRecommendation = tab.key"
          >
            <text>{{ tab.label }}</text>
          </view>
        </view>

        <view v-if="recommendationItems.length" class="recommend-grid">
          <view
            v-for="item in recommendationItems"
            :key="item.key"
            class="recommend-card"
            @tap="openRecommendation(item)"
          >
            <view class="recommend-cover">
              <image
                v-if="hasUsableImage(item.image)"
                class="recommend-image"
                :src="normalizeImage(item.image)"
                mode="aspectFill"
              ></image>
              <view v-else class="recommend-placeholder">
                <view class="mini-vase mini-vase--one"></view>
                <view class="mini-vase mini-vase--two"></view>
                <view class="mini-vase mini-vase--three"></view>
              </view>
              <text class="recommend-badge">{{ item.eyebrow }}</text>
            </view>
            <view class="recommend-body">
              <text class="recommend-name">{{ item.title }}</text>
              <text class="recommend-desc">{{ item.description || '在传统技艺里，发现今日生活的新意。' }}</text>
              <view class="recommend-meta">
                <text v-if="item.price !== null" class="recommend-price">¥{{ formatPrice(item.price) }}</text>
                <text v-else>{{ item.meta }}</text>
                <text class="recommend-arrow">›</text>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="recommend-empty">
          <content-state type="empty" :message="activeRecommendationEmptyText" />
        </view>
      </view>
    </template>

    <view v-if="loading && loaded" class="refresh-mask">
      <view class="refresh-tip">正在更新首页…</view>
    </view>

    <!-- 首页启用绿色底部导航主题；其他页面不传 theme 时仍使用原默认主题。 -->
    <bottom-nav current="home" theme="green" />
  </view>
</template>

<script>
import BottomNav from '@/components/bottom-nav.vue'
import ContentState from '@/components/content-state.vue'
import tabbarPageMixin from '@/mixins/tabbar-page.js'
import { getCities, getHome } from '@/common/request/api.js'
import { formatDateTime, formatPrice, normalizeImage, shortText } from '@/common/utils.js'

// 保存用户最近选择的城市，重新进入首页时优先恢复该城市。
const CITY_STORAGE_KEY = 'home-city-code'

//
// 首页静态图片统一放在 /static/home/。
// 此地址存入 data 后通过 :src 绑定，避免 UniApp 编译器把固定模板地址改写为
// /assets/bronze-beast.[hash].png，而小程序产物中又没有对应文件。
//
const MASCOT_IMAGE = '/static/home/bronze-beast.png'

// 微信小程序 tabBar 页面必须使用 switchTab 跳转，普通页面使用 navigateTo。
const TAB_ROUTES = [
  '/pages/index/index',
  '/pages/shop/list',
  '/pages/activity/list',
  '/pages/community/index',
  '/pages/profile/index'
]

//
// 分类名称由后端返回，这里只维护“分类名称 -> 首页本地图标”的视觉映射。
// 所有首页专用图标和青铜兽都集中存放在 client_code/static/home/。
//
const CATEGORY_ICON_SOURCES = {
  '传统美术': '/static/home/traditional-art.svg',
  '传统技艺': '/static/home/traditional-craft.svg',
  '传统民俗': '/static/home/traditional-folk.svg',
  '传统医药': '/static/home/traditional-medicine.svg',
  '传统戏曲': '/static/home/traditional-opera.svg',
  '非遗美食': '/static/home/traditional-food.svg'
}

// 首页推荐标签配置。key 用于数据分流，label 只负责页面展示。
const RECOMMENDATION_TABS = [
  { key: 'today', label: '今日推荐' },
  { key: 'product', label: '热门文创' },
  { key: 'course', label: '本周手作' },
  { key: 'activity', label: '近期活动' }
]

//
// 为首页接口数据提供稳定的初始结构。
// 即使接口字段暂时为空，模板中的数组操作也不会因为 undefined 而报错。
//
function createEmptyHomeData() {
  return {
    city: null,
    banners: [],
    categories: [],
    heritageProjects: [],
    inheritors: [],
    courses: [],
    products: [],
    activities: [],
    news: []
  }
}

// 把接口中的异常空值统一转换为空数组，保护 v-for、slice 和 map 等操作。
function toArray(value) {
  return Array.isArray(value) ? value : []
}

export default {
  components: {
    BottomNav,
    ContentState
  },
  mixins: [tabbarPageMixin],
  data() {
    return {
      // 首页主体业务数据，由 /api/home 一次性返回。
      homeData: createEmptyHomeData(),
      // 城市列表单独请求；城市接口失败不会阻断整个首页显示。
      cities: [],
      selectedCityCode: '',
      // loading 控制首次加载和下拉刷新；loaded 用于区分两种加载状态。
      loading: false,
      loaded: false,
      // 首页主请求失败使用 loadError；城市附属请求失败使用 cityError。
      loadError: '',
      cityError: '',
      // 青铜兽使用动态地址，原因见 MASCOT_IMAGE 上方注释。
      mascotImage: MASCOT_IMAGE,
      activeRecommendation: 'today',
      recommendationTabs: RECOMMENDATION_TABS
    }
  },
  computed: {
    // picker 需要数组下标，因此根据当前城市编码反查位置。
    cityIndex() {
      const index = this.cities.findIndex(item => item.code === this.selectedCityCode)
      return index >= 0 ? index : 0
    },
    // 优先展示首页接口确认的城市，其次使用城市列表中的当前选择。
    selectedCityName() {
      if (this.homeData.city && this.homeData.city.name) return this.homeData.city.name
      const selected = this.cities[this.cityIndex]
      return selected && selected.name ? selected.name : '选择城市'
    },
    //
    // 将商品、课程、活动、非遗项目和传承人的不同字段统一成卡片结构，
    // 模板只负责渲染一种卡片，避免为每个标签复制一整套页面代码。
    //
    recommendationItems() {
      if (this.activeRecommendation === 'product') {
        return this.homeData.products.slice(0, 6).map(item => ({
          key: `product-${item.id}`,
          type: 'product',
          id: item.id,
          image: item.cover,
          eyebrow: item.category || '非遗文创',
          title: item.name,
          description: this.shortText(item.summary || item.description, 30),
          price: item.price,
          meta: ''
        }))
      }

      if (this.activeRecommendation === 'course') {
        return this.homeData.courses.slice(0, 6).map(item => ({
          key: `course-${item.id}`,
          type: 'course',
          id: item.id,
          image: item.cover,
          eyebrow: item.category || '手作体验',
          title: item.title,
          description: this.shortText(item.summary || item.location, 30),
          price: item.price,
          meta: ''
        }))
      }

      if (this.activeRecommendation === 'activity') {
        return this.homeData.activities.slice(0, 6).map(item => ({
          key: `activity-${item.id}`,
          type: 'activity',
          id: item.id,
          image: item.cover,
          eyebrow: '同城活动',
          title: item.name,
          description: this.shortText(item.summary || item.description || item.location, 30),
          price: null,
          meta: this.formatDateTime(item.startTime)
        }))
      }

      const projectItems = this.homeData.heritageProjects.map(item => ({
        key: `project-${item.id}`,
        type: 'project',
        id: item.id,
        image: item.cover,
        eyebrow: item.level || item.category || '非遗项目',
        title: item.name,
        description: this.shortText(item.summary || item.description, 30),
        price: null,
        meta: item.region || '非遗代表性项目'
      }))
      const inheritorItems = this.homeData.inheritors.map(item => ({
        key: `inheritor-${item.id}`,
        type: 'inheritor',
        id: item.id,
        image: item.portrait,
        eyebrow: '守艺人',
        title: item.displayName,
        description: this.shortText(item.profile, 30),
        price: null,
        meta: item.skillType || '非遗传承人'
      }))
      return projectItems.concat(inheritorItems).slice(0, 6)
    },
    // 不同标签给出对应的空数据说明，避免用户误以为页面加载异常。
    activeRecommendationEmptyText() {
      const messages = {
        today: '暂无今日推荐',
        product: '暂无推荐文创商品',
        course: '本周暂无可预约手作课程',
        activity: `${this.selectedCityName}暂无近期活动`
      }
      return messages[this.activeRecommendation]
    }
  },
  // 页面首次加载时恢复城市选择，然后拉取首页数据。
  onLoad() {
    const storedCityCode = uni.getStorageSync(CITY_STORAGE_KEY)
    this.selectedCityCode = typeof storedCityCode === 'string' ? storedCityCode : ''
    this.loadHome()
  },
  // 下拉刷新复用同一加载方法，并确保动画在成功或失败后都能停止。
  onPullDownRefresh() {
    this.loadHome().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    formatDateTime,
    formatPrice,
    normalizeImage,
    shortText,
    //
    // 首页核心加载流程：
    // 1. 防止重复点击或下拉触发并发请求；
    // 2. 首页主接口和城市接口并行请求；
    // 3. 城市接口失败只降级城市切换，不让整个首页进入错误页；
    // 4. 首页主接口失败时保留重试入口。
    //
    async loadHome() {
      if (this.loading) return
      this.loading = true
      this.loadError = ''
      this.cityError = ''

      const params = this.selectedCityCode ? { cityCode: this.selectedCityCode } : {}
      try {
        const cityRequest = getCities()
          .then(value => ({ success: true, value }))
          .catch(error => ({ success: false, error }))
        const [homeResult, cityResult] = await Promise.all([
          getHome(params),
          cityRequest
        ])

        this.applyHomeData(homeResult)

        if (cityResult.success) {
          this.cities = toArray(cityResult.value)
        } else {
          this.cityError = '城市列表暂时不可用'
        }

        const currentCity = this.homeData.city
        if (currentCity && currentCity.code) {
          this.selectedCityCode = currentCity.code
          uni.setStorageSync(CITY_STORAGE_KEY, currentCity.code)
          if (!this.cities.some(item => item.code === currentCity.code)) {
            this.cities = [currentCity].concat(this.cities)
          }
        }
        this.loaded = true
      } catch (error) {
        this.loadError = this.getErrorMessage(error, '首页加载失败，请检查网络后重试')
      } finally {
        this.loading = false
      }
    },
    // 统一整理后端返回值，确保每一个列表字段始终都是数组。
    applyHomeData(payload) {
      const data = payload || {}
      this.homeData = {
        city: data.city || null,
        banners: toArray(data.banners),
        categories: toArray(data.categories),
        heritageProjects: toArray(data.heritageProjects),
        inheritors: toArray(data.inheritors),
        courses: toArray(data.courses),
        products: toArray(data.products),
        activities: toArray(data.activities),
        news: toArray(data.news)
      }
    },
    // 优先显示请求层返回的具体错误；没有具体信息时使用页面级兜底文案。
    getErrorMessage(error, fallback) {
      return error && error.message ? error.message : fallback
    },
    // 城市切换后持久化编码，并使用新城市重新加载首页推荐内容。
    async handleCityChange(event) {
      const nextCity = this.cities[Number(event.detail.value)]
      if (!nextCity || nextCity.code === this.selectedCityCode) return
      this.selectedCityCode = nextCity.code
      uni.setStorageSync(CITY_STORAGE_KEY, nextCity.code)
      await this.loadHome()
    },
    // 根据接口分类名称查找本地图标；未知分类返回空字符串并显示“艺”字兜底。
    categoryIconSource(name) {
      return CATEGORY_ICON_SOURCES[name] || ''
    },
    //
    // 旧项目中的示例轮播图和 Logo 与当前设计主题不匹配。
    // 接口若仍返回这些旧素材，首页使用 CSS 瓶器占位，避免错误风格直接露出。
    //
    hasUsableImage(value) {
      if (!value) return false
      const source = String(value).toLowerCase()
      return ![
        '/static/img/logo.png',
        '/static/img/logo1.jpg',
        '/static/img/1.png',
        '/static/img/lbt1.jpg',
        '/static/img/lbt2.jpg',
        '/static/img/lbt3.jpg'
      ].includes(source)
    },
    // 根据标准化后的卡片类型选择对应页面；暂未独立实现的类型进入已有模块。
    openRecommendation(item) {
      if (!item) return
      if (item.type === 'product') {
        this.goProductDetail(item.id)
        return
      }
      if (item.type === 'activity') {
        this.goActivityDetail(item.id)
        return
      }
      if (item.type === 'course') {
        this.goActivity()
        return
      }
      this.goCommunity()
    },
    handleBanner(item) {
      if (!item || !item.link || !String(item.link).startsWith('/pages/')) return
      this.openPage(item.link)
    },
    // 按微信小程序路由规则区分 tabBar 页面和普通页面。
    openPage(url) {
      if (TAB_ROUTES.includes(url)) {
        uni.switchTab({ url })
        return
      }
      uni.navigateTo({ url })
    },
    goSearch() {
      uni.navigateTo({ url: '/pages/search/index' })
    },
    goProductDetail(id) {
      uni.navigateTo({ url: `/pages/shop/detail?id=${id}` })
    },
    goActivityDetail(id) {
      uni.navigateTo({ url: `/pages/activity/detail?id=${id}` })
    },
    goNewsDetail(id) {
      uni.navigateTo({ url: `/pages/news/detail?id=${id}` })
    },
    goShop() {
      uni.switchTab({ url: '/pages/shop/list' })
    },
    goActivity() {
      uni.switchTab({ url: '/pages/activity/list' })
    },
    goNewsList() {
      uni.navigateTo({ url: '/pages/news/list' })
    },
    goCommunity() {
      uni.switchTab({ url: '/pages/community/index' })
    },
    handleUnavailable(pageName) {
      uni.showToast({
        title: `${pageName}将在对应模块中实现`,
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.home-page {
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
  background: $ichip-color-page;
  color: $ichip-color-ink;
}

.home-hero {
  position: relative;
  padding: $ichip-space-2 $ichip-space-4 $ichip-space-3;
}

.location-row,
.location-picker,
.section-head,
.course-bottom,
.product-meta,
.activity-bottom,
.news-meta {
  display: flex;
  align-items: center;
}

.location-row,
.section-head,
.course-bottom,
.product-meta,
.activity-bottom,
.news-meta {
  justify-content: space-between;
}

.location-copy {
  min-width: 0;
}

.location-label {
  display: block;
  margin-bottom: $ichip-space-1;
  color: $ichip-color-muted;
  font-size: $ichip-font-small;
  letter-spacing: 3rpx;
}

.location-picker {
  min-height: 48rpx;
}

.location-pin {
  width: 13rpx;
  height: 13rpx;
  margin-right: 13rpx;
  overflow: hidden;
  border: 2rpx solid $ichip-color-brand;
  border-radius: 50%;
  color: transparent;
  font-size: 1rpx;
}

.location-name {
  color: $ichip-color-ink;
  font-size: 32rpx;
  font-weight: $ichip-weight-semibold;
  letter-spacing: 2rpx;
}

.location-arrow {
  margin-left: 10rpx;
  color: $ichip-color-muted;
  font-size: 24rpx;
}

.location-error {
  display: block;
  margin-top: $ichip-space-1;
  color: $ichip-color-brand;
  font-size: $ichip-font-caption;
}

.brand-mark {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  min-width: 92rpx;
  padding-left: $ichip-space-2;
  border-left: 2rpx solid rgba($ichip-color-brand, 0.36);
  color: $ichip-color-brand;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 30rpx;
  font-weight: $ichip-weight-semibold;
  letter-spacing: 5rpx;
}

.brand-mark-sub {
  margin-top: 4rpx;
  color: $ichip-color-muted;
  font-family: "PingFang SC", sans-serif;
  font-size: 16rpx;
  font-weight: $ichip-weight-regular;
  letter-spacing: 4rpx;
}

.home-title {
  margin-top: $ichip-space-4;
  color: $ichip-color-ink;
  font-family: "STSong", "Songti SC", serif;
  font-size: $ichip-font-display;
  font-weight: $ichip-weight-semibold;
  letter-spacing: 2rpx;
}

.home-subtitle {
  margin-top: 12rpx;
  color: $ichip-color-muted;
  font-size: $ichip-font-body;
  letter-spacing: 1rpx;
}

.search-entry {
  display: flex;
  align-items: center;
  min-height: 78rpx;
  margin-top: $ichip-space-4;
  padding: 0 $ichip-space-3;
  border: 1rpx solid $ichip-color-line;
  border-radius: $ichip-radius-md;
  background: rgba(250, 247, 241, 0.72);
}

.search-icon {
  margin-right: 14rpx;
  color: $ichip-color-jade;
  font-size: 32rpx;
}

.search-placeholder {
  flex: 1;
  overflow: hidden;
  color: $ichip-color-muted;
  font-size: 24rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-action {
  margin-left: $ichip-space-2;
  color: $ichip-color-brand;
  font-size: $ichip-font-small;
  font-weight: $ichip-weight-medium;
  letter-spacing: 2rpx;
}

.page-state-card {
  min-height: 420rpx;
  margin: $ichip-space-3 $ichip-space-4;
  padding: $ichip-space-4;
  border: 1rpx solid $ichip-color-line;
  border-radius: $ichip-radius-md;
  background: rgba($ichip-color-surface, 0.6);
}

.home-section {
  margin: $ichip-space-section $ichip-space-4 0;
}

.banner-section {
  margin-top: $ichip-space-3;
}

.banner-swiper {
  height: 364rpx;
  overflow: hidden;
  border-radius: $ichip-radius-banner;
  background: #ddd5ca;
}

.banner-item,
.banner-image {
  width: 100%;
  height: 100%;
}

.banner-item {
  position: relative;
}

.banner-mask {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 100rpx $ichip-space-4 $ichip-space-4;
  background: linear-gradient(180deg, rgba(23, 19, 16, 0), rgba(23, 19, 16, 0.82));
}

.banner-kicker {
  margin-bottom: 10rpx;
  color: rgba(255, 255, 255, 0.72);
  font-size: $ichip-font-caption;
  letter-spacing: 6rpx;
}

.banner-title {
  max-width: 86%;
  overflow: hidden;
  color: #fffdf9;
  font-family: "STSong", "Songti SC", serif;
  font-size: 36rpx;
  font-weight: $ichip-weight-medium;
  letter-spacing: 1rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.banner-link {
  margin-top: 10rpx;
  color: rgba(255, 255, 255, 0.78);
  font-size: $ichip-font-small;
}

.section-head {
  align-items: flex-end;
  margin-bottom: $ichip-space-4;
}

.section-title {
  color: $ichip-color-ink;
  font-family: "STSong", "Songti SC", serif;
  font-size: 36rpx;
  font-weight: $ichip-weight-medium;
  letter-spacing: 2rpx;
}

.section-subtitle {
  margin-top: $ichip-space-1;
  color: $ichip-color-muted;
  font-size: $ichip-font-small;
  letter-spacing: 1rpx;
}

.section-note {
  padding-bottom: 2rpx;
  color: $ichip-color-jade;
  font-size: $ichip-font-small;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $ichip-space-4 $ichip-space-2;
  padding: 2rpx 0;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.category-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 88rpx;
  height: 88rpx;
  border: 1rpx solid #c8cfc8;
  border-radius: 50%;
  background: transparent;
  color: $ichip-color-jade;
}

.category-glyph {
  width: 48rpx;
  height: 48rpx;
}

.category-name {
  margin-top: 14rpx;
  color: $ichip-color-ink;
  font-size: 24rpx;
  letter-spacing: 1rpx;
}

.horizontal-scroll {
  width: 100%;
  white-space: nowrap;
}

.inheritor-list {
  display: inline-flex;
  gap: $ichip-space-3;
  padding-right: $ichip-space-4;
}

.project-featured {
  position: relative;
  width: 100%;
  height: 356rpx;
  overflow: hidden;
  border-radius: $ichip-radius-md;
  background: #d8d0c5;
}

.project-featured__cover {
  width: 100%;
  height: 100%;
}

.project-featured__shade {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 112rpx $ichip-space-4 $ichip-space-4;
  background: linear-gradient(180deg, transparent, rgba(25, 22, 19, 0.86));
}

.authority-seal {
  display: inline-flex;
  align-items: center;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.7);
  border-radius: $ichip-radius-tag;
  background: rgba(46, 39, 33, 0.76);
  color: #fffaf2;
  backdrop-filter: blur(8rpx);
}

.authority-seal--featured {
  margin-bottom: 14rpx;
}

.authority-seal__mark {
  padding: 7rpx 9rpx;
  background: $ichip-color-brand;
  font-size: 17rpx;
  letter-spacing: 2rpx;
}

.authority-seal__level {
  max-width: 134rpx;
  overflow: hidden;
  padding: 7rpx 10rpx;
  font-size: 18rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-featured__name {
  overflow: hidden;
  color: #fffdf9;
  font-family: "STSong", "Songti SC", serif;
  font-size: 38rpx;
  font-weight: $ichip-weight-medium;
  letter-spacing: 2rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-featured__meta,
.project-secondary__meta {
  display: flex;
  gap: $ichip-space-2;
  margin-top: 10rpx;
  color: rgba(255, 255, 255, 0.72);
  font-size: $ichip-font-caption;
}

.project-secondary-scroll {
  margin-top: $ichip-space-3;
}

.project-secondary-list {
  display: inline-flex;
  gap: $ichip-space-3;
  padding-right: $ichip-space-4;
}

.project-secondary {
  display: flex;
  width: 412rpx;
  padding-bottom: $ichip-space-3;
  border-bottom: 1rpx solid $ichip-color-line;
}

.project-secondary__cover {
  width: 152rpx;
  height: 124rpx;
  flex-shrink: 0;
  border-radius: $ichip-radius-sm;
  background: #ddd5ca;
}

.project-secondary__content {
  flex: 1;
  min-width: 0;
  margin-left: $ichip-space-2;
  white-space: normal;
}

.project-secondary__level {
  color: $ichip-color-brand;
  font-size: 18rpx;
  letter-spacing: 2rpx;
}

.project-secondary__name,
.course-title,
.product-name,
.activity-name,
.news-title {
  color: $ichip-color-ink;
  font-size: 28rpx;
  font-weight: $ichip-weight-medium;
  line-height: 1.48;
}

.project-secondary__name {
  display: -webkit-box;
  margin-top: 5rpx;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.project-secondary__meta {
  max-width: 218rpx;
  overflow: hidden;
  color: $ichip-color-muted;
  font-size: 18rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.inheritor-card {
  position: relative;
  width: 244rpx;
  height: 344rpx;
  overflow: hidden;
  border-radius: $ichip-radius-md;
  background: #d8d0c5;
}

.inheritor-portrait {
  width: 100%;
  height: 100%;
}

.inheritor-shade {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 70rpx $ichip-space-3 $ichip-space-3;
  background: linear-gradient(180deg, transparent, rgba(28, 24, 21, 0.84));
  white-space: normal;
}

.inheritor-name {
  overflow: hidden;
  color: #fffdf9;
  font-size: 30rpx;
  font-weight: $ichip-weight-medium;
  letter-spacing: 1rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.inheritor-meta {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 7rpx;
}

.inheritor-skill,
.inheritor-level {
  overflow: hidden;
  color: rgba(255, 255, 255, 0.74);
  font-size: 18rpx;
  letter-spacing: 2rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.inheritor-level {
  padding: 3rpx 6rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.38);
  border-radius: $ichip-radius-tag;
}

.course-card,
.activity-card,
.news-item {
  display: flex;
  padding: $ichip-space-3 0;
  border-bottom: 1rpx solid $ichip-color-line;
}

.course-card:first-child,
.activity-card:first-child,
.news-item:first-child {
  padding-top: 0;
}

.course-card:last-child,
.activity-card:last-child,
.news-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.activity-cover {
  width: 206rpx;
  height: 160rpx;
  flex-shrink: 0;
  border-radius: $ichip-radius-sm;
  background: #ddd5ca;
}

.course-cover {
  width: 176rpx;
  height: 148rpx;
  flex-shrink: 0;
  margin-left: $ichip-space-3;
  border-radius: $ichip-radius-sm;
  background: #ddd5ca;
}

.activity-body,
.news-body {
  flex: 1;
  min-width: 0;
  margin-left: $ichip-space-3;
}

.course-body {
  flex: 1;
  min-width: 0;
}

.course-kicker,
.activity-kicker,
.news-kicker {
  color: $ichip-color-brand;
  font-size: 18rpx;
  letter-spacing: 3rpx;
}

.course-kicker {
  display: flex;
  gap: $ichip-space-2;
  color: $ichip-color-jade;
}

.course-title,
.activity-name {
  margin-top: 5rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-title {
  font-size: 30rpx;
}

.course-date {
  margin-top: 8rpx;
  color: $ichip-color-brand;
  font-size: $ichip-font-small;
  font-weight: $ichip-weight-medium;
  letter-spacing: 1rpx;
}

.course-line,
.activity-line {
  margin-top: 7rpx;
  overflow: hidden;
  color: $ichip-color-muted;
  font-size: $ichip-font-caption;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-line__label {
  margin-right: 10rpx;
  color: $ichip-color-jade;
}

.course-bottom,
.activity-bottom {
  margin-top: 9rpx;
}

.course-price,
.product-price {
  color: $ichip-color-muted;
  font-size: $ichip-font-caption;
  font-weight: $ichip-weight-regular;
}

.remaining-text,
.sales-text,
.activity-bottom {
  color: $ichip-color-muted;
  font-size: $ichip-font-caption;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $ichip-space-4 $ichip-space-3;
}

.product-card {
  min-width: 0;
}

.product-cover {
  width: 100%;
  height: 244rpx;
  border-radius: $ichip-radius-sm;
  background: #ddd5ca;
}

.product-kicker {
  margin-top: 13rpx;
  color: $ichip-color-jade;
  font-size: 18rpx;
  letter-spacing: 2rpx;
}

.product-name {
  height: 78rpx;
  margin-top: 5rpx;
  overflow: hidden;
}

.product-meta {
  margin-top: 5rpx;
}

.sales-text {
  color: $ichip-color-faint;
  font-size: 18rpx;
}

.activity-kicker {
  color: $ichip-color-jade;
}

.news-item {
  align-items: center;
}

.news-index {
  width: 60rpx;
  flex-shrink: 0;
  align-self: flex-start;
  padding-top: 2rpx;
  color: $ichip-color-gold;
  font-family: Georgia, serif;
  font-size: 22rpx;
  letter-spacing: 1rpx;
}

.news-body {
  margin-left: 0;
  margin-right: $ichip-space-3;
}

.news-title {
  display: -webkit-box;
  margin-top: 7rpx;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.news-cover {
  width: 152rpx;
  height: 108rpx;
  flex-shrink: 0;
  border-radius: $ichip-radius-sm;
  background: #ddd5ca;
}

.news-meta {
  justify-content: flex-start;
  gap: 12rpx;
  margin-top: 8rpx;
  color: $ichip-color-muted;
  font-size: 18rpx;
}

.refresh-mask {
  position: fixed;
  top: calc(24rpx + env(safe-area-inset-top));
  right: $ichip-space-3;
  z-index: 50;
}

.refresh-tip {
  padding: 12rpx 18rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.16);
  border-radius: 999rpx;
  background: rgba(44, 39, 35, 0.88);
  color: #fff;
  font-size: $ichip-font-caption;
}
</style>

<style lang="scss" scoped>
/*
 * 下面是项目原有首页基础样式，保留用于兼容已有组件结构。
 * 本轮绿色主题的精确视觉覆盖集中写在文件末尾的第二个 scoped style 中，
 * 便于后续根据设计稿逐块调整，而不影响旧结构的回退能力。
 */
$home-bg: #edf3e7;
$home-green: #087d79;
$home-deep: #285f5c;
$home-pale: #dcebd1;
$home-card: #f4faea;
$home-line: rgba(36, 105, 97, 0.24);
$home-ink: #24423f;

/* 首页全局底色和纸张纹理，同时为固定底部导航预留安全距离。 */
.home-page {
  min-height: 100vh;
  padding-bottom: calc(152rpx + env(safe-area-inset-bottom));
  overflow-x: hidden;
  background:
    radial-gradient(circle at 18% 15%, rgba(255, 255, 255, 0.72) 0, rgba(255, 255, 255, 0) 24%),
    linear-gradient(180deg, #eef4e8 0%, #f4f6ed 56%, #edf3e7 100%);
  color: $home-ink;
}

.home-page::before {
  position: fixed;
  inset: 0;
  z-index: 0;
  opacity: 0.16;
  background-image:
    linear-gradient(45deg, rgba(34, 102, 94, 0.05) 25%, transparent 25%),
    linear-gradient(-45deg, rgba(34, 102, 94, 0.04) 25%, transparent 25%);
  background-size: 20rpx 20rpx;
  content: '';
  pointer-events: none;
}

.safe-top,
.brand-heading,
.search-entry,
.hero-art,
.page-state-card,
.system-section,
.feature-grid,
.recommendation-section,
.refresh-mask {
  position: relative;
  z-index: 1;
}

.brand-heading {
  padding: 12rpx 32rpx 16rpx;
  color: $home-green;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 39rpx;
  font-weight: 600;
  letter-spacing: 7rpx;
  text-align: center;
}

.search-entry {
  display: flex;
  align-items: center;
  height: 64rpx;
  margin: 0 32rpx 18rpx;
  padding: 0 22rpx;
  border: 1rpx solid rgba(70, 110, 98, 0.1);
  border-radius: 32rpx;
  background: rgba(220, 223, 213, 0.88);
}

.search-placeholder {
  flex: 1;
  overflow: hidden;
  color: #71827d;
  font-size: 22rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-more {
  margin-right: 22rpx;
  color: $home-deep;
  font-size: 36rpx;
  line-height: 1;
}

.search-symbol {
  position: relative;
  width: 25rpx;
  height: 25rpx;
  border: 3rpx solid $home-deep;
  border-radius: 50%;
}

.search-symbol::after {
  position: absolute;
  right: -9rpx;
  bottom: -6rpx;
  width: 12rpx;
  height: 3rpx;
  transform: rotate(48deg);
  border-radius: 2rpx;
  background: $home-deep;
  content: '';
}

/* 主视觉容器允许青铜兽向下溢出，形成设计稿中的跨区悬浮效果。 */
.hero-art {
  height: 394rpx;
  overflow: visible;
  background:
    linear-gradient(180deg, rgba(247, 248, 241, 0.98) 0%, rgba(240, 246, 236, 0.7) 52%, rgba(210, 232, 217, 0.92) 100%);
}

.hero-copy {
  position: absolute;
  top: 36rpx;
  left: 38rpx;
  z-index: 4;
  display: flex;
  flex-direction: column;
  color: #20312f;
}

.hero-caption {
  width: 230rpx;
  color: #65746f;
  font-family: Georgia, serif;
  font-size: 15rpx;
  letter-spacing: 1rpx;
  line-height: 1.45;
}

.hero-en {
  margin-top: 10rpx;
  font-family: Georgia, serif;
  font-size: 43rpx;
  letter-spacing: 2rpx;
  line-height: 1;
}

.hero-year {
  margin-top: 11rpx;
  font-family: Georgia, serif;
  font-size: 15rpx;
  letter-spacing: 6rpx;
}

.hero-mist {
  position: absolute;
  right: -60rpx;
  bottom: -80rpx;
  width: 680rpx;
  height: 260rpx;
  transform: rotate(-4deg);
  border-radius: 50%;
  background: rgba(208, 227, 216, 0.8);
  filter: blur(14rpx);
}

.hero-mist--two {
  right: 240rpx;
  bottom: -125rpx;
  width: 520rpx;
  height: 260rpx;
  background: rgba(244, 247, 237, 0.94);
}

.vase {
  position: absolute;
  bottom: -18rpx;
  z-index: 2;
  width: 118rpx;
  height: 168rpx;
  border: 2rpx solid rgba(22, 119, 110, 0.28);
  border-radius: 42% 42% 32% 32% / 20% 20% 56% 56%;
  background:
    radial-gradient(circle at 68% 27%, rgba(255, 255, 255, 0.84) 0 8%, transparent 9%),
    linear-gradient(90deg, rgba(45, 134, 122, 0.14), rgba(216, 240, 227, 0.88) 44%, rgba(34, 122, 112, 0.3));
  box-shadow: inset -14rpx -10rpx 26rpx rgba(20, 107, 99, 0.14), 0 14rpx 28rpx rgba(58, 105, 93, 0.08);
}

.vase-neck {
  position: absolute;
  top: -52rpx;
  left: 31rpx;
  width: 52rpx;
  height: 65rpx;
  border: 2rpx solid rgba(22, 119, 110, 0.28);
  border-radius: 10rpx 10rpx 19rpx 19rpx;
  background: linear-gradient(90deg, rgba(54, 140, 128, 0.26), rgba(232, 246, 237, 0.92), rgba(42, 130, 118, 0.28));
}

.vase--main {
  right: 126rpx;
  bottom: -28rpx;
  width: 180rpx;
  height: 270rpx;
}

.vase--main .vase-neck {
  top: -78rpx;
  left: 48rpx;
  width: 80rpx;
  height: 94rpx;
}

.vase--left {
  left: 188rpx;
  transform: scale(0.78);
  opacity: 0.62;
}

.vase--right {
  right: -18rpx;
  transform: scale(0.92);
  opacity: 0.68;
}

.vase--far {
  left: 64rpx;
  bottom: -38rpx;
  transform: scale(0.58);
  opacity: 0.36;
}

.vase-branch {
  position: absolute;
  top: -148rpx;
  left: 106rpx;
  width: 5rpx;
  height: 158rpx;
  transform: rotate(21deg);
  transform-origin: bottom;
  border-radius: 50%;
  background: #527c68;
}

.vase-branch::before,
.vase-branch::after {
  position: absolute;
  width: 72rpx;
  height: 58rpx;
  border-radius: 50%;
  background: radial-gradient(circle at 50% 50%, rgba(64, 130, 104, 0.85) 0 8%, transparent 10%), radial-gradient(circle at 30% 35%, rgba(83, 149, 118, 0.7) 0 7%, transparent 9%);
  background-size: 17rpx 17rpx, 19rpx 19rpx;
  content: '';
}

.vase-branch::before {
  top: -8rpx;
  left: -45rpx;
}

.vase-branch::after {
  top: 42rpx;
  right: -47rpx;
  transform: scale(0.72);
}

/* 顶部青铜兽：锚定主视觉右下角，并覆盖在瓶器和下一分区之上。 */
.hero-mascot {
  position: absolute;
  right: 10rpx;
  bottom: -39rpx;
  z-index: 6;
  width: 132rpx;
  height: 157rpx;
}

.page-state-card {
  min-height: 320rpx;
  margin: 28rpx 32rpx;
  padding: 28rpx;
  border: 1rpx solid $home-line;
  border-radius: 18rpx;
  background: rgba(247, 250, 239, 0.86);
}

.system-section {
  padding: 30rpx 32rpx 0;
}

.ornament-title {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 31rpx;
  color: $home-green;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 35rpx;
  font-weight: 600;
  letter-spacing: 5rpx;
}

.ornament-title::before,
.ornament-title::after {
  width: 43rpx;
  height: 1rpx;
  margin: 0 15rpx;
  background: $home-green;
  box-shadow: 7rpx -6rpx 0 -0.5rpx rgba(8, 125, 121, 0.55), -7rpx 6rpx 0 -0.5rpx rgba(8, 125, 121, 0.55);
  content: '';
}

/* 六个体系始终在同一行展示，每一列平均分配可用宽度。 */
.category-row {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 5rpx;
}

.category-item {
  display: flex;
  min-width: 0;
  flex-direction: column;
  align-items: center;
}

/*
 * 六边形使用四层真实元素，而不是伪元素边框：
 * 外框 -> 浅色间隔 -> 内框 -> 图标底色。
 * 这种结构在微信小程序中比 border + 伪元素更稳定。
 */
.category-hex,
.category-hex__gap,
.category-hex__line,
.category-hex__content {
  display: flex;
  align-items: center;
  justify-content: center;
  -webkit-clip-path: polygon(25% 3%, 75% 3%, 100% 50%, 75% 97%, 25% 97%, 0 50%);
  clip-path: polygon(25% 3%, 75% 3%, 100% 50%, 75% 97%, 25% 97%, 0 50%);
}

.category-hex {
  width: 92rpx;
  height: 84rpx;
  background: $home-deep;
  filter: drop-shadow(0 4rpx 4rpx rgba(30, 91, 84, 0.12));
}

.category-hex__gap {
  width: 86rpx;
  height: 78rpx;
  background: #f4f7e9;
}

.category-hex__line {
  width: 80rpx;
  height: 72rpx;
  background: rgba(35, 104, 96, 0.78);
}

.category-hex__content {
  width: 74rpx;
  height: 66rpx;
  background: linear-gradient(145deg, #ffffff 0%, #edf5df 100%);
}

.category-glyph {
  position: relative;
  z-index: 1;
  width: 44rpx;
  height: 44rpx;
}

.category-fallback {
  position: relative;
  z-index: 1;
  color: $home-green;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 32rpx;
}

.category-name {
  width: 100%;
  margin-top: 10rpx;
  overflow: hidden;
  color: $home-deep;
  font-size: 17rpx;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 左侧一张大卡、右侧两张小卡，对应首页设计稿中的 1+2 布局。 */
.feature-grid {
  display: grid;
  grid-template-columns: 0.95fr 1.05fr;
  gap: 16rpx;
  margin: 45rpx 32rpx 0;
}

.feature-stack {
  display: grid;
  gap: 16rpx;
}

.feature-card {
  position: relative;
  display: flex;
  min-height: 132rpx;
  overflow: hidden;
  border: 1rpx solid rgba(97, 146, 115, 0.23);
  border-radius: 15rpx;
  background:
    radial-gradient(ellipse at 100% 100%, rgba(161, 207, 176, 0.42), transparent 49%),
    linear-gradient(135deg, rgba(246, 252, 236, 0.98), rgba(224, 242, 216, 0.93));
  box-shadow: 0 5rpx 11rpx rgba(74, 110, 82, 0.14);
}

.feature-card::after {
  position: absolute;
  right: -25rpx;
  bottom: -22rpx;
  width: 170rpx;
  height: 75rpx;
  border: 2rpx solid rgba(38, 135, 112, 0.14);
  border-radius: 50%;
  content: '';
}

.feature-card--knowledge {
  min-height: 280rpx;
}

.feature-copy {
  position: relative;
  z-index: 2;
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  padding: 20rpx 18rpx;
}

.feature-title {
  overflow: hidden;
  color: $home-green;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 25rpx;
  font-weight: 600;
  letter-spacing: 1rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.feature-desc,
.feature-error {
  margin-top: 9rpx;
  color: #52736d;
  font-size: 16rpx;
  line-height: 1.45;
}

.feature-link,
.city-change {
  margin-top: auto;
  padding-top: 14rpx;
  color: $home-deep;
  font-size: 17rpx;
}

.feature-error {
  color: #9a5b4b;
}

/* 功能卡青铜兽复用顶部素材，靠右下展示，文字层级始终位于其上方。 */
.feature-mascot {
  position: absolute;
  right: 4rpx;
  bottom: -2rpx;
  z-index: 1;
  width: 138rpx;
  height: 164rpx;
}

.feature-emblem {
  position: absolute;
  right: 18rpx;
  bottom: 18rpx;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 112rpx;
  height: 120rpx;
  border: 5rpx double #6f6547;
  border-radius: 38rpx 32rpx 42rpx 30rpx;
  background: linear-gradient(145deg, #8c815c, #ddc99c 48%, #526753);
  color: #f7edcf;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 42rpx;
  font-weight: 700;
  box-shadow: 0 10rpx 22rpx rgba(44, 75, 56, 0.18);
}

.feature-emblem--small {
  right: 16rpx;
  bottom: 11rpx;
  width: 59rpx;
  height: 68rpx;
  border-width: 3rpx;
  border-radius: 19rpx;
  font-size: 25rpx;
}

.city-picker {
  position: absolute;
  right: 18rpx;
  bottom: 17rpx;
  z-index: 3;
}

/* 推荐区域与上方功能卡保持统一左右留白。 */
.recommendation-section {
  margin-top: 34rpx;
  padding: 0 32rpx 30rpx;
}

.recommend-tabs {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 20rpx;
  border-bottom: 1rpx solid rgba(42, 113, 104, 0.13);
}

.recommend-tab {
  position: relative;
  padding: 14rpx 4rpx 16rpx;
  color: #63827c;
  font-size: 20rpx;
  letter-spacing: 1rpx;
}

.recommend-tab--active {
  color: $home-green;
  font-weight: 600;
}

.recommend-tab--active::after {
  position: absolute;
  right: 16%;
  bottom: -2rpx;
  left: 16%;
  height: 4rpx;
  border-radius: 2rpx;
  background: $home-green;
  content: '';
}

/* 推荐卡使用两列网格，卡片数据来自 recommendationItems 的统一结构。 */
.recommend-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20rpx 16rpx;
}

.recommend-card {
  min-width: 0;
  overflow: hidden;
  border: 1rpx solid rgba(75, 122, 98, 0.2);
  border-radius: 12rpx;
  background: rgba(250, 252, 243, 0.94);
  box-shadow: 0 5rpx 11rpx rgba(70, 106, 76, 0.12);
}

.recommend-cover {
  position: relative;
  height: 228rpx;
  overflow: hidden;
  background: linear-gradient(180deg, #e8f1ed, #c7ded9);
}

.recommend-image {
  width: 100%;
  height: 100%;
}

.recommend-placeholder {
  position: relative;
  width: 100%;
  height: 100%;
  background:
    linear-gradient(90deg, rgba(255,255,255,.32) 1rpx, transparent 1rpx),
    linear-gradient(rgba(255,255,255,.28) 1rpx, transparent 1rpx),
    linear-gradient(150deg, #eaf2ef, #bad5d0);
  background-size: 28rpx 28rpx, 28rpx 28rpx, auto;
}

.mini-vase {
  position: absolute;
  bottom: 20rpx;
  width: 73rpx;
  height: 130rpx;
  border: 2rpx solid rgba(38, 111, 108, 0.36);
  border-radius: 42% 42% 30% 30% / 18% 18% 58% 58%;
  background: linear-gradient(90deg, rgba(28, 118, 113, 0.22), rgba(245, 251, 248, 0.88), rgba(45, 122, 117, 0.26));
  box-shadow: inset -9rpx -9rpx 15rpx rgba(27, 100, 94, 0.1);
}

.mini-vase::before {
  position: absolute;
  top: -33rpx;
  left: 22rpx;
  width: 27rpx;
  height: 38rpx;
  border: 2rpx solid rgba(38, 111, 108, 0.36);
  border-radius: 8rpx;
  background: #d8e8e3;
  content: '';
}

.mini-vase--one {
  left: 50rpx;
  transform: scale(0.72);
  opacity: 0.7;
}

.mini-vase--two {
  left: 130rpx;
  z-index: 2;
}

.mini-vase--three {
  right: 38rpx;
  transform: scale(0.82);
  opacity: 0.76;
}

.recommend-badge {
  position: absolute;
  top: 13rpx;
  left: 13rpx;
  max-width: 74%;
  overflow: hidden;
  padding: 5rpx 10rpx;
  border-radius: 12rpx;
  background: rgba(242, 249, 237, 0.84);
  color: $home-deep;
  font-size: 16rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-body {
  display: flex;
  min-height: 151rpx;
  flex-direction: column;
  padding: 14rpx 15rpx 12rpx;
}

.recommend-name {
  overflow: hidden;
  color: #233e3b;
  font-size: 23rpx;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-desc {
  display: -webkit-box;
  margin-top: 6rpx;
  overflow: hidden;
  color: #6f807b;
  font-size: 16rpx;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.recommend-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 9rpx;
  color: #58766f;
  font-size: 17rpx;
}

.recommend-price {
  color: #a54535;
  font-family: Georgia, serif;
  font-size: 26rpx;
  font-weight: 700;
}

.recommend-arrow {
  color: $home-green;
  font-size: 29rpx;
}

.recommend-empty {
  min-height: 230rpx;
  border: 1rpx dashed rgba(44, 117, 106, 0.2);
  border-radius: 14rpx;
  background: rgba(248, 251, 241, 0.72);
}

.refresh-mask {
  position: fixed;
  top: calc(24rpx + env(safe-area-inset-top));
  right: 24rpx;
  z-index: 50;
}

.refresh-tip {
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(23, 91, 84, 0.9);
  color: #fff;
  font-size: 20rpx;
}

@media screen and (max-width: 350px) {
  .category-row {
    gap: 1rpx;
  }

  .category-hex {
    width: 82rpx;
    height: 76rpx;
  }

  .category-hex__gap {
    width: 76rpx;
    height: 70rpx;
  }

  .category-hex__line {
    width: 70rpx;
    height: 64rpx;
  }

  .category-hex__content {
    width: 64rpx;
    height: 58rpx;
  }

  .category-name,
  .feature-desc,
  .recommend-desc {
    font-size: 15rpx;
  }
}
</style>
