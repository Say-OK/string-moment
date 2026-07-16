<template>
  <div class="page-container">
    <div class="form-container" v-if="!isEditing && !isChangingPassword">
      <h2 class="form-title">个人中心</h2>

      <div v-if="errorMessage" class="message message-error">
        {{ errorMessage }}
      </div>

      <div class="card">
        <div class="info-row">
          <span class="info-label">用户名</span>
          <span class="info-value">{{ userInfo.username }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">昵称</span>
          <span class="info-value">{{ userInfo.nickname || '未设置' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">手机号</span>
          <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">角色</span>
          <span class="info-value">{{ userInfo.role === 1 ? '管理员' : '普通用户' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">状态</span>
          <span class="info-value">{{ userInfo.status === 1 ? '正常' : '禁用' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">注册时间</span>
          <span class="info-value">{{ userInfo.createTime }}</span>
        </div>
      </div>

      <button class="btn btn-primary" @click="startEdit" style="margin-bottom: 12px;">
        编辑信息
      </button>
      <button class="btn btn-secondary" @click="startChangePassword">
        修改密码
      </button>
    </div>

    <!-- 编辑信息表单 -->
    <div class="form-container" v-if="isEditing">
      <h2 class="form-title">编辑信息</h2>

      <div v-if="errorMessage" class="message message-error">
        {{ errorMessage }}
      </div>

      <div v-if="successMessage" class="message message-success">
        {{ successMessage }}
      </div>

      <form @submit.prevent="handleUpdate">
        <div class="form-item">
          <label class="form-label">昵称</label>
          <input
            v-model="editForm.nickname"
            type="text"
            class="form-input"
            placeholder="2-20位字符"
            :class="{ error: editErrors.nickname }"
          />
          <div v-if="editErrors.nickname" class="error-message">{{ editErrors.nickname }}</div>
        </div>

        <div class="form-item">
          <label class="form-label">手机号</label>
          <input
            v-model="editForm.phone"
            type="text"
            class="form-input"
            placeholder="11位手机号"
            :class="{ error: editErrors.phone }"
          />
          <div v-if="editErrors.phone" class="error-message">{{ editErrors.phone }}</div>
        </div>

        <div class="form-item">
          <label class="form-label">头像URL</label>
          <input
            v-model="editForm.avatar"
            type="text"
            class="form-input avatar-input"
            placeholder="头像URL地址"
          />
        </div>

        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? '保存中...' : '保存' }}
        </button>
      </form>

      <div class="divider">
        <span>或者</span>
      </div>

      <button class="btn btn-secondary" @click="cancelEdit">
        返回
      </button>
    </div>

    <!-- 修改密码表单 -->
    <div class="form-container" v-if="isChangingPassword">
      <h2 class="form-title">修改密码</h2>

      <div v-if="errorMessage" class="message message-error">
        {{ errorMessage }}
      </div>

      <div v-if="successMessage" class="message message-success">
        {{ successMessage }}
      </div>

      <form @submit.prevent="handleChangePassword">
        <div class="form-item">
          <label class="form-label">旧密码 <span style="color: #f5222d;">*</span></label>
          <input
            v-model="passwordForm.oldPassword"
            type="password"
            class="form-input"
            placeholder="请输入旧密码"
            :class="{ error: passwordErrors.oldPassword }"
          />
          <div v-if="passwordErrors.oldPassword" class="error-message">{{ passwordErrors.oldPassword }}</div>
        </div>

        <div class="form-item">
          <label class="form-label">新密码 <span style="color: #f5222d;">*</span></label>
          <input
            v-model="passwordForm.newPassword"
            type="password"
            class="form-input"
            placeholder="6-20位字符"
            :class="{ error: passwordErrors.newPassword }"
          />
          <div v-if="passwordErrors.newPassword" class="error-message">{{ passwordErrors.newPassword }}</div>
        </div>

        <div class="form-item">
          <label class="form-label">确认新密码 <span style="color: #f5222d;">*</span></label>
          <input
            v-model="passwordForm.confirmPassword"
            type="password"
            class="form-input"
            placeholder="再次输入新密码"
            :class="{ error: passwordErrors.confirmPassword }"
          />
          <div v-if="passwordErrors.confirmPassword" class="error-message">{{ passwordErrors.confirmPassword }}</div>
        </div>

        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? '修改中...' : '修改密码' }}
        </button>
      </form>

      <div class="divider">
        <span>或者</span>
      </div>

      <button class="btn btn-secondary" @click="cancelChangePassword">
        返回
      </button>
    </div>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {getUserInfo, updatePassword, updateUserInfo} from '../api/user'

const router = useRouter()

const userInfo = ref({
  username: '',
  nickname: '',
  phone: '',
  avatar: '',
  role: 0,
  status: 1,
  createTime: ''
})

const isEditing = ref(false)
const isChangingPassword = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const loading = ref(false)

const editForm = reactive({
  nickname: '',
  phone: '',
  avatar: ''
})

const editErrors = reactive({
  nickname: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordErrors = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data

    // 同步到编辑表单
    editForm.nickname = res.data.nickname || ''
    editForm.phone = res.data.phone || ''
    editForm.avatar = res.data.avatar || ''
  } catch (error) {
    errorMessage.value = error.message || '获取用户信息失败'
  }
}

// 开始编辑
const startEdit = () => {
  isEditing.value = true
  errorMessage.value = ''
  successMessage.value = ''
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  errorMessage.value = ''
  successMessage.value = ''
  // 重置表单
  editForm.nickname = userInfo.value.nickname || ''
  editForm.phone = userInfo.value.phone || ''
  editForm.avatar = userInfo.value.avatar || ''
}

// 验证编辑表单
const validateEditForm = () => {
  let isValid = true

  if (editForm.nickname && (editForm.nickname.length < 2 || editForm.nickname.length > 50)) {
    editErrors.nickname = '昵称长度必须在2-50位之间'
    isValid = false
  } else {
    editErrors.nickname = ''
  }

  if (editForm.phone && !/^1[3-9]\d{9}$/.test(editForm.phone)) {
    editErrors.phone = '手机号格式不正确'
    isValid = false
  } else {
    editErrors.phone = ''
  }

  return isValid
}

// 更新用户信息
const handleUpdate = async () => {
  if (!validateEditForm()) return

  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const data = {}
    if (editForm.nickname) data.nickname = editForm.nickname
    if (editForm.phone) data.phone = editForm.phone
    if (editForm.avatar) data.avatar = editForm.avatar

    const res = await updateUserInfo(data)
    userInfo.value = res.data
    successMessage.value = '更新成功！'

    // 更新localStorage
    localStorage.setItem('user', JSON.stringify(res.data))

    setTimeout(() => {
      isEditing.value = false
      successMessage.value = ''
    }, 1500)
  } catch (error) {
    errorMessage.value = error.message || '更新失败，请重试'
  } finally {
    loading.value = false
  }
}

// 开始修改密码
const startChangePassword = () => {
  isChangingPassword.value = true
  errorMessage.value = ''
  successMessage.value = ''
}

// 取消修改密码
const cancelChangePassword = () => {
  isChangingPassword.value = false
  errorMessage.value = ''
  successMessage.value = ''
  // 重置表单
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

// 验证密码表单
const validatePasswordForm = () => {
  let isValid = true

  if (!passwordForm.oldPassword) {
    passwordErrors.oldPassword = '旧密码不能为空'
    isValid = false
  } else {
    passwordErrors.oldPassword = ''
  }

  if (!passwordForm.newPassword) {
    passwordErrors.newPassword = '新密码不能为空'
    isValid = false
  } else if (passwordForm.newPassword.length < 6 || passwordForm.newPassword.length > 20) {
    passwordErrors.newPassword = '新密码长度必须在6-20位之间'
    isValid = false
  } else {
    passwordErrors.newPassword = ''
  }

  if (!passwordForm.confirmPassword) {
    passwordErrors.confirmPassword = '请确认新密码'
    isValid = false
  } else if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordErrors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  } else {
    passwordErrors.confirmPassword = ''
  }

  return isValid
}

// 修改密码
const handleChangePassword = async () => {
  if (!validatePasswordForm()) return

  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    successMessage.value = '密码修改成功！即将跳转到登录页面...'

    setTimeout(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }, 1500)
  } catch (error) {
    errorMessage.value = error.message || '修改密码失败，请重试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
/* 头像输入框加宽并调整位置 */
.avatar-input {
  width: 100%;
  min-width: 350px;
  max-width: 500px;
  margin-left: 20px;
}
</style>