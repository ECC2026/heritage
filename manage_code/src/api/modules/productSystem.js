import { get, post, put } from '../../utils/request'

// 产品体系列表
export function getProductSystemList(params) {
  return get('/admin/product-systems', params)
}

// 新增产品体系
export function addProductSystem(data) {
  return post('/admin/product-systems', data)
}

// 更新产品体系
export function updateProductSystem(id, data) {
  return put(`/admin/product-systems/${id}`, data)
}

// 更新产品体系状态
export function updateProductSystemStatus(id, data) {
  return put(`/admin/product-systems/${id}/status`, data)
}
