import config from '@/common/config.js'
import { clearAuth, getToken } from '@/common/session.js'

export function request(options) {
  return new Promise((resolve, reject) => {
    const token = getToken()

    uni.request({
      url: `${config.baseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      timeout: options.timeout || 15000,
      header: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
        ...(options.header || {})
      },
      success: (response) => {
        const payload = response.data || {}

        if (payload.code === 200) {
          resolve(payload.data)
          return
        }

        if (payload.code === 401) {
          clearAuth()
          uni.showToast({
            title: payload.message || '登录已过期',
            icon: 'none'
          })
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages/login/login'
            })
          }, 300)
          reject(payload)
          return
        }

        uni.showToast({
          title: payload.message || '请求失败',
          icon: 'none'
        })
        reject(payload)
      },
      fail: (error) => {
        uni.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(error)
      }
    })
  })
}

export default request
