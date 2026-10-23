import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'
import LoginView from '@/views/LoginView.vue'
import CategoriesView from '@/views/CategoriesView.vue'
import TransactionsView from '@/views/TransactionsView.vue'
import PlaceholderView from '@/views/PlaceholderView.vue'
import NotFoundView from '@/views/NotFoundView.vue'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'dashboard',
        component: PlaceholderView,
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'transactions',
        name: 'transactions',
        component: TransactionsView,
        meta: { title: '账单', requiresAuth: true }
      },
      {
        path: 'categories',
        name: 'categories',
        component: CategoriesView,
        meta: { title: '分类管理', requiresAuth: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFoundView,
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 登录态以服务端 /auth/me 为准：首次导航先恢复会话，再决定是否放行。
router.beforeEach(async (to) => {
  const auth = useAuthStore()

  // 刷新页面时先向服务端恢复会话，避免把已登录用户误判为未登录
  if (!auth.initialized) {
    await auth.fetchCurrentUser()
  }

  const requiresAuth = Boolean(to.meta.requiresAuth)
  if (requiresAuth && !auth.isAuthenticated) {
    // 记录原始路径，登录成功后回到用户本来想访问的页面
    return {
      name: 'login',
      query: { redirect: to.fullPath }
    }
  }

  // 已登录用户访问登录页没有意义，直接回首页
  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }

  return true
})

router.afterEach((to) => {
  const title = typeof to.meta.title === 'string' ? to.meta.title : '个人记账'
  document.title = `${title} · 个人记账`
})

export default router
