<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { 
  Menu as MenuIcon,
  User, 
  Key, 
  Stamp, 
  Monitor,
  SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const displayName = computed(() => authStore.user.name || authStore.user.username || 'Admin')

const menuItems = [
  { label: '仪表盘', path: '/dashboard', icon: Monitor },
  { label: '员工管理', path: '/admin-manage', icon: Key },
  { label: '用户档案', path: '/user-manage', icon: User },
  { label: '认证审核', path: '/seller-auth', icon: Stamp },
]

function handleLogout() {
  authStore.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="zz-admin-shell">
    <aside class="zz-sidebar">
      <div class="sidebar-top">
        <img src="/logo.jpg" class="brand-logo" />
        <span class="brand-title">赚赚管理端</span>
      </div>

      <nav class="sidebar-nav">
        <RouterLink
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          active-class="active"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="sidebar-bottom">
        <div class="user-brief">
          <el-avatar :size="24">{{ displayName.charAt(0) }}</el-avatar>
          <span class="u-name">{{ displayName }}</span>
        </div>
      </div>
    </aside>

    <div class="zz-main">
      <header class="zz-topbar">
        <div class="topbar-left">
          <span class="breadcrumb">{{ route.meta.title || '系统' }}</span>
        </div>
        <div class="topbar-right">
          <el-dropdown trigger="click">
            <div class="profile-trigger">
              <el-icon><MenuIcon /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout" class="logout-red">
                  <el-icon><SwitchButton /></el-icon>安全退出
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="zz-body">
        <div class="content-container">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.zz-admin-shell {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: transparent;
}

.zz-sidebar {
  width: 236px;
  border-right: 1px solid var(--admin-border);
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #fff8eb 0%, #fff2de 100%);
  box-shadow: 6px 0 20px rgba(168, 111, 36, 0.08);
}

.sidebar-top {
  height: 70px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  gap: 12px;
}

.brand-logo {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 6px 14px rgba(184, 117, 30, 0.25);
}

.brand-title {
  font-size: 16px;
  font-weight: 700;
  color: #4e3218;
  letter-spacing: 0.4px;
}

.sidebar-nav {
  flex: 1;
  padding: 10px 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 12px;
  border-radius: 10px;
  color: #785335;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.2s;
  font-weight: 600;
  border: 1px solid transparent;
}

.nav-item:hover {
  background-color: #fff7eb;
  border-color: #f1d5af;
  color: #5d3c1f;
}

.nav-item.active {
  color: #7b4308;
  border-color: #f0c68f;
  background: linear-gradient(135deg, #ffefd7 0%, #ffe3ba 100%);
  box-shadow: 0 8px 14px rgba(224, 146, 58, 0.24);
}

.sidebar-bottom {
  padding: 16px;
  border-top: 1px solid var(--admin-border);
}

.user-brief {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #785335;
}

.zz-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.zz-topbar {
  height: 70px;
  padding: 0 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #efd7ba;
  background: rgba(255, 249, 236, 0.72);
  backdrop-filter: blur(8px);
}

.breadcrumb {
  font-size: 16px;
  font-weight: 700;
  color: #5a391d;
}

.profile-trigger {
  cursor: pointer;
  color: #7d5633;
  padding: 8px;
  border-radius: 10px;
  border: 1px solid transparent;
}

.profile-trigger:hover {
  background: #fff4e3;
  border-color: #efd0a5;
}

.zz-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: transparent;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.logout-red {
  color: #c14231 !important;
}

@media (max-width: 960px) {
  .zz-sidebar {
    width: 206px;
  }

  .zz-topbar {
    padding: 0 16px;
  }

  .zz-body {
    padding: 16px;
  }
}
</style>
