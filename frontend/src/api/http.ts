import axios, { type AxiosError, type AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types/api'

const CSRF_COOKIE_NAME = 'XSRF-TOKEN'
const CSRF_HEADER_NAME = 'X-XSRF-TOKEN'
const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS'])
const AUTH_ME_URL = '/auth/me'
const AUTH_LOGIN_URL = '/auth/login'

/**
 * 统一的 API 错误：携带后端错误码、message 与可选字段级 details。
 */
export class ApiClientError extends Error {
  readonly code: string
  readonly details?: Record<string, unknown>
  readonly status?: number

  constructor(code: string, message: string, options?: { details?: Record<string, unknown>; status?: number }) {
    super(message)
    this.name = 'ApiClientError'
    this.code = code
    this.details = options?.details
    this.status = options?.status
  }
}

/** 读取非 HttpOnly 的 CSRF Cookie；不存在时返回 null。 */
export function readCookie(name: string): string | null {
  const prefix = `${name}=`
  for (const part of document.cookie.split(';')) {
    const trimmed = part.trim()
    if (trimmed.startsWith(prefix)) {
      return decodeURIComponent(trimmed.slice(prefix.length))
    }
  }
  return null
}

export function hasCsrfToken(): boolean {
  return readCookie(CSRF_COOKIE_NAME) !== null
}

type UnauthorizedHandler = () => void
let unauthorizedHandler: UnauthorizedHandler | null = null

/** 由 main.ts 注册：401 时清理内存中的登录态并跳转登录页。 */
export function setUnauthorizedHandler(handler: UnauthorizedHandler | null): void {
  unauthorizedHandler = handler
}

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 15000,
  withCredentials: true,
  headers: {
    Accept: 'application/json'
  }
})

// CSRF token 来自非 HttpOnly 的 XSRF-TOKEN Cookie；只在同源写请求上附加。
instance.interceptors.request.use((config) => {
  const method = (config.method ?? 'get').toUpperCase()
  if (!SAFE_METHODS.has(method)) {
    const token = readCookie(CSRF_COOKIE_NAME)
    if (token) {
      config.headers.set(CSRF_HEADER_NAME, token)
    }
  }
  return config
})

/**
 * 统一请求流程：
 * 1. 成功响应必须是 {data, error} 合同结构，error 非空即视为业务失败；
 * 2. 401 在排除“登录态探测”和“登录请求”后触发全局未登录处理；
 * 3. 无响应按网络失败处理，不自动重试写请求，避免重复提交。
 */
async function request<T>(config: AxiosRequestConfig): Promise<T> {
  try {
    const response = await instance.request<ApiResponse<T>>(config)
    const body = response.data
    if (body && body.error) {
      throw new ApiClientError(body.error.code, body.error.message, {
        details: body.error.details,
        status: response.status
      })
    }
    return body.data as T
  } catch (error) {
    if (error instanceof ApiClientError) {
      throw error
    }
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<ApiResponse<unknown>>
      const status = axiosError.response?.status
      const payload = axiosError.response?.data as ApiResponse<unknown> | undefined
      const rawError = payload?.error ?? null
      // 只把真正符合合同结构（code/message 均为字符串）的错误体当成 ApiError；
      // Spring Boot 默认错误页里的 error 是字符串（如 "Forbidden"），必须排除。
      const bodyError =
        rawError &&
        typeof rawError === 'object' &&
        typeof rawError.code === 'string' &&
        typeof rawError.message === 'string'
          ? rawError
          : null
      // 登录态探测（/auth/me）与登录请求本身不需要触发全局跳转
      const isAuthBootstrap = config.url === AUTH_ME_URL && status === 401
      const isLogin = config.url === AUTH_LOGIN_URL

      if (bodyError) {
        // 后端按合同返回错误：优先使用它的 code/message/details
        if (status === 401 && !isAuthBootstrap && !isLogin) {
          unauthorizedHandler?.()
        }
        throw new ApiClientError(bodyError.code, bodyError.message, {
          details: bodyError.details,
          status
        })
      }

      if (!axiosError.response) {
        // 无 HTTP 响应：网络中断、超时或被浏览器拦截
        throw new ApiClientError('NETWORK_ERROR', '网络请求失败，请检查网络后重试')
      }

      if (status === 401) {
        if (!isAuthBootstrap && !isLogin) {
          unauthorizedHandler?.()
        }
        throw new ApiClientError('UNAUTHENTICATED', '未登录或会话已失效', { status })
      }

      // CSRF 校验失败走 Spring Security 默认 403，响应体可能不是合同结构，按状态码兜底映射
      if (status === 403) {
        throw new ApiClientError('FORBIDDEN', '无权执行该操作，或安全校验失败', { status })
      }

      // 后端返回了非合同结构（例如代理/网关错误页），只给通用提示
      throw new ApiClientError('UNKNOWN_ERROR', '请求处理失败，请稍后重试', { status })
    }
    throw new ApiClientError('UNKNOWN_ERROR', '请求处理失败，请稍后重试')
  }
}

/**
 * 唯一的 API 入口：视图与 store 不直接使用 Axios 实例。
 * 成功时返回后端的 data，失败时抛出 ApiClientError。
 */
export const http = {
  get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return request<T>({ ...config, url, method: 'GET' })
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return request<T>({ ...config, url, method: 'POST', data })
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return request<T>({ ...config, url, method: 'PUT', data })
  },
  delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return request<T>({ ...config, url, method: 'DELETE' })
  }
}

export default http
