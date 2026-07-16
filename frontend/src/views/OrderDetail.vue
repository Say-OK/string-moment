<template>
  <div class="page-container">
    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="!order" class="empty-message">
      订单不存在
    </div>

    <div v-else class="order-detail">
      <!-- 订单头部 -->
      <div class="detail-card">
        <div class="card-header">
          <h2 class="card-title">订单信息</h2>
          <div class="order-status" :class="getStatusClass(order.status)">
            {{ getStatusText(order.status) }}
          </div>
        </div>
        <div class="card-body">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">订单号</span>
              <span class="info-value">{{ order.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">订单类型</span>
              <span class="info-value">{{ order.orderType === 1 ? '普通订单' : '秒杀订单' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">订单金额</span>
              <span class="info-value amount">¥{{ order.totalAmount }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">实付金额</span>
              <span class="info-value amount">¥{{ order.payAmount || order.totalAmount }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ order.createTime }}</span>
            </div>
            <div class="info-item" v-if="order.paymentTime">
              <span class="info-label">支付时间</span>
              <span class="info-value">{{ order.paymentTime }}</span>
            </div>
            <div class="info-item" v-if="order.deliveryTime">
              <span class="info-label">发货时间</span>
              <span class="info-value">{{ order.deliveryTime }}</span>
            </div>
            <div class="info-item" v-if="order.receiveTime">
              <span class="info-label">收货时间</span>
              <span class="info-value">{{ order.receiveTime }}</span>
            </div>
            <div class="info-item" v-if="order.closeTime">
              <span class="info-label">关闭时间</span>
              <span class="info-value">{{ order.closeTime }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="detail-card">
        <div class="card-header">
          <h2 class="card-title">商品信息</h2>
        </div>
        <div class="card-body">
          <div class="product-list">
            <div v-for="item in order.items" :key="item.id" class="product-item">
              <img :src="item.productImage" :alt="item.productName" class="product-image" />
              <div class="product-details">
                <div class="product-name">{{ item.productName }}</div>
                <div class="product-meta">
                  <span>单价：¥{{ item.unitPrice }}</span>
                  <span>数量：{{ item.quantity }}</span>
                </div>
              </div>
              <div class="product-total">
                ¥{{ item.totalPrice }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 收货地址 -->
      <div class="detail-card">
        <div class="card-header">
          <h2 class="card-title">收货地址</h2>
        </div>
        <div class="card-body">
          <div class="address-info">
            <div class="address-row">
              <span class="address-label">收货人</span>
              <span class="address-value">{{ order.receiverName }}</span>
            </div>
            <div class="address-row">
              <span class="address-label">联系电话</span>
              <span class="address-value">{{ order.receiverPhone }}</span>
            </div>
            <div class="address-row">
              <span class="address-label">收货地址</span>
              <span class="address-value">
                {{ order.receiverProvince }}{{ order.receiverCity }}{{ order.receiverDistrict }}{{ order.receiverDetailAddress }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="detail-actions">
        <button
          v-if="order.status === 0"
          class="btn btn-secondary"
          @click="handleCancel"
        >
          取消订单
        </button>
        <button
          v-if="order.status === 0"
          class="btn btn-primary"
          @click="handlePay"
        >
          支付订单
        </button>
        <button
          v-if="order.status === 2"
          class="btn btn-primary"
          @click="handleConfirm"
        >
          确认收货
        </button>
        <button class="btn btn-secondary" @click="goBack">
          返回列表
        </button>
      </div>
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
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {cancelOrder, confirmOrder, getOrderDetail, payOrder} from '../api/order'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(false)

// 模态框状态
const showPayConfirm = ref(false)
const showPaySuccess = ref(false)
const showReceiveConfirm = ref(false)
const showReceiveSuccess = ref(false)
const showCancelConfirm = ref(false)
const showCancelSuccess = ref(false)
const errorMessage = ref('')

// 加载订单详情
const loadOrder = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getOrderDetail(id)
    order.value = res.data
  } catch (error) {
    console.error('加载订单详情失败:', error)
    alert('加载订单详情失败')
  } finally {
    loading.value = false
  }
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
const handleCancel = () => {
  showCancelConfirm.value = true
}

// 关闭取消确认模态框
const closeCancelConfirm = () => {
  showCancelConfirm.value = false
}

// 确认取消
const confirmCancel = async () => {
  try {
    await cancelOrder(order.value.id)
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
  loadOrder()
}

// 支付订单
const handlePay = () => {
  showPayConfirm.value = true
}

// 关闭支付确认模态框
const closePayConfirm = () => {
  showPayConfirm.value = false
}

// 确认支付
const confirmPay = async () => {
  try {
    await payOrder(order.value.id)
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
  loadOrder()
}

// 确认收货
const handleConfirm = () => {
  showReceiveConfirm.value = true
}

// 关闭收货确认模态框
const closeReceiveConfirm = () => {
  showReceiveConfirm.value = false
}

// 确认收货
const confirmReceive = async () => {
  try {
    await confirmOrder(order.value.id)
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
  loadOrder()
}

// 返回列表
const goBack = () => {
  router.push('/orders')
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.order-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f9f9f9;
  border-bottom: 1px solid #eee;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.card-body {
  padding: 20px;
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

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-label {
  font-size: 13px;
  color: #999;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.info-value.amount {
  font-size: 18px;
  color: #f5222d;
  font-weight: 600;
}

/* 商品列表 */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  background: #e0e0e0;
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
  display: flex;
  gap: 16px;
}

.product-total {
  font-size: 16px;
  font-weight: 600;
  color: #f5222d;
  min-width: 80px;
  text-align: right;
}

/* 收货地址 */
.address-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-row {
  display: flex;
  align-items: flex-start;
}

.address-label {
  width: 80px;
  font-size: 14px;
  color: #999;
  flex-shrink: 0;
}

.address-value {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

/* 操作按钮 */
.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

.detail-actions .btn {
  padding: 10px 24px;
  min-width: 100px;
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