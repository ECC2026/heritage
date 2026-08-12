<template>
  <!--
    “非遗”发现页只调整视觉层：
    项目、传承人、知识、种草四个频道及其请求、筛选、互动和跳转逻辑全部保留。
  -->
  <view class="app-page discover-page with-bottom-nav">
    <view class="safe-top"></view>

    <!-- 标题区复用首页山水背景与青铜兽，统一品牌风格，不增加新的业务入口。 -->
    <view class="discover-header">
      <!-- 动态绑定静态资源，避免 UniApp 将 /static 路径错误改写为 /assets 哈希地址。 -->
      <image class="discover-header__background" :src="headerBackground" mode="aspectFill"></image>
      <image class="discover-header__mascot" :src="mascotImage" mode="aspectFit"></image>

      <view class="discover-header__content">
        <view class="discover-eyebrow">ICHIP · DISCOVER</view>
        <view class="discover-title">非遗</view>
        <view class="discover-subtitle">发现仍在生活中的传统技艺</view>
      </view>

      <!-- 搜索行为保持不变，仍进入项目原有搜索页面。 -->
      <view class="discover-search" @click="goSearch">
        <text class="discover-search__icon">⌕</text>
        <text class="discover-search__text">搜非遗项目、传承人、地区</text>
        <text class="discover-search__arrow">→</text>
      </view>
    </view>

    <!-- 四频道只切换当前内容视图，不改变原有懒加载和刷新机制。 -->
    <view class="channel-tabs">
      <view
        v-for="item in channels"
        :key="item.key"
        class="channel-tab"
        :class="{ 'channel-tab--active': activeChannel === item.key }"
        @click="changeChannel(item.key)"
      >
        <text>{{ item.label }}</text>
        <view class="channel-tab__indicator"></view>
      </view>
    </view>

    <!-- 各频道继续处理首次加载、失败重试、正常内容和空数据状态。 -->
    <view class="channel-stage">
      <content-state
        v-if="currentChannelState.loading && !currentChannelState.loaded"
        type="loading"
        :message="channelLoadingMessage"
      />

      <content-state
        v-else-if="currentChannelState.error"
        type="error"
        :message="currentChannelState.error"
        :retrying="currentChannelState.loading"
        @retry="retryActiveChannel"
      />

      <template v-else>
        <!-- 项目频道：保留名录级别、非遗分类两组原筛选条件。 -->
        <view v-if="activeChannel === 'projects'" class="project-channel">
          <view class="filter-block">
            <view class="filter-label">名录级别</view>
            <scroll-view scroll-x class="filter-scroll">
              <view class="filter-row">
                <view
                  class="filter-option"
                  :class="{ 'filter-option--active': !selectedLevelCode }"
                  @click="selectedLevelCode = ''"
                >全部</view>
                <view
                  v-for="item in levels"
                  :key="item.code"
                  class="filter-option"
                  :class="{ 'filter-option--active': selectedLevelCode === item.code }"
                  @click="selectedLevelCode = item.code"
                >{{ item.name }}</view>
              </view>
            </scroll-view>
          </view>

          <view class="filter-block filter-block--category">
            <view class="filter-label">非遗分类</view>
            <scroll-view scroll-x class="filter-scroll">
              <view class="filter-row">
                <view
                  class="filter-option"
                  :class="{ 'filter-option--active': !selectedCategoryId }"
                  @click="selectedCategoryId = ''"
                >全部</view>
                <view
                  v-for="item in heritageCategories"
                  :key="item.id"
                  class="filter-option"
                  :class="{ 'filter-option--active': selectedCategoryId === String(item.id) }"
                  @click="selectedCategoryId = String(item.id)"
                >{{ item.name }}</view>
              </view>
            </scroll-view>
          </view>

          <view class="channel-heading">
            <view>
              <view class="channel-heading__title">非遗名录</view>
              <view class="channel-heading__note">以项目档案认识一方文脉</view>
            </view>
            <text class="channel-heading__count">{{ filteredProjects.length }} 项</text>
          </view>

          <view v-if="filteredProjects.length" class="project-grid">
            <view v-for="item in filteredProjects" :key="item.id" class="archive-card">
              <image
                class="archive-card__cover"
                :src="normalizeImage(item.cover, '/static/img/logo1.jpg')"
                mode="aspectFill"
              ></image>
              <view class="archive-card__meta">
                <text v-if="item.level" class="archive-level">{{ item.level }}</text>
                <text v-if="item.category" class="archive-category">{{ item.category }}</text>
              </view>
              <view class="archive-card__name">{{ item.name }}</view>
              <view v-if="item.region" class="archive-card__region">{{ item.region }}</view>
            </view>
          </view>
          <content-state v-else type="empty" message="当前筛选条件下暂无非遗项目" />
        </view>

        <!-- 传承人频道：只调整档案卡片外观，数据仍来自首页公开摘要。 -->
        <view v-else-if="activeChannel === 'inheritors'" class="inheritor-channel">
          <view class="channel-heading">
            <view>
              <view class="channel-heading__title">守艺之人</view>
              <view class="channel-heading__note">认识技艺背后的传承者</view>
            </view>
          </view>

          <view v-if="inheritors.length" class="inheritor-grid">
            <view v-for="item in inheritors" :key="item.id" class="inheritor-card">
              <image
                class="inheritor-card__portrait"
                :src="normalizeImage(item.portrait, '/static/img/logo.png')"
                mode="aspectFill"
              ></image>
              <view class="inheritor-card__shade">
                <view class="inheritor-card__name">{{ item.displayName }}</view>
                <view class="inheritor-card__meta">
                  <text v-if="item.skillType">{{ item.skillType }}</text>
                  <text v-if="item.level" class="inheritor-card__level">{{ item.level }}</text>
                </view>
              </view>
            </view>
          </view>
          <content-state v-else type="empty" message="暂无公开展示的传承人档案" />
        </view>

        <!-- 知识频道：保留资讯详情跳转和原字段展示。 -->
        <view v-else-if="activeChannel === 'knowledge'" class="knowledge-channel">
          <view class="channel-heading">
            <view>
              <view class="channel-heading__title">非遗知识</view>
              <view class="channel-heading__note">从文化资讯理解保护与传承</view>
            </view>
          </view>

          <view v-if="knowledgeItems.length" class="knowledge-list">
            <view
              v-for="(item, index) in knowledgeItems"
              :key="item.id"
              class="knowledge-item"
              @click="goNewsDetail(item.id)"
            >
              <view class="knowledge-index">{{ formatIndex(index) }}</view>
              <view class="knowledge-copy">
                <view class="knowledge-kicker">{{ item.category || '文化观察' }}</view>
                <view class="knowledge-title">{{ item.title }}</view>
                <view class="knowledge-summary">{{ shortText(item.summary || item.content, 52) }}</view>
                <view class="knowledge-source">{{ item.source || item.author || '非遗资讯' }}</view>
              </view>
              <image
                class="knowledge-cover"
                :src="normalizeImage(item.cover, '/static/img/logo1.jpg')"
                mode="aspectFill"
              ></image>
            </view>
          </view>
          <content-state v-else type="empty" message="暂无可阅读的非遗知识内容" />
        </view>

        <!-- 种草频道：发布、详情、点赞、评论和登录校验均保持原实现。 -->
        <view v-else class="notes-channel">
          <view class="notes-toolbar">
            <view>
              <view class="channel-heading__title">种草见闻</view>
              <view class="channel-heading__note">分享体验、场馆与手作记录</view>
            </view>
            <view class="publish-entry" @click="toPublish">发布种草</view>
          </view>

          <scroll-view scroll-x class="filter-scroll note-filter-scroll">
            <view class="filter-row">
              <view
                v-for="item in noteCategories"
                :key="item.label"
                class="filter-option"
                :class="{ 'filter-option--active': currentNoteCategory === item.value }"
                @click="changeNoteCategory(item.value)"
              >{{ item.label }}</view>
            </view>
          </scroll-view>

          <view v-if="posts.length" class="note-list">
            <view v-for="item in posts" :key="item.id" class="note-item">
              <view class="note-author">
                <image class="note-avatar" :src="normalizeImage(item.userAvatar)" mode="aspectFill"></image>
                <view class="note-author__copy">
                  <view class="note-author__name">{{ item.userName || '非遗爱好者' }}</view>
                  <view class="note-author__time">{{ formatDateTime(item.createTime) }}</view>
                </view>
                <text class="note-category">{{ noteCategoryLabel(item.category) }}</text>
              </view>

              <view class="note-title" @click="openDetail(item.id)">{{ item.title || '非遗体验记录' }}</view>
              <view class="note-content" @click="openDetail(item.id)">{{ shortText(item.content, 96) }}</view>

              <view v-if="getImages(item).length" class="note-images">
                <image
                  v-for="(image, imageIndex) in getImages(item)"
                  :key="imageIndex"
                  :src="image"
                  class="note-image"
                  mode="aspectFill"
                ></image>
              </view>

              <view class="note-actions">
                <text @click="likePost(item)">{{ item.liked ? '已赞' : '赞' }} {{ item.likes || 0 }}</text>
                <text @click="toggleComment(item)">{{ activePostId === item.id ? '收起评论' : `评论 ${item.comments || 0}` }}</text>
              </view>

              <view v-if="activePostId === item.id" class="comment-box">
                <view v-if="commentMap[item.id] && commentMap[item.id].length" class="comment-list">
                  <view v-for="comment in commentMap[item.id]" :key="comment.id" class="comment-item">
                    <image class="comment-avatar" :src="normalizeImage(comment.userAvatar)" mode="aspectFill"></image>
                    <view class="comment-body">
                      <text class="comment-name">{{ comment.userName }}</text>
                      <text class="comment-content">{{ comment.content }}</text>
                      <text class="comment-time">{{ formatDateTime(comment.createTime) }}</text>
                    </view>
                  </view>
                </view>
                <view v-else class="comment-empty">还没有评论，来留下第一条交流内容吧。</view>
                <textarea v-model.trim="commentDraft" class="comment-input" placeholder="写下你的评论…"></textarea>
                <view class="comment-submit" @click="submitComment(item)">提交评论</view>
              </view>
            </view>
          </view>
          <content-state v-else type="empty" message="暂无种草内容，来分享第一篇体验记录吧" />
        </view>
      </template>
    </view>

    <view v-if="currentChannelState.loading && currentChannelState.loaded" class="refresh-tip">正在更新…</view>
    <!-- 与首页、“我的”页面使用相同的绿色底部导航主题。 -->
    <bottom-nav current="community" theme="green" />
  </view>
