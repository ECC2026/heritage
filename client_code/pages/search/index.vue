<template>
  <view class="app-page search-page">
    <page-header title="全局搜索" />

    <view class="search-panel">
      <view class="search-box">
        <input
          v-model="keyword"
          class="search-input"
          type="text"
          confirm-type="search"
          maxlength="100"
          placeholder="搜索非遗项目、传承人、商品或课程"
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

      <scroll-view scroll-x class="type-scroll">
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
      <view v-else>
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
.search-page {
  padding-bottom: 48rpx;
}

.search-panel {
  margin: 0 24rpx 24rpx;
  padding: 24rpx;
  border-radius: 28rpx;
  background: rgba(255, 252, 247, 0.96);
  box-shadow: 0 14rpx 32rpx rgba(87, 55, 36, 0.06);
}

.search-box {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.search-input {
  flex: 1;
  height: 80rpx;
  padding: 0 26rpx;
  border: 2rpx solid rgba(166, 71, 45, 0.12);
  border-radius: 999rpx;
  background: #fff;
  color: #34251f;
  font-size: 27rpx;
}

.search-button {
  width: 132rpx;
  height: 80rpx;
  margin: 0;
  border-radius: 999rpx;
  background: #a6472d;
  color: #fff;
  font-size: 27rpx;
  line-height: 80rpx;
}

.search-button[disabled] {
  opacity: 0.55;
}

.type-scroll {
  margin-top: 22rpx;
  white-space: nowrap;
}

.type-list {
  display: inline-flex;
  gap: 14rpx;
}

.type-item {
  padding: 14rpx 24rpx;
  border-radius: 999rpx;
  background: #f5ece2;
  color: #755f52;
  font-size: 24rpx;
}

.type-item.active {
  background: #a6472d;
  color: #fff;
}

.result-card {
  min-height: 400rpx;
}

.result-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid rgba(166, 71, 45, 0.09);
}

.result-item:last-child {
  border-bottom: none;
}

.result-cover {
  width: 176rpx;
  height: 140rpx;
  flex-shrink: 0;
  border-radius: 20rpx;
  background: #f0e5d8;
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
  color: #34251f;
  font-size: 29rpx;
  font-weight: 700;
  line-height: 1.5;
}

.result-type {
  flex-shrink: 0;
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(166, 71, 45, 0.1);
  color: #8b381f;
  font-size: 20rpx;
}

.result-summary {
  margin-top: 10rpx;
  color: #806c60;
  font-size: 23rpx;
  line-height: 1.55;
}

.result-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 10rpx;
  color: #a6472d;
  font-size: 22rpx;
}

.load-more {
  height: 72rpx;
  margin-top: 28rpx;
  border-radius: 999rpx;
  background: #f5ece2;
  color: #8b381f;
  font-size: 25rpx;
  line-height: 72rpx;
}
</style>
