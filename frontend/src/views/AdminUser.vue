<template>
  <div class="admin-page">
    <!-- 成功/错误消息 -->
    <div v-if="successMessage" :class="['admin-success-message', isError ? 'error' : '']">
      {{ successMessage }}
    </div>

    <div class="admin-page-header">
      <h2>用户管理</h2>
    </div>

    <!-- 搜索筛选区 -->
    <div class="admin-filter-bar">
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="搜索用户名/昵称/手机号"
        class="admin-input"
        @keyup.enter="handleSearch"
      />
      <select v-model="filterStatus" class="admin-select">
        <option :value="null">全部状态</option>
        <option :value="1">正常</option>
        <option :value="0">禁用</option>
      </select>
      <select v-model="filterRole" class="admin-select">
        <option :value="null">全部角色</option>
        <option :value="1">管理员</option>
        <option :value="0">普通用户</option>
      </select>
      <button @click="handleSearch" class="admin-btn-secondary">搜索</button>
      <button @click="resetFilters" class="admin-btn-default">重置</button>
    </div>

    <!-- 用户列表 -->
    <div class="admin-table-container">
      <table class="admin-table">
        <thead>
          <tr>
            <th>用户ID</th>
            <th>头像</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>手机号</th>
            <th>角色</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="9" class="admin-loading">加载中...</td>
          </tr>
          <tr v-else-if="userList.length === 0">
            <td colspan="9" class="admin-empty">暂无数据</td>
          </tr>
          <tr v-else v-for="user in userList" :key="user.id">
            <td>{{ user.id }}</td>
            <td>
              <img
                v-if="user.avatar"
                :src="user.avatar"
                :alt="user.nickname || user.username"
                class="admin-user-avatar"
                @error="handleImgError"
              />
              <span v-else class="admin-no-avatar">无头像</span>
            </td>
            <td>{{ user.username }}</td>
            <td>{{ user.nickname || '-' }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>
              <span :class="['admin-status-tag', user.role === 1 ? 'role-admin' : 'role-user']">
                {{ user.role === 1 ? '管理员' : '普通用户' }}
              </span>
            </td>
            <td>
              <span :class="['admin-status-tag', user.status === 1 ? 'status-on' : 'status-off']">
                {{ user.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>{{ user.createTime }}</td>
            <td>
              <div class="admin-actions">
                <button @click="viewDetail(user)" class="admin-btn-link">详情</button>
                <button
                  v-if="user.status === 1"
                  @click="handleDisable(user)"
                  class="admin-btn-link warning"
                  :disabled="user.role === 1"
                  :class="{ 'disabled-btn': user.role === 1 }"
                >
                  禁用
                </button>
                <button
                  v-else
                  @click="handleEnable(user)"
                  class="admin-btn-link success"
                >
                  启用
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="admin-pagination" v-if="total > 0">
      <button
        :disabled="page === 1"
        @click="changePage(page - 1)"
        class="admin-page-btn"
      >
        上一页
      </button>
      <span class="admin-page-info">第 {{ page }} / {{ pages }} 页，共 {{ total }} 条</span>
      <button
        :disabled="page >= pages"
        @click="changePage(page + 1)"
        class="admin-page-btn"
      >
        下一页
      </button>
    </div>

    <!-- 用户详情模态框 -->
    <div v-if="showDetail" class="admin-modal-overlay" @click.self="showDetail = false">
      <div class="admin-modal">
        <div class="admin-modal-header">
          <h3>用户详情</h3>
          <button @click="showDetail = false" class="admin-modal-close">&times;</button>
        </div>
        <div class="admin-modal-body" v-if="detailUser">
          <div class="admin-detail-row">
            <span class="admin-detail-label">用户ID:</span>
            <span>{{ detailUser.id }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">用户名:</span>
            <span>{{ detailUser.username }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">昵称:</span>
            <span>{{ detailUser.nickname || '-' }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">手机号:</span>
            <span>{{ detailUser.phone || '-' }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">用户角色:</span>
            <span :class="['admin-status-tag', detailUser.role === 1 ? 'role-admin' : 'role-user']">
              {{ detailUser.role === 1 ? '管理员' : '普通用户' }}
            </span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">用户状态:</span>
            <span :class="['admin-status-tag', detailUser.status === 1 ? 'status-on' : 'status-off']">
              {{ detailUser.status === 1 ? '正常' : '禁用' }}
            </span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">注册时间:</span>
            <span>{{ detailUser.createTime }}</span>
          </div>
          <div class="admin-detail-row" v-if="detailUser.avatar">
            <span class="admin-detail-label">用户头像:</span>
            <img :src="detailUser.avatar" :alt="detailUser.nickname || detailUser.username" class="admin-detail-avatar" @error="handleImgError" />
          </div>
        </div>
      </div>
    </div>

    <!-- 自定义确认弹窗 -->
    <div v-if="showConfirmModal" class="admin-modal-overlay" @click.self="closeConfirmModal">
      <div class="admin-modal admin-modal-small">
        <div class="admin-modal-header">
          <h3>{{ confirmTitle }}</h3>
          <button @click="closeConfirmModal" class="admin-modal-close">&times;</button>
        </div>
        <div class="admin-modal-body">
          <p class="confirm-message">{{ confirmMessage }}</p>
          <div class="admin-modal-footer">
            <button @click="closeConfirmModal" class="admin-btn-default">取消</button>
            <button @click="handleConfirm" class="admin-btn-danger">确认</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {getAdminUserDetail, getAdminUserList, updateUserStatus} from '../api/admin'

// 列表数据
const userList = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const pages = ref(0)

// 筛选条件
const searchKeyword = ref('')
const filterStatus = ref(null)
const filterRole = ref(null)

// 用户详情
const showDetail = ref(false)
const detailUser = ref(null)

// 自定义确认弹窗相关
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmAction = ref(null)
const confirmData = ref(null)

// 成功/错误消息
const successMessage = ref('')
const isError = ref(false)

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value,
      role: filterRole.value
    }
    const res = await getAdminUserList(params)
    userList.value = res.data.list || []
    total.value = res.data.total
    pages.value = res.data.pages
  } catch (error) {
    showSuccess(error.message || '获取用户列表失败', true)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  page.value = 1
  fetchUserList()
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = null
  filterRole.value = null
  page.value = 1
  fetchUserList()
}

// 分页
const changePage = (newPage) => {
  page.value = newPage
  fetchUserList()
}

// 查看详情
const viewDetail = async (user) => {
  try {
    const res = await getAdminUserDetail(user.id)
    detailUser.value = res.data
    showDetail.value = true
  } catch (error) {
    showSuccess(error.message || '获取详情失败', true)
  }
}

// 禁用用户
const handleDisable = async (user) => {
  if (user.role === 1) {
    showSuccess('管理员不能禁用其他管理员账号', true)
    return
  }
  openConfirmModal(
    '确认禁用',
    `确定要禁用用户"${user.username}"吗？禁用后该用户将无法登录系统。`,
    async () => {
      await updateUserStatus(user.id, 0)
      showSuccess('用户已禁用')
    },
    user
  )
}

// 启用用户
const handleEnable = async (user) => {
  openConfirmModal(
    '确认启用',
    `确定要启用用户"${user.username}"吗？`,
    async () => {
      await updateUserStatus(user.id, 1)
      showSuccess('用户已启用')
    },
    user
  )
}

// 图片加载失败处理
const handleImgError = (e) => {
  e.target.style.display = 'none'
}

// 打开自定义确认弹窗
const openConfirmModal = (title, message, action, data) => {
  confirmTitle.value = title
  confirmMessage.value = message
  confirmAction.value = action
  confirmData.value = data
  showConfirmModal.value = true
}

// 关闭自定义确认弹窗
const closeConfirmModal = () => {
  showConfirmModal.value = false
  confirmAction.value = null
  confirmData.value = null
}

// 处理确认操作
const handleConfirm = async () => {
  if (!confirmAction.value) return

  try {
    await confirmAction.value(confirmData.value)
    closeConfirmModal()
    fetchUserList()
  } catch (error) {
    showSuccess(error.message || '操作失败', true)
    closeConfirmModal()
  }
}

// 显示成功消息（isError为true时显示错误样式）
const showSuccess = (message, isErrorParam = false) => {
  successMessage.value = message
  isError.value = isErrorParam
  // 1.5秒后自动消失
  setTimeout(() => {
    successMessage.value = ''
    isError.value = false
  }, 1500)
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.admin-page {
  background: white;
  border-radius: 8px;
  padding: 24px;
  min-height: calc(100vh - 48px);
}

.admin-success-message {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  padding: 12px 24px;
  background: #e8f5e9;
  color: #2e7d32;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  font-size: 14px;
  text-align: center;
  animation: fadeInOut 1.5s;
}

.admin-success-message.error {
  background: #ffebee;
  color: #c62828;
}

@keyframes fadeInOut {
  0% { opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { opacity: 0; }
}

.admin-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.admin-page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.admin-filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.admin-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  min-width: 200px;
}

.admin-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  min-width: 120px;
}

.admin-btn-secondary {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.admin-btn-default {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.admin-table-container {
  overflow-x: auto;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.admin-table th,
.admin-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.admin-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.admin-table tr:hover {
  background: #f8f9fa;
}

.admin-user-avatar {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 50%;
}

.admin-no-avatar {
  display: inline-block;
  width: 40px;
  height: 40px;
  line-height: 40px;
  text-align: center;
  color: #999;
  font-size: 12px;
  background-color: #f5f5f5;
  border-radius: 50%;
}

.admin-status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-on {
  background: #e6f7ee;
  color: #52c41a;
}

.status-off {
  background: #fff2f0;
  color: #ff4d4f;
}

.role-admin {
  background: #fff7e6;
  color: #fa8c16;
}

.role-user {
  background: #e6f7ff;
  color: #1890ff;
}

.admin-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.admin-btn-link {
  background: none;
  border: none;
  color: #4a90e2;
  cursor: pointer;
  font-size: 13px;
  padding: 0;
}

.admin-btn-link:hover {
  text-decoration: underline;
}

.admin-btn-link.warning {
  color: #fa8c16;
}

.admin-btn-link.success {
  color: #52c41a;
}

.admin-btn-link.disabled-btn {
  color: #999;
  cursor: not-allowed;
  opacity: 0.5;
}

.admin-btn-link.disabled-btn:hover {
  text-decoration: none;
}

.admin-loading,
.admin-empty {
  text-align: center;
  color: #999;
  padding: 40px !important;
}

.admin-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.admin-page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 14px;
}

.admin-page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.admin-page-info {
  color: #666;
  font-size: 14px;
}

/* 模态框样式 */
.admin-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.admin-modal {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.admin-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.admin-modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.admin-modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.admin-modal-body {
  padding: 20px;
}

/* 详情样式 */
.admin-detail-row {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}

.admin-detail-label {
  width: 80px;
  color: #666;
  flex-shrink: 0;
}

.admin-detail-avatar {
  max-width: 100px;
  border-radius: 50%;
}

/* 小尺寸模态框 */
.admin-modal-small {
  max-width: 400px;
  text-align: center;
}

.confirm-message {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
  line-height: 1.5;
}

.admin-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.admin-btn-danger {
  background: #ff4d4f;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.admin-btn-danger:hover {
  background: #d9363e;
}
</style>