import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message || '请求失败'

      if (status === 401) {
        // 检查当前路径，清除对应的token和用户信息
        const currentPath = window.location.pathname
        if (!currentPath.includes('/login') && !currentPath.includes('/admin/login')) {
          // 根据路径判断清除哪个系统的token
          if (currentPath.startsWith('/admin')) {
            // 管理员后台401，清除管理员token
            localStorage.removeItem('adminToken')
            localStorage.removeItem('adminUser')
            window.confirm('管理员登录已过期，请重新登录')
            window.location.href = '/admin/login'
          } else {
            // 用户端401，清除用户token
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            window.confirm('登录已过期，请重新登录')
            window.location.href = '/login'
          }
        }
      }

      return Promise.reject(new Error(message))
    }
    return Promise.reject(new Error(error.message || '网络错误'))
  }
)

export default request