import request from '@/utils/request'

function normalizeKeywordParams(params = {}) {
  const nextParams = { ...params }
  if (!nextParams.keyword && typeof nextParams.title === 'string' && nextParams.title.trim()) {
    nextParams.keyword = nextParams.title.trim()
  }
  delete nextParams.title
  return nextParams
}

function normalizeGoodsPayload(data = {}) {
  const nextData = { ...data }
  const cover = typeof nextData.cover === 'string' ? nextData.cover.trim() : ''

  if (!Array.isArray(nextData.imageUrls)) {
    nextData.imageUrls = cover ? [cover] : []
  }

  delete nextData.cover
  return nextData
}

export function fetchGoodsPage(params) {
  return request.get('/user/goods', {
    params: normalizeKeywordParams(params),
  })
}

export function fetchGoodsDetail(id) {
  return request.get(`/user/goods/${id}`)
}

export function fetchSellerGoodsPage(params) {
  return request.get('/user/seller/goods', {
    params: normalizeKeywordParams(params),
  })
}

export function createSellerGoodsDraft(data) {
  return request.post('/user/seller/goods', normalizeGoodsPayload(data))
}

export function updateSellerGoodsDraft(id, data) {
  return request.put(`/user/seller/goods/${id}`, normalizeGoodsPayload(data))
}

export function deleteSellerGoods(id) {
  return request.delete(`/user/seller/goods/${id}`)
}

export function submitSellerGoodsAudit(id) {
  return request.put(`/user/seller/goods/${id}/submit`)
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
