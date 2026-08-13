<template>
  <!--
    heritage-subpage 只提供与首页一致的青绿色宣纸视觉。
    页眉的 green 主题不会改变 PageHeader 原有的返回栈和首页兜底逻辑。
  -->
  <view class="app-page heritage-subpage login-page">
    <page-header title="登录" variant="green" />

    <!-- 水墨图片只作为无交互背景，并通过动态路径避免小程序编译成错误的 /assets 地址。 -->
    <view class="login-brand">
      <image class="login-brand__background" :src="pageVisualBackground" mode="aspectFill"></image>
      <view class="login-brand__content">
        <view class="login-kicker">HERITAGE ACCOUNT</view>
        <view class="login-title">欢迎回来</view>
        <view class="login-subtitle">登录非遗科技人文生态平台，继续探索传统技艺与当代生活。</view>
      </view>
    </view>

    <view class="login-panel">
      <view class="login-panel__heading">
        <text class="login-panel__title">账号登录</text>
        <text class="login-panel__note">支持手机号或用户名</text>
      </view>

      <view class="field-block">
        <text class="field-label">账号</text>
        <!--
          使用显式 value/input 回写，避免不同小程序编译端对 v-model.trim 的处理差异。
          输入时不做 trim，提交时再清理账号两侧空格，保证键盘输入内容能立即显示。
        -->
        <input
          :value="form.account"
          class="field-input"
          type="text"
          maxlength="50"
          confirm-type="next"
          cursor-spacing="24"
          placeholder="请输入手机号或用户名"
          placeholder-style="color:#84958f;font-size:26rpx;"
          @input="handleAccountInput"
        />
      </view>

      <view class="field-block">
        <text class="field-label">密码</text>
        <!-- password 明确使用布尔绑定；键盘确认仍调用原登录方法。 -->
        <input
          :value="form.password"
          class="field-input"
          type="text"
          :password="true"
          maxlength="64"
          confirm-type="done"
          cursor-spacing="24"
          placeholder="请输入密码"
          placeholder-style="color:#84958f;font-size:26rpx;"
          @input="handlePasswordInput"
          @confirm="handleLogin"
        />
      </view>

      <view class="button-row">
        <view class="secondary-button" @click="fillDemo">填入示例</view>
        <view class="primary-button" @click="handleLogin">立即登录</view>
      </view>

      <view class="login-helper">
        <text>示例账号：13800001001 / 123456</text>
        <text class="link-text" @click="toRegister">没有账号？去注册</text>
      </view>
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/page-header.vue'
import { getUserInfo as fetchUserInfo, login } from '@/common/request/api.js'
import { setToken, setUserInfo } from '@/common/session.js'

// 与首页及其他绿色业务页共用静态山水素材；登录页不加入青铜兽。
const PAGE_VISUAL_BACKGROUND = '/static/home/feature-side-bg.png'

