<template>
  <div class="page-container">
    <div class="form-container">
      <h2 class="form-title">用户登录</h2>

      <div v-if="errorMessage" class="message message-error">
        {{ errorMessage }}
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-item">
          <label class="form-label">用户名</label>
          <input
            v-model="form.username"
            type="text"
            class="form-input"
            placeholder="请输入用户名"
            :class="{ error: errors.username }"
          />
          <div v-if="errors.username" class="error-message">{{ errors.username }}</div>
        </div>

        <div class="form-item">
          <label class="form-label">密码</label>
          <input
            v-model="form.password"
            type="password"
            class="form-input"
            placeholder="请输入密码"
            :class="{ error: errors.password }"
          />
          <div v-if="errors.password" class="error-message">{{ errors.password }}</div>
        </div>

        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="divider">
        <span>还没有账号？</span>
      </div>

      <router-link to="/register" class="btn btn-secondary" style="display: block; text-align: center; line-height: 40px;">
        立即注册
      </router-link>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {login} from '../api/user'

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
    const res = await login({
      username: form.username,
      password: form.password
    })

    // 保存token和用户信息
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))

    // 跳转到商品首页
    router.push('/products')
  } catch (error) {
    errorMessage.value = error.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>