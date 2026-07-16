<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="admin-sidebar">
      <div class="admin-logo">
        <h2 class="logo-text">StringMoment</h2>
        <p class="logo-subtitle">管理后台</p>
      </div>

      <nav class="admin-menu">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="admin-menu-item"
          :class="{ active: isActive(item.path) }"
        >
          <span class="admin-menu-icon" v-html="item.icon"></span>
          <span class="admin-menu-text">{{ item.name }}</span>
        </router-link>
      </nav>

      <div class="admin-sidebar-footer">
        <div class="admin-user-info">
          <span class="admin-user-name">{{ adminUser?.nickname || adminUser?.username || '管理员' }}</span>
        </div>
        <button @click="handleLogout" class="admin-logout-btn">
          退出登录
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'

const router = useRouter()
const route = useRoute()

const menuItems = [
  {
    name: '商品管理',
    path: '/admin/product',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/></svg>'
  },
  {
    name: '秒杀活动',
    path: '/admin/seckill',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>'
  },
  {
    name: '订单管理',
    path: '/admin/order',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"/></svg>'
  },
  {
    name: '用户管理',
    path: '/admin/user',
    icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>'
  }
]

const adminUser = computed(() => {
  const userStr = localStorage.getItem('adminUser')
  return userStr ? JSON.parse(userStr) : null
})

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/')
}

const handleLogout = () => {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminUser')
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.admin-sidebar {
  width: 240px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
}

.admin-logo {
  padding: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.admin-logo .logo-text {
  margin: 0;
  font-size: 24px;
  color: #4a90e2;
  font-weight: bold;
}

.admin-logo .logo-subtitle {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #666;
}

.admin-menu {
  flex: 1;
  padding: 16px 0;
  overflow-y: auto;
}

.admin-menu-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  color: #333;
  text-decoration: none;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.admin-menu-item:hover {
  background: #f0f7ff;
  color: #4a90e2;
}

.admin-menu-item.active {
  background: #e6f2ff;
  color: #4a90e2;
  border-left-color: #4a90e2;
}

.admin-menu-icon {
  width: 20px;
  height: 20px;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.admin-menu-icon svg {
  width: 20px;
  height: 20px;
}

.admin-menu-text {
  font-size: 14px;
  font-weight: 500;
}

.admin-sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
}

.admin-user-info {
  margin-bottom: 12px;
}

.admin-user-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.admin-logout-btn {
  width: 100%;
  padding: 10px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 6px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.admin-logout-btn:hover {
  background: #e74c3c;
  border-color: #e74c3c;
  color: white;
}

.admin-main {
  flex: 1;
  margin-left: 240px;
  padding: 24px;
  min-height: 100vh;
}
</style>