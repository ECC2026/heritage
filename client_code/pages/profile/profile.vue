<template>
  <view class="legacy-page app-page">
    <view class="safe-top"></view>
    <view class="legacy-card">
      <text>该旧模板页已停用，请使用 /pages/profile/index</text>
    </view>
  </view>
</template>

<script>
export default {}
</script>

<style lang="scss" scoped>
.legacy-page {
  min-height: 100vh;
  padding: 24rpx;
}

.legacy-card {
  padding: 32rpx 28rpx;
  border-radius: 28rpx;
  background: rgba(255, 252, 247, 0.94);
  color: #5f4a3f;
  font-size: 28rpx;
  line-height: 1.8;
}
</style>
