// API接口封装
import { request } from './request.js'
import config from '@/common/config.js'
import { getToken } from '@/common/session.js'

// 用户登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'POST',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/user/register',
    method: 'POST',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info'
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'PUT',
    data
  })
}

export function uploadImage(filePath) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    uni.uploadFile({
      url: `${config.baseUrl}/upload/image`,
      filePath,
      name: 'file',
      header: {
        Authorization: token ? `Bearer ${token}` : ''
      },
      success: (response) => {
        try {
          const payload = JSON.parse(response.data || '{}')
          if (payload.code === 200) {
            resolve(payload.data)
            return
          }
          uni.showToast({
            title: payload.message || '上传失败',
            icon: 'none'
          })
          reject(payload)
        } catch (error) {
          reject(error)
        }
      },
      fail: (error) => {
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
        reject(error)
      }
    })
  })
}

// 获取首页轮播图
export function getBanners() {
  return request({
    url: '/banners/enable'
  })
}

// 获取热门非遗项目
export function getHeritageProjects() {
  return request({
    url: '/heritage-projects/all'
  })
}

// 获取非遗项目详情
export function getHeritageProjectDetail(id) {
  return request({
    url: `/heritage-projects/${id}`
  })
}

// 获取资讯列表
export function getNewsList(params) {
  return request({
    url: '/news',
    data: params
  })
}

// 获取资讯详情
export function getNewsDetail(id) {
  return request({
    url: `/news/${id}`
  })
}

// 获取商品列表
export function getProducts(params) {
  return request({
    url: '/products',
    data: params
  })
}

// 获取商品详情
export function getProductDetail(id) {
  return request({
    url: `/products/${id}`
  })
}

// 获取活动列表
export function getActivities(params) {
  return request({
    url: '/activities/enable',
    data: params
  })
}

// 获取活动详情
export function getActivityDetail(id) {
  return request({
    url: `/activities/${id}`
  })
}

// 报名活动
export function signupActivity(data) {
  return request({
    url: '/signups',
    method: 'POST',
    data
  })
}

export function getMySignups(params) {
  return request({
    url: '/signups/my',
    data: params
  })
}

export function cancelSignup(id) {
  return request({
    url: `/signups/${id}/cancel`,
    method: 'PUT'
  })
}

// 获取社区帖子列表
export function getPosts(params) {
  return request({
    url: '/posts',
    data: params
  })
}

export function getMyPosts(params) {
  return request({
    url: '/posts/my',
    data: params
  })
}

export function getPostDetail(id) {
  return request({
    url: `/posts/${id}`
  })
}

export function togglePostLike(id) {
  return request({
    url: `/posts/${id}/like`,
    method: 'POST'
  })
}

// 发布帖子
export function publishPost(data) {
  return request({
    url: '/posts',
    method: 'POST',
    data
  })
}

// 评论帖子
export function commentPost(data) {
  return request({
    url: '/comments',
    method: 'POST',
    data
  })
}

export function getComments(params) {
  return request({
    url: '/comments',
    data: params
  })
}

export function deletePost(id) {
  return request({
    url: `/posts/${id}`,
    method: 'DELETE'
  })
}

export function toggleFavorite(data) {
  return request({
    url: '/favorites/toggle',
    method: 'POST',
    data
  })
}

export function getFavoriteStatus(params) {
  return request({
    url: '/favorites/status',
    data: params
  })
}

export function getFavoriteStats() {
  return request({
    url: '/favorites/stats'
  })
}

export function getMyFavorites(params) {
  return request({
    url: '/favorites/my',
    data: params
  })
}

// 添加购物车
export function addCart(data) {
  return request({
    url: '/cart',
    method: 'POST',
    data
  })
}

// 获取购物车列表
export function getCartList() {
  return request({
    url: '/cart'
  })
}

// 创建订单
export function createOrder(data) {
  return request({
    url: '/orders',
    method: 'POST',
    data
  })
}

// 获取订单列表
export function getOrders(params) {
  return request({
    url: '/orders/my',
    data: params
  })
}

// 更新订单状态（确认收货等）
export function updateOrderStatus(id, data) {
  return request({
    url: `/orders/${id}/status`,
    method: 'PUT',
    data
  })
}

export function getMyInheritorApplication() {
  return request({
    url: '/inheritors/my'
  })
}

export function applyInheritor(data) {
  return request({
    url: '/inheritors/apply',
    method: 'POST',
    data
  })
}
