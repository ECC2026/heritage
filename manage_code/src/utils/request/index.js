/**
 * API服务封装
 */
import axios from 'axios'
import { requestInterceptors, requestErrorHandler } from './requestInterceptors'
import { responseInterceptors, responseErrorHandler } from './responseInterceptors'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

service.interceptors.request.use(
  requestInterceptors,
  requestErrorHandler
)

service.interceptors.response.use(
  responseInterceptors,
  responseErrorHandler
)

export function get(url, params = {}, config = {}) {
  return service({
    url,
    method: 'GET',
    params,
    ...config
  })
}

export function post(url, data = {}, config = {}) {
  return service({
    url,
    method: 'POST',
    data,
    ...config
  })
}

export function put(url, data = {}, config = {}) {
  return service({
    url,
    method: 'PUT',
    data,
    ...config
  })
}

export function del(url, params = {}, config = {}) {
  return service({
    url,
    method: 'DELETE',
    params,
    ...config
  })
}

export default service
