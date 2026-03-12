<template>
  <div class="forbidden-page app-card fade-in-up">
    <h1>无权限访问</h1>
    <p>当前账号角色不允许进入管理员后台，请使用管理员账号登录。</p>
    <button class="app-btn primary" @click="goLogin">返回登录</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goLogin() {
  router.replace({
    name: 'login',
  })
}
</script>

<style scoped>
.forbidden-page {
  min-height: 100vh;
  display: grid;
  place-content: center;
  gap: 12px;
  text-align: center;
  padding: 28px;
}

.forbidden-page h1 {
  margin: 0;
  font-size: 36px;
  color: #5e3a1d;
}

.forbidden-page p {
  color: var(--text-secondary);
}

.forbidden-page button {
  justify-self: center;
}
</style>
