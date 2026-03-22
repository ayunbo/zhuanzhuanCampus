import { createRouter, createWebHistory } from 'vue-router'
import { hasValidStoredToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: {
      title: '管理员登录',
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
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: {
      path: '/dashboard',
      query: {
        panel: 'overview',
      },
    },
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: {
          title: '后台总览',
        },
      },
      {
        path: 'product-manage',
        name: 'productManage',
        component: () => import('@/views/admin/AdminGoodsView.vue'),
        meta: {
          title: '商品管理',
        },
      },
      {
        path: 'order-manage',
        name: 'orderManage',
        component: () => import('@/views/admin/AdminOrderListView.vue'),
        meta: {
          title: '订单管理',
        },
      },
      {
        path: 'order-manage/detail/:id',
        name: 'adminOrderDetail',
        component: () => import('@/views/admin/AdminOrderDetailView.vue'),
        meta: {
          title: '订单详情',
        },
      },
      {
        path: 'admin-manage',
        name: 'adminManage',
        component: () => import('@/views/admin/AdminManageView.vue'),
        meta: {
          title: '管理员管理',
        },
      },
      {
        path: 'user-manage',
        name: 'userManage',
        component: () => import('@/views/admin/UserManagementView.vue'),
        meta: {
          title: '用户管理',
        },
      },
      {
        path: 'seller-auth',
        name: 'sellerAuth',
        component: () => import('@/views/admin/SellerAuthView.vue'),
        meta: {
          title: '卖家认证审核',
        },
      },
      {
        path: 'category-manage',
        name: 'categoryManage',
        component: () => import('@/views/admin/CategoryManageView.vue'),
        meta: {
          title: '分类管理',
        },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to) => {
  const isLoginRoute = to.path === '/login'
  const loggedIn = hasValidStoredToken()

  if (isLoginRoute) {
    if (!loggedIn) {
      return true
    }

    const redirectPath =
      typeof to.query.redirect === 'string' && to.query.redirect && to.query.redirect !== '/login'
        ? to.query.redirect
        : '/dashboard'
    return redirectPath
  }

  if (!loggedIn) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  return true
})

export default router
