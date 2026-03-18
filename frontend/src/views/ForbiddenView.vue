<template>
  <div class="forbidden-wrap">
    <section class="forbidden-card app-card fade-in-up">
      <span class="code">403</span>
      <h1>无权限访问</h1>
      <button class="app-btn primary" @click="goLogin">返回登录</button>
    </section>
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
.forbidden-wrap {
  min-height: 100vh;
  padding: 20px;
  display: grid;
  place-items: center;
}

.forbidden-card {
  width: min(420px, 100%);
  padding: 34px 24px;
  text-align: center;
  display: grid;
  gap: 12px;
  justify-items: center;
}

.code {
  font-family: 'Lexend', sans-serif;
  font-size: 72px;
  line-height: 1;
  color: #2a7ae7;
  text-shadow: 0 10px 20px rgba(47, 139, 255, 0.24);
}

.forbidden-card h1 {
  margin: 0;
  font-size: 24px;
  color: #264a79;
}
</style>
