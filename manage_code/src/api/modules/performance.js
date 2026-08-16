import { get, post, put, del } from '../../utils/request'

// 获取演出列表
export function getPerformanceList(params) {
  return get('/performances', params)
}

// 获取演出详情
export function getPerformanceDetail(id) {
  return get(`/performances/${id}`)
}

// 添加演出
export function addPerformance(data) {
  return post('/performances', data)
}

// 更新演出
export function updatePerformance(id, data) {
  return put(`/performances/${id}`, data)
}

// 删除演出
export function deletePerformance(id) {
  return del(`/performances/${id}`)
}

// 更新演出状态
export function updatePerformanceStatus(id, data) {
  return put(`/performances/${id}/status`, data)
}
