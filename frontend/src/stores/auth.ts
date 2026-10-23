import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser, login as loginRequest, logout as logoutRequest } from '@/api/auth'
import type { CurrentUser, LoginPayload } from '@/types/auth'

/**
 * 登录态以服务端 /auth/me 为唯一依据，不在 localStorage 保存任何凭据。
 */
export const useAuthStore = defineStore('auth', () => {
  const user = ref<CurrentUser | null>(null)
  const initialized = ref(false)
  const checking = ref(false)

  const isAuthenticated = computed(() => user.value !== null)

  /**
   * 页面刷新或首次导航时恢复会话：/auth/me 返回 401 视为未登录，不向页面抛错。
   */
  async function fetchCurrentUser(): Promise<void> {
    if (checking.value) {
      return
    }
    checking.value = true
    try {
      user.value = await getCurrentUser()
    } catch {
      user.value = null
    } finally {
      initialized.value = true
      checking.value = false
    }
  }

  /**
   * 登录成功后直接写入内存状态，避免紧接着再请求一次 /auth/me。
   */
  async function login(payload: LoginPayload): Promise<CurrentUser> {
    const current = await loginRequest(payload)
    user.value = current
    initialized.value = true
    return current
  }

  /**
   * 无论登出接口是否成功，都清空本地登录态，避免 UI 停留在“已登录”。
   */
  async function logout(): Promise<void> {
    try {
      await logoutRequest()
    } finally {
      user.value = null
      initialized.value = true
    }
  }

  /**
   * 仅供全局 401 回调使用：清理内存状态，不发起任何请求。
   */
  function clear(): void {
    user.value = null
    initialized.value = true
  }

  return {
    user,
    initialized,
    checking,
    isAuthenticated,
    fetchCurrentUser,
    login,
    logout,
    clear
  }
})
