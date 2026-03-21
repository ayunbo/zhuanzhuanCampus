<template>
  <div class="login-page">
    <section class="login-panel app-card fade-in-up">
      <header class="panel-header">
        <img class="brand-logo" src="/logo.jpg" alt="赚赚 logo" />
        <div>
          <h1>赚赚平台</h1>
          <p>{{ activeMode === 'admin' ? '管理员登录' : '用户 / 卖家登录' }}</p>
        </div>
      </header>

      <div class="mode-switch">
        <button class="mode-btn" :class="activeMode === 'admin' ? 'active' : ''" type="button" @click="setMode('admin')">
          管理员
        </button>
        <button class="mode-btn" :class="activeMode === 'user' ? 'active' : ''" type="button" @click="setMode('user')">
          用户 / 卖家
        </button>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <label>
          <span>{{ activeMode === 'admin' ? '管理员账号' : '学号或手机号' }}</span>
          <input
            v-model="form.account"
            class="app-input"
            type="text"
            :placeholder="activeMode === 'admin' ? '请输入管理员账号' : '请输入学号或手机号'"
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

      <p v-if="activeMode === 'user'" class="login-tip">
        卖家同样使用用户入口登录；如果当前账号角色是卖家，登录后会自动跳转到卖家工作台。
      </p>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/admin'
import { userLogin } from '@/api/user'
import { ADMIN_ROLE, SELLER_ROLE } from '@/constants/auth'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const loading = ref(false)
const activeMode = ref(route.query.mode === 'user' ? 'user' : 'admin')

const form = reactive({
  account: '',
  password: '',
})

function normalize(value) {
  return typeof value === 'string' ? value.trim() : ''
}

function setMode(mode) {
  activeMode.value = mode
}

function defaultRedirect(loginResult) {
  if (loginResult?.role === ADMIN_ROLE) {
    return '/dashboard'
  }

  if (loginResult?.role === SELLER_ROLE) {
    return '/market/seller/goods'
  }

  return '/market/goods'
}

async function handleLogin() {
  const account = normalize(form.account)
  const password = normalize(form.password)

  if (!account || !password) {
    ElMessage.warning('账号和密码不能为空')
    return
  }

  loading.value = true

  try {
    const loginResult = activeMode.value === 'admin'
      ? await adminLogin({ username: account, password })
      : await userLogin({ account, password })

    if (!loginResult?.token) {
      throw new Error('登录成功但未返回 token，请检查后端响应')
    }

    authStore.setLoginInfo(loginResult)
    ElMessage.success('登录成功')

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : defaultRedirect(loginResult)
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
  background-image:
    linear-gradient(125deg, rgba(255, 244, 222, 0.72) 0%, rgba(255, 240, 211, 0.48) 100%),
    url('/login-bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.login-panel {
  width: min(460px, 100%);
  padding: 30px 24px;
  margin-left: auto;
  margin-right: clamp(20px, 3vw, 64px);
  border: 1px solid #f0d4af;
  backdrop-filter: blur(5px);
  background: rgba(255, 250, 241, 0.93);
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
  margin: 0;
  font-size: 28px;
  line-height: 1.2;
  color: #5e3b1e;
}

.panel-header p {
  margin-top: 4px;
  color: var(--text-secondary);
}

.mode-switch {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  padding: 6px;
  border-radius: 14px;
  background: #fff2e0;
  border: 1px solid #f0d4af;
}

.mode-btn {
  min-height: 40px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: #7a5635;
  font-weight: 700;
  cursor: pointer;
}

.mode-btn.active {
  color: #fff;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-deep) 100%);
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
  color: #6e4b2a;
}

.submit-btn {
  margin-top: 8px;
  min-height: 46px;
}

.login-tip {
  margin: 16px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
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
