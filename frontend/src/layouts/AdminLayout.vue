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
  Goods,
  ArrowLeftBold,
  ArrowRightBold,
  Document,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const mobileNavVisible = ref(false)
const secondaryCollapsed = ref(false)

const displayName = computed(() => authStore.user.name || authStore.user.username || '管理员')

const primaryGroups = [
  {
    key: 'sales',
    label: '销售管理',
    icon: Goods,
    items: [
      {
        key: 'dashboard',
        label: '统计',
        path: '/dashboard',
        icon: DataBoard,
        tasks: [{ label: '看板总览', path: '/dashboard' }],
      },
      {
        key: 'product',
        label: '商品',
        path: '/product-manage',
        icon: Goods,
        tasks: [
          { label: '所有商品', path: '/product-manage', query: { status: 'all' } },
          { label: '待审核', path: '/product-manage', query: { status: 'to_review' } },
          { label: '销售中', path: '/product-manage', query: { status: 'online' } },
          { label: '已下架', path: '/product-manage', query: { status: 'offline' } },
          { label: '审核驳回', path: '/product-manage', query: { status: 'rejected' } },
        ],
      },
      {
        key: 'order',
        label: '订单',
        path: '/order-manage',
        icon: Document,
        tasks: [
          { label: '所有订单', path: '/order-manage', query: { status: 'all' } },
          { label: '待买家付款', path: '/order-manage', query: { status: 'pending_pay' } },
          { label: '已支付', path: '/order-manage', query: { status: 'paid' } },
          { label: '已完成', path: '/order-manage', query: { status: 'completed' } },
          { label: '已取消', path: '/order-manage', query: { status: 'canceled' } },
          { label: '超时关闭', path: '/order-manage', query: { status: 'timeout_closed' } },
        ],
      },
    ],
  },
  {
    key: 'account',
    label: '账号管理',
    icon: User,
    items: [
      {
        key: 'user',
        label: '用户',
        path: '/user-manage',
        icon: User,
        tasks: [{ label: '用户列表', path: '/user-manage' }],
      },
      {
        key: 'admin',
        label: '管理员',
        path: '/admin-manage',
        icon: UserFilled,
        tasks: [{ label: '管理员列表', path: '/admin-manage' }],
      },
      {
        key: 'sellerAuth',
        label: '认证审核',
        path: '/seller-auth',
        icon: Stamp,
        tasks: [
          { label: '全部', path: '/seller-auth' },
          { label: '待审核', path: '/seller-auth', query: { status: 'pending' } },
          { label: '已通过', path: '/seller-auth', query: { status: 'approved' } },
          { label: '已驳回', path: '/seller-auth', query: { status: 'rejected' } },
          { label: '已撤回', path: '/seller-auth', query: { status: 'revoked' } },
        ],
      },
    ],
  },
  {
    key: 'system',
    label: '系统配置',
    icon: Grid,
    items: [
      {
        key: 'category',
        label: '分类',
        path: '/category-manage',
        icon: Grid,
        tasks: [{ label: '分类总览', path: '/category-manage' }],
      },
      {
        key: 'auditLog',
        label: '日志',
        path: '/audit-log',
        icon: Document,
        tasks: [
          { label: '全部日志', path: '/audit-log' },
          { label: '商品审核', path: '/audit-log', query: { operationType: '1' } },
          { label: '认证审核', path: '/audit-log', query: { operationType: '2' } },
          { label: '举报处理', path: '/audit-log', query: { operationType: '3' } },
        ],
      },
    ],
  },
]

const activePrimaryItem = computed(() => {
  for (const group of primaryGroups) {
    const matchedItem = group.items.find((item) => route.path === item.path)
    if (matchedItem) {
      return matchedItem
    }
  }
  return primaryGroups[0].items[0]
})

const secondaryTasks = computed(() => activePrimaryItem.value?.tasks || [])
const showSecondary = computed(() => secondaryTasks.value.length > 1)

function buildTo(link) {
  if (link.query && Object.keys(link.query).length > 0) {
    return { path: link.path, query: link.query }
  }
  return { path: link.path }
}

function closeMobileNav() {
  mobileNavVisible.value = false
}

function isPrimaryActive(item) {
  return route.path === item.path
}

function isSecondaryActive(task) {
  if (route.path !== task.path) {
    return false
  }

  const expectedQuery = task.query || {}
  const queryKeys = Object.keys(expectedQuery)

  if (queryKeys.length === 0) {
    const relatedKeys = new Set(
      secondaryTasks.value.flatMap((entry) => Object.keys(entry.query || {})),
    )
    for (const key of relatedKeys) {
      if (route.query[key] !== undefined) {
        return false
      }
    }
    return true
  }

  return queryKeys.every((key) => String(route.query[key] ?? '') === String(expectedQuery[key]))
}

