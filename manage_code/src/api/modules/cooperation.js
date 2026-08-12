import { get, put } from '../../utils/request'

// 合作申请列表
export function getCooperationApplicationList(params) {
  return get('/admin/cooperation-applications', params)
}

// 合作申请详情
export function getCooperationApplicationDetail(id) {
  return get(`/admin/cooperation-applications/${id}`)
}

// 更新合作申请状态与备注
export function updateCooperationApplicationStatus(id, data) {
  return put(`/admin/cooperation-applications/${id}/status`, data)
}
