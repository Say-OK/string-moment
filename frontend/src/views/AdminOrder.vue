<template>
  <div class="admin-page">
    <div class="admin-page-header">
      <h2>订单管理</h2>
    </div>

    <!-- 搜索筛选区 -->
    <div class="admin-filter-bar">
      <input
        v-model="searchOrderNo"
        type="text"
        placeholder="搜索订单号"
        class="admin-input"
        @keyup.enter="handleSearch"
      />
      <select v-model="filterOrderType" class="admin-select">
        <option :value="null">全部类型</option>
        <option :value="1">普通订单</option>
        <option :value="2">秒杀订单</option>
      </select>
      <select v-model="filterStatus" class="admin-select">
        <option :value="null">全部状态</option>
        <option :value="0">待支付</option>
        <option :value="1">已支付</option>
        <option :value="2">已发货</option>
        <option :value="3">已完成</option>
        <option :value="4">已取消</option>
      </select>
      <button @click="handleSearch" class="admin-btn-secondary">搜索</button>
      <button @click="resetFilters" class="admin-btn-default">重置</button>
    </div>

    <!-- 订单列表 -->
    <div class="admin-table-container">
      <table class="admin-table">
        <thead>
          <tr>
            <th>订单ID</th>
            <th>订单号</th>
            <th>商品图片</th>
            <th>商品名称</th>
            <th>商品数量</th>
            <th>订单金额</th>
            <th>订单类型</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="10" class="admin-loading">加载中...</td>
          </tr>
          <tr v-else-if="orderList.length === 0">
            <td colspan="10" class="admin-empty">暂无数据</td>
          </tr>
          <tr v-else v-for="order in orderList" :key="order.id">
            <td>{{ order.id }}</td>
            <td>{{ order.orderNo }}</td>
            <td>
              <img
                v-if="order.firstProductImage"
                :src="order.firstProductImage"
                :alt="order.firstProductName"
                class="admin-product-img"
                @error="handleImgError"
              />
              <span v-else class="admin-no-img">无图片</span>
            </td>
            <td class="admin-text-ellipsis" :title="order.firstProductName">
              {{ order.firstProductName }}
            </td>
            <td>{{ order.productCount }}件</td>
            <td class="admin-price">¥{{ order.totalAmount }}</td>
            <td>
              <span :class="['admin-type-tag', order.orderType === 2 ? 'type-seckill' : 'type-normal']">
                {{ order.orderType === 2 ? '秒杀' : '普通' }}
              </span>
            </td>
            <td>
              <span :class="['admin-status-tag', getStatusClass(order.status)]">
                {{ getStatusText(order.status) }}
              </span>
            </td>
            <td>{{ order.createTime }}</td>
            <td>
              <div class="admin-actions">
                <button @click="viewDetail(order)" class="admin-btn-link">详情</button>
                <button
                  v-if="order.status === 1"
                  @click="handleShip(order)"
                  class="admin-btn-link success"
                >
                  发货
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

    <!-- 订单详情模态框 -->
    <div v-if="showDetail" class="admin-modal-overlay" @click.self="showDetail = false">
      <div class="admin-modal admin-modal-large">
        <div class="admin-modal-header">
          <h3>订单详情</h3>
          <button @click="showDetail = false" class="admin-modal-close">&times;</button>
        </div>
        <div class="admin-modal-body" v-if="detailOrder">
          <!-- 基本信息 -->
          <div class="admin-detail-section">
            <h4>基本信息</h4>
            <div class="admin-detail-row">
              <span class="admin-detail-label">订单ID:</span>
              <span>{{ detailOrder.id }}</span>
            </div>
            <div class="admin-detail-row">
              <span class="admin-detail-label">订单号:</span>
              <span>{{ detailOrder.orderNo }}</span>
            </div>
            <div class="admin-detail-row">
              <span class="admin-detail-label">用户ID:</span>
              <span>{{ detailOrder.userId }}</span>
            </div>
            <div class="admin-detail-row">
              <span class="admin-detail-label">订单类型:</span>
              <span :class="['admin-type-tag', detailOrder.orderType === 2 ? 'type-seckill' : 'type-normal']">
                {{ detailOrder.orderType === 2 ? '秒杀订单' : '普通订单' }}
              </span>
            </div>
            <div class="admin-detail-row" v-if="detailOrder.seckillActivityId">
              <span class="admin-detail-label">秒杀活动ID:</span>
              <span>{{ detailOrder.seckillActivityId }}</span>
            </div>
          </div>

          <!-- 金额信息 -->
          <div class="admin-detail-section">
            <h4>金额信息</h4>
            <div class="admin-detail-row">
              <span class="admin-detail-label">订单总额:</span>
              <span class="admin-price">¥{{ detailOrder.totalAmount }}</span>
            </div>
            <div class="admin-detail-row">
              <span class="admin-detail-label">实付金额:</span>
              <span class="admin-price">¥{{ detailOrder.payAmount }}</span>
            </div>
          </div>

          <!-- 状态信息 -->
          <div class="admin-detail-section">
            <h4>状态信息</h4>
            <div class="admin-detail-row">
              <span class="admin-detail-label">订单状态:</span>
              <span :class="['admin-status-tag', getStatusClass(detailOrder.status)]">
                {{ getStatusText(detailOrder.status) }}
              </span>
            </div>
            <div class="admin-detail-row">
              <span class="admin-detail-label">创建时间:</span>
              <span>{{ detailOrder.createTime }}</span>
            </div>
            <div class="admin-detail-row" v-if="detailOrder.paymentTime">
              <span class="admin-detail-label">支付时间:</span>
              <span>{{ detailOrder.paymentTime }}</span>
            </div>
            <div class="admin-detail-row" v-if="detailOrder.deliveryTime">
              <span class="admin-detail-label">发货时间:</span>
              <span>{{ detailOrder.deliveryTime }}</span>
            </div>
            <div class="admin-detail-row" v-if="detailOrder.receiveTime">
              <span class="admin-detail-label">收货时间:</span>
              <span>{{ detailOrder.receiveTime }}</span>
            </div>
            <div class="admin-detail-row" v-if="detailOrder.closeTime">
              <span class="admin-detail-label">关闭时间:</span>
              <span>{{ detailOrder.closeTime }}</span>
            </div>
          </div>

          <!-- 收货地址 -->
          <div class="admin-detail-section">
            <h4>收货地址</h4>
            <div class="admin-detail-row">
              <span class="admin-detail-label">收货人:</span>
              <span>{{ detailOrder.receiverName }}</span>
            </div>
            <div class="admin-detail-row">
              <span class="admin-detail-label">联系电话:</span>
              <span>{{ detailOrder.receiverPhone }}</span>
            </div>
            <div class="admin-detail-row">
              <span class="admin-detail-label">收货地址:</span>
              <span>
                {{ detailOrder.receiverProvince }}
                {{ detailOrder.receiverCity }}
                {{ detailOrder.receiverDistrict }}
                {{ detailOrder.receiverDetailAddress }}
              </span>
            </div>
          </div>

          <!-- 商品列表 -->
          <div class="admin-detail-section" v-if="detailOrder.items && detailOrder.items.length > 0">
            <h4>商品列表</h4>
            <div class="admin-order-items">
              <div v-for="item in detailOrder.items" :key="item.id" class="admin-order-item">
                <img
                  v-if="item.productImage"
                  :src="item.productImage"
                  :alt="item.productName"
                  class="admin-item-img"
                  @error="handleImgError"
                />
                <div class="admin-item-info">
                  <div class="admin-item-name">{{ item.productName }}</div>
                  <div class="admin-item-price">¥{{ item.unitPrice }} x {{ item.quantity }}</div>
                </div>
                <div class="admin-item-total">¥{{ item.totalPrice }}</div>
              </div>
            </div>
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
            <button @click="handleConfirm" class="admin-btn-confirm">确认</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作成功弹窗 -->
    <div v-if="showSuccessModal" class="admin-modal-overlay" @click="closeSuccessModal">
      <div class="admin-modal admin-modal-small success-modal">
        <div class="admin-modal-header">
          <h3 class="success-title">{{ successTitle }}</h3>
          <button @click="closeSuccessModal" class="admin-modal-close">&times;</button>
        </div>
        <div class="admin-modal-body">
          <p class="success-message">{{ successMessage }}</p>
          <div class="admin-modal-footer">
            <button @click="closeSuccessModal" class="admin-btn-confirm">确定</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {getAdminOrderDetail, getAdminOrderList, shipOrder} from '../api/admin'

