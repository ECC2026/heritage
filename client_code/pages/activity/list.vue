<template>
  <view class="app-page activity-page with-bottom-nav">
    <view class="safe-top"></view>

    <view class="activity-header">
      <view class="activity-title">活动</view>
      <view class="activity-subtitle">去现场，感受正在发生的非遗</view>
    </view>

    <view class="activity-section">
      <view class="section-heading">
        <view>
          <view class="section-heading__title">近期活动</view>
          <view class="section-heading__subtitle">与一门传统技艺，当面相遇</view>
        </view>
        <text v-if="activities.length" class="section-heading__count">{{ activities.length }} 场</text>
      </view>

      <content-state
        v-if="loading && !loaded"
        type="loading"
        message="正在整理近期活动…"
      />
      <content-state
        v-else-if="error"
        type="error"
        :message="error"
        :retrying="loading"
        @retry="loadActivities"
      />
      <template v-else-if="activities.length">
        <view class="featured-activity" @click="toDetail(featuredActivity.id)">
          <image
            class="featured-activity__cover"
            :src="activityImage(featuredActivity)"
            mode="aspectFill"
            @error="handleActivityImageError(featuredActivity.id)"
          ></image>
          <view class="featured-activity__date">{{ formatActivityDate(featuredActivity.startTime, true) }}</view>
          <view class="featured-activity__title">{{ featuredActivity.title || featuredActivity.name }}</view>
          <view v-if="featuredActivity.description" class="featured-activity__intro">
            {{ shortText(featuredActivity.description, 46) }}
          </view>
          <view class="featured-activity__meta">
            <text>{{ featuredActivity.location || '地点待定' }}</text>
            <text class="meta-separator">·</text>
            <text :class="statusClass(featuredActivity)">{{ statusLabel(featuredActivity) }}</text>
            <text class="meta-separator">·</text>
            <text>{{ remainingLabel(featuredActivity) }}</text>
          </view>
        </view>

        <view v-if="otherActivities.length" class="activity-list">
          <view
            v-for="item in otherActivities"
            :key="item.id"
            class="activity-item"
            @click="toDetail(item.id)"
          >
            <image
              class="activity-item__cover"
              :src="activityImage(item)"
              mode="aspectFill"
              @error="handleActivityImageError(item.id)"
            ></image>
            <view class="activity-item__body">
              <view class="activity-item__date">{{ formatActivityDate(item.startTime) }}</view>
              <view class="activity-item__title">{{ item.title || item.name }}</view>
              <view class="activity-item__location">{{ item.location || '地点待定' }}</view>
              <view class="activity-item__foot">
                <text :class="statusClass(item)">{{ statusLabel(item) }}</text>
                <text>{{ remainingLabel(item) }}</text>
              </view>
            </view>
          </view>
        </view>
      </template>
      <content-state v-else type="empty" message="近期暂无开放报名的活动" />
    </view>

    <view v-if="loading && loaded" class="refresh-tip">正在更新…</view>
    <bottom-nav current="activity" />
  </view>
</template>

<script>
import BottomNav from '@/components/bottom-nav.vue'
import ContentState from '@/components/content-state.vue'
import tabbarPageMixin from '@/mixins/tabbar-page.js'
import { getActivities } from '@/common/request/api.js'
import { normalizeImage, shortText } from '@/common/utils.js'

