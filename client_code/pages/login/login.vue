<template>
  <view class="login-page">
    <page-header title="登录" />
    <view class="login-brand">
      <view class="soft-pill">Client Login</view>
      <view class="login-title">欢迎回到非遗文化互动平台</view>
      <view class="login-subtitle">使用手机号或用户名登录，继续浏览资讯、报名活动和购买文创商品。</view>
    </view>

    <view class="login-panel">
      <view class="field-block">
        <text class="field-label">账号</text>
        <input v-model.trim="form.account" class="field-input" placeholder="请输入手机号或用户名" />
      </view>

      <view class="field-block">
        <text class="field-label">密码</text>
        <input v-model.trim="form.password" class="field-input" password placeholder="请输入密码" />
      </view>

      <view class="button-row">
        <view class="secondary-button" @click="fillDemo">填入示例</view>
        <view class="primary-button" @click="handleLogin">立即登录</view>
      </view>

      <view class="login-helper">
        <text>示例账号可用 `13800001001 / 123456`</text>
        <text class="link-text" @click="toRegister">没有账号？去注册</text>
      </view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getUserInfo as fetchUserInfo, login } from '@/common/request/api.js'
import { setToken, setUserInfo } from '@/common/session.js'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      form: {
        account: '',
        password: ''
      }
    }
  },
  methods: {
    fillDemo() {
      this.form.account = '13800001001'
      this.form.password = '123456'
    },
    async handleLogin() {
      if (!this.form.account) {
        uni.showToast({ title: '请输入账号', icon: 'none' })
        return
      }
      if (!this.form.password) {
        uni.showToast({ title: '请输入密码', icon: 'none' })
        return
      }

      const payload = {
        password: this.form.password
      }

      if (/^\d{11}$/.test(this.form.account)) {
        payload.phone = this.form.account
      } else {
        payload.username = this.form.account
      }

      const token = await login(payload)
      setToken(token)
      const userInfo = await fetchUserInfo()
      setUserInfo(userInfo)

      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        uni.switchTab({ url: '/pages/index/index' })
      }, 300)
    },
    toRegister() {
      uni.navigateTo({ url: '/pages/register/register' })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  padding: 0 36rpx 48rpx;
  background:
    radial-gradient(circle at top left, rgba(216, 139, 69, 0.2), transparent 28%),
    linear-gradient(160deg, #5a3528 0%, #8b4d31 52%, #d88b45 100%);
}

.login-brand {
  padding: 60rpx 10rpx 70rpx;
  color: #fff6ee;
}

.login-title {
  margin-top: 26rpx;
  font-size: 54rpx;
  font-weight: 700;
  line-height: 1.3;
}

.login-subtitle {
  margin-top: 20rpx;
  font-size: 26rpx;
  line-height: 1.8;
  color: rgba(255, 246, 238, 0.82);
}

.login-panel {
  padding: 38rpx 30rpx;
  border-radius: 32rpx;
  background: rgba(255, 252, 247, 0.96);
}

.field-block {
  margin-bottom: 28rpx;
}

.button-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
  margin-top: 18rpx;
}

.login-helper {
  display: flex;
  justify-content: space-between;
  margin-top: 26rpx;
  font-size: 24rpx;
  color: #8a7466;
}

.link-text {
  color: #8b381f;
}
</style>
