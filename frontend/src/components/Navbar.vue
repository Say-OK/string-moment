<template>
  <nav class="navbar">
    <router-link to="/products" class="nav-brand">String Moment</router-link>
    <div class="nav-links">
      <router-link to="/products" class="nav-link">商品首页</router-link>
      <router-link to="/seckill" class="nav-link" v-if="isLoggedIn">秒杀活动</router-link>
      <router-link to="/orders" class="nav-link" v-if="isLoggedIn">我的订单</router-link>
      <router-link to="/user" class="nav-link" v-if="isLoggedIn">个人中心</router-link>
      <a href="#" class="nav-link" @click.prevent="handleLogout" v-if="isLoggedIn">退出登录</a>
    </div>
  </nav>
</template>

<script setup>
import {computed} from 'vue'
import {useRouter} from 'vue-router'

const router = useRouter()

const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>