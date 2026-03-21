<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const navItems = computed(() => {
  const items = [{ label: '商品广场', path: '/market/goods' }]

  if (authStore.isSeller) {
    items.push({ label: '卖家工作台', path: '/market/seller/goods' })
  }

  if (authStore.isAdmin) {
    items.push({ label: '管理后台', path: '/dashboard' })
  }

  return items
})

const displayName = computed(() => authStore.user.name || authStore.user.studentNo || '访客')

function handleAuthAction() {
  if (authStore.isLoggedIn) {
    authStore.logout()
    router.push('/market/goods')
    return
  }

  router.push({
    path: '/login',
    query: {
      mode: 'user',
      redirect: route.fullPath,
    },
  })
}
</script>

<template>
  <div class="site-shell">
    <header class="site-header">
      <div class="header-inner">
        <RouterLink to="/market/goods" class="brand">
          <img src="/logo.jpg" alt="logo" class="brand-logo" />
          <div>
            <h1>赚赚校园</h1>
            <p>商品全生命周期验证入口</p>
          </div>
        </RouterLink>

        <nav class="site-nav">
          <RouterLink
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-link"
            active-class="active"
          >
            {{ item.label }}
          </RouterLink>
        </nav>

        <div class="header-actions">
          <div v-if="authStore.isLoggedIn" class="user-chip">
            <span class="role">{{ authStore.roleLabel }}</span>
            <strong>{{ displayName }}</strong>
          </div>

          <button class="app-btn primary" type="button" @click="handleAuthAction">
            {{ authStore.isLoggedIn ? '退出登录' : '用户登录' }}
          </button>
        </div>
      </div>
    </header>

    <main class="site-main">
      <div class="site-content">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<style scoped>
.site-shell {
  min-height: 100vh;
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 80;
  border-bottom: 1px solid var(--border);
  backdrop-filter: blur(10px);
  background: rgba(255, 249, 237, 0.9);
}

.header-inner {
  max-width: 1260px;
  margin: 0 auto;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.brand-logo {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  border: 2px solid #fff;
  box-shadow: 0 8px 18px rgba(170, 112, 27, 0.28);
}

.brand h1 {
  margin: 0;
  font-size: 20px;
  color: #573719;
}

.brand p {
  margin: 2px 0 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.site-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.nav-link {
  padding: 9px 12px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  color: #7b5634;
  transition: all 0.2s ease;
}

.nav-link:hover,
.nav-link.active {
  color: #7a4208;
  background: #ffe8c5;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 999px;
  background: #fff5e5;
  border: 1px solid #f2d5ad;
  font-size: 13px;
}

.user-chip .role {
  color: #8e5b20;
  font-weight: 700;
}

.site-main {
  padding: 22px 18px 28px;
}

.site-content {
  max-width: 1260px;
  margin: 0 auto;
}

@media (max-width: 980px) {
  .header-inner {
    flex-direction: column;
    align-items: stretch;
  }

  .site-nav,
  .header-actions {
    justify-content: space-between;
  }
}

@media (max-width: 640px) {
  .site-main {
    padding: 14px 12px 22px;
  }

  .brand {
    align-items: flex-start;
  }

  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
