<template>
  <view class="app-page inheritor-page" style="margin-top: 20px;">
    <page-header title="传承人认证" />

    <view class="section-card status-card">
      <view class="section-head">
        <text class="section-title">申请状态</text>
        <text class="section-note">{{ statusText(application.auditStatus) }}</text>
      </view>
      <view v-if="application.id">
        <view v-if="Number(application.auditStatus) === 1" class="status-pill status-success">已通过</view>
        <view v-else-if="Number(application.auditStatus) === 2" class="status-pill status-danger">未通过</view>
        <view v-else class="status-pill status-pending">待审核</view>
        <text class="status-line">申请时间：{{ formatDateTime(application.createTime) || '刚刚提交' }}</text>
        <text v-if="application.auditTime" class="status-line">审核时间：{{ formatDateTime(application.auditTime) }}</text>
        <text v-if="application.auditRemark" class="status-line">审核说明：{{ application.auditRemark }}</text>
      </view>
      <view v-else class="empty-block compact-empty">
        <text>你还没有提交传承人认证申请，完善资料后即可提交审核。</text>
      </view>
    </view>

    <view class="section-card">
      <text class="field-label">姓名</text>
      <input v-model.trim="form.name" class="field-input" placeholder="请输入真实姓名" />

      <text class="field-label">联系电话</text>
      <input v-model.trim="form.phone" class="field-input" type="number" maxlength="11" placeholder="请输入联系电话" />

      <text class="field-label">身份证号</text>
      <input v-model.trim="form.idCard" class="field-input" maxlength="18" placeholder="选填，用于审核核验" />

      <text class="field-label">技艺类型</text>
      <input v-model.trim="form.skillType" class="field-input" placeholder="如蜀绣、竹编、川剧变脸" />

      <text class="field-label">技艺简介</text>
      <textarea v-model.trim="form.skillDesc" class="field-textarea" placeholder="请简要介绍你的非遗技艺方向、擅长内容和传承特色"></textarea>

      <text class="field-label">从业经历</text>
      <textarea v-model.trim="form.experience" class="field-textarea" placeholder="可填写学习经历、从业年限、参展参赛或授课经历"></textarea>

      <text class="field-label">资质证明</text>
      <view class="certificate-block" @click="chooseCertificate">
        <image v-if="certificatePreview" :src="certificatePreview" class="certificate-image" mode="aspectFill"></image>
        <view v-else class="certificate-placeholder">
          <text class="certificate-plus">+</text>
          <text class="certificate-text">上传证书/证明材料</text>
        </view>
      </view>
    </view>

    <view class="button-row">
      <button class="secondary-button button-item" @click="fillFromProfile">同步个人资料</button>
      <button class="primary-button button-item" :disabled="submitDisabled || submitting" :loading="submitting" @click="handleSubmit">
        {{ submitLabel }}
      </button>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { applyInheritor, getMyInheritorApplication, getUserInfo as fetchUserInfo, uploadImage } from '@/common/request/api.js'
