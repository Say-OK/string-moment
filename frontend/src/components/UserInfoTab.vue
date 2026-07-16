<template>
  <div class="user-info-tab">
    <h1 class="page-title">用户信息</h1>

    <!-- 头像区域 -->
    <div class="avatar-section">
      <div class="avatar-display">
        <img :src="userInfo.avatar || '/default-avatar.jpg'" alt="头像" class="avatar-image" />
      </div>
      <div class="avatar-upload">
        <button class="btn btn-secondary" @click="triggerUpload">更换头像</button>
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleUpload"
        />
      </div>
    </div>

    <!-- 用户信息（只读展示） -->
    <div class="info-display">
      <div class="info-item">
        <label class="info-label">用户名：</label>
        <span class="info-value">{{ userInfo.username }}</span>
      </div>
      <div class="info-item">
        <label class="info-label">昵称：</label>
        <span class="info-value">{{ userInfo.nickname || '未设置' }}</span>
      </div>
      <div class="info-item">
        <label class="info-label">手机号：</label>
        <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
      </div>
      <div class="info-item">
        <label class="info-label">注册时间：</label>
        <span class="info-value">{{ userInfo.createTime }}</span>
      </div>
      <button class="btn btn-primary edit-btn" @click="openEditModal">编辑信息</button>
    </div>

    <!-- 编辑信息模态框 -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <h2 class="modal-title">编辑用户信息</h2>

        <div class="form-group">
          <label class="form-label">昵称：</label>
          <input v-model="editForm.nickname" class="form-input" placeholder="请输入昵称" />
        </div>

        <div class="form-group">
          <label class="form-label">手机号：</label>
          <input v-model="editForm.phone" class="form-input" placeholder="请输入手机号" />
        </div>

        <div class="modal-actions">
          <button class="btn btn-primary" @click="handleEdit">保存</button>
          <button class="btn btn-secondary" @click="closeEditModal">取消</button>
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
import {onMounted, ref} from 'vue'
import {getUserInfo, updateUserInfo} from '../api/user'
import {uploadAvatar} from '../api/file'

const userInfo = ref({})
const showEditModal = ref(false)
const editForm = ref({
  nickname: '',
  phone: ''
})
const message = ref('')
const messageType = ref('')
const fileInput = ref(null)

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
  } catch (error) {
    console.error('加载用户信息失败:', error)
    showMessage('加载用户信息失败', 'error')
  }
}

// 触发文件选择
const triggerUpload = () => {
  fileInput.value.click()
}

// 上传头像
const handleUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    showMessage('只能上传图片文件', 'error')
    return
  }

  // 检查文件大小（10MB）
  if (file.size > 10 * 1024 * 1024) {
    showMessage('图片大小不能超过10MB', 'error')
    return
  }

  try {
    const res = await uploadAvatar(file)
    const avatarUrl = res.data

    // 更新用户头像
    await updateUserInfo({ avatar: avatarUrl })
    userInfo.value.avatar = avatarUrl
    showMessage('头像更新成功', 'success')
  } catch (error) {
    console.error('上传头像失败:', error)
    showMessage(error.message || '上传头像失败', 'error')
  }

  // 清空文件输入
  event.target.value = ''
}

// 打开编辑模态框
const openEditModal = () => {
  editForm.value.nickname = userInfo.value.nickname || ''
  editForm.value.phone = userInfo.value.phone || ''
  showEditModal.value = true
}

// 关闭编辑模态框
const closeEditModal = () => {
  showEditModal.value = false
}

// 编辑用户信息
const handleEdit = async () => {
  try {
    await updateUserInfo(editForm.value)
    userInfo.value.nickname = editForm.value.nickname
    userInfo.value.phone = editForm.value.phone
    closeEditModal()
    showMessage('用户信息更新成功', 'success')
  } catch (error) {
    console.error('更新用户信息失败:', error)
    showMessage(error.message || '更新用户信息失败', 'error')
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
  loadUserInfo()
})
</script>

<style scoped>
.user-info-tab {
  max-width: 600px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
}

.avatar-display {
  width: 160px;
  height: 160px;
  border-radius: 80px;
  overflow: hidden;
  border: 2px solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload button {
  height: 40px;
}

/* 信息展示 */
.info-display {
  background: #f9f9f9;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.info-item:last-of-type {
  margin-bottom: 24px;
}

.info-label {
  width: 100px;
  font-size: 14px;
  color: #666;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.edit-btn {
  width: 100%;
  height: 40px;
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
  max-width: 500px;
  width: 90%;
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