function navigatePrimary(item) {
  if (item.tasks && item.tasks.length > 0) {
    router.push(buildTo(item.tasks[0]))
    return
  }
  router.push(buildTo(item))
}

function toggleSecondary() {
  secondaryCollapsed.value = !secondaryCollapsed.value
}

function handleLogout() {
  authStore.logout()
  router.replace('/login')
}

watch(
  () => route.fullPath,
  () => {
    closeMobileNav()
  },
)
</script>

<template>
  <div class="admin-layout">
    <aside class="primary-panel" :class="{ open: mobileNavVisible }">
      <header class="brand-row">
        <img src="/logo.jpg" alt="logo" class="brand-logo" />
        <div class="brand-text">
          <p class="brand-sub">Campus Admin</p>
          <h2 class="brand-title">转转运营台</h2>
        </div>
      </header>

      <nav class="primary-scroll">
        <section v-for="group in primaryGroups" :key="group.key" class="primary-group">
          <div class="group-head">
            <span class="group-main">
              <el-icon><component :is="group.icon" /></el-icon>
              <span>{{ group.label }}</span>
            </span>
          </div>

          <div class="group-links">
            <button
              v-for="item in group.items"
              :key="item.key"
              type="button"
              class="primary-link"
              :class="{ active: isPrimaryActive(item), 'is-long': item.label.length >= 4 }"
              @click="navigatePrimary(item)"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </button>
          </div>
        </section>
      </nav>

      <footer class="side-footer">
        <el-avatar :size="30" class="avatar-mark">{{ displayName.charAt(0) }}</el-avatar>
        <div class="footer-user">
          <span>当前账号</span>
          <strong>{{ displayName }}</strong>
        </div>
      </footer>
    </aside>

    <button v-if="mobileNavVisible" type="button" class="mobile-mask" @click="closeMobileNav" />

    <section v-if="showSecondary" class="secondary-panel" :class="{ collapsed: secondaryCollapsed }">
      <div class="secondary-inner">
        <header class="secondary-head">
          <h3>{{ activePrimaryItem.label }}</h3>
          <p>任务栏</p>
        </header>

        <nav class="secondary-links">
          <RouterLink
            v-for="task in secondaryTasks"
            :key="`${task.path}-${JSON.stringify(task.query || {})}`"
            :to="buildTo(task)"
            class="secondary-link"
            :class="{ active: isSecondaryActive(task) }"
          >
            {{ task.label }}
          </RouterLink>
        </nav>
      </div>
    </section>

    <button
      v-if="showSecondary"
      type="button"
      class="secondary-toggle"
      :class="{ collapsed: secondaryCollapsed }"
      @click="toggleSecondary"
    >
      <el-icon v-if="!secondaryCollapsed"><ArrowLeftBold /></el-icon>
      <el-icon v-else><ArrowRightBold /></el-icon>
    </button>

    <div class="main-panel">
      <header class="topbar">
        <button type="button" class="mobile-trigger" @click="mobileNavVisible = true">
          <el-icon><MenuIcon /></el-icon>
        </button>

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
        <section class="page-shell">
          <RouterView v-slot="{ Component, route: currentRoute }">
            <Transition name="route-slide-right">
              <div :key="currentRoute.path" class="route-view-host">
                <component :is="Component" />
              </div>
            </Transition>
          </RouterView>
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  --primary-width: 172px;
  --secondary-width: 206px;

  height: 100vh;
  display: flex;
  overflow: hidden;
  position: relative;
}

.primary-panel {
  flex: 0 0 var(--primary-width);
  width: var(--primary-width);
  height: 100vh;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #2a2c31 0%, #1f2024 100%);
  border-right: 1px solid #373c45;
  overflow-y: auto;
  z-index: 25;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 4px 10px;
}

.brand-logo {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.4);
}

.brand-text {
  min-width: 0;
}

.brand-sub {
  margin: 0;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.58);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.brand-title {
  margin: 1px 0 0;
  font-size: 15px;
  color: #f6f9ff;
  white-space: nowrap;
}