</template>

<script>
import BottomNav from '@/components/bottom-nav.vue'
import ContentState from '@/components/content-state.vue'
import tabbarPageMixin from '@/mixins/tabbar-page.js'
import {
  commentPost,
  getComments,
  getHeritageCategories,
  getHeritageLevels,
  getHeritageProjects,
  getHome,
  getNewsList,
  getPosts,
  togglePostLike
} from '@/common/request/api.js'
import { requireLogin } from '@/common/session.js'
import { formatDateTime, normalizeImage, shortText } from '@/common/utils.js'

// 标题区复用首页已有静态资源，不在“非遗”页面保存重复图片。
const HEADER_BACKGROUND = '/static/home/feature-side-bg.png'
const MASCOT_IMAGE = '/static/home/bronze-beast.png'

// 四个频道的 key 用于数据状态和加载分流，label 只负责页面展示。
const CHANNELS = [
  { key: 'projects', label: '项目' },
  { key: 'inheritors', label: '传承人' },
  { key: 'knowledge', label: '知识' },
  { key: 'notes', label: '种草' }
]

const NOTE_CATEGORIES = [
  { label: '全部', value: '' },
  { label: '体验分享', value: '经验分享' },
  { label: '探店探馆', value: '活动招募' },
  { label: '手作记录', value: '技艺交流' },
  { label: '非遗好物', value: '交流讨论' }
]

