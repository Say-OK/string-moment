<template>
  <div class="page-container">
    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="!activity" class="empty-message">
      秒杀活动不存在或已结束
    </div>

    <div v-else class="detail-container">
      <!-- 返回按钮 -->
      <div class="back-btn">
        <button class="btn btn-secondary" @click="goBack">← 返回列表</button>
      </div>

      <!-- 活动详情卡片 -->
      <div class="detail-card">
        <!-- 活动状态 -->
        <div class="status-banner" :class="getStatusClass(activity.status)">
          {{ getStatusText(activity.status) }}
        </div>

        <div class="detail-content">
          <!-- 左侧：商品图片 -->
          <div class="detail-left">
            <div class="product-image-large">
              <img :src="activity.productImage" :alt="activity.productName" />
            </div>
          </div>

          <!-- 右侧：活动信息 -->
          <div class="detail-right">
            <h1 class="activity-title">{{ activity.name }}</h1>

            <!-- 商品名称 -->
            <div class="product-info">
              <span class="label">商品：</span>
              <span class="value">{{ activity.productName }}</span>
            </div>

            <!-- 价格信息 -->
            <div class="price-section">
              <div class="price-row">
                <span class="seckill-label">秒杀价</span>
                <span class="seckill-price">¥{{ activity.seckillPrice }}</span>
              </div>
              <div class="price-row">
                <span class="original-label">原价</span>
                <span class="original-price">¥{{ activity.originalPrice }}</span>
              </div>
            </div>

            <!-- 库存信息 -->
            <div class="stock-section">
              <div class="stock-row">
                <span class="label">总库存：</span>
                <span class="value">{{ activity.totalStock }} 件</span>
              </div>
              <div class="stock-row">
                <span class="label">剩余库存：</span>
                <span class="value">{{ activity.availableStock }} 件</span>
              </div>
              <div class="stock-row">
                <span class="label">已售：</span>
                <span class="value sold">{{ activity.totalStock - activity.availableStock }} 件</span>
              </div>
              <div class="stock-bar-container">
                <div class="stock-bar">
                  <div class="stock-bar-fill" :style="{ width: stockPercent + '%' }"></div>
                </div>
                <span class="stock-percent">{{ stockPercent }}%</span>
              </div>
            </div>

            <!-- 活动时间 -->
            <div class="time-section">
              <div class="time-row">
                <span class="label">开始时间：</span>
                <span class="value">{{ activity.startTime }}</span>
              </div>
              <div class="time-row">
                <span class="label">结束时间：</span>
                <span class="value">{{ activity.endTime }}</span>
              </div>
            </div>

            <!-- 倒计时（未开始的活动） -->
            <div v-if="activity.status === 0" class="countdown-section">
              <div class="countdown-label">距离活动开始还有：</div>
              <div class="countdown-timer">{{ countdown }}</div>
            </div>

            <!-- 秒杀资格和操作 -->
            <div class="action-section">
              <!-- 未开始 -->
              <div v-if="activity.status === 0" class="qualification-info">
                <div class="info-text">活动未开始，请等待</div>
                <button class="btn btn-disabled" disabled>暂未开始</button>
              </div>

              <!-- 进行中 -->
              <div v-else-if="activity.status === 1">
                <div v-if="qualificationLoading" class="loading-small">检查资格中...</div>
                <div v-else>
                  <!-- 可以秒杀 -->
                  <div v-if="qualification === 0" class="can-seckill">
                    <div class="success-text">✓ 您有秒杀资格！</div>
                    <div class="address-select">
                      <label>选择收货地址：</label>
                      <select v-model="selectedAddressId" class="address-dropdown">
                        <option value="">请选择地址</option>
                        <option v-for="addr in addresses" :key="addr.id" :value="addr.id">
                          {{ addr.receiverName }} - {{ addr.detailAddress }}
                        </option>
                      </select>
                    </div>
                    <button
                      class="btn btn-seckill"
                      :disabled="!selectedAddressId || seckillLoading"
                      @click="handleSeckill"
                    >
                      {{ seckillLoading ? '秒杀中...' : '立即秒杀' }}
                    </button>
                  </div>

                  <!-- 无资格 -->
                  <div v-else class="no-qualification">
                    <div class="warning-text">{{ getQualificationText(qualification) }}</div>
                    <button class="btn btn-disabled" disabled>无法秒杀</button>
                  </div>
                </div>
              </div>

              <!-- 已结束 -->
              <div v-else-if="activity.status === 2" class="ended-section">
                <div class="info-text">活动已结束</div>
                <button class="btn btn-disabled" disabled>已结束</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 秒杀结果弹窗 -->
      <div v-if="showResultModal" class="modal-overlay" @click="closeResultModal">
        <div class="modal-content" @click.stop>
          <div v-if="seckillResult" class="result-container">
            <!-- 成功 -->
            <div v-if="seckillResult.status === 0" class="result-success">
              <div class="result-icon">✓</div>
              <div class="result-title">秒杀成功！</div>
              <div class="result-info">
                <div class="info-row">
                  <span class="label">订单号：</span>
                  <span class="value">{{ seckillResult.orderNo }}</span>
                </div>
                <div class="info-row">
                  <span class="label">商品名称：</span>
                  <span class="value">{{ seckillResult.productName }}</span>
                </div>
                <div class="info-row">
                  <span class="label">秒杀价格：</span>
                  <span class="value price">¥{{ seckillResult.seckillPrice }}</span>
                </div>
                <div class="info-row">
                  <span class="label">订单金额：</span>
                  <span class="value price">¥{{ seckillResult.totalAmount }}</span>
                </div>
                <div class="info-row">
                  <span class="label">购买数量：</span>
                  <span class="value">{{ seckillResult.quantity }}</span>
                </div>
                <div class="info-row warning">
                  <span class="label">支付提示：</span>
                  <span class="value">请在 {{ seckillResult.paymentTimeout }} 分钟内完成支付</span>
                </div>
              </div>
              <div class="result-actions">
                <button class="btn btn-primary" @click="goToOrder(seckillResult.orderId)">查看订单</button>
                <button class="btn btn-secondary" @click="closeResultModal">关闭</button>
              </div>
            </div>

            <!-- 失败 -->
            <div v-else class="result-fail">
              <div class="result-icon fail">✗</div>
              <div class="result-title">秒杀失败</div>
              <div class="result-message">{{ seckillResult.message }}</div>
              <button class="btn btn-secondary" @click="closeResultModal">关闭</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {checkSeckillQualification, executeSeckill, getSeckillActivityDetail} from '../api/seckill'
