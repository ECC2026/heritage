<script>
export default {
  onLaunch() {
    console.log('非遗文化互动平台已启动')
  }
}
</script>

<style lang="scss">
page {
  background: #f6efe6;
  color: #2f241f;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

view,
text,
button,
input,
textarea,
image,
scroll-view,
swiper,
swiper-item {
  box-sizing: border-box;
}

button::after {
  border: none;
}

.app-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(217, 164, 65, 0.12), transparent 28%),
    linear-gradient(180deg, #fbf7f1 0%, #f5ede2 100%);
}

.with-app-tabbar,
.with-bottom-nav {
  padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

.safe-top {
  padding-top: calc(28rpx + env(safe-area-inset-top));
}

.hero-card {
  margin: 0 24rpx;
  padding: 28rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #fff7ef 0%, #f6e4d0 100%);
  box-shadow: 0 18rpx 44rpx rgba(87, 55, 36, 0.08);
}

.section-card {
  margin: 24rpx;
  padding: 28rpx;
  border-radius: 28rpx;
  background: rgba(255, 252, 247, 0.94);
  box-shadow: 0 14rpx 32rpx rgba(87, 55, 36, 0.06);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #34251f;
}

.section-note {
  font-size: 24rpx;
  color: #8a7466;
}

.soft-pill {
  display: inline-flex;
  align-items: center;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(166, 71, 45, 0.1);
  color: #8b381f;
  font-size: 22rpx;
}

.primary-button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #a6472d 0%, #d88b45 100%);
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
}

.secondary-button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  border-radius: 999rpx;
  background: #fff;
  color: #8b381f;
  font-size: 30rpx;
  border: 2rpx solid rgba(166, 71, 45, 0.18);
}

.empty-block {
  padding: 48rpx 20rpx;
  text-align: center;
  color: #8a7466;
  font-size: 26rpx;
}

.field-label {
  display: block;
  margin-bottom: 14rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #4a3931;
}

.field-input,
.field-textarea,
.field-picker {
  width: 100%;
  background: #fff;
  border-radius: 24rpx;
  border: 2rpx solid rgba(166, 71, 45, 0.1);
  padding: 0 26rpx;
  font-size: 28rpx;
  color: #2f241f;
}

.field-input,
.field-picker {
  height: 88rpx;
  line-height: 88rpx;
}

.field-textarea {
  min-height: 220rpx;
  padding-top: 22rpx;
  padding-bottom: 22rpx;
}
</style>
