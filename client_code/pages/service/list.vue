<template>
  <view class="app-page service-list-page" style="margin-top: 20px;">
    <page-header title="服务预约" />

    <view class="section-card filter-card">
      <scroll-view scroll-x class="filter-scroll">
        <view class="filter-row">
          <view
            class="filter-tab"
            :class="{ active: currentSystemId === '' }"
            @click="switchSystem('')"
          >全部</view>
          <view
            v-for="item in systems"
            :key="item.id"
            class="filter-tab"
            :class="{ active: Number(currentSystemId) === Number(item.id) }"
            @click="switchSystem(item.id)"
          >{{ item.name }}</view>
        </view>
      </scroll-view>
      <view class="search-row">
        <text class="search-icon">⌕</text>
        <input
          v-model.trim="keyword"
          class="search-input"
          placeholder="搜索服务名称"
          confirm-type="search"
          @confirm="loadServices(1)"
        />
        <view class="search-action" @click="loadServices(1)">搜索</view>
      </view>
    </view>

    <view class="section-card">
      <view v-if="loading && !loaded" class="empty-block">
        <text>正在加载服务...</text>
      </view>

      <view v-else-if="services.length">
        <view v-for="item in services" :key="item.id" class="service-card" @click="goDetail(item.id)">
          <image class="service-cover" :src="normalizeImage(item.cover, '/static/img/logo1.jpg')" mode="aspectFill"></image>
          <view class="service-body">
            <view class="service-head">
              <text class="service-name">{{ item.name }}</text>
              <text class="service-system">{{ item.productSystem || '特色服务' }}</text>
            </view>
            <text class="service-summary">{{ shortText(item.summary || item.description || '暂无简介', 36) }}</text>
            <view class="service-meta">
              <text>{{ item.providerName || '平台直营' }}</text>
              <text v-if="item.location">{{ item.location }}</text>
            </view>
            <view class="service-bottom">
              <text class="service-price">¥{{ formatPrice(item.price) }}<text v-if="item.unit">/{{ item.unit }}</text></text>
              <text class="service-book">去预约 →</text>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty-block">
        <text>暂无可预约服务，去看看其他非遗体验吧。</text>
      </view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getProductSystems, getServices } from '@/common/request/api.js'
import { formatPrice, normalizeImage, shortText } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      systems: [],
      currentSystemId: '',
      keyword: '',
      services: [],
      loading: false,
      loaded: false,
      page: 1,
      size: 20,
      total: 0
    }
  },
  onLoad() {
    this.loadSystems()
    this.loadServices(1)
  },
  onReachBottom() {
    if (this.services.length < this.total) {
      this.loadServices(this.page + 1)
    }
  },
  onPullDownRefresh() {
    Promise.all([this.loadSystems(), this.loadServices(1)]).finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    formatPrice,
    normalizeImage,
    shortText,
    async loadSystems() {
      try {
        const result = await getProductSystems()
        this.systems = Array.isArray(result) ? result : []
      } catch (error) {
        this.systems = []
      }
    },
    switchSystem(id) {
      this.currentSystemId = id
      this.loadServices(1)
    },
    async loadServices(nextPage) {
      if (this.loading) {
        return
      }
      this.loading = true
      const params = {
        page: nextPage,
        size: this.size
      }
      if (this.currentSystemId !== '') {
        params.productSystemId = this.currentSystemId
      }
      if (this.keyword) {
        params.keyword = this.keyword
      }
      try {
        const result = await getServices(params)
        const list = result && result.list ? result.list : []
        this.total = Number(result && result.total ? result.total : list.length)
        this.services = nextPage === 1 ? list : this.services.concat(list)
        this.page = nextPage
        this.loaded = true
      } catch (error) {
        if (nextPage === 1) {
          this.services = []
        }
      } finally {
        this.loading = false
      }
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/service/detail?id=${id}` })
    }
  }
}
</script>

<style lang="scss" scoped>
.service-list-page {
  padding: 24rpx;
  padding-bottom: 48rpx;
  background:
    radial-gradient(circle at top right, rgba(166, 71, 45, 0.14), transparent 30%),
    linear-gradient(180deg, #f8efe7 0%, #f4f1ec 100%);
}

.filter-card {
  padding: 24rpx;
}

.filter-scroll {
  width: 100%;
  white-space: nowrap;
}

.filter-row {
  display: inline-flex;
  gap: 14rpx;
  padding-right: 12rpx;
}

.filter-tab {
  padding: 12rpx 26rpx;
  border-radius: 999rpx;
  background: rgba(255, 251, 246, 0.9);
  color: #7c5f52;
  font-size: 24rpx;
}

.filter-tab.active {
  background: #a6472d;
  color: #fff;
}

.search-row {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
  padding: 0 20rpx;
  height: 72rpx;
  border-radius: 18rpx;
  background: rgba(247, 238, 230, 0.9);
}

.search-icon {
  margin-right: 12rpx;
  color: #a6472d;
  font-size: 28rpx;
}

.search-input {
  flex: 1;
  font-size: 26rpx;
  color: #34251f;
}

.search-action {
  margin-left: 12rpx;
  color: #a6472d;
  font-size: 24rpx;
  font-weight: 700;
}

.service-card {
  display: flex;
  gap: 18rpx;
  padding: 24rpx 0;
  border-top: 1rpx solid #f0e1d8;
}

.service-card:first-child {
  padding-top: 0;
  border-top: none;
}

.service-cover {
  width: 200rpx;
  height: 200rpx;
  flex-shrink: 0;
  border-radius: 20rpx;
  background: #f1e4d7;
}

.service-body {
  flex: 1;
  min-width: 0;
}

.service-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12rpx;
}

.service-name {
  flex: 1;
  font-size: 30rpx;
  font-weight: 700;
  color: #2f1f18;
  line-height: 1.4;
}

.service-system {
  flex-shrink: 0;
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(166, 71, 45, 0.12);
  color: #a6472d;
  font-size: 20rpx;
}

.service-summary {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #8d7063;
}

.service-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #9c7d70;
}

.service-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12rpx;
}

.service-price {
  color: #a6472d;
  font-size: 28rpx;
  font-weight: 700;
}

.service-price text {
  font-size: 22rpx;
  font-weight: 400;
  color: #9c7d70;
}

.service-book {
  color: #a6472d;
  font-size: 24rpx;
  font-weight: 700;
}
</style>
