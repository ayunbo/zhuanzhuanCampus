import request from '@/utils/request'

export function userLogin(data) {
  return request.post('/user/login', data)
}
