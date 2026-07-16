<template>
  <div class="page-container">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <label class="filter-label">活动状态：</label>
          <select v-model="currentStatus" @change="handleFilter" class="filter-select">
            <option :value="null">全部</option>
            <option :value="1">进行中</option>
            <option :value="0">未开始</option>
            <option :value="2">已结束</option>
          </select>
        </div>

        <button class="btn btn-primary filter-btn" @click="handleFilter">筛选</button>
        <button class="btn btn-secondary filter-btn" @click="resetFilter">重置</button>
      </div>
    </div>

    <!-- 活动列表 -->
    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="activities.length === 0" class="empty-message">
      暂无秒杀活动
    </div>

    <div v-else class="activity-grid">
      <div
        v-for="activity in activities"
        :key="activity.id"
        class="activity-card"
        @click="goToDetail(activity.id)"
      >
        <!-- 活动状态标签 -->
        <div class="status-badge" :class="getStatusClass(activity.status)">
          {{ getStatusText(activity.status) }}
        </div>

        <!-- 商品图片 -->
        <div class="activity-image">
          <img :src="activity.productImage" :alt="activity.productName" />
        </div>

        <!-- 活动信息 -->
        <div class="activity-info">
          <div class="activity-name">{{ activity.name }}</div>
          <div class="product-name">{{ activity.productName }}</div>

          <!-- 价格信息 -->
          <div class="price-row">
            <span class="seckill-price">¥{{ activity.seckillPrice }}</span>
            <span class="original-price">¥{{ activity.originalPrice }}</span>
          </div>

          <!-- 库存信息 -->
          <div class="stock-info">
            <div class="stock-bar">
              <div class="stock-bar-fill" :style="{ width: activity.stockPercent + '%' }"></div>
            </div>
            <span class="stock-text">
              {{ activity.stockStatus === 0 ? '已售罄' : `剩余 ${activity.stockPercent}%` }}
            </span>
          </div>

          <!-- 活动时间 -->
          <div class="time-info">
            <div class="time-row">
              <span class="time-label">开始：</span>
              <span class="time-value">{{ activity.startTime }}</span>
            </div>
            <div class="time-row">
              <span class="time-label">结束：</span>
              <span class="time-value">{{ activity.endTime }}</span>
            </div>
          </div>

          <!-- 倒计时（仅进行中的活动显示） -->
          <div v-if="activity.status === 1 && activity.timeLeft > 0" class="countdown">
            <span class="countdown-label">剩余时间：</span>
            <span class="countdown-time">{{ formatCountdown(activity.timeLeft) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, onUnmounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {getSeckillActivityList} from '../api/seckill'
import {getActivityStatusClass, getActivityStatusText, SeckillConstant} from '../constants/seckill'

const router = useRouter()

const activities = ref([])
const loading = ref(false)
const currentStatus = ref(null)
let countdownTimer = null

// 加载活动列表
const loadActivities = async () => {
  loading.value = true
  try {
    const params = {}
    if (currentStatus.value !== null) {
      params.status = currentStatus.value
    }
    const res = await getSeckillActivityList(params)
    activities.value = res.data || []
  } catch (error) {
    console.error('加载秒杀活动失败:', error)
  } finally {
    loading.value = false
  }
}

// 筛选
const handleFilter = () => {
  loadActivities()
}

// 重置筛选
const resetFilter = () => {
  currentStatus.value = null
  loadActivities()
}

// 查看详情
const goToDetail = (id) => {
  router.push(`/seckill/${id}`)
}

// 获取状态文本
const getStatusText = (status) => {
  return getActivityStatusText(status)
}

// 获取状态样式类
const getStatusClass = (status) => {
  return getActivityStatusClass(status)
}

// 格式化倒计时
const formatCountdown = (seconds) => {
  if (seconds <= 0) return '00:00:00'

  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

// 更新倒计时
const updateCountdown = () => {
  activities.value.forEach(activity => {
    if (activity.status === SeckillConstant.ACTIVITY_ON_GOING && activity.timeLeft > 0) {
      activity.timeLeft = Math.max(0, activity.timeLeft - 1)
    }
  })
}

onMounted(() => {
  loadActivities()
  // 启动倒计时定时器
  countdownTimer = setInterval(updateCountdown, 1000)
})

onUnmounted(() => {
  // 清除定时器
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
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
}

.filter-btn {
  height: 32px;
  width: auto;
  padding: 0 16px;
}

/* 活动网格 */
.activity-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.activity-card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;
  position: relative;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

/* 状态标签 */
.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  z-index: 1;
}

.status-active {
  background: #ff4d4f;
  color: #ffffff;
}

.status-pending {
  background: #faad14;
  color: #ffffff;
}

.status-ended {
  background: #d9d9d9;
  color: #666;
}

/* 商品图片 */
.activity-image {
  width: 100%;
  height: 200px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.activity-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}

/* 活动信息 */
.activity-info {
  padding: 16px;
}

.activity-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-name {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 价格信息 */
.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
}

.seckill-price {
  font-size: 22px;
  font-weight: 700;
  color: #ff4d4f;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

/* 库存信息 */
.stock-info {
  margin-bottom: 12px;
}

.stock-bar {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 6px;
}

.stock-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff4d4f, #ff7875);
  border-radius: 4px;
  transition: width 0.3s;
}

.stock-text {
  font-size: 12px;
  color: #666;
}

/* 时间信息 */
.time-info {
  padding: 12px;
  background: #f9f9f9;
  border-radius: 6px;
  margin-bottom: 12px;
}

.time-row {
  display: flex;
  font-size: 12px;
  line-height: 1.8;
}

.time-label {
  color: #999;
  width: 48px;
}

.time-value {
  color: #666;
}

/* 倒计时 */
.countdown {
  padding: 10px;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe7e7 100%);
  border-radius: 6px;
  text-align: center;
}

.countdown-label {
  font-size: 12px;
  color: #ff4d4f;
}

.countdown-time {
  font-size: 18px;
  font-weight: 700;
  color: #ff4d4f;
  margin-left: 8px;
  font-family: 'Courier New', monospace;
}

/* 空状态和加载 */
.empty-message {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 16px;
}
</style>