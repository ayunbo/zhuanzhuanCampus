<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Menu as MenuIcon,
  UserFilled,
  User,
  Stamp,
  DataBoard,
  SwitchButton,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const displayName = computed(() => authStore.user.name || authStore.user.username || '管理员')

const menuItems = [
  { label: '后台总览', path: '/dashboard', icon: DataBoard },
  { label: '管理员管理', path: '/admin-manage', icon: UserFilled },
  { label: '用户管理', path: '/user-manage', icon: User },
  { label: '卖家认证审核', path: '/seller-auth', icon: Stamp },
  { label: '分类管理', path: '/category-manage', icon: MenuIcon },
]

function handleLogout() {
  authStore.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <div class="sidebar-top">
        <img src="/logo.jpg" alt="logo" class="brand-logo" />
        <span class="brand-title">赚赚管理后台</span>
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
          <span>{{ displayName }}</span>
        </div>
      </div>
    </aside>

    <div class="admin-main">
      <header class="topbar">
        <span class="breadcrumb">{{ route.meta.title || '系统管理' }}</span>
        <el-dropdown trigger="click">
          <div class="profile-trigger">
            <el-icon><MenuIcon /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout" class="logout-item">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="page-body">
        <div class="content-wrap fade-in-up">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-shell {
  display: flex;
  min-height: 100vh;
}

.admin-sidebar {
  width: 236px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--admin-border);
  background: linear-gradient(180deg, #fff8eb 0%, #fff2df 100%);
  box-shadow: 8px 0 22px rgba(170, 111, 36, 0.1);
  position: sticky;
  top: 0;
  height: 100vh;
}

.sidebar-top {
  height: 70px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
}

.brand-logo {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: 2px solid rgba(255, 255, 255, 0.92);
  box-shadow: 0 6px 14px rgba(173, 109, 36, 0.24);
}

.brand-title {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.3px;
  color: #53361b;
}

.sidebar-nav {
  flex: 1;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  padding: 11px 12px;
  border-radius: 10px;
  border: 1px solid transparent;
  font-size: 14px;
  font-weight: 600;
  color: #7a5635;
  transition: all 0.2s ease;
}

.nav-item:hover {
  color: #5d3c1f;
  border-color: #f1d2a8;
  background: #fff8ed;
}

.nav-item.active {
  color: #7a4208;
  border-color: #f1c78f;
  background: linear-gradient(135deg, #ffefd7 0%, #ffe4be 100%);
  box-shadow: 0 8px 14px rgba(223, 148, 64, 0.24);
}

.sidebar-bottom {
  border-top: 1px solid var(--admin-border);
  padding: 16px;
}

.user-brief {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #7a5635;
}

.admin-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  border-bottom: 1px solid #efd7ba;
  background: rgba(255, 249, 236, 0.75);
  backdrop-filter: blur(8px);
  position: sticky;
  top: 0;
  z-index: 60;
}

.breadcrumb {
  font-size: 16px;
  font-weight: 700;
  color: #5b3a1d;
}

.profile-trigger {
  cursor: pointer;
  color: #7b5634;
  padding: 8px;
  border-radius: 10px;
  border: 1px solid transparent;
}

.profile-trigger:hover {
  background: #fff4e3;
  border-color: #efd1a8;
}

.page-body {
  flex: 1;
  padding: 22px;
  overflow-y: auto;
}

.content-wrap {
  width: 100%;
  max-width: 1260px;
  margin: 0 auto;
}

.logout-item {
  color: #c14131 !important;
}

@media (max-width: 960px) {
  .admin-sidebar {
    width: 206px;
  }

  .topbar {
    padding: 0 14px;
  }

  .page-body {
    padding: 14px;
  }
}
</style>
