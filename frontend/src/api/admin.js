import request from '@/utils/request'

export function adminLogin(data) {
  return request.post('/admin/login', data)
}

export function createAdmin(data) {
  return request.post('/admin/admin', data)
}

export function fetchAdminPage(params) {
  return request.get('/admin/admin/page', {
    params,
  })
}

export function fetchAdminById(id) {
  return request.get(`/admin/admin/${id}`)
}

export function updateAdmin(data) {
  return request.put('/admin/admin', data)
}

export function deleteAdmin(id) {
  return request.delete(`/admin/admin/${id}`)
}

export function createUser(data) {
  return request.post('/admin/user', data)
}

export function fetchUserPage(params) {
  return request.get('/admin/user/page', {
    params,
  })
}

export function fetchUserById(id) {
  return request.get(`/admin/user/${id}`)
}

export function updateUser(data) {
  return request.put('/admin/user', data)
}

export function deleteUser(id) {
  return request.delete(`/admin/user/${id}`)
}

export function fetchSellerAuthPage(params) {
  return request.get('/admin/seller-auth/page', {
    params,
  })
}

export function auditSellerAuth(data) {
  return request.put('/admin/seller-auth/audit', data)
}

// =========================
// 管理员订单模块


// 订单分页查询
export function getAdminOrderPage(params) {
  return request({
    url: '/admin/order/page',
    method: 'get',
    params,
  })
}

// 订单详情
export function getAdminOrderDetail(id) {
  return request({
    url: `/admin/order/detail/${id}`,
    method: 'get',
  })
}

// 编辑订单交易信息
export function updateAdminOrder(data) {
  return request({
    url: '/admin/order/update',
    method: 'put',
    data,
  })
}

// 修改订单状态
export function updateAdminOrderStatus(data) {
  return request({
    url: '/admin/order/status',
    method: 'put',
    data,
  })
}

// 删除订单
export function deleteAdminOrder(id) {
  return request({
    url: `/admin/order/${id}`,
    method: 'delete',
  })
}