.primary-scroll {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.primary-group {
  border-radius: 10px;
  border: 1px solid #3a3f49;
  background: rgba(43, 46, 53, 0.95);
}

.group-head {
  width: 100%;
  height: 36px;
  padding: 0 8px;
  border-bottom: 1px solid #3b414d;
  color: #e8ca3f;
  display: flex;
  align-items: center;
  font-size: 13px;
  font-weight: 700;
}

.group-main {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.group-links {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px;
  padding: 8px 6px 6px;
}

.primary-link {
  min-height: 34px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: #dde3ed;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.primary-link span {
  white-space: nowrap;
}

.primary-link.is-long {
  grid-column: 1 / -1;
  justify-content: flex-start;
  padding: 0 10px;
}

.primary-link:hover {
  background: #393f4a;
  border-color: #4a5160;
}

.primary-link.active {
  color: #2b2c32;
  border-color: #f2db53;
  background: linear-gradient(135deg, #f4df59 0%, #ebd141 100%);
}

.side-footer {
  margin-top: 8px;
  padding: 8px;
  border-radius: 10px;
  border: 1px solid #3a404a;
  background: #2a2e36;
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar-mark {
  background: linear-gradient(135deg, #f4df59 0%, #ebd141 100%);
  color: #2c3038;
  font-weight: 700;
}

.footer-user {
  display: grid;
  min-width: 0;
}

.footer-user span {
  color: rgba(255, 255, 255, 0.62);
  font-size: 11px;
}

.footer-user strong {
  color: #eef2f9;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.secondary-panel {
  width: var(--secondary-width);
  height: 100vh;
  border-right: 1px solid #dce8f8;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  transition: width 0.24s ease, border-color 0.24s ease;
  overflow: hidden;
  z-index: 20;
}

.secondary-panel.collapsed {
  width: 0;
  border-right-color: transparent;
}

.secondary-inner {
  width: var(--secondary-width);
  height: 100%;
  padding: 16px 12px;
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 10px;
}

.secondary-head h3 {
  margin: 0;
  font-size: 16px;
  color: #233f67;
}

.secondary-head p {
  margin: 2px 0 0;
  color: #8fa0ba;
  font-size: 12px;
}

.secondary-links {
  display: grid;
  gap: 6px;
  align-content: start;
}

.secondary-link {
  min-height: 42px;
  border: 1px solid transparent;
  border-radius: 10px;
  background: #ffffff;
  color: #657b9e;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  padding: 0 12px;
  transition: all 0.2s ease;
}

.secondary-link:hover {
  border-color: #d6e4f8;
  background: #f4f9ff;
  color: #35557f;
}

.secondary-link.active {
  color: #2b2f37;
  border-color: #f0d445;
  background: linear-gradient(135deg, #f7e05e 0%, #efd746 100%);
}

.secondary-toggle {
  position: absolute;
  left: calc(var(--primary-width) + var(--secondary-width) - 13px);
  top: 118px;
  width: 26px;
  height: 34px;
  border-radius: 999px;
  border: 1px solid #d7e4f6;
  background: #f8fbff;
  color: #7689a6;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 40;
  transition: left 0.24s ease;
}

.secondary-toggle.collapsed {
  left: calc(var(--primary-width) - 13px);
}

.main-panel {
  flex: 1;
  min-width: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.topbar {
  height: 62px;
  border-bottom: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.96);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  position: sticky;
  top: 0;
  z-index: 18;
}

.mobile-trigger {
  display: none;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  border: 1px solid #d3e3f7;
  background: #f5f9ff;
  color: #3a5f92;
}

.topbar-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

.name-pill {
  padding: 7px 12px;
  border-radius: 999px;
  border: 1px solid #d4e4f7;
  background: #f2f7ff;
  color: #2a4f82;
  font-weight: 600;
}

.menu-trigger {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid #d7e6f9;
  background: #fbfdff;
  color: #355f96;
  cursor: pointer;
}

.page-main {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding: 12px;
}

.page-shell {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.route-view-host {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.route-slide-right-enter-active,
.route-slide-right-leave-active {
  width: 100%;
  will-change: transform, opacity;
}

.route-slide-right-enter-active {
  transition: transform 0.46s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.32s ease;
  position: relative;
  z-index: 2;
  backface-visibility: hidden;
}

.route-slide-right-leave-active {
  transition: none;
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  backface-visibility: hidden;
}

.route-slide-right-enter-from {
  transform: translate3d(-72px, 0, 0);
  opacity: 0;
}

.route-slide-right-enter-to {
  transform: translate3d(0, 0, 0);
  opacity: 1;
}

.route-slide-right-leave-from {
  transform: translate3d(0, 0, 0);
}

.route-slide-right-leave-to {
  transform: translate3d(0, 0, 0);
}

.logout-item {
  color: #b53f4a !important;
}

.mobile-mask {
  position: fixed;
  inset: 0;
  border: 0;
  background: rgba(0, 0, 0, 0.36);
  z-index: 35;
}

@media (max-width: 1180px) {
  .admin-layout {
    --primary-width: 164px;
    --secondary-width: 190px;
  }
}

@media (max-width: 980px) {
  .primary-panel {
    position: fixed;
    left: 0;
    top: 0;
    transform: translateX(-100%);
    transition: transform 0.24s ease;
  }

  .primary-panel.open {
    transform: translateX(0);
  }

  .secondary-panel,
  .secondary-toggle,
  .name-pill {
    display: none;
  }

  .mobile-trigger {
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
