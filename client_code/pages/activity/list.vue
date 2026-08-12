<template>
  <view class="app-page activity-page with-bottom-nav">
    <view class="safe-top"></view>

    <!--
      活动页主视觉与首页共用水墨山景，只作为背景展示，不参与任何业务逻辑。
      页面明确不放青铜兽；活动加载、状态计算、余席显示和详情跳转均维持原实现。
    -->
    <view class="activity-header">
      <image class="activity-header__background" :src="pageVisualBackground" mode="aspectFill"></image>
      <view class="activity-header__kicker">HERITAGE EVENTS</view>
      <view class="activity-title">非遗活动</view>
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
    <!-- 仅切换公共底部导航的视觉主题，不改活动 Tab 的 current 标识。 -->
    <bottom-nav current="activity" theme="green" />
  </view>
</template>

<script>
import BottomNav from '@/components/bottom-nav.vue'
import ContentState from '@/components/content-state.vue'
import tabbarPageMixin from '@/mixins/tabbar-page.js'
import { getActivities } from '@/common/request/api.js'
import { normalizeImage, shortText } from '@/common/utils.js'

// 复用首页静态图片目录中的山景，保持素材集中管理且避免页面间重复文件。
const PAGE_VISUAL_BACKGROUND = '/static/home/feature-side-bg.png'

