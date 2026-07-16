<template>
  <div id="app">
    <Navbar v-if="!isAuthPage" />
    <router-view />
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {useRoute} from 'vue-router'
import Navbar from './components/Navbar.vue'

const route = useRoute()

const isAuthPage = computed(() => {
  return route.path === '/login' ||
         route.path === '/register' ||
         route.path.startsWith('/admin')
})
</script>

<style>
/* 已在style.css中定义全局样式 */
</style>