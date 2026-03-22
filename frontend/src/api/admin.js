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

export function createCategory(data) {
  return request.post('/admin/category', data)
}

export function fetchCategoryPage(params) {
  return request.get('/admin/category/page', {
    params,
  })
}

export function fetchCategoryTree() {
  return request.get('/admin/category/tree')
}

export function updateCategory(data) {
  return request.put('/admin/category', data)
}

export function updateCategoryStatus(data) {
  return request.put('/admin/category/status', data)
}

export function updateCategorySort(data) {
  return request.put('/admin/category/sort', data)
}

export function deleteCategory(id) {
  return request.delete(`/admin/category/${id}`)
}

export function fetchAdminGoodsPage(params) {
  const nextParams = { ...params }
  if (!nextParams.keyword && typeof nextParams.title === 'string' && nextParams.title.trim()) {
    nextParams.keyword = nextParams.title.trim()
  }
  delete nextParams.title

  return request.get('/admin/goods', {
    params: nextParams,
  })
}

export function fetchAdminGoodsDetail(id) {
  return request.get(`/admin/goods/${id}`)
}

export function auditAdminGoods(data) {
  const goodsId = data?.goodsId
  const payload = {
    status: data?.status,
    reason: data?.reason,
  }

  return request.put(`/admin/goods/${goodsId}/audit`, payload)
}

export function getAdminOrderPage(params) {
  return request.get('/admin/order/page', {
    params,
  })
}

export function getAdminOrderDetail(id) {
  return request.get(`/admin/order/detail/${id}`)
}

export function updateAdminOrder(data) {
  return request.put('/admin/order/update', data)
}

export function updateAdminOrderStatus(data) {
  return request.put('/admin/order/status', data)
}

export function deleteAdminOrder(id) {
  return request.delete(`/admin/order/${id}`)
}
