const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token) {
  uni.setStorageSync(TOKEN_KEY, token || '')
}

export function clearToken() {
  uni.removeStorageSync(TOKEN_KEY)
}

export function getUserInfo() {
  return uni.getStorageSync(USER_INFO_KEY) || {}
}

export function setUserInfo(userInfo) {
  uni.setStorageSync(USER_INFO_KEY, userInfo || {})
}

export function clearUserInfo() {
  uni.removeStorageSync(USER_INFO_KEY)
}

export function clearAuth() {
  clearToken()
  clearUserInfo()
}

export function isLoggedIn() {
  return !!getToken()
}

export function requireLogin() {
  if (isLoggedIn()) return true
  uni.showToast({
    title: '请先登录',
    icon: 'none'
  })
  setTimeout(() => {
    uni.navigateTo({
      url: '/pages/login/login'
    })
  }, 300)
  return false
}
