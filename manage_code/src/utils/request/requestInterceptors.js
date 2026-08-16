/**
 * 请求拦截器
 */
import { useUserStore } from '../../stores/user'

export function requestInterceptors(config) {
  const userStore = useUserStore()

  config.headers = config.headers || {}
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }

  if (config.method === 'get') {
    config.params = {
      ...config.params,
      t: Date.now()
    }
  }

  return config
}

/**
 * 请求错误处理
 */
export function requestErrorHandler(error) {
  console.error('请求错误:', error)
  return Promise.reject(error)
}