export default {
  components: {
    PageHeader
  },
  data() {
    return {
      // 纯 UI 背景，不会进入登录请求。
      pageVisualBackground: PAGE_VISUAL_BACKGROUND,
      form: {
        account: '',
        password: ''
      }
    }
  },
  methods: {
    // 显式同步原生 input 的值，修复账号框输入后页面不显示的问题。
    handleAccountInput(event) {
      this.form.account = event && event.detail ? event.detail.value : ''
    },
    // 密码同样显式回写；不在输入过程中裁剪字符，避免光标和内容异常。
    handlePasswordInput(event) {
      this.form.password = event && event.detail ? event.detail.value : ''
    },
    fillDemo() {
      this.form.account = '13800001001'
      this.form.password = '123456'
    },
    async handleLogin() {
      // 账号只在提交时清理两侧空格；密码保持用户真实输入内容。
      const account = String(this.form.account || '').trim()
      const password = String(this.form.password || '')

      if (!account) {
        uni.showToast({ title: '请输入账号', icon: 'none' })
        return
      }
      if (!password) {
        uni.showToast({ title: '请输入密码', icon: 'none' })
        return
      }

      const payload = {
        password
      }

      if (/^\d{11}$/.test(account)) {
        payload.phone = account
      } else {
        payload.username = account
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
@import "@/styles/heritage-subpage.scss";

.login-page {
  min-height: 100vh;
  padding-bottom: calc(64rpx + env(safe-area-inset-bottom));
}

/* 登录主视觉使用水墨山景，不包含青铜兽；内容层始终位于图片上方。 */
.login-brand {
  position: relative;
  z-index: 1;
  min-height: 210rpx;
  margin: 8rpx 28rpx 24rpx;
  overflow: hidden;
  border: 1rpx solid $heritage-line;
  border-radius: 22rpx;
  background: #f5f8ea;
  box-shadow: 0 8rpx 22rpx rgba(63, 102, 74, 0.12);
}

.login-brand__background {
  position: absolute;
  inset: 0;
  z-index: 0;
  display: block;
  width: 100%;
  height: 100%;
  opacity: 0.82;
  pointer-events: none;
}

.login-brand__content {
  position: relative;
  z-index: 1;
  padding: 30rpx 28rpx 32rpx;
}

.login-kicker {
  color: rgba(40, 95, 92, 0.68);
  font-family: Georgia, serif;
  font-size: 16rpx;
  letter-spacing: 6rpx;
}

.login-title {
  margin-top: 7rpx;
  color: $heritage-green;
  font-family: "STKaiti", "KaiTi", "STSong", serif;
  font-size: 46rpx;
  font-weight: 600;
  letter-spacing: 7rpx;
  line-height: 1.3;
}

.login-subtitle {
  max-width: 78%;
  margin-top: 12rpx;
  color: $heritage-muted;
  font-size: 21rpx;
  line-height: 1.65;
}

/* 表单面板抬到纹理层上方，确保原生 input 能获取焦点和接收键盘事件。 */
.login-panel {
  position: relative;
  z-index: 2;
  margin: 0 28rpx;
  padding: 30rpx 26rpx;
  border: 1rpx solid $heritage-line;
  border-radius: 22rpx;
  background: rgba(249, 252, 242, 0.92);
  box-shadow: 0 8rpx 22rpx rgba(63, 102, 74, 0.1);
}

.login-panel__heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 26rpx;
}

.login-panel__title {
  position: relative;
  padding-left: 24rpx;
  color: $heritage-green;
  font-family: "STKaiti", "KaiTi", "STSong", serif;
  font-size: 31rpx;
  font-weight: 600;
  letter-spacing: 3rpx;
}

.login-panel__title::before {
  position: absolute;
  top: 50%;
  left: 0;
  width: 14rpx;
  height: 3rpx;
  border-radius: 3rpx;
  background: $heritage-green;
  content: '';
}

.login-panel__note {
  color: $heritage-muted;
  font-size: 19rpx;
}

.field-block {
  margin-bottom: 24rpx;
}

.field-label {
  margin-bottom: 11rpx;
  color: $heritage-ink;
  font-size: 24rpx;
}

/*
 * position/z-index 和 pointer-events 明确保证输入框位于所有装饰背景之上。
 * box-sizing 防止全宽输入框加 padding 后溢出面板，改善微信端点击区域。
 */
.field-input {
  position: relative;
  z-index: 3;
  box-sizing: border-box;
  width: 100%;
  height: 78rpx;
  padding: 0 24rpx;
  border: 1rpx solid rgba(36, 105, 97, 0.18);
  border-radius: 18rpx;
  background: rgba(252, 253, 248, 0.96);
  color: $heritage-ink;
  font-size: 27rpx;
  line-height: 78rpx;
  pointer-events: auto;
}

.button-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
  margin-top: 18rpx;
}

.button-row .secondary-button,
.button-row .primary-button {
  height: 78rpx;
  font-size: 26rpx;
}

.login-helper {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 24rpx;
  color: $heritage-muted;
  font-size: 21rpx;
  line-height: 1.5;
}

.link-text {
  color: $heritage-green;
  font-weight: 600;
}
</style>
