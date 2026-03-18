<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Menu as MenuIcon,
  UserFilled,
  User,
  Stamp,
  DataBoard,
  Grid,
  SwitchButton,
  MoreFilled,
  ArrowDown,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const mobileNavVisible = ref(false)

const displayName = computed(() => authStore.user.name || authStore.user.username || '管理员')

const menuGroups = [
  {
    key: 'workspace',
    label: '工作台',
    icon: DataBoard,
    items: [{ label: '后台总览', path: '/dashboard', icon: DataBoard }],
  },
  {
    key: 'users',
    label: '用户中心',
    icon: User,
    items: [
      { label: '用户管理', path: '/user-manage', icon: User },
      { label: '卖家认证审核', path: '/seller-auth', icon: Stamp },
    ],
  },
  {
    key: 'config',
    label: '平台配置',
    icon: Grid,
    items: [
      { label: '管理员管理', path: '/admin-manage', icon: UserFilled },
      { label: '分类管理', path: '/category-manage', icon: Grid },
    ],
  },
]
const openGroupKeys = ref(menuGroups.map((group) => group.key))

function closeMobileNav() {
  mobileNavVisible.value = false
}

function findGroupKeyByPath(path) {
  const matchedGroup = menuGroups.find((group) => group.items.some((item) => item.path === path))
  return matchedGroup?.key || ''
}

function isGroupOpen(groupKey) {
  return openGroupKeys.value.includes(groupKey)
}

function toggleGroup(groupKey) {
  if (isGroupOpen(groupKey)) {
    openGroupKeys.value = openGroupKeys.value.filter((key) => key !== groupKey)
    return
  }

  openGroupKeys.value = [...openGroupKeys.value, groupKey]
}

function handleLogout() {
  authStore.logout()
  router.replace('/login')
}

watch(
  () => route.fullPath,
  () => {
    const activeGroupKey = findGroupKeyByPath(route.path)
    if (activeGroupKey && !isGroupOpen(activeGroupKey)) {
      openGroupKeys.value = [...openGroupKeys.value, activeGroupKey]
    }
    closeMobileNav()
  },
)
</script>

<template>
  <div class="admin-layout">
    <div class="bg-orb orb-a" />
    <div class="bg-orb orb-b" />

    <aside class="side-panel" :class="{ open: mobileNavVisible }">
      <header class="brand-row">
        <img src="/logo.jpg" alt="logo" class="brand-logo" />
        <div>
          <p class="brand-sub">Campus Admin</p>
          <h2 class="brand-title">转转管理端</h2>
        </div>
      </header>

      <nav class="nav-list">
        <section v-for="group in menuGroups" :key="group.key" class="nav-group">
          <button type="button" class="group-trigger" @click="toggleGroup(group.key)">
            <span class="group-main">
              <el-icon><component :is="group.icon" /></el-icon>
              <span>{{ group.label }}</span>
            </span>
            <el-icon class="group-arrow" :class="{ open: isGroupOpen(group.key) }"><ArrowDown /></el-icon>
          </button>

          <div v-show="isGroupOpen(group.key)" class="group-items">
            <RouterLink
              v-for="item in group.items"
              :key="item.path"
              :to="item.path"
              class="nav-item"
              active-class="active"
              @click="closeMobileNav"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </RouterLink>
          </div>
        </section>
      </nav>

      <footer class="side-footer">
        <el-avatar :size="32" class="avatar-mark">{{ displayName.charAt(0) }}</el-avatar>
        <div class="footer-user">
          <span class="footer-label">当前账号</span>
          <strong>{{ displayName }}</strong>
        </div>
      </footer>
    </aside>

    <button v-if="mobileNavVisible" type="button" class="mobile-mask" @click="closeMobileNav" />

    <div class="main-panel">
      <header class="topbar">
        <div class="topbar-left">
          <button type="button" class="mobile-trigger" @click="mobileNavVisible = true">
            <el-icon><MenuIcon /></el-icon>
          </button>

          <div class="title-stack">
            <p>Control Center</p>
            <h1>运营工作台</h1>
          </div>
        </div>

        <div class="topbar-right">
          <span class="name-pill">{{ displayName }}</span>

          <el-dropdown trigger="click">
            <button type="button" class="menu-trigger">
              <el-icon><MoreFilled /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="page-main">
        <section class="page-shell fade-in-up">
          <RouterView />
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  position: relative;
  min-height: 100vh;
  display: flex;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
  filter: blur(0);
}

