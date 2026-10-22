import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import PlaceholderView from '@/views/PlaceholderView.vue'
import NotFoundView from '@/views/NotFoundView.vue'

// 阶段 0 只建立路由骨架：所有业务页面统一指向 PlaceholderView，
// 并明确提示“功能尚未接通”，不使用任何模拟数据。
// 后续阶段会把对应路由替换为真实页面，并加入认证守卫。
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'dashboard',
    component: PlaceholderView,
    meta: { title: '首页' }
  },
  {
    path: '/transactions',
    name: 'transactions',
    component: PlaceholderView,
    meta: { title: '账单' }
  },
  {
    path: '/categories',
    name: 'categories',
    component: PlaceholderView,
    meta: { title: '分类管理' }
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

router.afterEach((to) => {
  const title = typeof to.meta.title === 'string' ? to.meta.title : '个人记账'
  document.title = `${title} · 个人记账`
})

export default router
