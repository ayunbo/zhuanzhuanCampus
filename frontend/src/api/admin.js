import request from '@/utils/request'

/**
 * 登录
 */
export function adminLogin(data) {
  return request.post('/admin/login', data)
}

// ================= 管理员管理 =================

export function createAdmin(data) {
  return request.post('/admin/admin', data)
}

export function fetchAdminPage(params) {
  return request.get('/admin/admin/page', { params })
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

// ================= 普通用户管理 =================

export function createUser(data) {
  return request.post('/admin/user', data)
}

export function fetchUserPage(params) {
  return request.get('/admin/user/page', { params })
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

// ================= 卖家认证审核 =================

export function fetchSellerAuthPage(params) {
  return request.get('/admin/seller-auth/page', { params })
}

export function auditSellerAuth(data) {
  return request.put('/admin/seller-auth/audit', data)
}
