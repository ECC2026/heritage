<template>
  <view class="app-page discover-page with-bottom-nav">
    <view class="safe-top"></view>

    <view class="discover-header">
      <view class="discover-eyebrow">ICHIP · DISCOVER</view>
      <view class="discover-title">非遗</view>
      <view class="discover-subtitle">发现仍在生活中的传统技艺</view>

      <view class="discover-search" @click="goSearch">
        <text class="discover-search__icon">⌕</text>
        <text class="discover-search__text">搜非遗项目、传承人、地区</text>
        <text class="discover-search__arrow">→</text>
      </view>
    </view>

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
    <bottom-nav current="community" />
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
