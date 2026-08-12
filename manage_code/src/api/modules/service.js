import { get, post, put } from '../../utils/request'

// 服务列表
export function getServiceList(params) {
  return get('/admin/services', params)
}

// 新增服务
export function addService(data) {
  return post('/admin/services', data)
}

// 更新服务
export function updateService(id, data) {
  return put(`/admin/services/${id}`, data)
}

// 更新服务状态
export function updateServiceStatus(id, data) {
  return put(`/admin/services/${id}/status`, data)
}

// 服务场次列表
export function getServiceScheduleList(id) {
  return get(`/admin/services/${id}/schedules`)
}

// 新增服务场次
export function addServiceSchedule(id, data) {
  return post(`/admin/services/${id}/schedules`, data)
}
