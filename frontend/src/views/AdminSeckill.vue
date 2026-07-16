<template>
  <div class="admin-page">
    <!-- 成功/错误消息 -->
    <div v-if="successMessage" :class="['admin-success-message', isError ? 'error' : '']">
      {{ successMessage }}
    </div>

    <div class="admin-page-header">
      <h2>秒杀活动管理</h2>
      <button @click="openAddModal" class="admin-btn-primary">添加活动</button>
    </div>

    <!-- 搜索筛选区 -->
    <div class="admin-filter-bar">
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="搜索活动名称"
        class="admin-input"
        @keyup.enter="handleSearch"
      />
      <select v-model="filterStatus" class="admin-select">
        <option :value="null">全部状态</option>
        <option :value="0">未开始</option>
        <option :value="1">进行中</option>
        <option :value="2">已结束</option>
      </select>
      <button @click="handleSearch" class="admin-btn-secondary">搜索</button>
      <button @click="resetFilters" class="admin-btn-default">重置</button>
    </div>

    <!-- 秒杀活动列表 -->
    <div class="admin-table-container">
      <table class="admin-table">
        <thead>
          <tr>
            <th>活动ID</th>
            <th>活动名称</th>
            <th>商品图片</th>
            <th>商品名称</th>
            <th>原价</th>
            <th>秒杀价</th>
            <th>库存</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="11" class="admin-loading">加载中...</td>
          </tr>
          <tr v-else-if="activityList.length === 0">
            <td colspan="11" class="admin-empty">暂无数据</td>
          </tr>
          <tr v-else v-for="activity in activityList" :key="activity.id">
            <td>{{ activity.id }}</td>
            <td class="admin-text-ellipsis" :title="activity.name">{{ activity.name }}</td>
            <td>
              <img
                v-if="activity.productImage"
                :src="activity.productImage"
                :alt="activity.productName"
                class="admin-product-img"
                @error="handleImgError"
              />
              <span v-else class="admin-no-img">无图片</span>
            </td>
            <td>{{ activity.productName }}</td>
            <td>¥{{ activity.originalPrice }}</td>
            <td class="admin-price">¥{{ activity.seckillPrice }}</td>
            <td>{{ activity.stockPercent }}%</td>
            <td>{{ activity.startTime }}</td>
            <td>{{ activity.endTime }}</td>
            <td>
              <span :class="['admin-status-tag', getStatusClass(activity.status)]">
                {{ getStatusText(activity.status) }}
              </span>
            </td>
            <td>
              <div class="admin-actions">
                <button @click="viewDetail(activity)" class="admin-btn-link">详情</button>
                <button
                  @click="openEditModal(activity)"
                  class="admin-btn-link"
                  :disabled="activity.status === 1"
                  :class="{ 'disabled-btn': activity.status === 1 }"
                >
                  编辑
                </button>
                <button
                  v-if="activity.status === 1"
                  @click="handleStopActivity(activity)"
                  class="admin-btn-link stop-btn"
                >
                  停止
                </button>
                <button @click="handleDelete(activity)" class="admin-btn-link danger">删除</button>
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

    <!-- 添加/编辑秒杀活动模态框 -->
    <div v-if="showModal" class="admin-modal-overlay" @click.self="closeModal">
      <div class="admin-modal">
        <div class="admin-modal-header">
          <h3>{{ isEdit ? '编辑秒杀活动' : '添加秒杀活动' }}</h3>
          <button @click="closeModal" class="admin-modal-close">&times;</button>
        </div>
        <form @submit.prevent="handleSubmit" class="admin-modal-body">
          <div class="admin-form-item">
            <label>活动名称 <span class="required">*</span></label>
            <input v-model="formData.name" type="text" placeholder="请输入活动名称" />
          </div>
          <div class="admin-form-item" v-if="!isEdit">
            <label>选择商品 <span class="required">*</span></label>
            <select v-model="formData.productId" class="admin-select-full">
              <option :value="null">请选择商品</option>
              <option v-for="product in productList" :key="product.id" :value="product.id">
                {{ product.name }}（¥{{ product.price }}，库存: {{ product.stock }}）
              </option>
            </select>
          </div>
          <div class="admin-form-row">
            <div class="admin-form-item">
              <label>秒杀价格 <span class="required">*</span></label>
              <input v-model.number="formData.seckillPrice" type="number" step="0.01" placeholder="请输入秒杀价格" />
            </div>
            <div class="admin-form-item">
              <label>秒杀库存 <span class="required">*</span></label>
              <input v-model.number="formData.totalStock" type="number" placeholder="请输入秒杀库存" />
            </div>
          </div>
          <div class="admin-form-row">
            <div class="admin-form-item">
              <label>开始时间 <span class="required">*</span></label>
              <input v-model="formData.startTime" type="datetime-local" />
            </div>
            <div class="admin-form-item">
              <label>结束时间 <span class="required">*</span></label>
              <input v-model="formData.endTime" type="datetime-local" />
            </div>
          </div>
          <div v-if="modalError" class="admin-modal-error">{{ modalError }}</div>
          <div class="admin-modal-footer">
            <button type="button" @click="closeModal" class="admin-btn-default">取消</button>
            <button type="submit" :disabled="submitting" class="admin-btn-primary">
              {{ submitting ? '提交中...' : '确定' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 秒杀活动详情模态框 -->
    <div v-if="showDetail" class="admin-modal-overlay" @click.self="showDetail = false">
      <div class="admin-modal">
        <div class="admin-modal-header">
          <h3>秒杀活动详情</h3>
          <button @click="showDetail = false" class="admin-modal-close">&times;</button>
        </div>
        <div class="admin-modal-body" v-if="detailActivity">
          <div class="admin-detail-row">
            <span class="admin-detail-label">活动ID:</span>
            <span>{{ detailActivity.id }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">活动名称:</span>
            <span>{{ detailActivity.name }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">商品名称:</span>
            <span>{{ detailActivity.productName }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">原价:</span>
            <span>¥{{ detailActivity.originalPrice }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">秒杀价:</span>
            <span class="admin-price">¥{{ detailActivity.seckillPrice }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">总库存:</span>
            <span>{{ detailActivity.totalStock }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">剩余库存:</span>
            <span>{{ detailActivity.availableStock }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">开始时间:</span>
            <span>{{ detailActivity.startTime }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">结束时间:</span>
            <span>{{ detailActivity.endTime }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">活动状态:</span>
            <span :class="['admin-status-tag', getStatusClass(detailActivity.status)]">
              {{ getStatusText(detailActivity.status) }}
            </span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">创建时间:</span>
            <span>{{ detailActivity.createTime }}</span>
          </div>
          <div class="admin-detail-row" v-if="detailActivity.productImage">
            <span class="admin-detail-label">商品图片:</span>
            <img :src="detailActivity.productImage" :alt="detailActivity.productName" class="admin-detail-img" @error="handleImgError" />
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
import {onMounted, reactive, ref} from 'vue'
import {
  addSeckillActivity,
  deleteSeckillActivity,
  getAdminProductList,
  getAdminSeckillDetail,
  getAdminSeckillList,
  stopSeckillActivity,
  updateSeckillActivity
} from '../api/admin'

// 列表数据
const activityList = ref([])
const productList = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const pages = ref(0)

// 筛选条件
const searchKeyword = ref('')
const filterStatus = ref(null)

// 模态框
const showModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const modalError = ref('')
const editActivityId = ref(null)

// 详情
const showDetail = ref(false)
const detailActivity = ref(null)

// 自定义确认弹窗相关
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmAction = ref(null)
const confirmData = ref(null)

// 成功/错误消息
const successMessage = ref('')
const isError = ref(false)

// 表单数据
const formData = reactive({
  name: '',
  productId: null,
  seckillPrice: null,
  totalStock: null,
  startTime: '',
  endTime: ''
})

// 获取秒杀活动列表
const fetchActivityList = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value
    }
    const res = await getAdminSeckillList(params)
    activityList.value = res.data.list || []
    total.value = res.data.total
    pages.value = res.data.pages
  } catch (error) {
    showSuccess(error.message || '获取秒杀活动列表失败', true)
  } finally {
    loading.value = false
  }
}

// 获取商品列表（用于添加活动时选择商品）
const fetchProductList = async () => {
  try {
    const res = await getAdminProductList({ page: 1, size: 100 })
    productList.value = res.data.list || []
  } catch (error) {
    console.error('获取商品列表失败', error)
  }
}

// 搜索
const handleSearch = () => {
  page.value = 1
  fetchActivityList()
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = null
  page.value = 1
  fetchActivityList()
}

// 分页
const changePage = (newPage) => {
  page.value = newPage
  fetchActivityList()
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '未开始'
    case 1: return '进行中'
    case 2: return '已结束'
    default: return '未知'
  }
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-active'
    case 2: return 'status-ended'
    default: return ''
  }
}

// 打开添加模态框
const openAddModal = () => {
  isEdit.value = false
  editActivityId.value = null
  modalError.value = ''
  Object.assign(formData, {
    name: '',
    productId: null,
    seckillPrice: null,
    totalStock: null,
    startTime: '',
    endTime: ''
  })
  fetchProductList()
  showModal.value = true
}

// 打开编辑模态框
const openEditModal = async (activity) => {
  isEdit.value = true
  editActivityId.value = activity.id
  modalError.value = ''

  // 获取详情以填充表单
  try {
    const res = await getAdminSeckillDetail(activity.id)
    const detail = res.data
    Object.assign(formData, {
      name: detail.name,
      productId: detail.productId,
      seckillPrice: detail.seckillPrice,
      totalStock: detail.totalStock,
      startTime: detail.startTime ? formatDateTimeLocal(detail.startTime) : '',
      endTime: detail.endTime ? formatDateTimeLocal(detail.endTime) : ''
    })
    showModal.value = true
  } catch (error) {
    showSuccess(error.message || '获取详情失败', true)
  }
}

// 格式化日期时间为 datetime-local 格式
const formatDateTimeLocal = (dateStr) => {
  // 将 yyyy-MM-dd HH:mm:ss 转为 yyyy-MM-ddTHH:mm
  return dateStr.replace(' ', 'T').slice(0, 16)
}

// 关闭模态框
const closeModal = () => {
  showModal.value = false
  modalError.value = ''
}

// 验证表单
const validateForm = () => {
  if (!formData.name?.trim()) {
    modalError.value = '请输入活动名称'
    return false
  }
  if (!isEdit.value && !formData.productId) {
    modalError.value = '请选择商品'
    return false
  }
  if (formData.seckillPrice === null || formData.seckillPrice <= 0) {
    modalError.value = '请输入有效的秒杀价格'
    return false
  }
  if (formData.totalStock === null || formData.totalStock < 1) {
    modalError.value = '秒杀库存至少为1'
    return false
  }
  if (!formData.startTime) {
    modalError.value = '请选择开始时间'
    return false
  }
  if (!formData.endTime) {
    modalError.value = '请选择结束时间'
    return false
  }
  if (new Date(formData.startTime) >= new Date(formData.endTime)) {
    modalError.value = '结束时间必须大于开始时间'
    return false
  }
  return true
}

// 提交表单
const handleSubmit = async () => {
  if (!validateForm()) return

  submitting.value = true
  modalError.value = ''

  try {
    const data = {
      name: formData.name.trim(),
      productId: formData.productId,
      seckillPrice: formData.seckillPrice,
      totalStock: formData.totalStock,
      startTime: formData.startTime,
      endTime: formData.endTime
    }

    if (isEdit.value) {
      await updateSeckillActivity(editActivityId.value, data)
      showSuccess('秒杀活动更新成功')
    } else {
      await addSeckillActivity(data)
      showSuccess('秒杀活动创建成功')
    }

    closeModal()
    fetchActivityList()
  } catch (error) {
    modalError.value = error.message || '操作失败'
  } finally {
    submitting.value = false
  }
}

// 查看详情
const viewDetail = async (activity) => {
  try {
    const res = await getAdminSeckillDetail(activity.id)
    detailActivity.value = res.data
    showDetail.value = true
  } catch (error) {
    showSuccess(error.message || '获取详情失败', true)
  }
}

// 删除秒杀活动
const handleDelete = async (activity) => {
  openConfirmModal(
    '确认删除',
    `确定要删除秒杀活动"${activity.name}"吗？`,
    async () => {
      try {
        await deleteSeckillActivity(activity.id)
        showSuccess('秒杀活动删除成功')
      } catch (error) {
        // 显示具体拒绝原因
        showSuccess(error.message || '删除失败', true)
      }
    },
    activity
  )
}

// 停止秒杀活动
const handleStopActivity = async (activity) => {
  openConfirmModal(
    '确认停止',
    `确定要停止秒杀活动"${activity.name}"吗？`,
    async () => {
      try {
        await stopSeckillActivity(activity.id)
        showSuccess('秒杀活动已停止')
      } catch (error) {
        showSuccess(error.message || '停止失败', true)
      }
    },
    activity
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
    fetchActivityList()
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
  fetchActivityList()
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

.admin-btn-primary {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.admin-btn-primary:hover:not(:disabled) {
  background: #357abd;
}

.admin-btn-primary:disabled {
  background: #b0c4de;
  cursor: not-allowed;
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

.admin-product-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.admin-no-img {
  display: inline-block;
  width: 60px;
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #999;
  font-size: 12px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.admin-text-ellipsis {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-price {
  color: #ff4d4f;
  font-weight: 600;
}

.admin-status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-active {
  background: #e6f7ee;
  color: #52c41a;
}

.status-ended {
  background: #f5f5f5;
  color: #999;
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

.admin-btn-link.danger {
  color: #ff4d4f;
}

.admin-btn-link.disabled-btn {
  color: #999;
  cursor: not-allowed;
  opacity: 0.5;
}

.admin-btn-link.disabled-btn:hover {
  text-decoration: none;
}

.admin-btn-link.stop-btn {
  color: white;
  background: #ff4d4f;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.admin-btn-link.stop-btn:hover {
  background: #d9363e;
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

.admin-form-row {
  display: flex;
  gap: 16px;
}

.admin-form-item {
  margin-bottom: 16px;
  flex: 1;
}

.admin-form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}

.admin-form-item .required {
  color: #ff4d4f;
}

.admin-form-item input,
.admin-form-item select,
.admin-form-item textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.admin-select-full {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background: white;
}

.admin-form-item input:focus,
.admin-form-item select:focus,
.admin-form-item textarea:focus {
  outline: none;
  border-color: #4a90e2;
}

.admin-modal-error {
  color: #ff4d4f;
  font-size: 14px;
  margin-bottom: 16px;
}

.admin-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
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

.admin-detail-img {
  max-width: 200px;
  border-radius: 4px;
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