export default {
  components: {
    BottomNav,
    ContentState
  },
  mixins: [tabbarPageMixin],
  data() {
    return {
      // 纯视觉配置；不会作为接口参数，也不会影响活动筛选或报名链路。
      pageVisualBackground: PAGE_VISUAL_BACKGROUND,
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
/* 活动页使用与首页、商城一致的局部主题变量，不污染详情页样式。 */
$page-bg: #edf3e7;
$theme-green: #087d79;
$theme-deep: #285f5c;
$theme-ink: #24423f;
$theme-muted: #66807a;
$theme-line: rgba(36, 105, 97, 0.22);
$theme-card: rgba(249, 252, 242, 0.94);

/* 宣纸渐变、轻纹理和底部安全距离均与首页保持统一。 */
.activity-page {
  position: relative;
  min-height: 100vh;
  padding-bottom: calc(152rpx + env(safe-area-inset-bottom));
  overflow-x: hidden;
  background:
    radial-gradient(circle at 16% 10%, rgba(255, 255, 255, 0.78) 0, rgba(255, 255, 255, 0) 25%),
    linear-gradient(180deg, #eef4e8 0%, #f5f7ef 52%, $page-bg 100%);
  color: $theme-ink;
}

.activity-page::before {
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

/* 真实内容位于纹理层上方，伪元素不会遮挡列表点击。 */
.safe-top,
.activity-header,
.activity-section,
.refresh-tip {
  position: relative;
  z-index: 1;
}

/* 页眉使用与商城相同的山水图和尺寸规则，形成两个业务页的一致入口感。 */
.activity-header {
  min-height: 182rpx;
  margin: 8rpx 28rpx 0;
  overflow: hidden;
  padding: 30rpx 28rpx;
  border: 1rpx solid $theme-line;
  border-radius: 22rpx;
  background: #f5f8ea;
  box-shadow: 0 8rpx 22rpx rgba(63, 102, 74, 0.12);
}

.activity-header__background {
  position: absolute;
  inset: 0;
  z-index: 0;
  display: block;
  width: 100%;
  height: 100%;
  opacity: 0.86;
  pointer-events: none;
}

.activity-header__kicker,
.activity-title,
.activity-subtitle {
  position: relative;
  z-index: 1;
}

.activity-header__kicker {
  color: rgba(40, 95, 92, 0.68);
  font-family: Georgia, serif;
  font-size: 16rpx;
  letter-spacing: 6rpx;
}

.activity-title {
  margin-top: 7rpx;
  color: $theme-green;
  font-family: "STKaiti", "KaiTi", "STSong", serif;
  font-size: 42rpx;
  font-weight: 600;
  letter-spacing: 7rpx;
}

.activity-subtitle {
  margin-top: 9rpx;
  color: $theme-muted;
  font-size: 21rpx;
  letter-spacing: 2rpx;
}

.activity-section {
  padding: 36rpx 28rpx 0;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

/* 标题左侧短线沿用商城样式，用 CSS 绘制以减少静态资源。 */
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

.section-heading__count {
  padding: 8rpx 15rpx;
  border: 1rpx solid $theme-line;
  border-radius: 999rpx;
  background: rgba(248, 251, 241, 0.78);
  color: $theme-deep;
  font-size: 19rpx;
}

/* 第一条活动仍由 featuredActivity 驱动，仅把原扁平区块收进重点卡片。 */
.featured-activity {
  overflow: hidden;
  padding-bottom: 24rpx;
  border: 1rpx solid rgba(75, 122, 98, 0.2);
  border-radius: 18rpx;
  background: $theme-card;
  box-shadow: 0 7rpx 17rpx rgba(70, 106, 76, 0.13);
}

.featured-activity__cover {
  display: block;
  width: 100%;
  height: 360rpx;
  background: linear-gradient(150deg, #eaf2ef, #bad5d0);
}

.featured-activity__date {
  margin: 20rpx 22rpx 0;
  color: $theme-green;
  font-size: 20rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
}

.featured-activity__title {
  margin: 9rpx 22rpx 0;
  color: $theme-ink;
  font-family: "STSong", "Songti SC", serif;
  font-size: 32rpx;
  font-weight: 600;
  line-height: 1.45;
}

.featured-activity__intro {
  display: -webkit-box;
  margin: 9rpx 22rpx 0;
  overflow: hidden;
  color: $theme-muted;
  font-size: 22rpx;
  line-height: 1.62;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.featured-activity__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 9rpx;
  margin: 15rpx 22rpx 0;
  color: $theme-muted;
  font-size: 20rpx;
}

.meta-separator {
  color: rgba(36, 105, 97, 0.28);
}

/* 状态颜色沿用原三态语义：开放为青绿、待确认为金色、结束为灰色。 */
.activity-status {
  font-weight: 600;
}

.activity-status--open {
  color: $theme-green;
}

.activity-status--pending {
  color: #9a7542;
}

.activity-status--closed {
  color: #82928d;
}

.activity-list {
  margin-top: 20rpx;
}

/* 其余活动仍保持纵向列表，只增加浅色卡片边界和触控区留白。 */
.activity-item {
  display: flex;
  gap: 18rpx;
  margin-top: 16rpx;
  padding: 16rpx;
  border: 1rpx solid rgba(75, 122, 98, 0.18);
  border-radius: 15rpx;
  background: rgba(249, 252, 242, 0.9);
  box-shadow: 0 5rpx 12rpx rgba(70, 106, 76, 0.09);
}

.activity-item:first-child {
  margin-top: 0;
}

.activity-item__cover {
  width: 224rpx;
  height: 164rpx;
  flex-shrink: 0;
  border-radius: 11rpx;
  background: linear-gradient(150deg, #eaf2ef, #bad5d0);
}

.activity-item__body {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
}

.activity-item__date {
  color: $theme-green;
  font-size: 18rpx;
  font-weight: 600;
}

.activity-item__title {
  display: -webkit-box;
  margin-top: 6rpx;
  overflow: hidden;
  color: $theme-ink;
  font-size: 25rpx;
  font-weight: 600;
  line-height: 1.4;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.activity-item__location {
  margin-top: 7rpx;
  overflow: hidden;
  color: $theme-muted;
  font-size: 19rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-item__foot {
  display: flex;
  justify-content: space-between;
  margin-top: auto;
  color: #82928d;
  font-size: 18rpx;
}

/* 刷新提示沿用首页深绿色胶囊；v-if 条件完全不变。 */
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
