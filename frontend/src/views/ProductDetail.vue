<template>
  <div class="page-container">
    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="!product" class="empty-message">
      商品不存在或已下架
    </div>

    <div v-else class="product-detail">
      <div class="product-detail-left">
        <div class="product-image-large">
          <img :src="product.imageUrl" :alt="product.name" />
        </div>
      </div>

      <div class="product-detail-right">
        <h1 class="product-title">{{ product.name }}</h1>

        <div class="product-price-large">¥{{ product.price }}</div>

        <div class="product-info-card">
          <div class="info-row">
            <span class="info-label">分类</span>
            <span class="info-value">{{ product.category || '未分类' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">库存</span>
            <span class="info-value">{{ product.stock }} 件</span>
          </div>
          <div class="info-row">
            <span class="info-label">销量</span>
            <span class="info-value">{{ product.saleCount }} 件</span>
          </div>
          <div class="info-row">
            <span class="info-label">上架时间</span>
            <span class="info-value">{{ product.createTime }}</span>
          </div>
        </div>

        <div class="product-description">
          <h3 class="description-title">商品描述</h3>
          <p class="description-text">{{ product.description || '暂无描述' }}</p>
        </div>

        <div class="product-actions">
          <button class="btn btn-primary btn-large" @click="handleBuy">
            立即购买
          </button>
          <button class="btn btn-secondary btn-large" @click="goBack">
            返回列表
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {getProductDetail} from '../api/product'

const route = useRoute()
const router = useRouter()

const product = ref(null)
const loading = ref(false)

// 加载商品详情
const loadProduct = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getProductDetail(id)
    product.value = res.data
  } catch (error) {
    console.error('加载商品详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push('/products')
}

// 立即购买
const handleBuy = () => {
  router.push(`/create-order/${product.value.id}`)
}

onMounted(() => {
  loadProduct()
})
</script>

<style scoped>
.product-detail {
  display: flex;
  gap: 40px;
  background: #ffffff;
  padding: 32px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-detail-left {
  width: 480px;
}

.product-image-large {
  width: 480px;
  height: 480px;
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

.product-detail-right {
  flex: 1;
}

.product-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  line-height: 1.4;
}

.product-price-large {
  font-size: 32px;
  font-weight: 700;
  color: #f5222d;
  margin-bottom: 24px;
}

.product-info-card {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  padding: 8px 0;
}

.info-label {
  width: 100px;
  color: #666;
  font-size: 14px;
}

.info-value {
  color: #333;
  font-size: 14px;
}

.product-description {
  margin-bottom: 32px;
}

.description-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.description-text {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
}

.product-actions {
  display: flex;
  gap: 16px;
}

.btn-large {
  height: 48px;
  font-size: 16px;
  padding: 0 32px;
}

/* 空状态和加载 */
.empty-message {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 16px;
}
</style>