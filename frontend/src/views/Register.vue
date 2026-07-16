<template>
  <div class="page-container">
    <div class="form-container">
      <h2 class="form-title">用户注册</h2>

      <div v-if="errorMessage" class="message message-error">
        {{ errorMessage }}
      </div>

      <div v-if="successMessage" class="message message-success">
        {{ successMessage }}
      </div>

      <form @submit.prevent="handleRegister">
        <div class="form-item">
          <label class="form-label">用户名 <span style="color: #f5222d;">*</span></label>
          <input
            v-model="form.username"
            type="text"
            class="form-input"
            placeholder="4-20位字母或数字"
            :class="{ error: errors.username }"
          />
          <div v-if="errors.username" class="error-message">{{ errors.username }}</div>
        </div>

        <div class="form-item">
          <label class="form-label">密码 <span style="color: #f5222d;">*</span></label>
          <input
            v-model="form.password"
            type="password"
            class="form-input"
            placeholder="6-20位字符"
            :class="{ error: errors.password }"
          />
          <div v-if="errors.password" class="error-message">{{ errors.password }}</div>
        </div>

        <div class="form-item">
          <label class="form-label">确认密码 <span style="color: #f5222d;">*</span></label>
          <input
            v-model="form.confirmPassword"
            type="password"
            class="form-input"
            placeholder="再次输入密码"
            :class="{ error: errors.confirmPassword }"
          />
          <div v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</div>
        </div>

        <div class="form-item">
          <label class="form-label">昵称</label>
          <input
            v-model="form.nickname"
            type="text"
            class="form-input"
            placeholder="2-20位字符（可选）"
            :class="{ error: errors.nickname }"
          />
          <div v-if="errors.nickname" class="error-message">{{ errors.nickname }}</div>
        </div>

        <div class="form-item">
          <label class="form-label">手机号</label>
          <input
            v-model="form.phone"
            type="text"
            class="form-input"
            placeholder="11位手机号（可选）"
            :class="{ error: errors.phone }"
          />
          <div v-if="errors.phone" class="error-message">{{ errors.phone }}</div>
        </div>

        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="divider">
        <span>已有账号？</span>
      </div>

      <router-link to="/login" class="btn btn-secondary" style="display: block; text-align: center; line-height: 40px;">
        立即登录
      </router-link>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {register} from '../api/user'

const router = useRouter()

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: ''
})

const errors = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: ''
})

const errorMessage = ref('')
const successMessage = ref('')
const loading = ref(false)

const validateForm = () => {
  let isValid = true

  // 用户名验证
  if (!form.username.trim()) {
    errors.username = '用户名不能为空'
    isValid = false
  } else if (form.username.length < 4 || form.username.length > 20) {
    errors.username = '用户名长度必须在4-20位之间'
    isValid = false
  } else if (!/^[a-zA-Z0-9]+$/.test(form.username)) {
    errors.username = '用户名只能包含字母和数字'
    isValid = false
  } else {
    errors.username = ''
  }

  // 密码验证
  if (!form.password) {
    errors.password = '密码不能为空'
    isValid = false
  } else if (form.password.length < 6 || form.password.length > 20) {
    errors.password = '密码长度必须在6-20位之间'
    isValid = false
  } else {
    errors.password = ''
  }

  // 确认密码验证
  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  } else {
    errors.confirmPassword = ''
  }

  // 昵称验证（可选）
  if (form.nickname && (form.nickname.length < 2 || form.nickname.length > 20)) {
    errors.nickname = '昵称长度必须在2-20位之间'
    isValid = false
  } else {
    errors.nickname = ''
  }

  // 手机号验证（可选）
  if (form.phone && !/^1[3-9]\d{9}$/.test(form.phone)) {
    errors.phone = '手机号格式不正确'
    isValid = false
  } else {
    errors.phone = ''
  }

  return isValid
}

const handleRegister = async () => {
  if (!validateForm()) return

  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const data = {
      username: form.username,
      password: form.password
    }

    if (form.nickname) data.nickname = form.nickname
    if (form.phone) data.phone = form.phone

    await register(data)

    successMessage.value = '注册成功！即将跳转到登录页面...'

    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    errorMessage.value = error.message || '注册失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>