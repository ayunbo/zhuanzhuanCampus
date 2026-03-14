import { createRouter, createWebHistory } from 'vue-router'

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
    redirect: '/dashboard',
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
        path: 'admin/order',
        name: 'admin-order',
        component: () => import('@/views/admin/AdminOrderListView.vue'),
        meta: {
          title: '订单管理',
        },
      },
      {
        path: 'admin/order/detail/:id',
        name: 'admin-order-detail',
        component: () => import('@/views/admin/AdminOrderDetailView.vue'),
        meta: {
          title: '订单详情',
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

export default router
