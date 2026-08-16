import { post, get } from '../../utils/request'

// 管理员登录
export function adminLogin(data) {
  return post('/admin/login', data)
}

// 获取管理员信息
export function getAdminInfo() {
  return get('/admin/info')
}

// 退出登录
export function logout() {
  return post('/admin/logout')
}
