import request from '@/utils/request'

export function submitPay(data) {
  return request.post('/user/pay/submit', data)
}

export function queryPayStatus(orderId) {
  return request.get(`/user/pay/status/${orderId}`)
}

export function mockPay(data) {
  return request.post('/user/pay/mock', data)
}
