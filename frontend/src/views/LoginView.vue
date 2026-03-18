<template>
  <div class="login-scene">
    <div class="login-grid">
      <section class="showcase float-slow">
        <span class="showcase-tag">Campus Console</span>
        <h1>转转管理端</h1>

        <div class="showcase-metrics">
          <article>
            <span>安全审查</span>
            <strong>24h</strong>
          </article>
          <article>
            <span>在线处理</span>
            <strong>实时</strong>
          </article>
          <article>
            <span>数据同步</span>
            <strong>稳定</strong>
          </article>
        </div>
      </section>

      <section class="login-card app-card fade-in-up">
        <header class="card-head">
          <img class="brand-logo" src="/logo.jpg" alt="转转 logo" />
          <div>
            <h2>管理员登录</h2>
            <p>Admin Access</p>
          </div>
        </header>

        <form class="login-form" @submit.prevent="handleLogin">
          <label>
            <span>账号</span>
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
    ElMessage.warning('账号和密码不能为空')
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
.login-scene {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 28px;
}

.login-grid {
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: 1fr minmax(360px, 430px);
  gap: 22px;
  align-items: stretch;
}

.showcase {
  position: relative;
  border-radius: 26px;
  border: 1px solid rgba(255, 255, 255, 0.78);
  padding: 32px;
  background:
    radial-gradient(260px 140px at 84% 22%, rgba(20, 184, 166, 0.24) 0%, rgba(20, 184, 166, 0) 75%),
    radial-gradient(320px 170px at 10% 90%, rgba(47, 139, 255, 0.24) 0%, rgba(47, 139, 255, 0) 74%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.86) 0%, rgba(241, 249, 255, 0.88) 100%);
  box-shadow: var(--shadow-soft);
  overflow: hidden;
}

.showcase::after {
  content: '';
  position: absolute;
  width: 220px;
  height: 220px;
  border-radius: 999px;
  right: -90px;
  bottom: -90px;
  background: radial-gradient(circle at center, rgba(255, 175, 113, 0.4) 0%, rgba(255, 175, 113, 0) 72%);
}

.showcase-tag {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid #cfe2fb;
  background: #edf5ff;
  color: #2d5c9a;
  font-family: 'Lexend', sans-serif;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.showcase h1 {
  margin: 18px 0 0;
  font-size: clamp(34px, 4vw, 52px);
  line-height: 1.05;
  color: #22426e;
}

.showcase-metrics {
  margin-top: 28px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.showcase-metrics article {
  border-radius: 14px;
  border: 1px solid #d6e7fa;
  padding: 12px;
  background: rgba(255, 255, 255, 0.8);
  display: grid;
  gap: 4px;
}

.showcase-metrics span {
  color: var(--text-secondary);
  font-size: 12px;
}

.showcase-metrics strong {
  font-family: 'Lexend', sans-serif;
  font-size: 22px;
  color: #1f4677;
}

.login-card {
  padding: 26px 24px;
  display: grid;
  align-content: center;
  gap: 18px;
}

.card-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  object-fit: cover;
  border: 2px solid #ffffff;
  box-shadow: 0 8px 14px rgba(50, 108, 182, 0.2);
}

.card-head h2 {
  margin: 0;
  font-size: 24px;
  color: #22426f;
}

.card-head p {
  margin: 3px 0 0;
  font-family: 'Lexend', sans-serif;
  color: var(--text-light);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.login-form {
  display: grid;
  gap: 14px;
}

.login-form label {
  display: grid;
  gap: 7px;
}

.login-form span {
  font-size: 13px;
  color: #49648d;
  font-weight: 600;
}

.submit-btn {
  margin-top: 6px;
  min-height: 44px;
  font-size: 14px;
}

@media (max-width: 960px) {
  .login-scene {
    padding: 16px;
  }

  .login-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .showcase {
    padding: 22px;
  }

  .showcase h1 {
    font-size: 38px;
  }
}

@media (max-width: 620px) {
  .showcase-metrics {
    grid-template-columns: 1fr;
  }

  .login-card {
    padding: 20px 18px;
  }
}
</style>
