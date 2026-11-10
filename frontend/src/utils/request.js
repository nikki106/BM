import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const saved = JSON.parse(localStorage.getItem('bm_user') || 'null')
  if (saved?.token) config.headers.Authorization = `Bearer ${saved.token}`
  return config
})

request.interceptors.response.use(
  (res) => {
    const { code, msg, data } = res.data
    if (code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      // 同时清掉 localStorage 和 pinia 里的登录状态,
      // 否则路由守卫会把 /login 弹回 /dashboard, 造成两个页面无限跳转
      useUserStore().logout()
      window.location.hash = '#/login'
      return Promise.reject(new Error(msg))
    }
    if (code !== 200) {
      ElMessage.error(msg || '请求失败')
      return Promise.reject(new Error(msg))
    }
    return data
  },
  (err) => {
    ElMessage.error(err.response?.data?.msg || '网络异常，请稍后重试')
    return Promise.reject(err)
  }
)

export default request
