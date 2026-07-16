<template>
  <div class="address-tab">
    <h1 class="page-title">收货地址</h1>

    <!-- 新建地址按钮 -->
    <div class="action-bar">
      <button class="btn btn-primary" @click="openAddModal">新建地址</button>
    </div>

    <!-- 地址列表 -->
    <div v-if="addresses.length === 0" class="empty-message">
      暂无收货地址，请点击"新建地址"添加
    </div>

    <div v-else class="address-list">
      <!-- 默认地址（不同颜色标识） -->
      <div
        v-for="address in sortedAddresses"
        :key="address.id"
        class="address-card"
        :class="{ 'default-address': address.isDefault === 1 }"
      >
        <!-- 右上角设为默认按钮 -->
        <div class="address-top-right">
          <span v-if="address.isDefault === 1" class="default-badge">默认地址</span>
          <button
            v-if="address.isDefault !== 1"
            class="btn btn-outline btn-small"
            @click="handleSetDefault(address.id)"
          >
            设为默认
          </button>
        </div>

        <div class="address-header">
          <span class="address-name">{{ address.receiverName }}</span>
          <span class="address-phone">{{ address.receiverPhone }}</span>
        </div>
        <div class="address-detail">
          {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
        </div>
        <div class="address-actions">
          <button class="btn btn-secondary btn-small" @click="openEditModal(address)">修改</button>
          <button class="btn btn-secondary btn-small" @click="handleDelete(address.id)">删除</button>
        </div>
      </div>
    </div>

    <!-- 新建/编辑地址模态框 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <h2 class="modal-title">{{ modalTitle }}</h2>

        <div class="form-group">
          <label class="form-label">收货人姓名：</label>
          <input v-model="addressForm.receiverName" class="form-input" placeholder="请输入收货人姓名" />
        </div>

        <div class="form-group">
          <label class="form-label">手机号：</label>
          <input v-model="addressForm.receiverPhone" class="form-input" placeholder="请输入手机号" />
        </div>

        <div class="form-group">
          <label class="form-label">省份：</label>
          <input v-model="addressForm.province" class="form-input" placeholder="请输入省份" />
        </div>

        <div class="form-group">
          <label class="form-label">城市：</label>
          <input v-model="addressForm.city" class="form-input" placeholder="请输入城市" />
        </div>

        <div class="form-group">
          <label class="form-label">区县：</label>
          <input v-model="addressForm.district" class="form-input" placeholder="请输入区县" />
        </div>

        <div class="form-group">
          <label class="form-label">详细地址：</label>
          <input v-model="addressForm.detailAddress" class="form-input" placeholder="请输入详细地址" />
        </div>

        <div class="modal-actions">
          <button class="btn btn-primary" @click="handleSave">保存</button>
          <button class="btn btn-secondary" @click="closeModal">取消</button>
        </div>
      </div>
    </div>

    <!-- 删除确认模态框 -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click="closeDeleteConfirm">
      <div class="modal-content delete-confirm-modal" @click.stop>
        <h2 class="modal-title">确认删除</h2>
        <p class="delete-message">确定要删除这个地址吗？删除后将无法恢复。</p>
        <div class="modal-actions">
          <button class="btn btn-danger" @click="confirmDelete">确认删除</button>
          <button class="btn btn-secondary" @click="closeDeleteConfirm">取消</button>
        </div>
      </div>
    </div>

    <!-- 成功/错误消息 -->
    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {addAddress, deleteAddress, getAddressList, setDefaultAddress, updateAddress} from '../api/address'

const addresses = ref([])
const showModal = ref(false)
const showDeleteConfirm = ref(false)
const deleteTargetId = ref(null)
const modalTitle = ref('新建地址')
const addressForm = ref({
  id: null,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: ''
})
const message = ref('')
const messageType = ref('')

// 默认地址排序到第一位
const sortedAddresses = computed(() => {
  const defaultAddress = addresses.value.find(addr => addr.isDefault === 1)
  const otherAddresses = addresses.value.filter(addr => addr.isDefault !== 1)

  if (defaultAddress) {
    return [defaultAddress, ...otherAddresses]
  }
  return otherAddresses
})

// 加载地址列表
const loadAddresses = async () => {
  try {
    const res = await getAddressList()
    addresses.value = res.data || []
  } catch (error) {
    console.error('加载地址失败:', error)
    showMessage('加载地址失败', 'error')
  }
}

// 打开新建模态框
const openAddModal = () => {
  modalTitle.value = '新建地址'
  addressForm.value = {
    id: null,
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: ''
  }
  showModal.value = true
}

// 打开编辑模态框
const openEditModal = (address) => {
  modalTitle.value = '修改地址'
  addressForm.value = {
    id: address.id,
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    province: address.province,
    city: address.city,
    district: address.district,
    detailAddress: address.detailAddress
  }
  showModal.value = true
}

// 关闭模态框
const closeModal = () => {
  showModal.value = false
}

// 保存地址
const handleSave = async () => {
  try {
    if (addressForm.value.id) {
      // 编辑
      await updateAddress(addressForm.value)
      showMessage('地址修改成功', 'success')
    } else {
      // 新建
      await addAddress(addressForm.value)
      showMessage('地址添加成功', 'success')
    }
    closeModal()
    loadAddresses()
  } catch (error) {
    console.error('保存地址失败:', error)
    showMessage(error.message || '保存地址失败', 'error')
  }
}

// 删除地址（打开确认模态框）
const handleDelete = (id) => {
  deleteTargetId.value = id
  showDeleteConfirm.value = true
}

// 关闭删除确认模态框
const closeDeleteConfirm = () => {
  showDeleteConfirm.value = false
  deleteTargetId.value = null
}

// 确认删除
const confirmDelete = async () => {
  if (!deleteTargetId.value) return

  try {
    await deleteAddress(deleteTargetId.value)
    showMessage('地址删除成功', 'success')
    closeDeleteConfirm()
    loadAddresses()
  } catch (error) {
    console.error('删除地址失败:', error)
    showMessage(error.message || '删除地址失败', 'error')
  }
}

// 设置默认地址
const handleSetDefault = async (id) => {
  try {
    await setDefaultAddress(id)
    showMessage('默认地址设置成功', 'success')
    loadAddresses()
  } catch (error) {
    console.error('设置默认地址失败:', error)
    showMessage(error.message || '设置默认地址失败', 'error')
  }
}

// 显示消息
const showMessage = (text, type) => {
  message.value = text
  messageType.value = type === 'error' ? 'message-error' : 'message-success'
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

onMounted(() => {
  loadAddresses()
})
</script>

<style scoped>
.address-tab {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

/* 操作栏 */
.action-bar {
  margin-bottom: 16px;
}

/* 空提示 */
.empty-message {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}

/* 地址列表 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-card {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  position: relative;
}

.default-address {
  border: 2px solid #4a90e2;
  background: #e8f4ff;
}

/* 右上角设为默认按钮 */
.address-top-right {
  position: absolute;
  top: 16px;
  right: 16px;
}

.default-badge {
  background: #4a90e2;
  color: #fff;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
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

.address-detail {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.btn-small {
  height: 32px;
  padding: 0 12px;
  font-size: 12px;
}

/* 模态框 */
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

/* 删除确认模态框 */
.delete-confirm-modal {
  max-width: 400px;
  text-align: center;
}

.delete-message {
  font-size: 14px;
  color: #666;
  margin-bottom: 24px;
  line-height: 1.5;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  height: 40px;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-input:focus {
  outline: none;
  border-color: #4a90e2;
}

.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.modal-actions button {
  flex: 1;
  height: 40px;
}

/* 消息提示 */
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

.message-success {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}
</style>