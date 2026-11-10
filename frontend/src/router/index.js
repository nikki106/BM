import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue') },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'books', component: () => import('@/views/Book.vue'), meta: { title: '图书管理' } },
      { path: 'borrows', component: () => import('@/views/Borrow.vue'), meta: { title: '借阅管理' } },
      { path: 'readers', component: () => import('@/views/Reader.vue'), meta: { title: '读者管理' } },
      { path: 'notices', component: () => import('@/views/Notice.vue'), meta: { title: '公告管理' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const user = useUserStore()
  if (to.path !== '/login' && !user.token) return '/login'
  if (to.path === '/login' && user.token) return '/dashboard'
  if (user.role === 'READER' && ['/books', '/readers', '/notices'].includes(to.path)) return '/dashboard'
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 图书管理系统` : '图书管理系统'
})

export default router
