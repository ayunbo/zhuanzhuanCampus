import axios from 'axios'
import { getStoredToken } from '@/utils/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const token = getStoredToken()

  if (token) {
    config.headers = config.headers || {}
    config.headers.token = token
  }

  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data

    if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'code')) {
      if (payload.code === 1) {
        return payload.data
      }

      const businessError = new Error(payload.msg || '请求失败')
      businessError.name = 'BusinessError'
      businessError.code = payload.code
      return Promise.reject(businessError)
    }

    return payload
  },
  (error) => {
    const statusCode = error.response?.status
    if (statusCode === 401) {
      return Promise.reject(new Error('未登录或登录已过期'))
    }

    const serverMessage = error.response?.data?.msg
    if (serverMessage) {
      return Promise.reject(new Error(serverMessage))
    }

    if (error.message) {
      return Promise.reject(new Error(error.message))
    }

    return Promise.reject(new Error('网络请求失败'))
  },
)

export default request