.orb-a {
  width: 480px;
  height: 480px;
  top: -220px;
  left: -160px;
  background: radial-gradient(circle at center, rgba(71, 153, 255, 0.23) 0%, rgba(71, 153, 255, 0) 72%);
}

.orb-b {
  width: 380px;
  height: 380px;
  right: -120px;
  top: -120px;
  background: radial-gradient(circle at center, rgba(20, 184, 166, 0.2) 0%, rgba(20, 184, 166, 0) 74%);
}

.side-panel {
  position: relative;
  z-index: 40;
  width: 220px;
  padding: 14px 10px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--border);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(243, 250, 255, 0.95) 100%);
  backdrop-filter: blur(6px);
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 8px 12px;
}

.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.86);
  box-shadow: 0 8px 14px rgba(51, 120, 208, 0.22);
}

.brand-sub {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-light);
}

.brand-title {
  margin: 2px 0 0;
  font-size: 16px;
  line-height: 1.2;
  color: #26426b;
}

.nav-list {
  flex: 1;
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-group {
  border: 1px solid #d9e9fb;
  border-radius: 12px;
  background: rgba(248, 252, 255, 0.9);
}

.group-trigger {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 9px 10px;
  border: 0;
  border-bottom: 1px solid transparent;
  background: transparent;
  color: #3f5c85;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
}

.group-main {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.group-arrow {
  transition: transform 0.2s ease;
}

.group-arrow.open {
  transform: rotate(180deg);
}

.group-items {
  display: grid;
  gap: 6px;
  padding: 0 8px 8px;
  border-top: 1px solid #deebfa;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px;
  margin-top: 6px;
  border-radius: 12px;
  border: 1px solid transparent;
  color: #425b84;
  font-size: 13px;
  font-weight: 600;
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.nav-item:hover {
  border-color: var(--border);
  background: #f4f9ff;
  color: #2d4f82;
  transform: translateX(2px);
}

.nav-item.active {
  color: #1f6fd8;
  border-color: #bdd8ff;
  background: linear-gradient(135deg, #eaf4ff 0%, #f6fbff 100%);
  box-shadow: inset 0 0 0 1px rgba(182, 215, 255, 0.6);
}

.side-footer {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px;
  border-radius: 14px;
  background: #f0f7ff;
  border: 1px solid #d4e5fa;
}

.avatar-mark {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-deep) 100%);
  color: #ffffff;
  font-weight: 700;
}

.footer-user {
  display: grid;
  line-height: 1.25;
}

.footer-label {
  font-size: 12px;
  color: var(--text-light);
}

.footer-user strong {
  font-size: 14px;
  color: #28456f;
}

.main-panel {
  position: relative;
  z-index: 10;
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 76px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 26px;
  border-bottom: 1px solid var(--border);
  background: rgba(251, 254, 255, 0.82);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 30;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.mobile-trigger {
  display: none;
  width: 40px;
  height: 40px;
  border: 1px solid #d4e5fa;
  border-radius: 11px;
  background: #f3f9ff;
  color: #315a92;
  cursor: pointer;
}

.title-stack p {
  margin: 0;
  font-family: 'Lexend', sans-serif;
  font-size: 11px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--text-light);
}

.title-stack h1 {
  margin: 1px 0 0;
  font-size: 21px;
  color: #24436f;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.name-pill {
  padding: 7px 12px;
  border-radius: 999px;
  border: 1px solid #cfe2fb;
  background: #ecf5ff;
  color: #30598f;
  font-size: 13px;
  font-weight: 600;
}

.menu-trigger {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  border: 1px solid #d5e6fb;
  background: #f7fbff;
  color: #355f96;
  cursor: pointer;
}

.menu-trigger:hover {
  border-color: #bfd7f8;
  background: #edf5ff;
}

.page-main {
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.page-shell {
  width: 100%;
  margin: 0;
}

.logout-item {
  color: #b53f4a !important;
}

.mobile-mask {
  position: fixed;
  inset: 0;
  z-index: 35;
  border: 0;
  background: rgba(19, 34, 58, 0.24);
}

@media (max-width: 1024px) {
  .side-panel {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    transform: translateX(-100%);
    transition: transform 0.26s ease;
  }

  .side-panel.open {
    transform: translateX(0);
  }

  .mobile-trigger {
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  .topbar {
    padding: 0 14px;
  }

  .title-stack h1 {
    font-size: 18px;
  }

  .name-pill {
    display: none;
  }

  .page-main {
    padding: 14px;
  }
}

@media (max-width: 640px) {
  .title-stack p {
    display: none;
  }

  .title-stack h1 {
    font-size: 17px;
  }
}
</style>
