<template>
  <div class="page-container">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <label class="filter-label">分类：</label>
          <select v-model="queryParams.category" @change="handleFilter" class="filter-select">
            <option value="">全部</option>
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
        </div>

        <div class="filter-item">
          <label class="filter-label">搜索：</label>
          <input
            v-model="queryParams.keyword"
            type="text"
            class="filter-input"
            placeholder="搜索商品名称"
            @keyup.enter="handleFilter"
          />
        </div>

        <div class="filter-item">
          <label class="filter-label">价格：</label>
          <input
            v-model.number="queryParams.minPrice"
            type="number"
            class="filter-input-small"
            placeholder="最低"
          />
          <span style="margin: 0 8px;">-</span>
          <input
            v-model.number="queryParams.maxPrice"
            type="number"
            class="filter-input-small"
            placeholder="最高"
          />
        </div>

        <button class="btn btn-primary filter-btn" @click="handleFilter">筛选</button>
        <button class="btn btn-secondary filter-btn" @click="resetFilter">重置</button>
      </div>

      <div class="filter-row">
        <div class="filter-item">
          <label class="filter-label">排序：</label>
          <select v-model="queryParams.sortBy" @change="handleFilter" class="filter-select">
            <option value="">默认</option>
            <option value="price">价格</option>
            <option value="sale_count">销量</option>
            <option value="create_time">上架时间</option>
          </select>
          <select v-model="queryParams.sortOrder" @change="handleFilter" class="filter-select" style="margin-left: 8px;">
            <option value="asc">升序</option>
            <option value="desc">降序</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 商品网格 -->
    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="products.length === 0" class="empty-message">
      暂无商品
    </div>

    <div v-else class="product-grid">
      <div
        v-for="product in products"
        :key="product.id"
        class="product-card"
        @click="goToDetail(product.id)"
      >
        <div class="product-image">
          <img :src="product.imageUrl" :alt="product.name" />
        </div>
        <div class="product-info">
          <div class="product-name">{{ product.name }}</div>
          <div class="product-price">¥{{ product.price }}</div>
          <div class="product-meta">
            <span>库存：{{ product.stock }}</span>
            <span>销量：{{ product.saleCount }}</span>
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
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {getCategoryList, getProductList} from '../api/product'

const router = useRouter()

const products = ref([])
const categories = ref([])
const total = ref(0)
const pages = ref(0)
const loading = ref(false)

const queryParams = reactive({
  page: 1,
  size: 12,
  category: '',
  keyword: '',
  minPrice: null,
  maxPrice: null,
  sortBy: '',
  sortOrder: 'desc'
})

// 加载商品列表
const loadProducts = async () => {
  loading.value = true
  try {
    const res = await getProductList(queryParams)
    products.value = res.data.list
    total.value = res.data.total
    pages.value = res.data.pages
  } catch (error) {
    console.error('加载商品失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 筛选
const handleFilter = () => {
  queryParams.page = 1
  loadProducts()
}

// 重置筛选
const resetFilter = () => {
  queryParams.category = ''
  queryParams.keyword = ''
  queryParams.minPrice = null
  queryParams.maxPrice = null
  queryParams.sortBy = ''
  queryParams.sortOrder = 'desc'
  queryParams.page = 1
  loadProducts()
}

// 切换页码
const changePage = (page) => {
  queryParams.page = page
  loadProducts()
}

// 查看详情
const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

onMounted(() => {
  loadCategories()
  loadProducts()
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
  margin-bottom: 12px;
}

.filter-row:last-child {
  margin-bottom: 0;
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

.filter-input {
  width: 200px;
  height: 32px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.filter-input-small {
  width: 80px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.filter-btn {
  height: 32px;
  width: auto;
  padding: 0 16px;
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.product-card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.product-image {
  width: 100%;
  height: 200px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 12px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 18px;
  font-weight: 600;
  color: #f5222d;
  margin-bottom: 6px;
}

.product-meta {
  font-size: 12px;
  color: #999;
  display: flex;
  gap: 8px;
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
}
</style>