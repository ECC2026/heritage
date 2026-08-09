import { get, post, put, del } from '../../utils/request'

// 获取商品列表
export function getProductList(params) {
  return get('/products', params)
}

// 获取商品详情
export function getProductDetail(id) {
  return get(`/products/${id}`)
}

// 添加商品
export function addProduct(data) {
  return post('/products', data)
}

// 更新商品
export function updateProduct(id, data) {
  return put(`/products/${id}`, data)
}

// 删除商品
export function deleteProduct(id) {
  return del(`/products/${id}`)
}

// 更新商品状态
export function updateProductStatus(id, data) {
  return put(`/products/${id}/status`, data)
}

// 更新商品库存
export function updateProductStock(id, data) {
  return put(`/products/${id}/stock`, data)
}
