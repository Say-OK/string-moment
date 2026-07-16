<template>
  <div class="page-container">
    <!-- 订单类型切换 -->
    <div class="order-type-tabs">
      <button
        class="tab-btn"
        :class="{ active: queryParams.orderType === 1 }"
        @click="switchOrderType(1)"
      >
        普通订单
      </button>
      <button
        class="tab-btn"
        :class="{ active: queryParams.orderType === 2 }"
        @click="switchOrderType(2)"
      >
        秒杀订单
      </button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <label class="filter-label">订单状态：</label>
          <select v-model="queryParams.status" class="filter-select" @change="handleFilter">
            <option :value="null">全部</option>
            <option :value="0">待支付</option>
            <option :value="1">已支付</option>
            <option :value="2">已发货</option>
            <option :value="3">已完成</option>
            <option :value="4">已取消</option>
          </select>
        </div>

        <button class="btn btn-primary filter-btn" @click="handleFilter">筛选</button>
        <button class="btn btn-secondary filter-btn" @click="resetFilter">重置</button>
      </div>
    </div>

    <!-- 订单列表 -->
    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="orders.length === 0" class="empty-message">
      暂无订单
    </div>

    <div v-else class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ order.createTime }}</span>
          </div>
          <div class="order-status" :class="getStatusClass(order.status)">
            {{ getStatusText(order.status) }}
          </div>
        </div>

        <div class="order-body">
          <div class="product-info">
            <img :src="order.firstProductImage" :alt="order.firstProductName" class="product-image" />
            <div class="product-details">
              <div class="product-name">{{ order.firstProductName }}</div>
              <div class="product-meta">
                <span v-if="order.productCount > 1">共{{ order.productCount }}件商品</span>
                <span v-else>数量：{{ order.firstProductQuantity }}</span>
              </div>
            </div>
          </div>

          <div class="order-amount">
            <div class="amount-label">订单金额</div>
            <div class="amount-value">¥{{ order.totalAmount }}</div>
          </div>
        </div>

        <div class="order-footer">
          <div class="order-actions">
            <button
              v-if="order.status === 0"
              class="btn btn-secondary btn-sm"
              @click="handleCancel(order.id)"
            >
              取消订单
            </button>
            <button
              v-if="order.status === 0"
              class="btn btn-primary btn-sm"
              @click="handlePay(order.id)"
            >
              支付订单
            </button>
            <button
              v-if="order.status === 2"
              class="btn btn-primary btn-sm"
              @click="handleConfirm(order.id)"
            >
              确认收货
            </button>
            <button
              class="btn btn-secondary btn-sm"
              @click="goToDetail(order.id)"
            >
              查看详情
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination">
      <button
        class="btn btn-secondary pagination-btn"
        :disabled="queryParams.page === 1"
        @click="changePage(queryParams.page - 1)"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ queryParams.page }} / {{ pages }} 页，共 {{ total }} 条
      </span>
      <button
        class="btn btn-secondary pagination-btn"
        :disabled="queryParams.page === pages"
        @click="changePage(queryParams.page + 1)"
      >
        下一页
      </button>
    </div>

    <!-- 确认支付模态框 -->
    <div v-if="showPayConfirm" class="modal-overlay" @click="closePayConfirm">
      <div class="modal-content confirm-modal" @click.stop>
        <h2 class="modal-title">确认支付</h2>
        <p class="confirm-message">确定要支付这个订单吗？</p>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="confirmPay">确认支付</button>
          <button class="btn btn-secondary" @click="closePayConfirm">取消</button>
        </div>
      </div>
    </div>

    <!-- 支付成功模态框 -->
    <div v-if="showPaySuccess" class="modal-overlay" @click="closePaySuccess">
      <div class="modal-content success-modal" @click.stop>
        <h2 class="modal-title success-title">支付成功</h2>
        <p class="success-message">订单支付成功！</p>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="closePaySuccess">确定</button>
        </div>
      </div>
    </div>

    <!-- 确认收货模态框 -->
    <div v-if="showReceiveConfirm" class="modal-overlay" @click="closeReceiveConfirm">
      <div class="modal-content confirm-modal" @click.stop>
        <h2 class="modal-title">确认收货</h2>
        <p class="confirm-message">确定已收到货物吗？</p>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="confirmReceive">确认收货</button>
          <button class="btn btn-secondary" @click="closeReceiveConfirm">取消</button>
        </div>
      </div>
    </div>

    <!-- 收货成功模态框 -->
    <div v-if="showReceiveSuccess" class="modal-overlay" @click="closeReceiveSuccess">
      <div class="modal-content success-modal" @click.stop>
        <h2 class="modal-title success-title">收货成功</h2>
        <p class="success-message">确认收货成功！</p>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="closeReceiveSuccess">确定</button>
        </div>
      </div>
    </div>

    <!-- 确认取消订单模态框 -->
    <div v-if="showCancelConfirm" class="modal-overlay" @click="closeCancelConfirm">
      <div class="modal-content confirm-modal" @click.stop>
        <h2 class="modal-title">确认取消</h2>
        <p class="confirm-message">确定要取消这个订单吗？</p>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="confirmCancel">确认取消</button>
          <button class="btn btn-secondary" @click="closeCancelConfirm">关闭</button>
        </div>
      </div>
    </div>

    <!-- 取消成功模态框 -->
    <div v-if="showCancelSuccess" class="modal-overlay" @click="closeCancelSuccess">
      <div class="modal-content success-modal" @click.stop>
        <h2 class="modal-title success-title">取消成功</h2>
        <p class="success-message">订单取消成功！</p>
        <div class="modal-actions">
          <button class="btn btn-primary" @click="closeCancelSuccess">确定</button>
        </div>
      </div>
    </div>

    <!-- 错误消息 -->
    <div v-if="errorMessage" class="message message-error">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {cancelOrder, confirmOrder, getOrderList, payOrder} from '../api/order'