import {getAddressList} from '../api/address'
import {
  getActivityStatusClass,
  getActivityStatusText,
  getQualificationText,
  SeckillConstant
} from '../constants/seckill'

const route = useRoute()
const router = useRouter()

const activity = ref(null)
const loading = ref(false)
const qualification = ref(null)
const qualificationLoading = ref(false)
const addresses = ref([])
const selectedAddressId = ref('')
const seckillLoading = ref(false)
const seckillResult = ref(null)
const showResultModal = ref(false)
const countdown = ref('00:00:00')
let countdownTimer = null

// 计算库存百分比
const stockPercent = computed(() => {
  if (!activity.value || activity.value.totalStock === 0) return 0
  return Math.round((activity.value.availableStock / activity.value.totalStock) * 100)
})

// 加载活动详情
const loadActivity = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getSeckillActivityDetail(id)
    activity.value = res.data

    // 如果活动未开始，启动倒计时
    if (activity.value.status === SeckillConstant.ACTIVITY_NOT_STARTED) {
      startCountdown()
    }

    // 如果活动进行中，检查资格并加载地址
    if (activity.value.status === SeckillConstant.ACTIVITY_ON_GOING) {
      checkQualification()
      loadAddresses()
    }
  } catch (error) {
    console.error('加载活动详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 检查秒杀资格
const checkQualification = async () => {
  qualificationLoading.value = true
  try {
    const res = await checkSeckillQualification(activity.value.id)
    qualification.value = res.data
  } catch (error) {
    console.error('检查资格失败:', error)
    qualification.value = SeckillConstant.QUALIFY_ACTIVITY_ENDED
  } finally {
    qualificationLoading.value = false
  }
}

// 加载地址列表
const loadAddresses = async () => {
  try {
    const res = await getAddressList()
    addresses.value = res.data || []
    // 默认选择第一个地址
    if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].id
    }
  } catch (error) {
    console.error('加载地址失败:', error)
  }
}

// 执行秒杀
const handleSeckill = async () => {
  if (!selectedAddressId.value) {
    alert('请选择收货地址')
    return
  }

  seckillLoading.value = true
  try {
    const res = await executeSeckill({
      seckillActivityId: activity.value.id,
      addressId: selectedAddressId.value
    })
    seckillResult.value = res.data
    showResultModal.value = true

    // 如果成功，重新加载活动信息
    if (res.data.status === SeckillConstant.RESULT_SUCCESS) {
      setTimeout(() => {
        loadActivity()
      }, 1000)
    }
  } catch (error) {
    console.error('秒杀失败:', error)
    alert(error.message || '秒杀失败，请重试')
  } finally {
    seckillLoading.value = false
  }
}

// 启动倒计时
const startCountdown = () => {
  if (!activity.value || !activity.value.startTime) return

  const updateCountdown = () => {
    const startTime = new Date(activity.value.startTime).getTime()
    const now = Date.now()
    const diff = Math.max(0, startTime - now)

    if (diff === 0) {
      // 倒计时结束，重新加载活动
      loadActivity()
      return
    }

    const hours = Math.floor(diff / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)

    countdown.value = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  }

  updateCountdown()
  countdownTimer = setInterval(updateCountdown, 1000)
}

