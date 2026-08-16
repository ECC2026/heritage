import { get, post, put, del } from '../../utils/request'

// 获取普通用户列表
export function getUserList(params) {
  return get('/users', params)
}

// 获取用户详情
export function getUserDetail(id) {
  return get(`/users/${id}`)
}

// 更新用户状态
export function updateUserStatus(id, data) {
  return put(`/users/${id}/status`, data)
}

// 删除用户
export function deleteUser(id) {
  return del(`/users/${id}`)
}

// 获取传承人列表
export function getInheritantList(params) {
  return get('/inheritors', params)
}

// 获取传承人详情
export function getInheritantDetail(id) {
  return get(`/inheritors/${id}`)
}

// 审核传承人资质
export function auditInheritor(id, data) {
  return put(`/inheritors/${id}/audit`, data)
}