const router = useRouter()

const orders = ref([])
const total = ref(0)
const pages = ref(0)
const loading = ref(false)

const queryParams = reactive({
  page: 1,
  size: 10,
  status: null,
  orderType: 1  // 默认普通订单
})

// 模态框状态
const showPayConfirm = ref(false)
const showPaySuccess = ref(false)
const showReceiveConfirm = ref(false)
const showReceiveSuccess = ref(false)
const showCancelConfirm = ref(false)
const showCancelSuccess = ref(false)
const errorMessage = ref('')
const currentOrderId = ref(null)

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    // 如果status为null，不传递该参数
    if (params.status === null) {
      delete params.status
    }
    const res = await getOrderList(params)
    orders.value = res.data.list
    total.value = res.data.total
    pages.value = res.data.pages
  } catch (error) {
    console.error('加载订单失败:', error)
    alert('加载订单失败')
  } finally {
    loading.value = false
  }
}

// 切换订单类型
const switchOrderType = (type) => {
  queryParams.orderType = type
  queryParams.page = 1
  loadOrders()
}

// 筛选
const handleFilter = () => {
  queryParams.page = 1
  loadOrders()
}

// 重置筛选
const resetFilter = () => {
  queryParams.status = null
  queryParams.page = 1
  loadOrders()
}

// 切换页码
const changePage = (page) => {
  queryParams.page = page
  loadOrders()
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '已支付',
    2: '已发货',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-paid',
    2: 'status-shipped',
    3: 'status-completed',
    4: 'status-cancelled'
  }
  return classMap[status] || ''
}

// 取消订单
const handleCancel = (id) => {
  currentOrderId.value = id
  showCancelConfirm.value = true
}

// 关闭取消确认模态框
const closeCancelConfirm = () => {
  showCancelConfirm.value = false
  currentOrderId.value = null
}

// 确认取消
const confirmCancel = async () => {
  try {
    await cancelOrder(currentOrderId.value)
    closeCancelConfirm()
    showCancelSuccess.value = true
  } catch (error) {
    console.error('取消订单失败:', error)
    closeCancelConfirm()
    errorMessage.value = error.message || '取消订单失败'
    setTimeout(() => {
      errorMessage.value = ''
    }, 3000)
  }
}

// 关闭取消成功模态框
const closeCancelSuccess = () => {
  showCancelSuccess.value = false
  loadOrders()
}

// 支付订单
const handlePay = (id) => {
  currentOrderId.value = id
  showPayConfirm.value = true
}

// 关闭支付确认模态框
const closePayConfirm = () => {
  showPayConfirm.value = false
  currentOrderId.value = null
}

