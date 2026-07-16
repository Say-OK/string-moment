<template>
  <div class="admin-login-page">
    <div class="admin-login-container">
      <div class="admin-login-header">
        <h1 class="logo">StringMoment</h1>
        <h2>管理员登录</h2>
        <p>后台管理系统</p>
      </div>

      <div v-if="errorMessage" class="admin-message-error">
        {{ errorMessage }}
      </div>

      <form @submit.prevent="handleLogin" class="admin-login-form">
        <div class="admin-form-item">
          <label>用户名</label>
          <input
            v-model="form.username"
            type="text"
            placeholder="请输入管理员用户名"
            :class="{ 'admin-input-error': errors.username }"
          />
          <div v-if="errors.username" class="admin-error-text">{{ errors.username }}</div>
        </div>

        <div class="admin-form-item">
          <label>密码</label>
          <input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :class="{ 'admin-input-error': errors.password }"
          />
          <div v-if="errors.password" class="admin-error-text">{{ errors.password }}</div>
        </div>

        <button type="submit" class="admin-btn-primary" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <router-link to="/" class="admin-back-link">
          返回用户端
        </router-link>
      </form>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {adminLogin} from '../api/admin'

const router = useRouter()

const form = reactive({
  username: '',
  password: ''
})

const errors = reactive({
  username: '',
  password: ''
})

const errorMessage = ref('')
const loading = ref(false)

const validateForm = () => {
  let isValid = true

  if (!form.username.trim()) {
    errors.username = '用户名不能为空'
    isValid = false
  } else {
    errors.username = ''
  }

  if (!form.password) {
    errors.password = '密码不能为空'
    isValid = false
  } else {
    errors.password = ''
  }

  return isValid
}

const handleLogin = async () => {
  if (!validateForm()) return

  loading.value = true
  errorMessage.value = ''

  try {
    const res = await adminLogin({
      username: form.username,
      password: form.password
    })

    // 保存管理员token和用户信息（使用不同的key）
    localStorage.setItem('adminToken', res.data.token)
    localStorage.setItem('adminUser', JSON.stringify(res.data.user))

    // 跳转到管理员后台
    router.push('/admin/product')
  } catch (error) {
    errorMessage.value = error.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.admin-login-container {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.admin-login-header {
  text-align: center;
  margin-bottom: 30px;
}

.admin-login-header .logo {
  color: #4a90e2;
  font-size: 32px;
  font-weight: bold;
  margin: 0 0 16px 0;
}

.admin-login-header h2 {
  color: #333;
  font-size: 24px;
  margin: 0 0 8px 0;
}

.admin-login-header p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.admin-login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.admin-form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.admin-form-item label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.admin-form-item input {
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.admin-form-item input:focus {
  outline: none;
  border-color: #4a90e2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
}

.admin-form-item input.admin-input-error {
  border-color: #e74c3c;
}

.admin-error-text {
  color: #e74c3c;
  font-size: 12px;
}

.admin-message-error {
  background: #fee;
  color: #c0392b;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
}

.admin-btn-primary {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 14px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s, transform 0.2s;
}

.admin-btn-primary:hover:not(:disabled) {
  background: #357abd;
  transform: translateY(-1px);
}

.admin-btn-primary:disabled {
  background: #b0c4de;
  cursor: not-allowed;
}

.admin-back-link {
  display: block;
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-top: 16px;
  text-decoration: none;
  transition: color 0.3s;
}

.admin-back-link:hover {
  color: #4a90e2;
}
</style>