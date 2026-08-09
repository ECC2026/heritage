import { get, post, put, del } from '../../utils/request'

// 获取轮播图列表
export function getBannerList(params) {
  return get('/banners', params)
}

// 获取轮播图详情
export function getBannerDetail(id) {
  return get(`/banners/${id}`)
}

// 添加轮播图
export function addBanner(data) {
  return post('/banners', data)
}

// 更新轮播图
export function updateBanner(id, data) {
  return put(`/banners/${id}`, data)
}

// 删除轮播图
export function deleteBanner(id) {
  return del(`/banners/${id}`)
}

// 更新轮播图状态
export function updateBannerStatus(id, data) {
  return put(`/banners/${id}/status`, data)
}
