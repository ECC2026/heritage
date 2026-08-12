<template>
  <view class="app-page home-page with-bottom-nav">
    <view class="safe-top"></view>

    <view class="home-hero">
      <view class="location-row">
        <view class="location-copy">
          <text class="location-label">同城非遗</text>
          <picker
            :range="cities"
            range-key="name"
            :value="cityIndex"
            :disabled="loading || !cities.length"
            @change="handleCityChange"
          >
            <view class="location-picker">
              <text class="location-pin">●</text>
              <text class="location-name">{{ selectedCityName }}</text>
              <text class="location-arrow">⌄</text>
            </view>
          </picker>
          <text v-if="cityError" class="location-error">{{ cityError }}</text>
        </view>
        <view class="brand-mark">
          <text>非遗</text>
          <text class="brand-mark-sub">江南志</text>
        </view>
      </view>

      <view class="home-title">发现身边的非遗之美</view>
      <view class="home-subtitle">看传承、学手艺、逛好物、约体验</view>

      <view class="search-entry" @click="goSearch">
        <text class="search-icon">⌕</text>
        <text class="search-placeholder">搜索非遗项目、传承人、商品或课程</text>
        <text class="search-action">搜索</text>
      </view>
    </view>

    <view v-if="loading && !loaded" class="page-state-card">
      <content-state type="loading" message="正在加载首页内容…" />
    </view>

    <view v-else-if="loadError" class="page-state-card">
      <content-state
        type="error"
        :message="loadError"
        :retrying="loading"
        @retry="loadHome"
      />
    </view>

    <template v-else>
      <view class="home-section banner-section">
        <swiper
          v-if="homeData.banners.length"
          class="banner-swiper"
          indicator-dots
          autoplay
          circular
          indicator-color="rgba(255,255,255,.55)"
          indicator-active-color="#ffffff"
        >
          <swiper-item v-for="item in homeData.banners" :key="item.id">
            <view class="banner-item" @click="handleBanner(item)">
              <image
                class="banner-image"
                :src="normalizeImage(item.image, '/static/img/lbt1.jpg')"
                mode="aspectFill"
              ></image>
              <view class="banner-mask">
                <view class="banner-kicker">本期策展</view>
                <view class="banner-title">{{ item.title }}</view>
                <view class="banner-link">查看推荐内容 <text>→</text></view>
              </view>
            </view>
          </swiper-item>
        </swiper>
        <content-state v-else type="empty" message="暂无轮播推荐" />
      </view>

      <view class="home-section category-section">
        <view class="section-head">
          <view>
            <view class="section-title">六大非遗分类</view>
            <view class="section-subtitle">从兴趣出发探索传统文化</view>
          </view>
        </view>
        <view v-if="homeData.categories.length" class="category-grid">
          <view
            v-for="item in homeData.categories"
            :key="item.id"
            class="category-item"
            @click="handleUnavailable('非遗分类页')"
          >
            <view class="category-icon">
              <image
                v-if="categoryIconSource(item.name)"
                class="category-glyph"
                :src="categoryIconSource(item.name)"
                mode="aspectFit"
              ></image>
            </view>
            <text class="category-name">{{ item.name }}</text>
          </view>
        </view>
        <content-state v-else type="empty" message="暂无非遗分类" />
      </view>

      <view class="home-section project-section">
        <view class="section-head">
          <view>
            <view class="section-title">权威非遗专区</view>
            <view class="section-subtitle">发现经认定的非遗代表性项目</view>
          </view>
        </view>
        <view v-if="homeData.heritageProjects.length" class="project-curation">
          <view
            class="project-featured"
            @click="handleUnavailable('非遗项目详情页')"
          >
            <image
              class="project-featured__cover"
              :src="normalizeImage(homeData.heritageProjects[0].cover, '/static/img/logo1.jpg')"
              mode="aspectFill"
            ></image>
            <view class="project-featured__shade">
              <view v-if="homeData.heritageProjects[0].level" class="authority-seal authority-seal--featured">
                <text class="authority-seal__mark">认定</text>
                <text class="authority-seal__level">{{ homeData.heritageProjects[0].level }}</text>
              </view>
              <view class="project-featured__name">{{ homeData.heritageProjects[0].name }}</view>
              <view class="project-featured__meta">
                <text v-if="homeData.heritageProjects[0].category">{{ homeData.heritageProjects[0].category }}</text>
                <text v-if="homeData.heritageProjects[0].region">{{ homeData.heritageProjects[0].region }}</text>
              </view>
            </view>
          </view>

          <scroll-view v-if="homeData.heritageProjects.length > 1" scroll-x class="horizontal-scroll project-secondary-scroll">
            <view class="project-secondary-list">
              <view
                v-for="item in homeData.heritageProjects.slice(1)"
                :key="item.id"
                class="project-secondary"
                @click="handleUnavailable('非遗项目详情页')"
              >
                <image class="project-secondary__cover" :src="normalizeImage(item.cover, '/static/img/logo1.jpg')" mode="aspectFill"></image>
                <view class="project-secondary__content">
                  <view v-if="item.level" class="project-secondary__level">{{ item.level }}</view>
                  <view class="project-secondary__name">{{ item.name }}</view>
                  <view class="project-secondary__meta">
                    <text v-if="item.category">{{ item.category }}</text>
                    <text v-if="item.region">{{ item.region }}</text>
                  </view>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>
        <content-state v-else type="empty" message="暂无权威非遗项目" />
      </view>

      <view class="home-section inheritor-section">
        <view class="section-head">
          <view>
            <view class="section-title">热门传承人</view>
            <view class="section-subtitle">听见守艺人的故事</view>
          </view>
        </view>
        <scroll-view v-if="homeData.inheritors.length" scroll-x class="horizontal-scroll">
          <view class="inheritor-list">
            <view
              v-for="item in homeData.inheritors"
              :key="item.id"
              class="inheritor-card"
              @click="handleUnavailable('传承人详情页')"
            >
              <image class="inheritor-portrait" :src="normalizeImage(item.portrait, '/static/img/logo.png')" mode="aspectFill"></image>
              <view class="inheritor-shade">
                <view class="inheritor-name">{{ item.displayName }}</view>
                <view class="inheritor-meta">
                  <text v-if="item.skillType" class="inheritor-skill">{{ item.skillType }}</text>
                  <text v-if="item.level" class="inheritor-level">{{ item.level }}</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
        <content-state v-else type="empty" message="暂无推荐传承人" />
      </view>

      <view class="home-section course-section">
        <view class="section-head">
          <view>
            <view class="section-title">本周手作</view>
            <view class="section-subtitle">预约一场与传统技艺的相遇</view>
          </view>
        </view>
        <view v-if="homeData.courses.length" class="course-list">
          <view
            v-for="item in homeData.courses"
            :key="item.id"
            class="course-card"
            @click="handleUnavailable('手作课程详情页')"
          >
            <view class="course-body">
              <view class="course-kicker">
                <text>{{ item.category || '手作体验' }}</text>
                <text v-if="item.durationMinutes">{{ item.durationMinutes }} 分钟</text>
              </view>
              <view class="course-title">{{ item.title }}</view>
              <view class="course-date">{{ formatDateTime(item.nextStartTime) }}</view>
              <view v-if="item.location" class="course-line"><text class="course-line__label">地点</text>{{ item.location }}</view>
              <view class="course-bottom">
                <text class="course-price">参考 ¥{{ formatPrice(item.price) }}</text>
                <text class="remaining-text">余 {{ item.remaining || 0 }} 位</text>
              </view>
            </view>
            <image class="course-cover" :src="normalizeImage(item.cover, '/static/img/logo1.jpg')" mode="aspectFill"></image>
          </view>
        </view>
        <content-state v-else type="empty" message="本周暂无可预约手作课程" />
      </view>

      <view class="home-section service-section">
        <view class="section-head">
          <view>
            <view class="section-title">生态服务</view>
            <view class="section-subtitle">康养陪伴、民俗演艺等预约体验</view>
          </view>
          <text class="section-note" @click="goServiceList">全部服务 →</text>
        </view>
        <view class="service-quick-grid">
          <view class="service-quick-card" @click="goServiceList">
            <text class="service-quick-icon">康</text>
            <text class="service-quick-name">康养陪伴</text>
            <text class="service-quick-note">预约慢节奏陪伴服务</text>
          </view>
          <view class="service-quick-card" @click="goServiceList">
            <text class="service-quick-icon">演</text>
            <text class="service-quick-name">民俗演艺</text>
            <text class="service-quick-note">预约现场演艺场次</text>
          </view>
          <view class="service-quick-card" @click="goMyBookings">
            <text class="service-quick-icon">约</text>
            <text class="service-quick-name">我的预约</text>
            <text class="service-quick-note">查看与取消预约</text>
          </view>
          <view class="service-quick-card" @click="goCooperation">
            <text class="service-quick-icon">合</text>
            <text class="service-quick-name">B端合作</text>
            <text class="service-quick-note">文旅、定制、活动合作</text>
          </view>
        </view>
      </view>

      <view class="home-section product-section">
        <view class="section-head">
          <view>
            <view class="section-title">热门文创</view>
            <view class="section-subtitle">把传统美学带回日常</view>
          </view>
          <text class="section-note" @click="goShop">进入商城 →</text>
        </view>
        <view v-if="homeData.products.length" class="product-grid">
          <view
            v-for="item in homeData.products"
            :key="item.id"
            class="product-card"
            @click="goProductDetail(item.id)"
          >
            <image class="product-cover" :src="normalizeImage(item.cover, '/static/img/logo1.jpg')" mode="aspectFill"></image>
            <view class="product-kicker">{{ item.category || '文创选物' }}</view>
            <view class="product-name">{{ item.name }}</view>
            <view class="product-meta">
              <text class="product-price">¥{{ formatPrice(item.price) }}</text>
              <text v-if="item.sales" class="sales-text">{{ item.sales }} 人收藏选购</text>
            </view>
          </view>
        </view>
        <content-state v-else type="empty" message="暂无推荐文创商品" />
      </view>

      <view class="home-section activity-section">
        <view class="section-head">
          <view>
            <view class="section-title">同城活动</view>
            <view class="section-subtitle">看看 {{ selectedCityName }} 最近有什么活动</view>
          </view>
          <text class="section-note" @click="goActivity">更多活动 →</text>
        </view>
        <view v-if="homeData.activities.length">
          <view
            v-for="item in homeData.activities"
            :key="item.id"
            class="activity-card"
            @click="goActivityDetail(item.id)"
          >
            <image class="activity-cover" :src="normalizeImage(item.cover, '/static/img/logo1.jpg')" mode="aspectFill"></image>
            <view class="activity-body">
              <view class="activity-kicker">同城限定</view>
              <view class="activity-name">{{ item.name }}</view>
              <view class="activity-line">{{ formatDateTime(item.startTime) }}</view>
              <view class="activity-line">{{ item.location }}</view>
              <view class="activity-bottom">
                <text v-if="item.organizerName">{{ item.organizerName }}</text>
                <text>余 {{ item.remaining || 0 }} 位</text>
              </view>
            </view>
          </view>
        </view>
        <content-state v-else type="empty" :message="`${selectedCityName}暂无推荐活动`" />
      </view>

      <view class="home-section news-section">
        <view class="section-head">
          <view>
            <view class="section-title">非遗资讯</view>
            <view class="section-subtitle">了解保护、传承与创新动态</view>
          </view>
          <text class="section-note" @click="goNewsList">查看更多 →</text>
        </view>
        <view v-if="homeData.news.length">
          <view
            v-for="(item, index) in homeData.news"
            :key="item.id"
            class="news-item"
            @click="goNewsDetail(item.id)"
          >
            <view class="news-index">{{ String(index + 1).padStart(2, '0') }}</view>
            <view class="news-body">
              <view class="news-kicker">{{ item.category || '文化观察' }}</view>
              <view class="news-title">{{ item.title }}</view>
              <view class="news-meta">
                <text v-if="item.source || item.author">{{ item.source || item.author }}</text>
                <text>{{ formatDateTime(item.createTime) }}</text>
              </view>
            </view>
            <image class="news-cover" :src="normalizeImage(item.cover, '/static/img/logo1.jpg')" mode="aspectFill"></image>
          </view>
        </view>
        <content-state v-else type="empty" message="暂无非遗资讯" />
      </view>
    </template>

    <view v-if="loading && loaded" class="refresh-mask">
      <view class="refresh-tip">正在更新首页…</view>
    </view>

    <bottom-nav current="home" />
  </view>