const NOTE_CATEGORY_LABELS = NOTE_CATEGORIES.reduce((result, item) => {
  if (item.value) result[item.value] = item.label
  return result
}, {
  问题求助: '交流问答'
})

function toArray(value) {
  return Array.isArray(value) ? value : []
}

function createChannelStates() {
  return CHANNELS.reduce((result, item) => {
    result[item.key] = {
      loading: false,
      loaded: false,
      error: ''
    }
    return result
  }, {})
}

export default {
  components: {
    BottomNav,
    ContentState
  },
  mixins: [tabbarPageMixin],
  data() {
    return {
      // 以下两项仅用于标题区装饰，不参与频道请求和互动逻辑。
      headerBackground: HEADER_BACKGROUND,
      mascotImage: MASCOT_IMAGE,
      channels: CHANNELS,
      activeChannel: 'projects',
      channelStates: createChannelStates(),
      levels: [],
      heritageCategories: [],
      projects: [],
      selectedLevelCode: '',
      selectedCategoryId: '',
      inheritors: [],
      knowledgeItems: [],
      posts: [],
      noteCategories: NOTE_CATEGORIES,
      currentNoteCategory: '',
      activePostId: null,
      commentDraft: '',
      commentMap: {}
    }
  },
  computed: {
    currentChannelState() {
      return this.channelStates[this.activeChannel]
    },
    channelLoadingMessage() {
      const messages = {
        projects: '正在整理非遗项目档案…',
        inheritors: '正在加载传承人档案…',
        knowledge: '正在加载非遗知识…',
        notes: '正在加载种草见闻…'
      }
      return messages[this.activeChannel]
    },
    filteredProjects() {
      const selectedLevel = this.levels.find(item => item.code === this.selectedLevelCode)
      const selectedCategory = this.heritageCategories.find(item => String(item.id) === this.selectedCategoryId)

      return this.projects.filter((item) => {
        const matchesLevel = !selectedLevel ||
          item.levelCode === selectedLevel.code ||
          item.level === selectedLevel.name
        const matchesCategory = !selectedCategory ||
          String(item.categoryId || '') === String(selectedCategory.id) ||
          item.category === selectedCategory.name
        return matchesLevel && matchesCategory
      })
    }
  },
  onShow() {
    const shouldRefreshNotes = this.activeChannel === 'notes' && this.channelStates.notes.loaded
    this.loadChannel(this.activeChannel, { force: shouldRefreshNotes })
  },
  onPullDownRefresh() {
    this.loadChannel(this.activeChannel, { force: true })
      .finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    formatDateTime,
    normalizeImage,
    shortText,
    formatIndex(index) {
      return String(index + 1).padStart(2, '0')
    },
    getErrorMessage(error, fallback) {
      return error && error.message ? error.message : fallback
    },
    changeChannel(channel) {
      if (channel === this.activeChannel) return
      this.activeChannel = channel
      this.activePostId = null
      this.commentDraft = ''
      this.loadChannel(channel)
    },
    retryActiveChannel() {
      this.loadChannel(this.activeChannel, { force: true })
    },
    async loadChannel(channel, options = {}) {
      const state = this.channelStates[channel]
      if (!state || state.loading || (state.loaded && !options.force)) return

      state.loading = true
      state.error = ''
      try {
        if (channel === 'projects') await this.loadProjects()
        if (channel === 'inheritors') await this.loadInheritors()
        if (channel === 'knowledge') await this.loadKnowledge()
        if (channel === 'notes') await this.loadPosts()
        state.loaded = true
      } catch (error) {
        state.error = this.getErrorMessage(error, '内容加载失败，请检查网络后重试')
      } finally {
        state.loading = false
      }
    },
    async loadProjects() {
      const [levels, categories, projects] = await Promise.all([
        getHeritageLevels(),
        getHeritageCategories(),
        getHeritageProjects()
      ])
      this.levels = toArray(levels)
      this.heritageCategories = toArray(categories)
      this.projects = toArray(projects)
    },
    async loadInheritors() {
      const home = await getHome()
      this.inheritors = toArray(home && home.inheritors)
    },
    async loadKnowledge() {
      const result = await getNewsList({ page: 1, size: 20, status: 1 })
      this.knowledgeItems = toArray(result && result.list)
    },
    async loadPosts() {
      const result = await getPosts({
        page: 1,
        size: 20,
        category: this.currentNoteCategory
      })
      this.posts = toArray(result && result.list).map(item => ({
        ...item,
        liked: false
      }))
    },
    changeNoteCategory(category) {
      if (category === this.currentNoteCategory) return
      this.currentNoteCategory = category
      this.activePostId = null
      this.commentDraft = ''
      this.loadChannel('notes', { force: true })
    },
    noteCategoryLabel(category) {
      return NOTE_CATEGORY_LABELS[category] || '非遗见闻'
    },
    goSearch() {
      uni.navigateTo({ url: '/pages/search/index' })
    },
    goNewsDetail(id) {
      uni.navigateTo({ url: `/pages/news/detail?id=${id}` })
    },
    toPublish() {
      uni.navigateTo({ url: '/pages/community/post' })
    },
    openDetail(id) {
      uni.navigateTo({ url: `/pages/community/detail?id=${id}` })
    },
    getImages(item) {
      if (!item || !item.images) return []
      return String(item.images)
        .split(',')
        .filter(Boolean)
        .map(url => normalizeImage(url))
    },
    async likePost(item) {
      if (!requireLogin()) return
      const result = await togglePostLike(item.id)
      item.likes = result && result.likes !== undefined ? result.likes : item.likes
      item.liked = !!(result && result.liked)
    },
    async toggleComment(item) {
      this.activePostId = this.activePostId === item.id ? null : item.id
      this.commentDraft = ''
      if (!this.activePostId) return

      const result = await getComments({
        postId: item.id,
        page: 1,
        size: 20
      })
      this.commentMap[item.id] = toArray(result && result.list)
    },
    async submitComment(item) {
      if (!requireLogin()) return
      if (!this.commentDraft) {
        uni.showToast({ title: '请输入评论内容', icon: 'none' })
        return
      }

      const content = this.commentDraft
      await commentPost({ postId: item.id, content })
      uni.showToast({ title: '评论成功', icon: 'success' })
      this.commentDraft = ''
      item.comments = (item.comments || 0) + 1

      const result = await getComments({
        postId: item.id,
        page: 1,
        size: 20
      })
      this.commentMap[item.id] = toArray(result && result.list)
    }
  }
}
</script>

