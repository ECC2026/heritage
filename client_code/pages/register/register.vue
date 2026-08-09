<template>
  <view class="register-page">
    <page-header title="注册" />
    <view class="register-head">
      <view class="soft-pill">Create Account</view>
      <view class="register-title">创建你的非遗账号</view>
      <view class="register-subtitle">注册后即可浏览资讯、加入社区、报名活动并下单购买文创商品。</view>
    </view>

    <view class="register-panel">
      <view class="field-block">
        <text class="field-label">手机号</text>
        <input v-model.trim="form.phone" class="field-input" type="number" maxlength="11" placeholder="请输入手机号" />
      </view>

      <view class="field-block">
        <text class="field-label">昵称</text>
        <input v-model.trim="form.nickname" class="field-input" placeholder="请输入昵称" />
      </view>

      <view class="field-block">
        <text class="field-label">用户名</text>
        <input v-model.trim="form.username" class="field-input" placeholder="可选，默认使用手机号" />
      </view>

      <view class="field-block">
        <text class="field-label">密码</text>
        <input v-model.trim="form.password" class="field-input" password placeholder="请输入密码" />
      </view>

      <view class="field-block">
        <text class="field-label">确认密码</text>
        <input v-model.trim="form.confirmPassword" class="field-input" password placeholder="请再次输入密码" />
      </view>

      <view class="primary-button" @click="handleRegister">完成注册</view>
      <view class="register-foot" @click="toLogin">已有账号，返回登录</view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { register } from '@/common/request/api.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      form: {
        phone: '',
        password: '',
        confirmPassword: '',
        nickname: '',
        username: ''
      }
    }
  },
  methods: {
    async handleRegister() {
      if (!/^\d{11}$/.test(this.form.phone)) {
        uni.showToast({ title: '请输入正确手机号', icon: 'none' })
        return
      }
      if (!this.form.password || this.form.password.length < 6) {
        uni.showToast({ title: '密码至少6位', icon: 'none' })
        return
      }
      if (this.form.password !== this.form.confirmPassword) {
        uni.showToast({ title: '两次密码输入不一致', icon: 'none' })
        return
      }

      await register({
        phone: this.form.phone,
        password: this.form.password,
        nickname: this.form.nickname,
        username: this.form.username
      })
      uni.showToast({ title: '注册成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 500)
    },
    toLogin() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  padding: 0 36rpx 48rpx;
  background:
    radial-gradient(circle at top right, rgba(216, 139, 69, 0.14), transparent 30%),
    linear-gradient(180deg, #fbf7f1 0%, #f3e7da 100%);
}

.register-head {
  padding: 60rpx 10rpx 40rpx;
}

.register-title {
  margin-top: 24rpx;
  font-size: 52rpx;
  font-weight: 700;
  color: #34251f;
}

.register-subtitle {
  margin-top: 18rpx;
  font-size: 26rpx;
  line-height: 1.8;
  color: #8a7466;
}

.register-panel {
  padding: 36rpx 30rpx;
  border-radius: 32rpx;
  background: rgba(255, 252, 247, 0.96);
}

.field-block {
  margin-bottom: 26rpx;
}

.register-foot {
  margin-top: 28rpx;
  text-align: center;
  color: #8b381f;
  font-size: 26rpx;
}
</style>
