import { get, post, put, del } from '../../utils/request'

// 获取资讯列表
export function getNewsList(params) {
  return get('/news', params)
}

// 获取资讯详情
export function getNewsDetail(id) {
  return get(`/news/${id}`)
}

// 添加资讯
export function addNews(data) {
  return post('/news', data)
}

// 更新资讯
export function updateNews(id, data) {
  return put(`/news/${id}`, data)
}

// 删除资讯
export function deleteNews(id) {
  return del(`/news/${id}`)
}

// 更新资讯状态
export function updateNewsStatus(id, data) {
  return put(`/news/${id}/status`, data)
}
