<template>
  <view class="app-page edit-page" style="margin-top: 20px;">
    <page-header title="资料编辑" />

    <view class="section-card edit-header">
      <view class="header-copy">
        <text class="section-title">资料编辑</text>
        <text class="section-note">完善昵称、邮箱和个人基础信息</text>
      </view>
      <view class="avatar-box" @click="chooseAvatar">
        <image :src="previewAvatar" class="avatar-image" mode="aspectFill" />
        <text class="avatar-tip">更换头像</text>
      </view>
    </view>

    <view class="section-card">
      <text class="field-label">昵称</text>
      <input v-model="form.nickname" class="field-input" maxlength="20" placeholder="请输入展示昵称" />

      <text class="field-label">手机号</text>
      <input :value="form.phone" class="field-input disabled-input" disabled />

      <text class="field-label">邮箱</text>
      <input v-model="form.email" class="field-input" maxlength="40" placeholder="用于后台联系与展示" />

      <text class="field-label">性别</text>
      <picker :range="genderOptions" range-key="label" :value="genderIndex" @change="handleGenderChange">
        <view class="field-picker">
          <text>{{ genderText(form.gender) }}</text>
          <text class="picker-arrow">></text>
        </view>
      </picker>

      <text class="field-label">生日</text>
      <picker mode="date" :value="form.birthday" start="1970-01-01" end="2035-12-31" @change="handleBirthdayChange">
        <view class="field-picker">
          <text>{{ form.birthday || '请选择生日' }}</text>
          <text class="picker-arrow">></text>
        </view>
      </picker>
    </view>

    <view class="button-row">
      <button class="secondary-button button-item" @click="handleCancel">取消</button>
      <button class="primary-button button-item" :loading="submitting" @click="handleSave">保存资料</button>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getUserInfo as fetchUserInfo, updateUserInfo, uploadImage } from '@/common/request/api.js'
import { getUserInfo, requireLogin, setUserInfo } from '@/common/session.js'
import { genderText, normalizeImage } from '@/common/utils.js'

const DEFAULT_AVATAR = '/static/img/logo.png'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      submitting: false,
      genderOptions: [
        { label: '保密', value: 0 },
        { label: '男', value: 1 },
        { label: '女', value: 2 }
      ],
      form: {
        avatar: '',
        nickname: '',
        phone: '',
        email: '',
        gender: 0,
        birthday: ''
      }
    }
  },
  computed: {
    genderIndex() {
      const index = this.genderOptions.findIndex((item) => item.value === Number(this.form.gender))
      return index === -1 ? 0 : index
    },
    previewAvatar() {
      return normalizeImage(this.form.avatar) || DEFAULT_AVATAR
    }
  },
  onLoad() {
    if (!requireLogin()) {
      return
    }
    this.fillForm(getUserInfo())
    this.loadRemoteData()
  },
  methods: {
    genderText,
    fillForm(userInfo) {
      const source = userInfo || {}
      this.form = {
        avatar: source.avatar || '',
        nickname: source.nickname || source.username || '',
        phone: source.phone || '',
        email: source.email || '',
        gender: source.gender === undefined || source.gender === null ? 0 : Number(source.gender),
        birthday: source.birthday || ''
      }
    },
    async loadRemoteData() {
      try {
        const userInfo = await fetchUserInfo()
        setUserInfo(userInfo || {})
        this.fillForm(userInfo)
      } catch (error) {}
    },
    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        success: async (res) => {
          const filePath = res.tempFilePaths && res.tempFilePaths.length ? res.tempFilePaths[0] : ''
          if (!filePath) {
            return
          }
          uni.showLoading({
            title: '上传中'
          })
          try {
            const avatarUrl = await uploadImage(filePath)
            this.form.avatar = avatarUrl
            uni.showToast({
              title: '头像上传成功',
              icon: 'success'
            })
          } catch (error) {
          } finally {
            uni.hideLoading()
          }
        }
      })
    },
    handleGenderChange(event) {
      const index = Number(event.detail.value || 0)
      this.form.gender = this.genderOptions[index].value
    },
    handleBirthdayChange(event) {
      this.form.birthday = event.detail.value
    },
    validateForm() {
      if (!this.form.nickname || !this.form.nickname.trim()) {
        uni.showToast({
          title: '请填写昵称',
          icon: 'none'
        })
        return false
      }

      if (this.form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.form.email)) {
        uni.showToast({
          title: '邮箱格式不正确',
          icon: 'none'
        })
        return false
      }

      return true
    },
    async handleSave() {
      if (!this.validateForm() || this.submitting) {
        return
      }

      this.submitting = true
      try {
        const payload = {
          avatar: this.form.avatar,
          nickname: this.form.nickname.trim(),
          email: this.form.email ? this.form.email.trim() : '',
          gender: Number(this.form.gender)
        }

        if (this.form.birthday) {
          payload.birthday = this.form.birthday
        }

        const userInfo = await updateUserInfo(payload)
        setUserInfo(userInfo || {})
        uni.showToast({
          title: '资料已保存',
          icon: 'success'
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 500)
      } catch (error) {
      } finally {
        this.submitting = false
      }
    },
    handleCancel() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.edit-page {
  padding: 24rpx;
  padding-bottom: 48rpx;
  background:
    radial-gradient(circle at top left, rgba(166, 71, 45, 0.14), transparent 28%),
    linear-gradient(180deg, #f8efe7 0%, #f4f1ec 100%);
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24rpx;
}

.header-copy {
  flex: 1;
}

.avatar-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.avatar-image {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  background: #f4e4d6;
}

.avatar-tip {
  font-size: 22rpx;
  color: #8b6a5e;
}

.disabled-input {
  color: #9a887f;
  background: #f7f0ea;
}

.picker-arrow {
  color: #c3a79a;
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