import { getUserInfo, requireLogin, setUserInfo } from '@/common/session.js'
import { formatDateTime, normalizeImage } from '@/common/utils.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      submitting: false,
      application: {},
      form: {
        name: '',
        phone: '',
        idCard: '',
        skillType: '',
        skillDesc: '',
        experience: '',
        certificate: ''
      }
    }
  },
  computed: {
    certificatePreview() {
      return normalizeImage(this.form.certificate, '')
    },
    submitDisabled() {
      return this.application && Number(this.application.auditStatus) === 1
    },
    submitLabel() {
      if (!this.application || !this.application.id) {
        return '提交认证申请'
      }
      if (Number(this.application.auditStatus) === 0) {
        return '审核中'
      }
      if (Number(this.application.auditStatus) === 1) {
        return '已认证'
      }
      return '重新提交申请'
    }
  },
  onShow() {
    if (!requireLogin()) {
      return
    }
    this.loadData()
  },
  methods: {
    formatDateTime,
    normalizeImage,
    async loadData() {
      await Promise.all([this.loadProfile(), this.loadApplication()])
    },
    async loadProfile() {
      try {
        const userInfo = await fetchUserInfo()
        setUserInfo(userInfo || {})
        this.fillFromProfile()
      } catch (error) {
        this.fillFromProfile()
      }
    },
    async loadApplication() {
      try {
        const result = await getMyInheritorApplication()
        this.application = result || {}
        if (this.application && this.application.id) {
          this.form = {
            name: this.application.name || this.form.name,
            phone: this.application.phone || this.form.phone,
            idCard: this.application.idCard || '',
            skillType: this.application.skillType || '',
            skillDesc: this.application.skillDesc || '',
            experience: this.application.experience || '',
            certificate: this.application.certificate || ''
          }
        }
      } catch (error) {
        this.application = {}
      }
    },
    fillFromProfile() {
      const user = getUserInfo() || {}
      this.form.name = this.form.name || user.nickname || user.username || ''
      this.form.phone = this.form.phone || user.phone || ''
    },
    statusText(status) {
      const map = {
        0: '待审核',
        1: '已通过',
        2: '未通过'
      }
      return map[status] || '未申请'
    },
    chooseCertificate() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        success: async (res) => {
          const filePath = res.tempFilePaths && res.tempFilePaths.length ? res.tempFilePaths[0] : ''
          if (!filePath) {
            return
          }
          uni.showLoading({ title: '上传中' })
          try {
            const url = await uploadImage(filePath)
            this.form.certificate = url
            uni.showToast({
              title: '材料上传成功',
              icon: 'success'
            })
          } catch (error) {
          } finally {
            uni.hideLoading()
          }
        }
      })
    },
    validateForm() {
      if (!this.form.name) {
        uni.showToast({ title: '请填写姓名', icon: 'none' })
        return false
      }
      if (!/^1\d{10}$/.test(this.form.phone)) {
        uni.showToast({ title: '请输入正确联系电话', icon: 'none' })
        return false
      }
      if (!this.form.skillType) {
        uni.showToast({ title: '请填写技艺类型', icon: 'none' })
        return false
      }
      if (!this.form.skillDesc) {
        uni.showToast({ title: '请填写技艺简介', icon: 'none' })
        return false
      }
      return true
    },
    async handleSubmit() {
      if (this.submitDisabled || this.submitting) {
        return
      }
      if (!this.validateForm()) {
        return
      }
      this.submitting = true
      try {
        await applyInheritor(this.form)
        uni.showToast({
          title: '申请已提交',
          icon: 'success'
        })
        await this.loadApplication()
      } catch (error) {
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.inheritor-page {
  padding: 24rpx;
  padding-bottom: 48rpx;
  background:
    radial-gradient(circle at top left, rgba(166, 71, 45, 0.14), transparent 28%),
    linear-gradient(180deg, #f8efe7 0%, #f4f1ec 100%);
}

.status-card {
  margin-bottom: 24rpx;
}

.compact-empty {
  padding: 12rpx 0 0;
}

.status-pill {
  display: inline-flex;
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 600;
}

.status-pending {
  background: rgba(197, 141, 26, 0.14);
  color: #b27a12;
}

.status-success {
  background: rgba(46, 145, 82, 0.14);
  color: #2e9152;
}

.status-danger {
  background: rgba(178, 74, 60, 0.14);
  color: #b24a3c;
}

.status-line {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #8a6f62;
}

.certificate-block {
  margin-top: 8rpx;
  border-radius: 24rpx;
  overflow: hidden;
  background: #fff8f1;
  border: 2rpx dashed rgba(166, 71, 45, 0.18);
}

.certificate-image {
  width: 100%;
  height: 320rpx;
  background: #f1e4d7;
}

.certificate-placeholder {
  height: 220rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8f7366;
}

.certificate-plus {
  font-size: 52rpx;
  color: #a6472d;
}

.certificate-text {
  margin-top: 8rpx;
  font-size: 24rpx;
}

.button-row {
  display: flex;
  gap: 18rpx;
  margin-top: 24rpx;
}

.button-item {
  flex: 1;
}
</style>
