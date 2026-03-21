import request from '@/utils/request'

export function fetchGoodsPage(params) {
  return request.get('/user/goods/page', {
    params,
  })
}

export function fetchGoodsDetail(id) {
  return request.get(`/user/goods/${id}`)
}

export function updateGoodsStats(data) {
  return request.put('/user/goods/stats', data)
}

export function fetchSellerGoodsPage(params) {
  return request.get('/user/seller/goods/page', {
    params,
  })
}

export function createSellerGoodsDraft(data) {
  return request.post('/user/seller/goods/draft', data)
}

export function updateSellerGoodsDraft(id, data) {
  return request.put(`/user/seller/goods/draft/${id}`, data)
}

export function submitSellerGoodsAudit(id) {
  return request.put(`/user/seller/goods/${id}/submit-audit`)
}

export function onShelfSellerGoods(id) {
  return request.put(`/user/seller/goods/${id}/on-shelf`)
}

export function offShelfSellerGoods(id) {
  return request.put(`/user/seller/goods/${id}/off-shelf`)
}

export function soldSellerGoods(id) {
  return request.put(`/user/seller/goods/${id}/sold`)
}
