import { createRouter, createWebHistory } from 'vue-router'
import { ADMIN_ROLE, SELLER_ROLE } from '@/constants/auth'
import { getStoredToken, getStoredUser } from '@/utils/auth'

function resolveHomeRoute(user) {
  if (user?.role === ADMIN_ROLE) {
    return '/dashboard'
  }

  if (user?.role === SELLER_ROLE) {
    return '/market/seller/goods'
  }

  return '/market/goods'
}

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: {
      title: '登录',
    },
  },
  {
    path: '/forbidden',
    name: 'forbidden',
    component: () => import('@/views/ForbiddenView.vue'),
    meta: {
      title: '无权限访问',
    },
  },
  {
    path: '/market',
    component: () => import('@/layouts/SiteLayout.vue'),
    redirect: '/market/goods',
    children: [
      {
        path: 'goods',
        name: 'goodsMarket',
        component: () => import('@/views/market/GoodsMarketView.vue'),
        meta: {
          title: '商品广场',
        },
      },
      {
        path: 'goods/:id',
        name: 'goodsDetail',
        component: () => import('@/views/market/GoodsDetailView.vue'),
        meta: {
          title: '商品详情',
        },
      },
      {
        path: 'seller/goods',
        name: 'sellerGoods',
        component: () => import('@/views/seller/SellerGoodsManageView.vue'),
        meta: {
          title: '卖家商品工作台',
          requiresAuth: true,
          roles: [SELLER_ROLE],
        },
      },
    ],
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    meta: {
      requiresAuth: true,
      roles: [ADMIN_ROLE],
    },
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: {
          title: '后台总览',
          requiresAuth: true,
          roles: [ADMIN_ROLE],
        },
      },
      {
        path: 'admin-manage',
        name: 'adminManage',
        component: () => import('@/views/admin/AdminManageView.vue'),
        meta: {
          title: '管理员管理',
          requiresAuth: true,
          roles: [ADMIN_ROLE],
        },
      },
      {
        path: 'user-manage',
        name: 'userManage',
        component: () => import('@/views/admin/UserManagementView.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          roles: [ADMIN_ROLE],
        },
      },
      {
        path: 'seller-auth',
        name: 'sellerAuth',
        component: () => import('@/views/admin/SellerAuthView.vue'),
        meta: {
          title: '卖家认证审核',
          requiresAuth: true,
          roles: [ADMIN_ROLE],
        },
      },
      {
        path: 'goods-manage',
        name: 'adminGoods',
        component: () => import('@/views/admin/AdminGoodsView.vue'),
        meta: {
          title: '商品审核管理',
          requiresAuth: true,
          roles: [ADMIN_ROLE],
        },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/market/goods',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to) => {
  const token = getStoredToken()
  const user = getStoredUser()

  if (to.name === 'login' && token) {
    return resolveHomeRoute(user)
  }

  const requiresAuth = to.matched.some((record) => record.meta?.requiresAuth)
  const roleRecord = [...to.matched].reverse().find((record) => Array.isArray(record.meta?.roles))
  const allowedRoles = roleRecord?.meta?.roles || null

  if (requiresAuth && !token) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath,
        mode: allowedRoles?.includes(ADMIN_ROLE) ? 'admin' : 'user',
      },
    }
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    return '/forbidden'
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta?.title ? `${to.meta.title} - 赚赚` : '赚赚'
})

export default router
