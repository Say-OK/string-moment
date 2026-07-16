<template>
  <div class="page-container">
    <div v-if="loading" class="loading">加载中...</div>

    <div v-else class="order-create">
      <h1 class="page-title">创建订单</h1>

      <!-- 错误/成功消息 -->
      <div v-if="errorMessage" class="message message-error">
        {{ errorMessage }}
      </div>
      <div v-if="successMessage" class="message message-success">
        {{ successMessage }}
      </div>

      <!-- 如果商品加载失败 -->
      <div v-if="!product" class="empty-message">
        商品信息加载失败，请返回商品详情页重新尝试
      </div>

      <!-- 商品信息（只有product存在才渲染） -->
      <div v-if="product" class="section-card">
        <h2 class="section-title">商品信息</h2>
        <div class="product-summary">
          <div class="product-image-small">
            <img :src="product.imageUrl" :alt="product.name" />
          </div>
          <div class="product-details">
            <div class="product-name">{{ product.name }}</div>
            <div class="product-category">分类：{{ product.category }}</div>
            <div class="product-price">单价：¥{{ product.price }}</div>
            <div class="product-stock">库存：{{ product.stock }} 件</div>
          </div>
        </div>

        <!-- 购买数量 -->
        <div class="quantity-selector">
          <label class="quantity-label">购买数量：</label>
          <div class="quantity-control">
            <button class="quantity-btn" @click="decreaseQuantity" :disabled="quantity <= 1">-</button>
            <input
              v-model.number="quantity"
              type="number"
              class="quantity-input"
              min="1"
              :max="product.stock"
            />
            <button class="quantity-btn" @click="increaseQuantity" :disabled="quantity >= product.stock">+</button>
          </div>
          <span class="quantity-hint">（最多可购买 {{ product.stock }} 件）</span>
        </div>

        <!-- 订单总额 -->
        <div class="order-total">
          <span class="total-label">订单总额：</span>
          <span class="total-price">¥{{ totalAmount }}</span>
        </div>
      </div>

      <!-- 收货地址 -->
      <div class="section-card">
        <h2 class="section-title">收货地址</h2>

        <div v-if="addresses.length === 0" class="empty-address">
          <p>您还没有收货地址</p>
          <p class="hint-text">请先在用户中心添加收货地址，然后再下单</p>
          <button class="btn btn-secondary" @click="goToUserCenter">去添加地址</button>
        </div>

        <div v-else class="address-list">
          <div
            v-for="address in addresses"
            :key="address.id"
            class="address-card"
            :class="{ 'address-selected': selectedAddressId === address.id }"
            @click="selectAddress(address.id)"
          >
            <div class="address-header">
              <span class="address-name">{{ address.receiverName }}</span>
              <span class="address-phone">{{ address.receiverPhone }}</span>
              <span v-if="address.isDefault === 1" class="default-badge">默认</span>
            </div>
            <div class="address-detail">
              {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
            </div>
          </div>
        </div>
      </div>

      <!-- 提交订单（只有product和addresses存在才渲染） -->
      <div v-if="product && addresses.length > 0" class="submit-section">
        <button
          class="btn btn-primary btn-large submit-btn"
          :disabled="!selectedAddressId || quantity <= 0"
          @click="handleSubmit"
        >
          提交订单
        </button>
        <button class="btn btn-secondary btn-large cancel-btn" @click="goBack">
          返回商品详情
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {getProductDetail} from '../api/product'
import {getAddressList} from '../api/address'
import {createOrder} from '../api/order'

const route = useRoute()
const router = useRouter()

const product = ref(null)
const addresses = ref([])
const quantity = ref(1)
const selectedAddressId = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// 订单总额
const totalAmount = computed(() => {
  if (!product.value || quantity.value <= 0) return 0
  return (product.value.price * quantity.value).toFixed(2)
})

// 加载商品详情
const loadProduct = async () => {
  try {
    const productId = route.params.productId
    console.log('加载商品，ID:', productId)  // 添加日志

    if (!productId) {
      errorMessage.value = '商品ID不存在'
      return
    }

    const res = await getProductDetail(productId)
    console.log('商品详情:', res.data)  // 添加日志
    product.value = res.data
  } catch (error) {
    console.error('加载商品失败:', error)
    errorMessage.value = '商品信息加载失败'
  }
}

// 加载地址列表
const loadAddresses = async () => {
  try {
    const res = await getAddressList()
    console.log('地址列表:', res.data)  // 添加日志
    addresses.value = res.data || []

    // 自动选择默认地址
    const defaultAddress = addresses.value.find(addr => addr.isDefault === 1)
    if (defaultAddress) {
      selectedAddressId.value = defaultAddress.id
    } else if (addresses.value.length > 0) {
      // 如果没有默认地址，选择第一个
      selectedAddressId.value = addresses.value[0].id
    }
  } catch (error) {
    console.error('加载地址失败:', error)
    errorMessage.value = '地址列表加载失败'
  }
}

// 增加数量
const increaseQuantity = () => {
  if (quantity.value < product.value.stock) {
    quantity.value++
  }
}

// 减少数量
const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

// 选择地址
const selectAddress = (id) => {
  selectedAddressId.value = id
}

// 提交订单
const handleSubmit = async () => {
  if (!selectedAddressId.value) {
    errorMessage.value = '请选择收货地址'
    return
  }

  if (quantity.value <= 0 || quantity.value > product.value.stock) {
    errorMessage.value = '购买数量不合法'
    return
  }

  try {
    errorMessage.value = ''
    const orderData = {
      addressId: selectedAddressId.value,
      items: [
        {
          productId: product.value.id,
          quantity: quantity.value
        }
      ]
    }

    const res = await createOrder(orderData)
    successMessage.value = '订单创建成功！订单号：' + res.data.orderNo
    setTimeout(() => {
      router.push('/products')
    }, 1500)
  } catch (error) {
    errorMessage.value = error.message || '订单创建失败'
  }
}

// 返回商品详情
const goBack = () => {
  router.back()
}

// 去用户中心添加地址
const goToUserCenter = () => {
  router.push('/user')
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadProduct(), loadAddresses()])
  } catch (error) {
    console.error('加载页面数据失败:', error)
    errorMessage.value = '加载数据失败，请刷新页面重试'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.order-create {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  text-align: center;
}

.section-card {
  background: #ffffff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

/* 商品信息 */
.product-summary {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.product-image-small {
  width: 120px;
  height: 120px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  overflow: hidden;
}

.product-image-small img {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}

.product-details {
  flex: 1;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.product-category,
.product-price,
.product-stock {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.product-price {
  color: #f5222d;
  font-weight: 500;
}

/* 购买数量 */
.quantity-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.quantity-label {
  font-size: 14px;
  color: #666;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 0;
}

.quantity-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  font-size: 16px;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quantity-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.quantity-btn:disabled {
  cursor: not-allowed;
  color: #999;
}

.quantity-input {
  width: 60px;
  height: 32px;
  border: 1px solid #ddd;
  border-left: none;
  border-right: none;
  text-align: center;
  font-size: 14px;
}

.quantity-hint {
  font-size: 12px;
  color: #999;
}

/* 订单总额 */
.order-total {
  display: flex;
  align-items: center;
  gap: 12px;
}

.total-label {
  font-size: 16px;
  color: #666;
}

.total-price {
  font-size: 24px;
  font-weight: 700;
  color: #f5222d;
}

/* 收货地址 */
.empty-address {
  text-align: center;
  padding: 20px;
  color: #999;
}

.hint-text {
  font-size: 14px;
  margin-bottom: 16px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-card {
  border: 2px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-card:hover {
  border-color: #999;
}

.address-selected {
  border-color: #4CAF50;
  background: #f0f8f0;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.address-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.address-phone {
  font-size: 14px;
  color: #666;
}

.default-badge {
  background: #4CAF50;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.address-detail {
  font-size: 14px;
  color: #666;
}

/* 提交按钮 */
.submit-section {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

.submit-btn,
.cancel-btn {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 消息提示 */
.message {
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 16px;
  font-size: 14px;
}

.message-error {
  background: #fff3f3;
  color: #f5222d;
  border: 1px solid #f5222d;
}

.message-success {
  background: #f0f8f0;
  color: #4CAF50;
  border: 1px solid #4CAF50;
}
</style>