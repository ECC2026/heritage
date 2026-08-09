import { get, put } from '../../utils/request'

// 获取报名列表
export function getSignupList(params) {
  return get('/signups', params)
}

// 获取报名详情
export function getSignupDetail(id) {
  return get(`/signups/${id}`)
}

// 更新报名状态
export function updateSignupStatus(id, data) {
  return put(`/signups/${id}/status`, data)
}

// 导出报名列表
export function exportSignups(params) {
  return get('/signups/export', params, { responseType: 'blob' })
}
