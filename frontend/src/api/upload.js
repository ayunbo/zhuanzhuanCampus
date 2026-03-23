import request from '@/utils/request'

export function uploadGoodsImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', 'goods')

  return request.post('/user/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}
