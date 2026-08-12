import { createRouter, createWebHistory } from 'vue-router'

export const dashboardChildren = [
  { path: 'home', name: 'Home', component: () => import('../views/home/index.vue'), meta: { title: '总览看板' } },
  { path: 'users', name: 'Users', component: () => import('../views/users/index.vue'), meta: { title: '用户管理' } },
  { path: 'inheritants', name: 'Inheritants', component: () => import('../views/inheritants/index.vue'), meta: { title: '传承人管理' } },
  { path: 'news', name: 'News', component: () => import('../views/news/index.vue'), meta: { title: '非遗资讯' } },
  { path: 'performances', name: 'Performances', component: () => import('../views/performances/index.vue'), meta: { title: '非遗演出' } },
  { path: 'products', name: 'Products', component: () => import('../views/products/index.vue'), meta: { title: '文创商品' } },
  { path: 'orders', name: 'Orders', component: () => import('../views/orders/index.vue'), meta: { title: '订单管理' } },
  { path: 'activities', name: 'Activities', component: () => import('../views/activities/index.vue'), meta: { title: '活动管理' } },
  { path: 'signups', name: 'Signups', component: () => import('../views/signups/index.vue'), meta: { title: '报名审核' } },
  { path: 'banners', name: 'Banners', component: () => import('../views/banners/index.vue'), meta: { title: '轮播图管理' } },
  { path: 'product-systems', name: 'ProductSystems', component: () => import('../views/product-systems/index.vue'), meta: { title: '产品体系' } },
  { path: 'services', name: 'Services', component: () => import('../views/services/index.vue'), meta: { title: '服务管理' } },
  { path: 'cooperations', name: 'Cooperations', component: () => import('../views/cooperations/index.vue'), meta: { title: '合作申请' } },
  { path: 'settings', name: 'Settings', component: () => import('../views/settings/index.vue'), meta: { title: '系统设置' } }
]

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '后台登录', public: true }
  },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/home',
    children: dashboardChildren
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  document.title = `${to.meta?.title || '非遗文化后台'} | 非遗文化互动平台`
  const token = localStorage.getItem('token')
  if (!to.meta?.public && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/home'
  }
  return true
})

export default router
