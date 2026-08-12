<template>
  <view class="app-page cooperation-page" style="margin-top: 20px;">
    <page-header title="B端合作" />

    <view class="section-card intro-card">
      <view class="intro-title">非遗生态合作</view>
      <view class="intro-desc">
        面向文旅机构、品牌企业、活动策划方与产业园区，开放平台产品体系、服务与场次资源，共同打造非遗消费与体验生态。
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">合作类型</text>
        <text class="section-note">选择最契合的合作方向</text>
      </view>
      <view v-if="types.length" class="type-grid">
        <view
          v-for="(label, code) in types"
          :key="code"
          class="type-card"
          :class="{ active: selectedType === code }"
          @click="selectedType = code"
        >
          <text class="type-code">{{ code }}</text>
          <text class="type-name">{{ label }}</text>
        </view>
      </view>
      <view v-else class="empty-block compact">
        <text>合作类型加载中...</text>
      </view>
    </view>

    <view class="section-card">
      <view class="section-head">
        <text class="section-title">申请信息</text>
        <text class="section-note">提交后由平台审核</text>
      </view>
      <view class="field-row">
        <text class="field-label">企业名称</text>
        <input v-model.trim="form.companyName" class="field-input" placeholder="请输入企业/机构名称" />
      </view>
      <view class="field-row">
        <text class="field-label">联系人</text>
        <input v-model.trim="form.contactName" class="field-input" placeholder="请输入联系人姓名" />
      </view>
      <view class="field-row">
        <text class="field-label">手机号</text>
        <input v-model.trim="form.contactPhone" class="field-input" type="number" maxlength="11" placeholder="请输入联系方式" />
      </view>
      <view class="field-block">
        <text class="field-label">合作需求</text>
        <textarea v-model.trim="form.requirement" class="field-textarea" placeholder="请简要描述合作诉求、预期内容与时间安排等"></textarea>
      </view>
    </view>

    <view class="bottom-wrap">
      <view class="primary-button" :class="{ disabled: submitting }" @click="handleSubmit">
        {{ submitting ? '提交中…' : '提交合作申请' }}
      </view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getCooperationTypes, submitCooperationApplication } from '@/common/request/api.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      types: {},
      selectedType: '',
      form: {
        companyName: '',
        contactName: '',
        contactPhone: '',
        requirement: ''
      },
      submitting: false
    }
  },
  onLoad() {
    this.loadTypes()
  },
  methods: {
    async loadTypes() {
      try {
        const result = await getCooperationTypes()
        this.types = result && typeof result === 'object' ? result : {}
      } catch (error) {
        this.types = {}
      }
    },
    async handleSubmit() {
      if (!this.selectedType) {
        uni.showToast({ title: '请选择合作类型', icon: 'none' })
        return
      }
      if (!this.form.companyName) {
        uni.showToast({ title: '请填写企业名称', icon: 'none' })
        return
      }
      if (!this.form.contactName) {
        uni.showToast({ title: '请填写联系人', icon: 'none' })
        return
      }
      if (!/^1\d{10}$/.test(this.form.contactPhone)) {
        uni.showToast({ title: '请填写正确的手机号', icon: 'none' })
        return
      }
      if (!this.form.requirement) {
        uni.showToast({ title: '请填写合作需求', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        await submitCooperationApplication({
          companyName: this.form.companyName,
          contactName: this.form.contactName,
          contactPhone: this.form.contactPhone,
          cooperationType: this.selectedType,
          requirement: this.form.requirement
        })
        uni.showToast({ title: '申请已提交', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 600)
      } catch (error) {
        uni.showToast({ title: (error && error.message) || '提交失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.cooperation-page {
  padding: 24rpx;
  padding-bottom: 140rpx;
  background:
    radial-gradient(circle at top right, rgba(166, 71, 45, 0.14), transparent 30%),
    linear-gradient(180deg, #f8efe7 0%, #f4f1ec 100%);
}

.intro-card {
  background: linear-gradient(135deg, #a6472d 0%, #c37a4f 100%);
}

.intro-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #fffaf2;
  letter-spacing: 2rpx;
}

.intro-desc {
  margin-top: 16rpx;
  font-size: 24rpx;
  line-height: 1.8;
  color: rgba(255, 250, 242, 0.88);
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.type-card {
  padding: 26rpx 24rpx;
  border-radius: 22rpx;
  background: linear-gradient(180deg, #fffaf5 0%, #f8eee5 100%);
}

.type-card.active {
  border: 2rpx solid #a6472d;
  background: rgba(166, 71, 45, 0.1);
}

.type-code {
  display: block;
  font-size: 24rpx;
  color: #a6472d;
  letter-spacing: 1rpx;
}

.type-name {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  font-weight: 700;
  color: #34251f;
}

.field-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 22rpx 0;
  border-top: 1rpx solid #f0e1d8;
}

.field-row:first-child {
  border-top: none;
  padding-top: 0;
}

.field-label {
  width: 140rpx;
  flex-shrink: 0;
  font-size: 26rpx;
  color: #6f5a4c;
}

.field-input {
  flex: 1;
  font-size: 28rpx;
  color: #34251f;
}

.field-block {
  padding-top: 22rpx;
  border-top: 1rpx solid #f0e1d8;
}

.field-block .field-label {
  display: block;
  width: auto;
  margin-bottom: 14rpx;
}

.field-textarea {
  width: 100%;
  height: 200rpx;
  box-sizing: border-box;
  padding: 18rpx 20rpx;
  border-radius: 16rpx;
  background: rgba(247, 238, 230, 0.9);
  font-size: 26rpx;
  color: #34251f;
}

.bottom-wrap {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
  background: rgba(255, 252, 247, 0.98);
  box-shadow: 0 -10rpx 30rpx rgba(77, 53, 39, 0.08);
}

.disabled {
  opacity: 0.78;
}

.compact {
  padding: 10rpx 0;
}
</style>
