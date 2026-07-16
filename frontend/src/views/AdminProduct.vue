<template>
  <div class="admin-page">
    <!-- 成功/错误消息 -->
    <div v-if="successMessage" :class="['admin-success-message', isError ? 'error' : '']">
      {{ successMessage }}
    </div>

    <div class="admin-page-header">
      <h2>商品管理</h2>
      <button @click="openAddModal" class="admin-btn-primary">添加商品</button>
    </div>

    <!-- 搜索筛选区 -->
    <div class="admin-filter-bar">
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="搜索商品名称"
        class="admin-input"
        @keyup.enter="handleSearch"
      />
      <select v-model="filterCategory" class="admin-select">
        <option value="">全部分类</option>
        <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
      </select>
      <select v-model="filterStatus" class="admin-select">
        <option :value="null">全部状态</option>
        <option :value="1">上架</option>
        <option :value="0">下架</option>
      </select>
      <button @click="handleSearch" class="admin-btn-secondary">搜索</button>
      <button @click="resetFilters" class="admin-btn-default">重置</button>
    </div>

    <!-- 商品列表 -->
    <div class="admin-table-container">
      <table class="admin-table">
        <thead>
          <tr>
            <th>商品ID</th>
            <th>商品图片</th>
            <th>商品名称</th>
            <th>分类</th>
            <th>价格</th>
            <th>库存</th>
            <th>销量</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="10" class="admin-loading">加载中...</td>
          </tr>
          <tr v-else-if="productList.length === 0">
            <td colspan="10" class="admin-empty">暂无数据</td>
          </tr>
          <tr v-else v-for="product in productList" :key="product.id">
            <td>{{ product.id }}</td>
            <td>
              <img
                v-if="product.imageUrl"
                :src="product.imageUrl"
                :alt="product.name"
                class="admin-product-img"
                @error="handleImgError"
              />
              <span v-else class="admin-no-img">无图片</span>
            </td>
            <td class="admin-text-ellipsis" :title="product.name">{{ product.name }}</td>
            <td>{{ product.category }}</td>
            <td>¥{{ product.price }}</td>
            <td>{{ product.stock }}</td>
            <td>{{ product.saleCount }}</td>
            <td>
              <span :class="['admin-status-tag', product.status === 1 ? 'status-on' : 'status-off']">
                {{ product.status === 1 ? '上架' : '下架' }}
              </span>
            </td>
            <td>{{ product.createTime }}</td>
            <td>
              <div class="admin-actions">
                <button @click="viewDetail(product)" class="admin-btn-link">详情</button>
                <button
                  @click="openEditModal(product)"
                  class="admin-btn-link"
                  :disabled="product.status === 1"
                  :class="{ 'disabled-btn': product.status === 1 }"
                >
                  编辑
                </button>
                <button
                  v-if="product.status === 1"
                  @click="handleOffProduct(product)"
                  class="admin-btn-link warning"
                >
                  下架
                </button>
                <button
                  v-else
                  @click="handleOnProduct(product)"
                  class="admin-btn-link success"
                >
                  上架
                </button>
                <button @click="handleDelete(product)" class="admin-btn-link danger">删除</button>
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

    <!-- 添加/编辑商品模态框 -->
    <div v-if="showModal" class="admin-modal-overlay" @click.self="closeModal">
      <div class="admin-modal">
        <div class="admin-modal-header">
          <h3>{{ isEdit ? '编辑商品' : '添加商品' }}</h3>
          <button @click="closeModal" class="admin-modal-close">&times;</button>
        </div>
        <form @submit.prevent="handleSubmit" class="admin-modal-body">
          <div class="admin-form-row">
            <div class="admin-form-item">
              <label>商品名称 <span class="required">*</span></label>
              <input v-model="formData.name" type="text" placeholder="请输入商品名称" />
            </div>
            <div class="admin-form-item">
              <label>商品分类 <span class="required">*</span></label>
              <input v-model="formData.category" type="text" placeholder="请输入商品分类" />
            </div>
          </div>
          <div class="admin-form-row">
            <div class="admin-form-item">
              <label>商品价格 <span class="required">*</span></label>
              <input v-model.number="formData.price" type="number" step="0.01" placeholder="请输入价格" />
            </div>
            <div class="admin-form-item">
              <label>商品库存 <span class="required">*</span></label>
              <input v-model.number="formData.stock" type="number" placeholder="请输入库存" />
            </div>
          </div>
          <div class="admin-form-item">
            <label>商品图片</label>
            <div class="image-upload-container">
              <div class="image-preview">
                <img
                  v-if="formData.imageUrl"
                  :src="formData.imageUrl"
                  :alt="formData.name || '商品图片'"
                  class="preview-image"
                  @error="handleImgError"
                />
                <div v-else class="no-image-placeholder">
                  <span>暂无图片</span>
                </div>
              </div>
              <div class="upload-actions">
                <button type="button" @click="triggerProductImageUpload" class="admin-btn-upload">
                  选择图片
                </button>
                <input
                  ref="productImageInput"
                  type="file"
                  accept="image/*"
                  style="display: none"
                  @change="handleProductImageUpload"
                />
                <span v-if="uploadingImage" class="upload-progress">上传中...</span>
                <span v-else-if="formData.imageUrl" class="upload-success">已上传</span>
              </div>
              <div class="image-url-display">
                <input v-model="formData.imageUrl" type="text" placeholder="图片URL（上传后自动填充）" readonly />
              </div>
            </div>
          </div>
          <div class="admin-form-item">
            <label>商品描述</label>
            <textarea v-model="formData.description" placeholder="请输入商品描述" rows="4"></textarea>
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

    <!-- 商品详情模态框 -->
    <div v-if="showDetail" class="admin-modal-overlay" @click.self="showDetail = false">
      <div class="admin-modal">
        <div class="admin-modal-header">
          <h3>商品详情</h3>
          <button @click="showDetail = false" class="admin-modal-close">&times;</button>
        </div>
        <div class="admin-modal-body" v-if="detailProduct">
          <div class="admin-detail-row">
            <span class="admin-detail-label">商品ID:</span>
            <span>{{ detailProduct.id }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">商品名称:</span>
            <span>{{ detailProduct.name }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">商品分类:</span>
            <span>{{ detailProduct.category }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">商品价格:</span>
            <span class="admin-price">¥{{ detailProduct.price }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">商品库存:</span>
            <span>{{ detailProduct.stock }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">销售数量:</span>
            <span>{{ detailProduct.saleCount }}</span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">商品状态:</span>
            <span :class="['admin-status-tag', detailProduct.status === 1 ? 'status-on' : 'status-off']">
              {{ detailProduct.status === 1 ? '上架' : '下架' }}
            </span>
          </div>
          <div class="admin-detail-row">
            <span class="admin-detail-label">创建时间:</span>
            <span>{{ detailProduct.createTime }}</span>
          </div>
          <div class="admin-detail-row" v-if="detailProduct.imageUrl">
            <span class="admin-detail-label">商品图片:</span>
            <img :src="detailProduct.imageUrl" :alt="detailProduct.name" class="admin-detail-img" @error="handleImgError" />
          </div>
          <div class="admin-detail-row" v-if="detailProduct.description">
            <span class="admin-detail-label">商品描述:</span>
            <span>{{ detailProduct.description }}</span>
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
  addProduct,
  deleteProduct,
  getAdminCategories,
  getAdminProductDetail,
  getAdminProductList,
  offProduct,
  onProduct,
  updateProduct,
  uploadProductImage
} from '../api/admin'

// 列表数据
const productList = ref([])
const categories = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const pages = ref(0)

// 筛选条件
const searchKeyword = ref('')
const filterCategory = ref('')
const filterStatus = ref(null)

// 模态框
const showModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const modalError = ref('')
const editProductId = ref(null)

// 商品详情
const showDetail = ref(false)
const detailProduct = ref(null)

// 图片上传相关
const productImageInput = ref(null)
const uploadingImage = ref(false)

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
  category: '',
  price: null,
  stock: null,
  imageUrl: '',
  description: ''
})

// 获取商品列表
const fetchProductList = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value || undefined,
      category: filterCategory.value || undefined,
      status: filterStatus.value
    }
    const res = await getAdminProductList(params)
    productList.value = res.data.list || []
    total.value = res.data.total
    pages.value = res.data.pages
  } catch (error) {
    showSuccess(error.message || '获取商品列表失败', true)
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getAdminCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

// 搜索
const handleSearch = () => {
  page.value = 1
  fetchProductList()
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  filterCategory.value = ''
  filterStatus.value = null
  page.value = 1
  fetchProductList()
}

// 分页
const changePage = (newPage) => {
  page.value = newPage
  fetchProductList()
}

// 打开添加模态框
const openAddModal = () => {
  isEdit.value = false
  editProductId.value = null
  modalError.value = ''
  Object.assign(formData, {
    name: '',
    category: '',
    price: null,
    stock: null,
    imageUrl: '',
    description: ''
  })
  showModal.value = true
}

// 打开编辑模态框
const openEditModal = (product) => {
  isEdit.value = true
  editProductId.value = product.id
  modalError.value = ''
  Object.assign(formData, {
    name: product.name,
    category: product.category,
    price: product.price,
    stock: product.stock,
    imageUrl: product.imageUrl || '',
    description: product.description || ''
  })
  showModal.value = true
}

// 关闭模态框
const closeModal = () => {
  showModal.value = false
  modalError.value = ''
}

// 验证表单
const validateForm = () => {
  if (!formData.name?.trim()) {
    modalError.value = '请输入商品名称'
    return false
  }
  if (!formData.category?.trim()) {
    modalError.value = '请输入商品分类'
    return false
  }
  if (formData.price === null || formData.price <= 0) {
    modalError.value = '请输入有效的商品价格'
    return false
  }
  if (formData.stock === null || formData.stock < 0) {
    modalError.value = '请输入有效的商品库存'
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
      category: formData.category.trim(),
      price: formData.price,
      stock: formData.stock,
      imageUrl: formData.imageUrl?.trim() || undefined,
      description: formData.description?.trim() || undefined
    }

    if (isEdit.value) {
      await updateProduct(editProductId.value, data)
      showSuccess('商品更新成功')
    } else {
      await addProduct(data)
      showSuccess('商品添加成功')
    }

    closeModal()
    fetchProductList()
  } catch (error) {
    modalError.value = error.message || '操作失败'
  } finally {
    submitting.value = false
  }
}

