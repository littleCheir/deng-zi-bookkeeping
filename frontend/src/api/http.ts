import axios from 'axios'

// 全局唯一的 Axios 实例。
// 阶段 0 只建立骨架，不发起任何业务请求：
// - CSRF 头（X-XSRF-TOKEN）读写
// - 统一错误处理与 401 处理
// 均按文档在阶段 1 实现，避免在组件和拦截器中重复弹出提示。
export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 15000,
  withCredentials: true,
  headers: {
    Accept: 'application/json'
  }
})

export default http
