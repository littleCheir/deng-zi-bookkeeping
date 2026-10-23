import { hasCsrfToken, http } from './http'
import type { CsrfInfo, CurrentUser, LoginPayload } from '@/types/auth'

/** 触发后端下发 XSRF-TOKEN Cookie。 */
export function getCsrf(): Promise<CsrfInfo> {
  return http.get<CsrfInfo>('/auth/csrf')
}

/**
 * 写请求前确保 CSRF Cookie 存在；不存在时先请求引导接口。
 * 不自动重试写请求：失败后由用户重新提交。
 */
export async function ensureCsrfToken(): Promise<void> {
  if (!hasCsrfToken()) {
    await getCsrf()
  }
}

export function login(payload: LoginPayload): Promise<CurrentUser> {
  return http.post<CurrentUser>('/auth/login', payload)
}

/** 登出请求体统一为空 JSON `{}`，并携带 CSRF 头。 */
export function logout(): Promise<void> {
  return http.post<void>('/auth/logout', {})
}

export function getCurrentUser(): Promise<CurrentUser> {
  return http.get<CurrentUser>('/auth/me')
}
