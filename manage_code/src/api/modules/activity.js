import { get, post, put, del } from '../../utils/request'

// 获取活动列表
export function getActivityList(params) {
  return get('/activities', params)
}

// 获取活动详情
export function getActivityDetail(id) {
  return get(`/activities/${id}`)
}

// 添加活动
export function addActivity(data) {
  return post('/activities', data)
}

// 更新活动
export function updateActivity(id, data) {
  return put(`/activities/${id}`, data)
}

// 删除活动
export function deleteActivity(id) {
  return del(`/activities/${id}`)
}

// 更新活动状态
export function updateActivityStatus(id, data) {
  return put(`/activities/${id}/status`, data)
}

// 审核活动
export function auditActivity(id, data) {
  return put(`/activities/${id}/audit`, data)
}
