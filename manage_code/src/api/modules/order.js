import { get, post, put, del } from '../../utils/request'

// 获取订单列表
export function getOrderList(params) {
  return get('/orders', params)
}

// 获取订单详情
export function getOrderDetail(id) {
  return get(`/orders/${id}`)
}

// 更新订单状态
export function updateOrderStatus(id, data) {
  return put(`/orders/${id}/status`, data)
}

// 删除订单
export function deleteOrder(id) {
  return del(`/orders/${id}`)
}

// 导出订单
export function exportOrders(params) {
  return get('/orders/export', params, { responseType: 'blob' })
}