<style lang="scss" scoped>
.discover-page {
  padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
  background: $ichip-color-page;
  color: $ichip-color-ink;
}

.discover-header {
  padding: $ichip-space-2 $ichip-space-4 $ichip-space-4;
}

.discover-eyebrow {
  color: $ichip-color-gold;
  font-size: 18rpx;
  letter-spacing: 6rpx;
}

.discover-title {
  margin-top: 18rpx;
  font-family: "STSong", "Songti SC", serif;
  font-size: 54rpx;
  font-weight: $ichip-weight-medium;
  letter-spacing: 8rpx;
}

.discover-subtitle {
  margin-top: 10rpx;
  color: $ichip-color-muted;
  font-size: $ichip-font-body;
  letter-spacing: 2rpx;
}

.discover-search {
  display: flex;
  align-items: center;
  height: 78rpx;
  margin-top: $ichip-space-4;
  padding: 0 $ichip-space-3;
  border: 1rpx solid $ichip-color-line;
  border-radius: $ichip-radius-sm;
  background: rgba($ichip-color-surface, 0.72);
}

.discover-search__icon {
  margin-right: 14rpx;
  color: $ichip-color-nav-active;
  font-size: 32rpx;
}

.discover-search__text {
  flex: 1;
  color: $ichip-color-muted;
  font-size: 24rpx;
}

.discover-search__arrow {
  color: $ichip-color-nav-active;
  font-size: 24rpx;
}

.channel-tabs {
  display: flex;
  margin: 0 $ichip-space-4;
  border-bottom: 1rpx solid $ichip-color-line;
}

.channel-tab {
  position: relative;
  flex: 1;
  padding: 20rpx 0 18rpx;
  color: $ichip-color-muted;
  font-size: 26rpx;
  text-align: center;
}

.channel-tab--active {
  color: $ichip-color-nav-active;
  font-weight: $ichip-weight-medium;
}

.channel-tab__indicator {
  position: absolute;
  bottom: -1rpx;
  left: 50%;
  width: 32rpx;
  height: 3rpx;
  border-radius: 3rpx;
  background: transparent;
  transform: translateX(-50%);
}

.channel-tab--active .channel-tab__indicator {
  background: $ichip-color-nav-active;
}

.channel-stage {
  min-height: 640rpx;
  padding: $ichip-space-4;
}

.filter-block--category {
  margin-top: $ichip-space-3;
}

.filter-label {
  margin-bottom: 14rpx;
  color: $ichip-color-faint;
  font-size: 19rpx;
  letter-spacing: 4rpx;
}

.filter-scroll {
  width: 100%;
  white-space: nowrap;
}

.filter-row {
  display: inline-flex;
  gap: 12rpx;
  padding-right: $ichip-space-4;
}

.filter-option {
  padding: 9rpx 16rpx;
  border: 1rpx solid $ichip-color-line;
  border-radius: $ichip-radius-tag;
  color: $ichip-color-muted;
  font-size: 21rpx;
}

.filter-option--active {
  border-color: rgba(100, 121, 110, 0.42);
  color: $ichip-color-nav-active;
  background: rgba(100, 121, 110, 0.06);
}

.channel-heading,
.notes-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin: $ichip-space-section 0 $ichip-space-4;
}

.channel-heading__title {
  font-family: "STSong", "Songti SC", serif;
  font-size: 34rpx;
  font-weight: $ichip-weight-medium;
  letter-spacing: 2rpx;
}

.channel-heading__note {
  margin-top: 8rpx;
  color: $ichip-color-muted;
  font-size: 21rpx;
}

.channel-heading__count {
  color: $ichip-color-faint;
  font-size: 20rpx;
}

.project-grid,
.inheritor-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: $ichip-space-4 $ichip-space-3;
}

.archive-card,
.inheritor-card {
  min-width: 0;
}

.archive-card__cover {
  width: 100%;
  height: 224rpx;
  border-radius: $ichip-radius-sm;
  background: #dcd4c9;
}

.archive-card__meta {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 14rpx;
}

