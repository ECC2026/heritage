<template>
  <view class="app-page" style="margin-top: 20px;">
    <page-header title="发布帖子" />
    <view class="hero-card">
      <view class="soft-pill">Create Post</view>
      <view class="post-title">发布社区帖子</view>
      <view class="post-note">分享你的非遗体验、活动见闻或学习心得，让社区内容更丰富。</view>
    </view>

    <view class="section-card">
      <view class="field-group">
        <text class="field-label">帖子标题</text>
        <input v-model.trim="form.title" class="field-input" placeholder="请输入帖子标题" />
      </view>

      <view class="field-group">
        <text class="field-label">帖子分类</text>
        <picker :value="categoryIndex" :range="categories" @change="onCategoryChange">
          <view class="field-picker">{{ categories[categoryIndex] }}</view>
        </picker>
      </view>

      <view class="field-group">
        <text class="field-label">帖子内容</text>
        <textarea v-model.trim="form.content" class="field-textarea" placeholder="写下你的分享内容..." />
      </view>

      <view class="field-group">
        <view class="image-head">
          <text class="field-label">配图上传</text>
          <text class="image-note">最多 3 张</text>
        </view>
        <view class="image-grid">
          <view v-for="(item, index) in imageList" :key="item" class="image-item">
            <image :src="item" class="image-preview" mode="aspectFill"></image>
            <view class="image-remove" @click="removeImage(index)">×</view>
          </view>
          <view v-if="imageList.length < 3" class="image-uploader" @click="chooseImages">
            <text class="image-plus">+</text>
            <text class="image-text">上传图片</text>
          </view>
        </view>
      </view>

      <view class="button-group">
        <view class="secondary-button" @click="previewTemplate">一键填充示例</view>
        <view class="primary-button" @click="handleSubmit">{{ uploading ? '上传中...' : '发布帖子' }}</view>
      </view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { publishPost, uploadImage } from '@/common/request/api.js'
import { requireLogin } from '@/common/session.js'
import { normalizeImage } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      uploading: false,
      form: {
        title: '',
        content: '',
        category: '交流讨论',
        images: ''
      },
      categories: ['交流讨论', '经验分享', '活动招募', '问题求助', '技艺交流'],
      categoryIndex: 0,
      imageList: []
    }
  },
  methods: {
    normalizeImage,
    onCategoryChange(event) {
      this.categoryIndex = Number(event.detail.value)
      this.form.category = this.categories[this.categoryIndex]
    },
    previewTemplate() {
      this.form.title = '第一次体验非遗活动的感受'
      this.form.content = '今天参加了平台上的非遗活动报名，现场体验很真实，也更能感受到传统文化的魅力。推荐大家多关注活动页，报名流程也很顺畅。'
      this.form.category = this.categories[this.categoryIndex]
    },
    chooseImages() {
      uni.chooseImage({
        count: 3 - this.imageList.length,
        sizeType: ['compressed'],
        success: async (res) => {
          const files = res.tempFilePaths || []
          if (!files.length) {
            return
          }
          this.uploading = true
          uni.showLoading({ title: '上传中' })
          try {
            for (const file of files) {
              const url = await uploadImage(file)
              this.imageList.push(normalizeImage(url))
            }
          } catch (error) {
          } finally {
            this.uploading = false
            uni.hideLoading()
          }
        }
      })
    },
    removeImage(index) {
      this.imageList.splice(index, 1)
    },
    async handleSubmit() {
      if (!requireLogin()) return
      if (!this.form.title) {
        uni.showToast({ title: '请输入标题', icon: 'none' })
        return
      }
      if (!this.form.content) {
        uni.showToast({ title: '请输入内容', icon: 'none' })
        return
      }
      if (this.uploading) {
        uni.showToast({ title: '图片上传中，请稍候', icon: 'none' })
        return
      }
      await publishPost({
        ...this.form,
        images: this.imageList.join(',')
      })
      uni.showToast({ title: '发布成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 400)
    }
  }
}
</script>

<style lang="scss" scoped>
.post-title {
  margin-top: 20rpx;
  font-size: 48rpx;
  font-weight: 700;
  color: #34251f;
}

.post-note {
  margin-top: 16rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: #8a7466;
}

.field-group {
  margin-bottom: 24rpx;
}

.button-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
  margin-top: 12rpx;
}

.image-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-note {
  font-size: 22rpx;
  color: #9a8477;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.image-item,
.image-uploader {
  position: relative;
  width: 196rpx;
  height: 196rpx;
  border-radius: 20rpx;
  overflow: hidden;
}

.image-preview {
  width: 100%;
  height: 100%;
  background: #f0e5d8;
}

.image-remove {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 38rpx;
  height: 38rpx;
  line-height: 38rpx;
  border-radius: 50%;
  text-align: center;
  font-size: 28rpx;
  color: #fff;
  background: rgba(0, 0, 0, 0.48);
}

.image-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f7efe7;
  border: 2rpx dashed rgba(166, 71, 45, 0.25);
}

.image-plus {
  font-size: 54rpx;
  color: #a6472d;
}

.image-text {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #8a7466;
}
</style>
