import {createRouter, createWebHistory} from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import UserCenter from '../views/UserCenter.vue'
import ProductList from '../views/ProductList.vue'
import ProductDetail from '../views/ProductDetail.vue'
import CreateOrder from '../views/CreateOrder.vue'
import OrderList from '../views/OrderList.vue'
import OrderDetail from '../views/OrderDetail.vue'
import SeckillList from '../views/SeckillList.vue'
import SeckillDetail from '../views/SeckillDetail.vue'
// 管理员后台页面
import AdminLogin from '../views/AdminLogin.vue'
import AdminLayout from '../views/AdminLayout.vue'
import AdminProduct from '../views/AdminProduct.vue'
import AdminSeckill from '../views/AdminSeckill.vue'
import AdminOrder from '../views/AdminOrder.vue'
import AdminUser from '../views/AdminUser.vue'

const routes = [
  // ==================== 用户端路由 ====================
  {
    path: '/',
    redirect: '/products'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false, title: '用户登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false, title: '用户注册' }
  },
  {
    path: '/products',
    name: 'ProductList',
    component: ProductList,
    meta: { requiresAuth: true, requiresUser: true, title: '商品首页' }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: ProductDetail,
    meta: { requiresAuth: true, requiresUser: true, title: '商品详情' }
  },
  {
    path: '/seckill',
    name: 'SeckillList',
    component: SeckillList,
    meta: { requiresAuth: true, requiresUser: true, title: '秒杀活动' }
  },
  {
    path: '/seckill/:id',
    name: 'SeckillDetail',
    component: SeckillDetail,
    meta: { requiresAuth: true, requiresUser: true, title: '秒杀详情' }
  },
  {
    path: '/create-order/:productId',
    name: 'CreateOrder',
    component: CreateOrder,
    meta: { requiresAuth: true, requiresUser: true, title: '创建订单' }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: OrderList,
    meta: { requiresAuth: true, requiresUser: true, title: '我的订单' }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: OrderDetail,
    meta: { requiresAuth: true, requiresUser: true, title: '订单详情' }
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: UserCenter,
    meta: { requiresAuth: true, requiresUser: true, title: '用户中心' }
  },

  // ==================== 管理员后台路由 ====================
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: AdminLogin,
    meta: { requiresAuth: false, requiresAdmin: false, title: '管理员登录' }
  },
  {
    path: '/admin',
    redirect: '/admin/product'
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true, title: '管理员后台' },
    children: [
      {
        path: 'product',
        name: 'AdminProduct',
        component: AdminProduct,
        meta: { requiresAuth: true, requiresAdmin: true, title: '商品管理' }
      },
      {
        path: 'seckill',
        name: 'AdminSeckill',
        component: AdminSeckill,
        meta: { requiresAuth: true, requiresAdmin: true, title: '秒杀活动管理' }
      },
      {
        path: 'order',
        name: 'AdminOrder',
        component: AdminOrder,
        meta: { requiresAuth: true, requiresAdmin: true, title: '订单管理' }
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: AdminUser,
        meta: { requiresAuth: true, requiresAdmin: true, title: '用户管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const adminToken = localStorage.getItem('adminToken')

  // 管理员后台路由守卫
  if (to.meta.requiresAdmin) {
    // 需要管理员权限，只检查adminToken，不干涉用户token
    if (!adminToken) {
      next('/admin/login')
      return
    }
    next()
    return
  }

  // 管理员登录页：如果已有管理员Token，跳转到管理后台
  if (to.path === '/admin/login' && adminToken) {
    next('/admin/product')
    return
  }

  // 用户端路由守卫
  if (to.meta.requiresUser) {
    // 需要用户权限，只检查token，不干涉管理员token
    if (!token) {
      next('/login')
      return
    }
    next()
    return
  }

  // 登录/注册页：如果已有用户Token，跳转到商品列表
  if ((to.path === '/login' || to.path === '/register') && token) {
    next('/products')
    return
  }

  next()
})

// 添加全局后置钩子，动态设置页面标题
router.afterEach((to) => {
  if (to.meta && to.meta.title) {
    document.title = `${to.meta.title} - StringMoment`
  } else {
    document.title = 'StringMoment'
  }
})

export default router