// 列表数据
const orderList = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const pages = ref(0)

// 筛选条件
const searchOrderNo = ref('')
const filterOrderType = ref(null)
const filterStatus = ref(null)

// 详情
const showDetail = ref(false)
const detailOrder = ref(null)

// 自定义确认弹窗相关
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmAction = ref(null)
const confirmData = ref(null)

// 成功弹窗相关
const showSuccessModal = ref(false)
const successTitle = ref('')
const successMessage = ref('')

// 获取订单列表
const fetchOrderList = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      orderNo: searchOrderNo.value || undefined,
      orderType: filterOrderType.value,
      status: filterStatus.value
    }
    const res = await getAdminOrderList(params)
    orderList.value = res.data.list || []
    total.value = res.data.total
    pages.value = res.data.pages
  } catch (error) {
    alert(error.message || '获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  page.value = 1
  fetchOrderList()
}

// 重置筛选
const resetFilters = () => {
  searchOrderNo.value = ''
  filterOrderType.value = null
  filterStatus.value = null
  page.value = 1
  fetchOrderList()
}

// 分页
const changePage = (newPage) => {
  page.value = newPage
  fetchOrderList()
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待支付'
    case 1: return '已支付'
    case 2: return '已发货'
    case 3: return '已完成'
    case 4: return '已取消'
    default: return '未知'
  }
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-paid'
    case 2: return 'status-shipped'
    case 3: return 'status-completed'
    case 4: return 'status-cancelled'
    default: return ''
  }
}

