<template>
  <view class="app-page search-page">
    <!-- green 只切换公共页眉外观，返回栈和首页兜底逻辑仍由 PageHeader 原方法处理。 -->
    <page-header title="全局搜索" variant="green" />

    <!--
      搜索主视觉复用首页已归档的水墨山景，只承担背景装饰，不参与搜索参数或事件。
      这里明确不放青铜兽；图片使用 data 动态绑定，兼容微信小程序的静态资源编译方式。
    -->
    <view class="search-panel">
      <image class="search-panel__background" :src="pageVisualBackground" mode="aspectFill"></image>
      <view class="search-panel__heading">
        <text class="search-panel__kicker">HERITAGE SEARCH</text>
        <text class="search-panel__title">寻觅非遗</text>
        <text class="search-panel__subtitle">从技艺、人物与好物中，找到心之所向</text>
      </view>

      <!-- 原有输入、搜索和禁用逻辑全部保留，只调整控件外观。 -->
      <view class="search-box">
        <input
          v-model="keyword"
          class="search-input"
          type="text"
          confirm-type="search"
          maxlength="100"
          placeholder="搜索非遗项目、传承人、商品或课程"
          placeholder-style="color:#78908a;font-size:25rpx;"
          @confirm="submitSearch"
          @input="handleKeywordInput"
        />
        <button
          class="search-button"
          :loading="loading"
          :disabled="loading || !keyword.trim()"
          @click="submitSearch"
        >搜索</button>
      </view>

      <!-- 搜索类型仍由 SEARCH_TYPES 驱动，点击后继续调用原 changeType 方法。 -->
      <scroll-view scroll-x class="type-scroll" :show-scrollbar="false">
        <view class="type-list">
          <view
            v-for="item in searchTypes"
            :key="item.value"
            class="type-item"
            :class="{ active: type === item.value }"
            @click="changeType(item.value)"
          >{{ item.label }}</view>
        </view>
      </scroll-view>
    </view>

    <view class="section-card result-card">
      <view class="section-head">
        <text class="section-title">搜索结果</text>
        <text v-if="searched && !loading && !errorMessage" class="section-note">共 {{ total }} 条</text>
      </view>

      <content-state v-if="loading" type="loading" message="正在搜索…" />
      <content-state
        v-else-if="errorMessage"
        type="error"
        :message="errorMessage"
        :retrying="loading"
        @retry="submitSearch"
      />
      <content-state
        v-else-if="!searched"
        type="empty"
        message="输入关键词，查找感兴趣的非遗内容"
      />
      <content-state
        v-else-if="!results.length"
        type="empty"
        message="没有找到相关内容，换个关键词试试"
      />
      <view v-else class="result-list">
        <view
          v-for="item in results"
          :key="`${item.type}-${item.id}`"
          class="result-item"
          @click="openResult(item)"
        >
          <image
            class="result-cover"
            :src="normalizeImage(item.cover, '/static/img/logo1.jpg')"
            mode="aspectFill"
          ></image>
          <view class="result-body">
            <view class="result-topline">
              <text class="result-title">{{ item.title }}</text>
              <text class="result-type">{{ getTypeLabel(item.type) }}</text>
            </view>
            <view class="result-summary">{{ shortText(item.summary, 48) || '查看内容详情' }}</view>
            <view class="result-meta">
              <text v-if="item.category">{{ item.category }}</text>
              <text v-if="item.levelCode">{{ item.levelCode }}</text>
              <text v-if="item.startTime">{{ formatDateTime(item.startTime) }}</text>
              <text v-if="item.price !== null && item.price !== undefined">¥{{ formatPrice(item.price) }}</text>
            </view>
          </view>
        </view>

        <button
          v-if="hasNext"
          class="load-more"
          :loading="loadingMore"
          :disabled="loadingMore"
          @click="loadMore"
        >{{ loadingMore ? '正在加载' : '加载更多' }}</button>
      </view>
    </view>
  </view>
</template>

<script>
import ContentState from '@/components/content-state.vue'
import PageHeader from '@/components/page-header.vue'
import { searchContent } from '@/common/request/api.js'
import { formatDateTime, formatPrice, normalizeImage, shortText } from '@/common/utils.js'

// 与首页、商城和活动页共用同一份水墨静态素材，避免重复文件散落在页面目录。
const PAGE_VISUAL_BACKGROUND = '/static/home/feature-side-bg.png'

const SEARCH_TYPES = [
  { value: 'all', label: '全部' },
  { value: 'heritage_project', label: '非遗项目' },
  { value: 'inheritor', label: '传承人' },
  { value: 'product', label: '文创商品' },
  { value: 'course', label: '手作课程' }
]

