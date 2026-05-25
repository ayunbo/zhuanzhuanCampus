import request from '@/utils/request'

export function uploadGoodsImage(file, goodsId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', 'goods')
  if (goodsId !== null && goodsId !== undefined && goodsId !== '') {
    formData.append('goodsId', String(goodsId))
  }

  return request.post('/user/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}
