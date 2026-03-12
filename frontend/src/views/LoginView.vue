<template>
  <div class="login-page">
    <section class="login-panel app-card fade-in-up">
      <header class="panel-header">
        <img class="brand-logo" src="/logo.jpg" alt="赚赚 logo" />
        <div>
          <h1>赚赚后台</h1>
          <p>管理员登录</p>
        </div>
      </header>

      <form class="login-form" @submit.prevent="handleLogin">
        <label>
          <span>管理员账号</span>
          <input
            v-model="form.username"
            class="app-input"
            type="text"
            placeholder="请输入管理员账号"
            autocomplete="username"
          />
        </label>

        <label>
          <span>密码</span>
          <input
            v-model="form.password"
            class="app-input"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
        </label>

        <button class="app-btn primary submit-btn" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

function normalize(value) {
  return typeof value === 'string' ? value.trim() : ''
}

async function handleLogin() {
  const username = normalize(form.username)
  const password = normalize(form.password)

  if (!username || !password) {
    ElMessage.warning('管理员账号和密码不能为空')
    return
  }

  loading.value = true

  try {
    const loginResult = await adminLogin({ username, password })

    if (!loginResult?.token) {
      throw new Error('登录成功但未返回 token，请检查后端响应')
    }

    authStore.setLoginInfo(loginResult)
    ElMessage.success('登录成功')

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    router.replace(redirect)
  } catch (error) {
    ElMessage.error(error.message || '登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url('/login-bg.png');
  background-size: 100% 100%;
  background-position: center;
  background-repeat: no-repeat;
}

.login-panel {
  width: min(460px, 100%);
  padding: 28px 24px;
  margin-left: auto;
  margin-right: clamp(20px, 3vw, 64px);
  backdrop-filter: blur(4px);
  background: rgba(255, 251, 243, 0.92);
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-logo {
  width: 68px;
  height: 68px;
  border-radius: 14px;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 8px 18px rgba(170, 112, 27, 0.28);
}

.panel-header h1 {
  font-size: 28px;
  line-height: 1.2;
}

.panel-header p {
  margin-top: 4px;
  color: var(--text-secondary);
}

.login-form {
  margin-top: 20px;
  display: grid;
  gap: 16px;
}

.login-form label {
  display: grid;
  gap: 8px;
}

.login-form span {
  font-weight: 600;
}

.submit-btn {
  margin-top: 8px;
  min-height: 46px;
}

@media (max-width: 960px) {
  .login-page {
    justify-content: flex-start;
    padding: 16px;
    background-size: cover;
    background-position: 62% center;
  }

  .login-panel {
    margin-right: auto;
    padding: 22px 18px;
  }

  .panel-header h1 {
    font-size: 24px;
  }
}
</style>
