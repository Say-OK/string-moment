<template>
  <div class="user-center-container">
    <!-- 左侧菜单栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>用户中心</h2>
      </div>
      <div class="sidebar-menu">
        <div
          class="menu-item"
          :class="{ active: currentTab === 'info' }"
          @click="currentTab = 'info'"
        >
          <span class="menu-icon">👤</span>
          <span class="menu-text">用户信息</span>
        </div>
        <div
          class="menu-item"
          :class="{ active: currentTab === 'address' }"
          @click="currentTab = 'address'"
        >
          <span class="menu-icon">📍</span>
          <span class="menu-text">收货地址</span>
        </div>
      </div>
    </div>

    <!-- 右侧内容 -->
    <div class="content">
      <!-- 用户信息 -->
      <div v-if="currentTab === 'info'" class="user-info-section">
        <UserInfoTab />
      </div>

      <!-- 收货地址 -->
      <div v-if="currentTab === 'address'" class="address-section">
        <AddressTab />
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import UserInfoTab from '../components/UserInfoTab.vue'
import AddressTab from '../components/AddressTab.vue'

const currentTab = ref('info')
</script>

<style scoped>
.user-center-container {
  display: flex;
  max-width: 1200px;
  margin: 20px auto;
  gap: 20px;
  padding: 0 20px;
}

/* 左侧菜单栏 */
.sidebar {
  width: 240px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.sidebar-header {
  background: #4a90e2;
  color: #fff;
  padding: 20px;
  text-align: center;
}

.sidebar-header h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.sidebar-menu {
  padding: 20px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.menu-item:hover {
  background: #f5f5f5;
}

.menu-item.active {
  background: #e8f4ff;
  border-right: 3px solid #4a90e2;
}

.menu-icon {
  font-size: 20px;
}

.menu-text {
  font-size: 16px;
  color: #333;
}

/* 右侧内容 */
.content {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  min-height: 600px;
}

.user-info-section,
.address-section {
  padding: 24px;
}
</style>