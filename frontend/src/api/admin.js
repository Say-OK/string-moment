import axios from 'axios'

// 创建管理员专用的请求实例
const adminRequest = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 使用管理员token
adminRequest.interceptors.request.use(
  config => {
    const adminToken = localStorage.getItem('adminToken')
    if (adminToken) {
      config.headers['Authorization'] = `Bearer ${adminToken}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
adminRequest.interceptors.response.use(
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
        // 检查当前路径是否已经在登录页，避免重复跳转
        const currentPath = window.location.pathname
        if (!currentPath.includes('/admin/login')) {
          // 管理员后台401，清除管理员token
          localStorage.removeItem('adminToken')
          localStorage.removeItem('adminUser')
          window.confirm('管理员登录已过期，请重新登录')
          window.location.href = '/admin/login'
        }
      }

      return Promise.reject(new Error(message))
    }
    return Promise.reject(new Error(error.message || '网络错误'))
  }
)

// ==================== 管理员登录 ====================

// 管理员登录
export function adminLogin(data) {
  return adminRequest.post('/admin/login', data)
}

// ==================== 商品管理 ====================

// 获取商品列表（管理员）
export function getAdminProductList(params) {
  return adminRequest.get('/admin/product/list', { params })
}

// 获取商品详情（管理员）
export function getAdminProductDetail(id) {
  return adminRequest.get(`/admin/product/detail/${id}`)
}

// 获取商品分类列表
export function getAdminCategories() {
  return adminRequest.get('/admin/product/categories')
}

// 添加商品
export function addProduct(data) {
  return adminRequest.post('/admin/product/add', data)
}

// 更新商品信息
export function updateProduct(id, data) {
  return adminRequest.put(`/admin/product/update/${id}`, data)
}

// 删除商品
export function deleteProduct(id) {
  return adminRequest.delete(`/admin/product/delete/${id}`)
}

// 商品上架
export function onProduct(id) {
  return adminRequest.put(`/admin/product/on/${id}`)
}

// 商品下架
export function offProduct(id) {
  return adminRequest.put(`/admin/product/off/${id}`)
}

// ==================== 秒杀活动管理 ====================

// 获取秒杀活动列表（管理员）
export function getAdminSeckillList(params) {
  return adminRequest.get('/admin/seckill/list', { params })
}

// 获取秒杀活动详情（管理员）
export function getAdminSeckillDetail(id) {
  return adminRequest.get(`/admin/seckill/detail/${id}`)
}

// 添加秒杀活动
export function addSeckillActivity(data) {
  return adminRequest.post('/admin/seckill/add', data)
}

// 更新秒杀活动
export function updateSeckillActivity(id, data) {
  return adminRequest.put(`/admin/seckill/update/${id}`, data)
}

// 删除秒杀活动
export function deleteSeckillActivity(id) {
  return adminRequest.delete(`/admin/seckill/delete/${id}`)
}

// 停止秒杀活动
export function stopSeckillActivity(id) {
  return adminRequest.put(`/admin/seckill/stop/${id}`)
}

// ==================== 订单管理 ====================

// 获取订单列表（管理员）
export function getAdminOrderList(params) {
  return adminRequest.get('/admin/order/list', { params })
}

// 获取订单详情（管理员）
export function getAdminOrderDetail(id) {
  return adminRequest.get(`/admin/order/detail/${id}`)
}

// 订单发货
export function shipOrder(id) {
  return adminRequest.put(`/admin/order/ship/${id}`)
}

// ==================== 用户管理 ====================

// 获取用户列表（管理员）
export function getAdminUserList(params) {
  return adminRequest.get('/admin/user/list', { params })
}

// 获取用户详情（管理员）
export function getAdminUserDetail(id) {
  return adminRequest.get(`/admin/user/detail/${id}`)
}

// 禁用/启用用户
export function updateUserStatus(id, status) {
  return adminRequest.put(`/admin/user/status/${id}`, null, { params: { status } })
}

// ==================== 文件上传 ====================

// 上传商品图片（管理员）
export function uploadProductImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return adminRequest.post('/file/upload/product', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传秒杀活动图片（管理员）
export function uploadSeckillImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return adminRequest.post('/file/upload/seckill', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export default adminRequest