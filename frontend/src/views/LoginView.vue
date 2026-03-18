<template>
  <div class="login-page">
    <section class="promo-pane">
      <div class="promo-overlay promo-overlay-a" />
      <div class="promo-overlay promo-overlay-b" />

      <div class="promo-head">
        <img src="/logo.jpg" alt="转转" class="promo-logo" />
        <div>
          <p class="promo-kicker">CAMPUS TRADE CONSOLE</p>
          <h1>校园二手交易平台管理端</h1>
        </div>
      </div>

      <ul class="promo-tags">
        <li>商品管理</li>
        <li>订单管理</li>
        <li>认证审核</li>
        <li>分类配置</li>
        <li>用户治理</li>
        <li>数据看板</li>
      </ul>

      <div class="promo-illustration">
        <div class="scene-card">
          <div class="scene-top">
            <span />
            <span />
            <span />
          </div>
          <div class="scene-grid">
            <div class="scene-item tall" />
            <div class="scene-item" />
            <div class="scene-item" />
            <div class="scene-item wide" />
          </div>
        </div>
        <div class="scene-chip chip-a">订单</div>
        <div class="scene-chip chip-b">商品</div>
        <div class="scene-chip chip-c">审核</div>
      </div>
    </section>

    <section class="login-pane">
      <article class="login-card fade-in-up">
        <header>
          <h2>账号登录</h2>
          <p>密码登录</p>
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

          <div class="form-meta">
            <label class="remember">
              <input type="checkbox" checked />
              <span>记住登录状态</span>
            </label>
            <a href="#">忘记密码</a>
          </div>

          <button class="login-btn" type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>
      </article>
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
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(420px, 0.9fr) minmax(520px, 1fr);
  background: #f5f6f8;
}

.promo-pane {
  position: relative;
  padding: 42px 44px 36px;
  background:
    radial-gradient(360px 220px at 88% 16%, rgba(126, 169, 239, 0.22) 0%, rgba(126, 169, 239, 0) 70%),
    linear-gradient(165deg, #f6e251 0%, #f3d53b 52%, #efc937 100%);
  overflow: hidden;
  display: grid;
  align-content: start;
  gap: 22px;
}

.promo-overlay {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
}

.promo-overlay-a {
  width: 260px;
  height: 260px;
  right: -90px;
  top: -70px;
  background: rgba(255, 255, 255, 0.22);
}

.promo-overlay-b {
  width: 340px;
  height: 160px;
  left: -80px;
  bottom: 24px;
  background: rgba(255, 255, 255, 0.16);
}

.promo-head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.promo-logo {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  border: 2px solid rgba(255, 255, 255, 0.7);
}

.promo-kicker {
  margin: 2px 0 4px;
  color: rgba(34, 53, 79, 0.68);
  font-size: 12px;
  letter-spacing: 0.12em;
  font-weight: 700;
}

.promo-head h1 {
  margin: 0;
  max-width: 420px;
  font-size: 38px;
  line-height: 1.12;
  color: #25344a;
}

.promo-tags {
  position: relative;
  z-index: 1;
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.promo-tags li {
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.56);
  background: rgba(255, 255, 255, 0.34);
  color: #2e3e56;
  font-size: 14px;
  font-weight: 700;
  text-align: center;
  padding: 9px 8px;
  backdrop-filter: blur(3px);
}

.promo-illustration {
  position: relative;
  z-index: 1;
  margin-top: auto;
  min-height: 300px;
}

.scene-card {
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: 0;
  top: 24px;
  border-radius: 26px;
  border: 2px solid rgba(255, 255, 255, 0.6);
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.9) 0%, rgba(253, 248, 213, 0.82) 100%);
  box-shadow: 0 26px 38px rgba(109, 89, 21, 0.16);
  padding: 20px;
}

.scene-top {
  display: flex;
  gap: 8px;
}

.scene-top span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(79, 108, 146, 0.28);
}

.scene-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  grid-auto-rows: 52px;
  gap: 10px;
}

.scene-item {
  border-radius: 14px;
  background: linear-gradient(140deg, #dbe6f8 0%, #f8fbff 100%);
  border: 1px solid #d4deec;
}

.scene-item.tall {
  grid-row: span 2;
}

.scene-item.wide {
  grid-column: span 2;
}

.scene-chip {
  position: absolute;
  border-radius: 999px;
  padding: 8px 14px;
  color: #1f334f;
  font-size: 13px;
  font-weight: 700;
  border: 1px solid rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 8px 14px rgba(91, 103, 121, 0.16);
}

.chip-a {
  left: 0;
  top: 18px;
}

.chip-b {
  right: 8px;
  top: 76px;
}

.chip-c {
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
}

.login-pane {
  display: grid;
  place-items: center;
  padding: 24px;
}

.login-card {
  width: min(460px, 100%);
  padding: 34px;
  border-radius: 18px;
  border: 1px solid #e2e8f4;
  background: #ffffff;
  box-shadow: 0 18px 30px rgba(45, 79, 130, 0.1);
}

.login-card h2 {
  margin: 0;
  font-size: 42px;
  color: #2a3f61;
}

.login-card p {
  margin: 8px 0 0;
  color: #6c7e9d;
  font-size: 24px;
}

.login-form {
  margin-top: 24px;
  display: grid;
  gap: 14px;
}

.login-form label {
  display: grid;
  gap: 8px;
}

.login-form span {
  color: #4b658b;
  font-size: 14px;
  font-weight: 600;
}

.form-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-top: 2px;
}

.remember {
  display: inline-flex !important;
  align-items: center;
  gap: 6px;
  color: #677d9f;
}

.form-meta a {
  color: #5f84ba;
  font-size: 14px;
}

.login-btn {
  border: 0;
  border-radius: 999px;
  min-height: 48px;
  background: linear-gradient(135deg, #f5df48 0%, #f1d533 100%);
  color: #2a3340;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 16px rgba(222, 191, 44, 0.32);
}

.login-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 1180px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .promo-pane {
    min-height: 460px;
    padding: 24px;
  }

  .promo-head h1 {
    font-size: 32px;
  }

  .promo-tags {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .promo-illustration {
    min-height: 240px;
  }
}

@media (max-width: 640px) {
  .login-card {
    padding: 22px 18px;
  }

  .login-card h2 {
    font-size: 32px;
  }

  .login-card p {
    font-size: 18px;
  }
}
</style>