export default {
  components: {
    BottomNav,
    ContentState
  },
  mixins: [tabbarPageMixin],
  data() {
    return {
      activities: [],
      loading: false,
      loaded: false,
      error: '',
      failedActivityImages: {}
    }
  },
  computed: {
    featuredActivity() {
      return this.activities[0] || {}
    },
    otherActivities() {
      return this.activities.slice(1)
    }
  },
  onShow() {
    this.loadActivities()
  },
  onPullDownRefresh() {
    this.loadActivities().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    shortText,
    async loadActivities() {
      if (this.loading) return
      this.loading = true
      this.error = ''
      try {
        const result = await getActivities({ page: 1, size: 20 })
        this.activities = result && Array.isArray(result.list) ? result.list : []
        this.loaded = true
      } catch (error) {
        this.error = error && error.message ? error.message : '活动加载失败，请稍后重试'
      } finally {
        this.loading = false
      }
    },
    formatActivityDate(value, includeTime = false) {
      if (!value) return '时间待定'
      const date = new Date(String(value).replace(/-/g, '/').replace('T', ' '))
      if (Number.isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 16)
      const pad = number => String(number).padStart(2, '0')
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const base = `${pad(date.getMonth() + 1)}.${pad(date.getDate())} · ${weekdays[date.getDay()]}`
      return includeTime ? `${base} · ${pad(date.getHours())}:${pad(date.getMinutes())}` : base
    },
    statusLabel(item) {
      if (Number(item.status) === 1) return '正在报名'
      if (Number(item.status) === 2) return '报名结束'
      return item.statusText || '待确认'
    },
    statusClass(item) {
      if (Number(item.status) === 1) return 'activity-status activity-status--open'
      if (Number(item.status) === 2) return 'activity-status activity-status--closed'
      return 'activity-status activity-status--pending'
    },
    remainingLabel(item) {
      const capacity = Number(item.maxParticipants || item.limitCount || 0)
      const signed = Number(item.signupCount || 0)
      if (capacity <= 0) return '名额开放'
      return `余 ${Math.max(capacity - signed, 0)} 席`
    },
    activityImage(item) {
      if (this.failedActivityImages[item.id]) return '/static/img/lbt2.jpg'
      return normalizeImage(item.cover, '/static/img/lbt2.jpg')
    },
    handleActivityImageError(id) {
      this.failedActivityImages[id] = true
    },
    toDetail(id) {
      uni.navigateTo({ url: `/pages/activity/detail?id=${id}` })
    }
  }
}
</script>

<style lang="scss" scoped>
.activity-page {
  background: $ichip-color-page;
  color: $ichip-color-ink;
}

.activity-header {
  padding: $ichip-space-2 28rpx 0;
}

.activity-title {
  font-family: "STSong", "Songti SC", serif;
  font-size: 42rpx;
  font-weight: $ichip-weight-medium;
  letter-spacing: 6rpx;
}

.activity-subtitle {
  margin-top: 10rpx;
  color: $ichip-color-muted;
  font-size: $ichip-font-body;
  letter-spacing: 2rpx;
}

.activity-section {
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

.section-heading__count {
  color: $ichip-color-faint;
  font-size: 20rpx;
}

.featured-activity {
  padding-bottom: 40rpx;
  border-bottom: 1rpx solid $ichip-color-line;
}

.featured-activity__cover {
  width: 100%;
  height: 390rpx;
  border-radius: $ichip-radius-banner;
  background: #ddd5ca;
}

.featured-activity__date {
  margin-top: $ichip-space-3;
  color: $ichip-color-gold;
  font-size: 21rpx;
  letter-spacing: 2rpx;
}

.featured-activity__title {
  margin-top: 10rpx;
  color: $ichip-color-ink;
  font-family: "STSong", "Songti SC", serif;
  font-size: 34rpx;
  font-weight: $ichip-weight-medium;
  line-height: 1.45;
}

.featured-activity__intro {
  display: -webkit-box;
  margin-top: 10rpx;
  overflow: hidden;
  color: $ichip-color-muted;
  font-size: 24rpx;
  line-height: 1.65;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.featured-activity__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 9rpx;
  margin-top: 16rpx;
  color: $ichip-color-muted;
  font-size: 21rpx;
}

.meta-separator {
  color: $ichip-color-line;
}

.activity-status {
  font-weight: $ichip-weight-medium;
}

.activity-status--open {
  color: $ichip-color-nav-active;
}

.activity-status--pending {
  color: $ichip-color-gold;
}

.activity-status--closed {
  color: $ichip-color-faint;
}

.activity-list {
  margin-top: 8rpx;
}

.activity-item {
  display: flex;
  gap: $ichip-space-3;
  padding: $ichip-space-4 0;
  border-bottom: 1rpx solid $ichip-color-line;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-item__cover {
  width: 224rpx;
  height: 154rpx;
  flex-shrink: 0;
  border-radius: $ichip-radius-sm;
  background: #ddd5ca;
}

.activity-item__body {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
}

.activity-item__date {
  color: $ichip-color-gold;
  font-size: 19rpx;
}

.activity-item__title {
  display: -webkit-box;
  margin-top: 6rpx;
  overflow: hidden;
  color: $ichip-color-ink;
  font-size: 28rpx;
  font-weight: $ichip-weight-medium;
  line-height: 1.4;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.activity-item__location {
  margin-top: 7rpx;
  overflow: hidden;
  color: $ichip-color-muted;
  font-size: 20rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-item__foot {
  display: flex;
  justify-content: space-between;
  margin-top: auto;
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