.archive-level,
.archive-category {
  max-width: 126rpx;
  overflow: hidden;
  padding: 4rpx 8rpx;
  border-radius: $ichip-radius-tag;
  font-size: 18rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.archive-level {
  border: 1rpx solid rgba($ichip-color-brand, 0.25);
  color: $ichip-color-brand;
}

.archive-category {
  color: $ichip-color-nav-active;
  background: rgba(100, 121, 110, 0.07);
}

.archive-card__name {
  display: -webkit-box;
  margin-top: 10rpx;
  overflow: hidden;
  color: $ichip-color-ink;
  font-size: 28rpx;
  font-weight: $ichip-weight-medium;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.archive-card__region {
  margin-top: 7rpx;
  overflow: hidden;
  color: $ichip-color-muted;
  font-size: 20rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.inheritor-channel .channel-heading,
.knowledge-channel .channel-heading {
  margin-top: $ichip-space-2;
}

.inheritor-card {
  position: relative;
  height: 390rpx;
  overflow: hidden;
  border-radius: $ichip-radius-md;
  background: #d8d0c5;
}

.inheritor-card__portrait {
  width: 100%;
  height: 100%;
}

.inheritor-card__shade {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 88rpx $ichip-space-3 $ichip-space-3;
  background: linear-gradient(180deg, transparent, rgba(27, 24, 21, 0.86));
}

.inheritor-card__name {
  color: #fffdf9;
  font-size: 30rpx;
  font-weight: $ichip-weight-medium;
}

.inheritor-card__meta {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.74);
  font-size: 18rpx;
}

.inheritor-card__level {
  padding: 3rpx 6rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.38);
  border-radius: $ichip-radius-tag;
}

.knowledge-item {
  display: flex;
  align-items: center;
  padding: $ichip-space-3 0;
  border-bottom: 1rpx solid $ichip-color-line;
}

.knowledge-item:first-child {
  padding-top: 0;
}

.knowledge-item:last-child {
  border-bottom: none;
}

.knowledge-index {
  width: 56rpx;
  flex-shrink: 0;
  align-self: flex-start;
  color: $ichip-color-gold;
  font-family: Georgia, serif;
  font-size: 20rpx;
}

.knowledge-copy {
  flex: 1;
  min-width: 0;
  margin-right: $ichip-space-3;
}

.knowledge-kicker {
  color: $ichip-color-nav-active;
  font-size: 18rpx;
  letter-spacing: 3rpx;
}

.knowledge-title {
  display: -webkit-box;
  margin-top: 7rpx;
  overflow: hidden;
  font-size: 28rpx;
  font-weight: $ichip-weight-medium;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.knowledge-summary {
  display: -webkit-box;
  margin-top: 8rpx;
  overflow: hidden;
  color: $ichip-color-muted;
  font-size: 20rpx;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.knowledge-source {
  margin-top: 8rpx;
  color: $ichip-color-faint;
  font-size: 18rpx;
}

.knowledge-cover {
  width: 156rpx;
  height: 120rpx;
  flex-shrink: 0;
  border-radius: $ichip-radius-sm;
  background: #dcd4c9;
}

.notes-toolbar {
  margin-top: $ichip-space-2;
}

.publish-entry {
  padding: 10rpx 16rpx;
  border: 1rpx solid rgba(100, 121, 110, 0.36);
  border-radius: $ichip-radius-tag;
  color: $ichip-color-nav-active;
  font-size: 21rpx;
}

.note-filter-scroll {
  margin-bottom: $ichip-space-3;
}

.note-item {
  padding: $ichip-space-4 0;
  border-bottom: 1rpx solid $ichip-color-line;
}

.note-item:first-child {
  padding-top: 0;
}

.note-item:last-child {
  border-bottom: none;
}

.note-author {
  display: flex;
  align-items: center;
}

.note-avatar,
.comment-avatar {
  width: 64rpx;
  height: 64rpx;
  flex-shrink: 0;
  border-radius: 50%;
  background: #dcd4c9;
}

.note-author__copy {
  flex: 1;
  min-width: 0;
  margin-left: 14rpx;
}

.note-author__name {
  color: $ichip-color-ink;
  font-size: 24rpx;
  font-weight: $ichip-weight-medium;
}

.note-author__time {
  margin-top: 4rpx;
  color: $ichip-color-faint;
  font-size: 18rpx;
}

.note-category {
  color: $ichip-color-nav-active;
  font-size: 19rpx;
}

.note-title {
  margin-top: 18rpx;
  color: $ichip-color-ink;
  font-family: "STSong", "Songti SC", serif;
  font-size: 32rpx;
  font-weight: $ichip-weight-medium;
  line-height: 1.45;
}

.note-content {
  display: -webkit-box;
  margin-top: 10rpx;
  overflow: hidden;
  color: $ichip-color-muted;
  font-size: 25rpx;
  line-height: 1.72;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.note-images {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10rpx;
  margin-top: 18rpx;
}

.note-image {
  width: 100%;
  height: 202rpx;
  border-radius: $ichip-radius-sm;
  background: #dcd4c9;
}

.note-actions {
  display: flex;
  justify-content: flex-end;
  gap: $ichip-space-4;
  margin-top: 18rpx;
  color: $ichip-color-muted;
  font-size: 22rpx;
}

.comment-box {
  margin-top: $ichip-space-3;
  padding: $ichip-space-3;
  border: 1rpx solid $ichip-color-line;
  border-radius: $ichip-radius-sm;
  background: rgba($ichip-color-surface, 0.62);
}

.comment-item {
  display: flex;
  gap: 12rpx;
  padding: 14rpx 0;
  border-top: 1rpx solid $ichip-color-line;
}

.comment-item:first-child {
  padding-top: 0;
  border-top: none;
}

.comment-avatar {
  width: 52rpx;
  height: 52rpx;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-name,
.comment-content,
.comment-time,
.comment-empty {
  display: block;
}

.comment-name {
  color: $ichip-color-ink;
  font-size: 22rpx;
  font-weight: $ichip-weight-medium;
}

.comment-content {
  margin-top: 5rpx;
  color: $ichip-color-muted;
  font-size: 23rpx;
  line-height: 1.6;
}

.comment-time,
.comment-empty {
  margin-top: 5rpx;
  color: $ichip-color-faint;
  font-size: 18rpx;
}

.comment-input {
  width: 100%;
  min-height: 128rpx;
  margin-top: $ichip-space-2;
  padding: 16rpx;
  border: 1rpx solid $ichip-color-line;
  border-radius: $ichip-radius-tag;
  background: $ichip-color-surface;
  color: $ichip-color-ink;
  font-size: 24rpx;
}

.comment-submit {
  width: 148rpx;
  margin-top: 14rpx;
  margin-left: auto;
  padding: 11rpx 0;
  border-radius: $ichip-radius-tag;
  background: $ichip-color-nav-active;
  color: #fff;
  font-size: 21rpx;
  text-align: center;
}

.refresh-tip {
  position: fixed;
  top: calc(24rpx + env(safe-area-inset-top));
  right: $ichip-space-3;
  z-index: 50;
  padding: 10rpx 16rpx;
  border-radius: $ichip-radius-tag;
  background: rgba(44, 39, 35, 0.86);
  color: #fff;
  font-size: 19rpx;
}
</style>

<style lang="scss" scoped>
/*
 * “非遗”发现页绿色主题覆盖。
 * 本段只负责颜色、间距、边框、背景和卡片层级；原模板事件与脚本方法保持不变。
 * 主题色与首页、“我的”页面使用同一组青绿配色。
 */
$discover-green: #087d79;
$discover-deep: #285f5c;
$discover-ink: #24423f;
$discover-muted: #668079;
$discover-faint: #8aa099;
$discover-paper: rgba(249, 252, 242, 0.95);
$discover-line: rgba(40, 105, 97, 0.2);
$discover-pale: #dcebd1;

/* 页面底色采用首页相同的浅绿纸张渐变，并为固定底部导航预留安全区。 */
.discover-page {
  position: relative;
  min-height: 100vh;
  padding-bottom: calc(154rpx + env(safe-area-inset-bottom));
  overflow-x: hidden;
  background:
    radial-gradient(circle at 14% 8%, rgba(255, 255, 255, 0.82) 0, transparent 28%),
    linear-gradient(180deg, #edf4e8 0%, #f5f7ef 52%, #edf3e7 100%);
  color: $discover-ink;
}

.discover-page::before {
  position: fixed;
  inset: 0;
  z-index: 0;
  opacity: 0.14;
  background-image:
    linear-gradient(45deg, rgba(35, 105, 96, 0.05) 25%, transparent 25%),
    linear-gradient(-45deg, rgba(35, 105, 96, 0.04) 25%, transparent 25%);
  background-size: 20rpx 20rpx;
  content: '';
  pointer-events: none;
}

/* 页面主体始终位于背景纹理之上。 */
.safe-top,
.discover-header,
.channel-tabs,
.channel-stage,
.refresh-tip {
  position: relative;
  z-index: 1;
}

/*
 * 标题区使用已有横向山水图作为背景，青铜兽独立叠放。
 * 背景和青铜兽均禁用指针事件，确保不会遮挡搜索框。
 */
.discover-header {
  margin: 0 32rpx;
  padding: 28rpx 25rpx 24rpx;
  overflow: hidden;
  border: 1rpx solid $discover-line;
  border-radius: 22rpx;
  background: #f6f8e9;
  box-shadow: 0 8rpx 20rpx rgba(62, 101, 72, 0.1);
}

.discover-header::after {
  position: absolute;
  inset: 0;
  z-index: 1;
  background: linear-gradient(90deg, rgba(250, 252, 243, 0.88) 0%, rgba(250, 252, 243, 0.48) 58%, rgba(250, 252, 243, 0.08) 100%);
  content: '';
  pointer-events: none;
}

.discover-header__background {
  position: absolute;
  inset: 0;
  z-index: 0;
  display: block;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.discover-header__mascot {
  position: absolute;
  top: 4rpx;
  right: 13rpx;
  z-index: 2;
  width: 132rpx;
  height: 157rpx;
  pointer-events: none;
}

.discover-header__content {
  position: relative;
  z-index: 3;
  width: calc(100% - 138rpx);
}

.discover-eyebrow {
  color: rgba(40, 95, 92, 0.62);
  font-family: Georgia, serif;
  font-size: 14rpx;
  letter-spacing: 6rpx;
}

.discover-title {
  margin-top: 8rpx;
  color: $discover-green;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 47rpx;
  font-weight: 600;
  letter-spacing: 9rpx;
}

.discover-subtitle {
  margin-top: 6rpx;
  color: $discover-muted;
  font-size: 20rpx;
  letter-spacing: 1rpx;
}

/* 搜索栏保留原跳转，只调整为首页相同的圆角浅绿色输入视觉。 */
.discover-search {
  position: relative;
  z-index: 4;
  display: flex;
  align-items: center;
  height: 64rpx;
  margin-top: 24rpx;
  padding: 0 20rpx;
  border: 1rpx solid rgba(40, 95, 92, 0.14);
  border-radius: 32rpx;
  background: rgba(237, 242, 226, 0.9);
}

.discover-search__icon {
  margin-right: 12rpx;
  color: $discover-deep;
  font-size: 29rpx;
}

.discover-search__text {
  min-width: 0;
  flex: 1;
  overflow: hidden;
  color: #71857f;
  font-size: 21rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.discover-search__arrow {
  color: $discover-deep;
  font-size: 22rpx;
}

/* 四频道改为一体化浅色标签栏，激活项使用实色玉绿胶囊。 */
.channel-tabs {
  display: flex;
  margin: 20rpx 32rpx 0;
  padding: 6rpx;
  border: 1rpx solid rgba(47, 108, 92, 0.14);
  border-radius: 999rpx;
  background: rgba(248, 251, 241, 0.82);
  box-shadow: 0 5rpx 13rpx rgba(61, 101, 71, 0.07);
}

.channel-tab {
  position: relative;
  flex: 1;
  padding: 13rpx 0;
  border-radius: 999rpx;
  color: $discover-muted;
  font-size: 22rpx;
  text-align: center;
  transition: color 0.2s ease, background-color 0.2s ease;
}

.channel-tab--active {
  background: linear-gradient(135deg, #38796f, $discover-deep);
  color: #f8fbef;
  font-weight: 600;
  box-shadow: 0 5rpx 12rpx rgba(40, 95, 92, 0.17);
}

/* 胶囊激活态已经足够明确，因此隐藏旧的下划线指示器。 */
.channel-tab__indicator,
.channel-tab--active .channel-tab__indicator {
  display: none;
}

.channel-stage {
  min-height: 640rpx;
  padding: 26rpx 32rpx 16rpx;
}

/* 项目筛选区使用独立纸张卡片，所有筛选点击和值绑定保持原样。 */
.filter-block {
  padding: 19rpx 20rpx;
  border: 1rpx solid rgba(46, 108, 91, 0.14);
  border-radius: 15rpx;
  background: rgba(249, 252, 242, 0.84);
  box-shadow: 0 4rpx 11rpx rgba(61, 101, 71, 0.06);
}

.filter-block--category {
  margin-top: 13rpx;
}

.filter-label {
  margin-bottom: 12rpx;
  color: $discover-deep;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 21rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
}

.filter-scroll {
  width: 100%;
  white-space: nowrap;
}

.filter-row {
  display: inline-flex;
  gap: 10rpx;
  padding-right: 24rpx;
}

.filter-option {
  padding: 8rpx 15rpx;
  border: 1rpx solid rgba(40, 95, 92, 0.17);
  border-radius: 999rpx;
  background: rgba(252, 253, 248, 0.76);
  color: $discover-muted;
  font-size: 19rpx;
}

.filter-option--active {
  border-color: rgba(40, 95, 92, 0.42);
  background: $discover-pale;
  color: $discover-deep;
  font-weight: 600;
}

/* 频道标题沿用首页青绿色宋体栏目标题。 */
.channel-heading,
.notes-toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin: 31rpx 0 19rpx;
}

.channel-heading__title {
  color: $discover-green;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 3rpx;
}

.channel-heading__note {
  margin-top: 6rpx;
  color: $discover-muted;
  font-size: 19rpx;
}

.channel-heading__count {
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(220, 235, 209, 0.8);
  color: $discover-deep;
  font-size: 18rpx;
}

/* 非遗项目使用两列纸张档案卡，封面与接口内容均保持原样。 */
.project-grid,
.inheritor-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx 14rpx;
}

.archive-card {
  min-width: 0;
  overflow: hidden;
  padding-bottom: 15rpx;
  border: 1rpx solid rgba(54, 113, 91, 0.16);
  border-radius: 13rpx;
  background: $discover-paper;
  box-shadow: 0 6rpx 13rpx rgba(61, 101, 71, 0.09);
}

.archive-card__cover {
  width: 100%;
  height: 218rpx;
  border-radius: 0;
  background: linear-gradient(145deg, #e7f0ea, #c8ddd6);
}

.archive-card__meta,
.archive-card__name,
.archive-card__region {
  margin-right: 14rpx;
  margin-left: 14rpx;
}

.archive-card__meta {
  display: flex;
  align-items: center;
  gap: 7rpx;
  margin-top: 12rpx;
}

.archive-level,
.archive-category {
  max-width: 118rpx;
  overflow: hidden;
  padding: 4rpx 8rpx;
  border-radius: 999rpx;
  font-size: 16rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.archive-level {
  border: 1rpx solid rgba(40, 95, 92, 0.23);
  color: $discover-deep;
}

.archive-category {
  background: rgba(220, 235, 209, 0.72);
  color: $discover-green;
}

.archive-card__name {
  display: -webkit-box;
  margin-top: 9rpx;
  overflow: hidden;
  color: $discover-ink;
  font-size: 25rpx;
  font-weight: 600;
  line-height: 1.42;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.archive-card__region {
  margin-top: 6rpx;
  overflow: hidden;
  color: $discover-muted;
  font-size: 18rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 传承人继续使用大图档案卡，仅改为首页同款青绿色遮罩和边框。 */
.inheritor-channel .channel-heading,
.knowledge-channel .channel-heading {
  margin-top: 5rpx;
}

.inheritor-card {
  position: relative;
  min-width: 0;
  height: 374rpx;
  overflow: hidden;
  border: 1rpx solid rgba(45, 105, 91, 0.21);
  border-radius: 15rpx;
  background: #d1dfd7;
  box-shadow: 0 7rpx 16rpx rgba(51, 93, 71, 0.12);
}

.inheritor-card__portrait {
  width: 100%;
  height: 100%;
}

.inheritor-card__shade {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 78rpx 20rpx 20rpx;
  background: linear-gradient(180deg, transparent, rgba(25, 72, 66, 0.88));
}

.inheritor-card__name {
  color: #fbfdf4;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 29rpx;
  font-weight: 600;
}

.inheritor-card__meta {
  display: flex;
  align-items: center;
  gap: 9rpx;
  margin-top: 7rpx;
  color: rgba(247, 251, 239, 0.76);
  font-size: 17rpx;
}

.inheritor-card__level {
  padding: 3rpx 7rpx;
  border: 1rpx solid rgba(247, 251, 239, 0.4);
  border-radius: 999rpx;
}

/* 知识频道每条内容独立成卡，仍使用原详情跳转。 */
.knowledge-list {
  display: grid;
  gap: 13rpx;
}

.knowledge-item {
  display: flex;
  align-items: center;
  padding: 19rpx;
  border: 1rpx solid rgba(48, 108, 91, 0.14);
  border-radius: 14rpx;
  background: $discover-paper;
  box-shadow: 0 5rpx 12rpx rgba(61, 101, 71, 0.07);
}

.knowledge-item:first-child {
  padding-top: 19rpx;
}

.knowledge-item:last-child {
  border-bottom: 1rpx solid rgba(48, 108, 91, 0.14);
}

.knowledge-index {
  width: 48rpx;
  align-self: flex-start;
  flex-shrink: 0;
  color: #759477;
  font-family: Georgia, serif;
  font-size: 18rpx;
}

.knowledge-copy {
  min-width: 0;
  flex: 1;
  margin-right: 15rpx;
}

.knowledge-kicker {
  color: $discover-green;
  font-size: 16rpx;
  letter-spacing: 2rpx;
}

.knowledge-title {
  display: -webkit-box;
  margin-top: 6rpx;
  overflow: hidden;
  color: $discover-ink;
  font-size: 25rpx;
  font-weight: 600;
  line-height: 1.42;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.knowledge-summary {
  display: -webkit-box;
  margin-top: 7rpx;
  overflow: hidden;
  color: $discover-muted;
  font-size: 18rpx;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.knowledge-source {
  margin-top: 6rpx;
  color: $discover-faint;
  font-size: 16rpx;
}

.knowledge-cover {
  width: 142rpx;
  height: 112rpx;
  flex-shrink: 0;
  border-radius: 11rpx;
  background: #d2e0d8;
}

/* 种草工具栏保留发布入口，并突出为绿色描边按钮。 */
.notes-toolbar {
  margin-top: 5rpx;
}

.publish-entry {
  padding: 9rpx 15rpx;
  border: 1rpx solid rgba(40, 95, 92, 0.34);
  border-radius: 999rpx;
  background: rgba(220, 235, 209, 0.62);
  color: $discover-deep;
  font-size: 19rpx;
}

.note-filter-scroll {
  margin-bottom: 15rpx;
}

/* 每篇种草笔记使用独立纸张卡，点赞、评论和详情点击区域保持原样。 */
.note-list {
  display: grid;
  gap: 15rpx;
}

.note-item {
  padding: 22rpx;
  border: 1rpx solid rgba(48, 108, 91, 0.15);
  border-radius: 15rpx;
  background: $discover-paper;
  box-shadow: 0 6rpx 14rpx rgba(61, 101, 71, 0.08);
}

.note-item:first-child {
  padding-top: 22rpx;
}

.note-item:last-child {
  border-bottom: 1rpx solid rgba(48, 108, 91, 0.15);
}

.note-author {
  display: flex;
  align-items: center;
}

.note-avatar,
.comment-avatar {
  width: 62rpx;
  height: 62rpx;
  flex-shrink: 0;
  border: 2rpx solid rgba(40, 95, 92, 0.2);
  border-radius: 50%;
  background: #d2e0d8;
}

.note-author__copy {
  min-width: 0;
  flex: 1;
  margin-left: 13rpx;
}

.note-author__name {
  color: $discover-ink;
  font-size: 22rpx;
  font-weight: 600;
}

.note-author__time {
  margin-top: 3rpx;
  color: $discover-faint;
  font-size: 16rpx;
}

.note-category {
  padding: 5rpx 10rpx;
  border-radius: 999rpx;
  background: rgba(220, 235, 209, 0.72);
  color: $discover-deep;
  font-size: 17rpx;
}

.note-title {
  margin-top: 16rpx;
  color: $discover-ink;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 29rpx;
  font-weight: 600;
  line-height: 1.42;
}

.note-content {
  display: -webkit-box;
  margin-top: 8rpx;
  overflow: hidden;
  color: $discover-muted;
  font-size: 22rpx;
  line-height: 1.65;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.note-images {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8rpx;
  margin-top: 16rpx;
}

.note-image {
  width: 100%;
  height: 190rpx;
  border-radius: 10rpx;
  background: #d2e0d8;
}

.note-actions {
  display: flex;
  justify-content: flex-end;
  gap: 26rpx;
  margin-top: 16rpx;
  color: $discover-deep;
  font-size: 20rpx;
}

/* 评论区维持原加载与提交逻辑，只统一输入框和按钮样式。 */
.comment-box {
  margin-top: 16rpx;
  padding: 17rpx;
  border: 1rpx solid rgba(40, 95, 92, 0.15);
  border-radius: 12rpx;
  background: rgba(235, 244, 226, 0.62);
}

.comment-item {
  display: flex;
  gap: 11rpx;
  padding: 13rpx 0;
  border-top: 1rpx solid rgba(40, 95, 92, 0.12);
}

.comment-item:first-child {
  padding-top: 0;
  border-top: none;
}

.comment-avatar {
  width: 50rpx;
  height: 50rpx;
}

.comment-body {
  min-width: 0;
  flex: 1;
}

.comment-name,
.comment-content,
.comment-time,
.comment-empty {
  display: block;
}

.comment-name {
  color: $discover-ink;
  font-size: 20rpx;
  font-weight: 600;
}

.comment-content {
  margin-top: 5rpx;
  color: $discover-muted;
  font-size: 21rpx;
  line-height: 1.55;
}

.comment-time,
.comment-empty {
  margin-top: 5rpx;
  color: $discover-faint;
  font-size: 16rpx;
}

.comment-input {
  width: 100%;
  min-height: 116rpx;
  margin-top: 12rpx;
  padding: 14rpx;
  border: 1rpx solid rgba(40, 95, 92, 0.16);
  border-radius: 10rpx;
  background: rgba(252, 253, 248, 0.9);
  color: $discover-ink;
  font-size: 21rpx;
}

.comment-submit {
  width: 136rpx;
  margin-top: 12rpx;
  margin-left: auto;
  padding: 10rpx 0;
  border-radius: 999rpx;
  background: $discover-deep;
  color: #f8fbef;
  font-size: 19rpx;
  text-align: center;
}

.refresh-tip {
  position: fixed;
  top: calc(24rpx + env(safe-area-inset-top));
  right: 24rpx;
  z-index: 50;
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(23, 91, 84, 0.9);
  color: #fff;
  font-size: 19rpx;
}

/* 小屏设备收紧标题区和卡片尺寸，避免青铜兽覆盖标题文字。 */
@media screen and (max-width: 350px) {
  .discover-header__mascot {
    width: 112rpx;
    height: 133rpx;
  }

  .discover-header__content {
    width: calc(100% - 112rpx);
  }

  .discover-subtitle,
  .discover-search__text {
    font-size: 18rpx;
  }

  .archive-card__cover {
    height: 198rpx;
  }
}
</style>