// 查看详情
const viewDetail = async (product) => {
  try {
    const res = await getAdminProductDetail(product.id)
    detailProduct.value = res.data
    showDetail.value = true
  } catch (error) {
    showSuccess(error.message || '获取详情失败', true)
  }
}

// 上架商品
const handleOnProduct = async (product) => {
  openConfirmModal(
    '确认上架',
    `确定要上架商品"${product.name}"吗？`,
    async () => {
      await onProduct(product.id)
      showSuccess('商品上架成功')
    },
    product
  )
}

// 下架商品
const handleOffProduct = async (product) => {
  openConfirmModal(
    '确认下架',
    `确定要下架商品"${product.name}"吗？`,
    async () => {
      await offProduct(product.id)
      showSuccess('商品下架成功')
    },
    product
  )
}

// 删除商品
const handleDelete = async (product) => {
  openConfirmModal(
    '确认删除',
    `确定要删除商品"${product.name}"吗？`,
    async () => {
      try {
        await deleteProduct(product.id)
        showSuccess('商品删除成功')
      } catch (error) {
        // 显示具体拒绝原因
        showSuccess(error.message || '删除失败', true)
      }
    },
    product
  )
}

// 图片加载失败处理
const handleImgError = (e) => {
  e.target.style.display = 'none'
}