// 确认支付
const confirmPay = async () => {
  try {
    await payOrder(currentOrderId.value)
    closePayConfirm()
    showPaySuccess.value = true
  } catch (error) {
    console.error('支付订单失败:', error)
    closePayConfirm()
    errorMessage.value = error.message || '支付订单失败'
    setTimeout(() => {
      errorMessage.value = ''
    }, 3000)
  }
}

// 关闭支付成功模态框
const closePaySuccess = () => {
  showPaySuccess.value = false
  loadOrders()
}

// 确认收货
const handleConfirm = (id) => {
  currentOrderId.value = id
  showReceiveConfirm.value = true
}

// 关闭收货确认模态框
const closeReceiveConfirm = () => {
  showReceiveConfirm.value = false
  currentOrderId.value = null
}

// 确认收货
const confirmReceive = async () => {
  try {
    await confirmOrder(currentOrderId.value)
    closeReceiveConfirm()
    showReceiveSuccess.value = true
  } catch (error) {
    console.error('确认收货失败:', error)
    closeReceiveConfirm()
    errorMessage.value = error.message || '确认收货失败'
    setTimeout(() => {
      errorMessage.value = ''
    }, 3000)
  }
}

// 关闭收货成功模态框
const closeReceiveSuccess = () => {
  showReceiveSuccess.value = false
  loadOrders()
}

// 查看详情
const goToDetail = (id) => {
  router.push(`/order/${id}`)
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
/* 订单类型切换 */
.order-type-tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  background: #ffffff;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.tab-btn {
  padding: 10px 32px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-btn:hover {
  border-color: #4a90e2;
  color: #4a90e2;
}

.tab-btn.active {
  background: #4a90e2;
  border-color: #4a90e2;
  color: #fff;
}

/* 筛选栏 */
.filter-bar {
  background: #ffffff;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-item {
  display: flex;
  align-items: center;
}

.filter-label {
  font-size: 14px;
  color: #666;
  margin-right: 8px;
}

.filter-select {
  height: 32px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: #fff;
  cursor: pointer;
  min-width: 120px;
}

.filter-btn {
  height: 32px;
  width: auto;
  padding: 0 16px;
}

/* 订单列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f9f9f9;
  border-bottom: 1px solid #eee;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.order-no {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.order-time {
  font-size: 13px;
  color: #999;
}

.order-status {
  font-size: 14px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 4px;
}

.status-pending {
  color: #ff9800;
  background: #fff3e0;
}

.status-paid {
  color: #2196f3;
  background: #e3f2fd;
}

.status-shipped {
  color: #4a90e2;
  background: #e3f2fd;
}

.status-completed {
  color: #4caf50;
  background: #e8f5e9;
}

.status-cancelled {
  color: #999;
  background: #f5f5f5;
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  background: #f5f5f5;
}

.product-details {
  flex: 1;
}

.product-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.product-meta {
  font-size: 13px;
  color: #999;
}

.order-amount {
  text-align: right;
  min-width: 150px;
}

.amount-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 4px;
}

.amount-value {
  font-size: 20px;
  font-weight: 600;
  color: #f5222d;
}

.order-footer {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.btn-sm {
  padding: 6px 16px;
  height: 32px;
  font-size: 13px;
  min-width: 90px;
  white-space: nowrap;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 24px;
}

.pagination-btn {
  height: 32px;
  padding: 0 20px;
  min-width: auto;
}

.page-info {
  font-size: 14px;
  color: #666;
  padding: 0 8px;
  min-width: 180px;
  text-align: center;
}

/* 空状态和加载 */
.empty-message {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 16px;
  background: #ffffff;
  border-radius: 8px;
}

/* 模态框样式 */
.modal-overlay {
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

.modal-content {
  background: #fff;
  padding: 32px;
  border-radius: 8px;
  max-width: 600px;
  width: 90%;
}

.confirm-modal,
.success-modal {
  max-width: 400px;
  text-align: center;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

.success-title {
  color: #52c41a;
}

.confirm-message,
.success-message {
  font-size: 14px;
  color: #666;
  margin-bottom: 24px;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 24px;
}

.modal-actions button {
  min-width: 120px;
  height: 40px;
}

/* 错误消息 */
.message {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 4px;
  font-size: 14px;
  z-index: 1001;
}

.message-error {
  background: #fff3f3;
  color: #f5222d;
  border: 1px solid #f5222d;
}
</style>