// 查看详情
const viewDetail = async (order) => {
  try {
    const res = await getAdminOrderDetail(order.id)
    detailOrder.value = res.data
    showDetail.value = true
  } catch (error) {
    alert(error.message || '获取详情失败')
  }
}

// 发货
const handleShip = async (order) => {
  openConfirmModal(
    '确认发货',
    `确定要发货订单"${order.orderNo}"吗？`,
    async () => {
      await shipOrder(order.id)
      showSuccess('发货成功', '订单发货成功！')
    },
    order
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
    fetchOrderList()
  } catch (error) {
    alert(error.message || '操作失败')
    closeConfirmModal()
  }
}

// 显示成功弹窗
const showSuccess = (title, message) => {
  successTitle.value = title
  successMessage.value = message
  showSuccessModal.value = true
}

// 关闭成功弹窗
const closeSuccessModal = () => {
  showSuccessModal.value = false
  successTitle.value = ''
  successMessage.value = ''
}

onMounted(() => {
  fetchOrderList()
})
</script>

<style scoped>
.admin-page {
  background: white;
  border-radius: 8px;
  padding: 24px;
  min-height: calc(100vh - 48px);
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
  color: #999;
  font-size: 12px;
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

.admin-type-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.type-normal {
  background: #e6f7ff;
  color: #1890ff;
}

.type-seckill {
  background: #fff2e8;
  color: #fa8c16;
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

.status-paid {
  background: #e6f7ee;
  color: #52c41a;
}

.status-shipped {
  background: #e6f7ff;
  color: #1890ff;
}

.status-completed {
  background: #f0f0f0;
  color: #8c8c8c;
}

.status-cancelled {
  background: #fff2f0;
  color: #ff4d4f;
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

.admin-btn-link.success {
  color: #52c41a;
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

.admin-modal-large {
  max-width: 800px;
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

.admin-detail-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.admin-detail-section:last-child {
  border-bottom: none;
}

.admin-detail-section h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #4a90e2;
}

.admin-detail-row {
  display: flex;
  margin-bottom: 10px;
  font-size: 14px;
}

.admin-detail-label {
  width: 100px;
  color: #666;
  flex-shrink: 0;
}

/* 商品列表样式 */
.admin-order-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.admin-order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.admin-item-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.admin-item-info {
  flex: 1;
}

.admin-item-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.admin-item-price {
  font-size: 12px;
  color: #999;
}

.admin-item-total {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: 600;
}

/* 小尺寸模态框 */
.admin-modal-small {
  max-width: 400px;
  text-align: center;
}

.confirm-message,
.success-message {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
  line-height: 1.5;
}

.admin-modal-footer {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 20px;
}

.admin-modal-footer button {
  min-width: 100px;
}

.admin-btn-confirm {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.admin-btn-confirm:hover {
  background: #357abd;
}

/* 成功弹窗样式 */
.success-modal .success-title {
  color: #52c41a;
}
</style>