</template>

<script>
import BottomNav from '@/components/bottom-nav.vue'
import ContentState from '@/components/content-state.vue'
import tabbarPageMixin from '@/mixins/tabbar-page.js'
import { getCities, getHome } from '@/common/request/api.js'
import { formatDateTime, formatPrice, normalizeImage, shortText } from '@/common/utils.js'

const CITY_STORAGE_KEY = 'home-city-code'
const TAB_ROUTES = [
  '/pages/index/index',
  '/pages/shop/list',
  '/pages/activity/list',
  '/pages/community/index',
  '/pages/profile/index'
]

const CATEGORY_ICON_SOURCES = {
  '传统美术': '/static/icons/heritage/traditional-art.svg',
  '传统技艺': '/static/icons/heritage/traditional-craft.svg',
  '传统民俗': '/static/icons/heritage/traditional-folk.svg',
  '传统医药': '/static/icons/heritage/traditional-medicine.svg',
  '传统戏曲': '/static/icons/heritage/traditional-opera.svg',
  '非遗美食': '/static/icons/heritage/traditional-food.svg'
}

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
      homeData: createEmptyHomeData(),
      cities: [],
      selectedCityCode: '',
      loading: false,
      loaded: false,
      loadError: '',
      cityError: ''
    }
  },
  computed: {
    cityIndex() {
      const index = this.cities.findIndex(item => item.code === this.selectedCityCode)
      return index >= 0 ? index : 0
    },
    selectedCityName() {
      if (this.homeData.city && this.homeData.city.name) return this.homeData.city.name
      const selected = this.cities[this.cityIndex]
      return selected && selected.name ? selected.name : '选择城市'
    }
  },
  onLoad() {
    const storedCityCode = uni.getStorageSync(CITY_STORAGE_KEY)
    this.selectedCityCode = typeof storedCityCode === 'string' ? storedCityCode : ''
    this.loadHome()
  },
  onPullDownRefresh() {
    this.loadHome().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    formatDateTime,
    formatPrice,
    normalizeImage,
    shortText,
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
    getErrorMessage(error, fallback) {
      return error && error.message ? error.message : fallback
    },
    async handleCityChange(event) {
      const nextCity = this.cities[Number(event.detail.value)]
      if (!nextCity || nextCity.code === this.selectedCityCode) return
      this.selectedCityCode = nextCity.code
      uni.setStorageSync(CITY_STORAGE_KEY, nextCity.code)
      await this.loadHome()
    },
    categoryIconSource(name) {
      return CATEGORY_ICON_SOURCES[name] || ''
    },
    handleBanner(item) {
      if (!item || !item.link || !String(item.link).startsWith('/pages/')) return
      this.openPage(item.link)
    },
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
    goServiceList() {
      uni.navigateTo({ url: '/pages/service/list' })
    },
    goMyBookings() {
      uni.navigateTo({ url: '/pages/service/my-bookings' })
    },
    goCooperation() {
      uni.navigateTo({ url: '/pages/cooperation/index' })
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

.service-quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $ichip-space-3;
}

.service-quick-card {
  padding: $ichip-space-3;
  border-radius: $ichip-radius-sm;
  background: rgba(250, 247, 241, 0.72);
  border: 1rpx solid $ichip-color-line;
}

.service-quick-icon {
  display: inline-flex;
  width: 56rpx;
  height: 56rpx;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  background: $ichip-color-brand;
  color: #fff;
  font-size: 24rpx;
}

.service-quick-name {
  display: block;
  margin-top: 12rpx;
  color: $ichip-color-ink;
  font-size: 26rpx;
  font-weight: $ichip-weight-medium;
}

.service-quick-note {
  display: block;
  margin-top: 6rpx;
  color: $ichip-color-muted;
  font-size: 20rpx;
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