export default {
  components: {
    ContentState,
    PageHeader
  },
  data() {
    return {
      // 纯 UI 配置，不会进入统一搜索接口的请求参数。
      pageVisualBackground: PAGE_VISUAL_BACKGROUND,
      keyword: '',
      type: 'all',
      searchTypes: SEARCH_TYPES,
      results: [],
      total: 0,
      page: 1,
      size: 10,
      hasNext: false,
      searched: false,
      loading: false,
      loadingMore: false,
      errorMessage: ''
    }
  },
  onLoad(options) {
    if (options && options.keyword) {
      this.keyword = decodeURIComponent(options.keyword)
      this.submitSearch()
    }
  },
  methods: {
    formatDateTime,
    formatPrice,
    normalizeImage,
    shortText,
    getTypeLabel(type) {
      const matched = SEARCH_TYPES.find(item => item.value === type)
      return matched ? matched.label : '内容'
    },
    handleKeywordInput(event) {
      if (event.detail.value.trim()) return
      this.results = []
      this.total = 0
      this.hasNext = false
      this.searched = false
      this.errorMessage = ''
    },
    changeType(type) {
      if (this.type === type || this.loading || this.loadingMore) return
      this.type = type
      if (this.keyword.trim()) this.submitSearch()
    },
    async submitSearch() {
      const keyword = this.keyword.trim()
      if (!keyword || this.loading || this.loadingMore) return

      this.loading = true
      this.errorMessage = ''
      this.page = 1
      try {
        const result = await searchContent({
          keyword,
          type: this.type,
          page: this.page,
          size: this.size
        })
        this.results = Array.isArray(result && result.list) ? result.list : []
        this.total = Number(result && result.total) || 0
        this.hasNext = Boolean(result && result.hasNext)
        this.searched = true
      } catch (error) {
        this.results = []
        this.total = 0
        this.hasNext = false
        this.searched = true
        this.errorMessage = this.getErrorMessage(error, '搜索失败，请检查网络后重试')
      } finally {
        this.loading = false
      }
    },
    async loadMore() {
      if (!this.hasNext || this.loadingMore) return
      this.loadingMore = true
      this.errorMessage = ''
      const nextPage = this.page + 1
      try {
        const result = await searchContent({
          keyword: this.keyword.trim(),
          type: this.type,
          page: nextPage,
          size: this.size
        })
        const nextList = Array.isArray(result && result.list) ? result.list : []
        this.results = this.results.concat(nextList)
        this.total = Number(result && result.total) || this.total
        this.hasNext = Boolean(result && result.hasNext)
        this.page = nextPage
      } catch (error) {
        uni.showToast({
          title: this.getErrorMessage(error, '加载更多失败'),
          icon: 'none'
        })
      } finally {
        this.loadingMore = false
      }
    },
    getErrorMessage(error, fallback) {
      return error && error.message ? error.message : fallback
    },
    openResult(item) {
      if (!item || !item.id) return
      if (item.type === 'product') {
        uni.navigateTo({ url: `/pages/shop/detail?id=${item.id}` })
        return
      }
      this.showPendingDetail()
    },
    showPendingDetail() {
      uni.showToast({
        title: '该详情页将在对应模块中实现',
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
/*
 * 搜索页局部色板与首页保持一致。
 * scoped 限制保证这里只改变搜索页，不影响搜索接口或其他业务页面。
 */
$page-bg: #edf3e7;
$theme-green: #087d79;
$theme-deep: #285f5c;
$theme-ink: #24423f;
$theme-muted: #66807a;
$theme-line: rgba(36, 105, 97, 0.22);
$theme-card: rgba(249, 252, 242, 0.94);

/* 首页同款宣纸渐变和细纹理，并给页面底部留出舒适浏览空间。 */
.search-page {
  position: relative;
  min-height: 100vh;
  padding-bottom: calc(68rpx + env(safe-area-inset-bottom));
  overflow-x: hidden;
  background:
    radial-gradient(circle at 16% 10%, rgba(255, 255, 255, 0.78) 0, rgba(255, 255, 255, 0) 25%),
    linear-gradient(180deg, #eef4e8 0%, #f5f7ef 52%, $page-bg 100%);
  color: $theme-ink;
}

.search-page::before {
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

/* 两个业务内容区抬到纹理层上方，确保背景永远不阻挡触控。 */
.search-panel {
  position: relative;
  z-index: 1;
  margin: 0 28rpx 24rpx;
  overflow: hidden;
  padding: 27rpx 24rpx 23rpx;
  border: 1rpx solid $theme-line;
  border-radius: 22rpx;
  background: #f5f8ea;
  box-shadow: 0 8rpx 22rpx rgba(63, 102, 74, 0.12);
}

/* 水墨图绝对定位在卡片底层；pointer-events 保证输入框、按钮和标签正常点击。 */
.search-panel__background {
  position: absolute;
  inset: 0;
  z-index: 0;
  display: block;
  width: 100%;
  height: 100%;
  opacity: 0.78;
  pointer-events: none;
}

.search-panel__heading,
.search-box,
.type-scroll {
  position: relative;
  z-index: 1;
}

.search-panel__heading {
  display: flex;
  flex-direction: column;
  padding: 0 4rpx 22rpx;
}

.search-panel__kicker {
  color: rgba(40, 95, 92, 0.68);
  font-family: Georgia, serif;
  font-size: 16rpx;
  letter-spacing: 6rpx;
}

.search-panel__title {
  margin-top: 6rpx;
  color: $theme-green;
  font-family: "STKaiti", "KaiTi", "STSong", serif;
  font-size: 40rpx;
  font-weight: 600;
  letter-spacing: 7rpx;
}

.search-panel__subtitle {
  margin-top: 7rpx;
  color: $theme-muted;
  font-size: 20rpx;
  letter-spacing: 1rpx;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

/* 输入框采用首页搜索条的半透明浅绿形态，原 v-model 和输入事件不变。 */
.search-input {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  border: 1rpx solid rgba(55, 112, 99, 0.17);
  border-radius: 999rpx;
  background: rgba(248, 251, 242, 0.9);
  color: $theme-ink;
  font-size: 25rpx;
  box-shadow: 0 3rpx 10rpx rgba(63, 98, 72, 0.07);
}

.search-button {
  width: 118rpx;
  height: 72rpx;
  margin: 0;
  border-radius: 999rpx;
  background: $theme-green;
  color: #f7fbf1;
  font-size: 25rpx;
  font-weight: 600;
  line-height: 72rpx;
  box-shadow: 0 5rpx 12rpx rgba(8, 125, 121, 0.17);
}

.search-button::after,
.load-more::after {
  border: none;
}

.search-button[disabled] {
  background: #a9bdb2;
  color: rgba(255, 255, 255, 0.88);
  opacity: 0.72;
  box-shadow: none;
}

.type-scroll {
  width: 100%;
  margin-top: 20rpx;
  white-space: nowrap;
}

.type-list {
  display: inline-flex;
  gap: 14rpx;
}

.type-item {
  padding: 11rpx 23rpx;
  border: 1rpx solid rgba(46, 110, 100, 0.15);
  border-radius: 999rpx;
  background: rgba(248, 251, 241, 0.76);
  color: $theme-muted;
  font-size: 22rpx;
}

.type-item.active {
  border-color: $theme-green;
  background: $theme-green;
  color: #f7fbf1;
  font-weight: 600;
  box-shadow: 0 5rpx 12rpx rgba(8, 125, 121, 0.17);
}

/* 覆盖全局棕色 section-card，让结果容器成为半透明浅绿纸张卡片。 */
.result-card {
  position: relative;
  z-index: 1;
  min-height: 410rpx;
  margin: 0 28rpx;
  padding: 26rpx 22rpx;
  border: 1rpx solid $theme-line;
  border-radius: 22rpx;
  background: rgba(249, 252, 242, 0.88);
  box-shadow: 0 8rpx 22rpx rgba(63, 102, 74, 0.1);
}

.section-head {
  margin-bottom: 20rpx;
}

/* 标题短线与首页和另外两个业务页保持一致，完全由 CSS 绘制。 */
.section-title {
  position: relative;
  padding-left: 24rpx;
  color: $theme-green;
  font-family: "STKaiti", "KaiTi", "STSong", serif;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 3rpx;
}

.section-title::before {
  position: absolute;
  top: 50%;
  left: 0;
  width: 14rpx;
  height: 3rpx;
  border-radius: 3rpx;
  background: $theme-green;
  content: '';
}

.section-note {
  padding: 7rpx 14rpx;
  border: 1rpx solid $theme-line;
  border-radius: 999rpx;
  background: rgba(248, 251, 241, 0.8);
  color: $theme-deep;
  font-size: 19rpx;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

/* 每条结果继续使用原数据和点击方法，仅由分割线列表改为独立浅色卡片。 */
.result-item {
  display: flex;
  padding: 15rpx;
  border: 1rpx solid rgba(75, 122, 98, 0.18);
  border-radius: 15rpx;
  background: $theme-card;
  box-shadow: 0 4rpx 10rpx rgba(70, 106, 76, 0.08);
}

.result-cover {
  width: 176rpx;
  height: 148rpx;
  flex-shrink: 0;
  border-radius: 11rpx;
  background: linear-gradient(150deg, #eaf2ef, #bad5d0);
}

.result-body {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.result-topline {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.result-title {
  flex: 1;
  color: $theme-ink;
  font-size: 26rpx;
  font-weight: 600;
  line-height: 1.45;
}

.result-type {
  flex-shrink: 0;
  padding: 5rpx 11rpx;
  border: 1rpx solid rgba(8, 125, 121, 0.13);
  border-radius: 999rpx;
  background: rgba(220, 235, 209, 0.66);
  color: $theme-deep;
  font-size: 18rpx;
}

.result-summary {
  display: -webkit-box;
  margin-top: 8rpx;
  overflow: hidden;
  color: $theme-muted;
  font-size: 21rpx;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.result-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx 12rpx;
  margin-top: 8rpx;
  color: $theme-green;
  font-size: 19rpx;
}

/* 加载更多按钮沿用原状态控制，只换成青绿色描边按钮。 */
.load-more {
  height: 72rpx;
  margin-top: 28rpx;
  border: 1rpx solid $theme-line;
  border-radius: 999rpx;
  background: rgba(220, 235, 209, 0.7);
  color: $theme-deep;
  font-size: 23rpx;
  font-weight: 600;
  line-height: 72rpx;
}

.load-more[disabled] {
  opacity: 0.65;
}
</style>