// 获取状态文本
const getStatusText = (status) => {
  return getActivityStatusText(status)
}

// 获取状态样式类
const getStatusClass = (status) => {
  return getActivityStatusClass(status)
}

// 返回列表
const goBack = () => {
  router.push('/seckill')
}

// 查看订单
const goToOrder = (orderId) => {
  router.push(`/order/${orderId}`)
}

// 关闭结果弹窗
const closeResultModal = () => {
  showResultModal.value = false
  seckillResult.value = null
}

onMounted(() => {
  loadActivity()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 20px;
}

.detail-card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.status-banner {
  padding: 12px 24px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
}

.status-active {
  background: linear-gradient(90deg, #ff4d4f, #ff7875);
}

.status-pending {
  background: linear-gradient(90deg, #faad14, #ffc53d);
}

.status-ended {
  background: linear-gradient(90deg, #d9d9d9, #bfbfbf);
}

.detail-content {
  display: flex;
  gap: 40px;
  padding: 32px;
}

.detail-left {
  width: 400px;
  flex-shrink: 0;
}

.product-image-large {
  width: 400px;
  height: 400px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  overflow: hidden;
}

.product-image-large img {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}

.detail-right {
  flex: 1;
}

.activity-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  line-height: 1.4;
}

.product-info {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
}

.product-info .label {
  color: #999;
}

.product-info .value {
  color: #333;
  font-weight: 500;
}

/* 价格区域 */
.price-section {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe7e7 100%);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.price-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}

.price-row:last-child {
  margin-bottom: 0;
}

.seckill-label {
  font-size: 14px;
  color: #ff4d4f;
  width: 60px;
}

.seckill-price {
  font-size: 32px;
  font-weight: 700;
  color: #ff4d4f;
}

.original-label {
  font-size: 12px;
  color: #999;
  width: 60px;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

/* 库存区域 */
.stock-section {
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 20px;
}

.stock-row {
  display: flex;
  font-size: 14px;
  margin-bottom: 8px;
}

.stock-row:last-of-type {
  margin-bottom: 12px;
}

.stock-row .label {
  color: #999;
  width: 80px;
}

.stock-row .value {
  color: #333;
}

.stock-row .value.sold {
  color: #ff4d4f;
  font-weight: 500;
}

.stock-bar-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stock-bar {
  flex: 1;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.stock-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff4d4f, #ff7875);
  border-radius: 4px;
}

.stock-percent {
  font-size: 12px;
  color: #ff4d4f;
  font-weight: 500;
  min-width: 40px;
  text-align: right;
}

/* 时间区域 */
.time-section {
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 20px;
}

.time-row {
  display: flex;
  font-size: 14px;
  margin-bottom: 8px;
}

.time-row:last-child {
  margin-bottom: 0;
}

.time-row .label {
  color: #999;
  width: 80px;
}

.time-row .value {
  color: #333;
}

/* 倒计时 */
.countdown-section {
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
}

.countdown-label {
  font-size: 14px;
  color: #fa8c16;
  margin-bottom: 8px;
}

.countdown-timer {
  font-size: 36px;
  font-weight: 700;
  color: #fa8c16;
  font-family: 'Courier New', monospace;
}

/* 操作区域 */
.action-section {
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.qualification-info,
.can-seckill,
.no-qualification,
.ended-section {
  text-align: center;
}

.info-text {
  font-size: 16px;
  color: #666;
  margin-bottom: 16px;
}

.success-text {
  font-size: 18px;
  font-weight: 600;
  color: #52c41a;
  margin-bottom: 16px;
}

.warning-text {
  font-size: 16px;
  color: #faad14;
  margin-bottom: 16px;
}

.address-select {
  margin-bottom: 16px;
}

.address-select label {
  font-size: 14px;
  color: #666;
  margin-right: 8px;
}

.address-dropdown {
  width: 300px;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.btn-seckill {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: #ffffff;
  border: none;
  padding: 12px 48px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-seckill:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
}

.btn-seckill:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.btn-disabled {
  background: #d9d9d9;
  color: #999;
  cursor: not-allowed;
}

/* 弹窗 */
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
  background: #ffffff;
  border-radius: 8px;
  padding: 32px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.result-container {
  text-align: center;
}

.result-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #52c41a;
  color: #ffffff;
  font-size: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.result-icon.fail {
  background: #ff4d4f;
}

.result-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

.result-info {
  text-align: left;
  background: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  padding: 8px 0;
  font-size: 14px;
}

.info-row .label {
  width: 80px;
  color: #666;
}

.info-row .value {
  color: #333;
}

.info-row .value.price {
  color: #ff4d4f;
  font-weight: 600;
}

.info-row.warning {
  color: #fa8c16;
}

.result-message {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
}

.result-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.loading-small {
  text-align: center;
  color: #999;
  padding: 20px;
}

/* 空状态和加载 */
.empty-message {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 16px;
}
</style>