// 触发图片上传
const triggerProductImageUpload = () => {
  productImageInput.value.click()
}

// 处理图片上传
const handleProductImageUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    modalError.value = '只能上传图片文件'
    return
  }

  // 检查文件大小（10MB）
  if (file.size > 10 * 1024 * 1024) {
    modalError.value = '图片大小不能超过10MB'
    return
  }

  uploadingImage.value = true
  modalError.value = ''

  try {
    const res = await uploadProductImage(file)
    formData.imageUrl = res.data
  } catch (error) {
    modalError.value = error.message || '上传图片失败'
  } finally {
    uploadingImage.value = false
  }

  // 清空文件输入
  event.target.value = ''
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
    fetchProductList()
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
  fetchProductList()
  fetchCategories()
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
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
.admin-form-item textarea,
.admin-form-item select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.admin-form-item input:focus,
.admin-form-item textarea:focus,
.admin-form-item select:focus {
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

.admin-price {
  color: #ff4d4f;
  font-weight: 600;
}

.admin-detail-img {
  max-width: 200px;
  border-radius: 4px;
}

/* 图片上传组件样式 */
.image-upload-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.image-preview {
  width: 120px;
  height: 120px;
  border: 2px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9f9f9;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image-placeholder {
  color: #999;
  font-size: 12px;
}

.upload-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-btn-upload {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.admin-btn-upload:hover {
  background: #357abd;
}

.upload-progress {
  color: #fa8c16;
  font-size: 13px;
}

.upload-success {
  color: #52c41a;
  font-size: 13px;
}

.image-url-display input {
  background: #f5f5f5;
  color: #666;
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