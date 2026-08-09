/**
 * 响应拦截器
 */
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import router from '../../router'

export function responseInterceptors(response) {
  const responseType = response.config?.responseType
  if (responseType === 'blob' || response.data instanceof Blob) {
    return response.data
  }

  const { code, message, data } = response.data

  if (code === 200) {
    return data
  }
  if (code === 401) {
    const userStore = useUserStore()
    userStore.logout()
    ElMessage.error('登录已过期，请重新登录')
    router.push('/login')
    return Promise.reject(new Error(message || '登录已过期'))
  }
  if (code === 403) {
    ElMessage.error('没有权限访问')
    return Promise.reject(new Error(message || '没有权限访问'))
  }
  if (code === 500) {
    ElMessage.error(message || '服务器错误')
    return Promise.reject(new Error(message || '服务器错误'))
  }

  ElMessage.error(message || '请求失败')
  return Promise.reject(new Error(message || '请求失败'))
}

/**
 * 响应错误处理
 */
export function responseErrorHandler(error) {
  console.error('响应错误:', error)

  if (error.response) {
    const status = error.response.status
    switch (status) {
      case 401:
        const userStore = useUserStore()
        userStore.logout()
        ElMessage.error('未授权，请重新登录')
        router.push('/login')
        break
      case 403:
        ElMessage.error('拒绝访问')
        break
      case 404:
        ElMessage.error('请求资源不存在')
        break
      case 500:
        ElMessage.error('服务器错误')
        break
      default:
        ElMessage.error('请求失败')
    }
  } else if (error.request) {
    ElMessage.error('网络连接失败，请检查网络')
  } else {
    ElMessage.error('请求配置错误')
  }

  return Promise.reject(error)
}
