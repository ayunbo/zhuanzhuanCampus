import axios from 'axios'
import router from '@/router'
import { clearStoredAuth, getStoredToken, isTokenExpired } from '@/utils/auth'

let redirectingToLogin = false

function isLoginRoutePath(path) {
  return typeof path === 'string' && path.startsWith('/login')
}

function redirectToLogin() {
  if (redirectingToLogin) {
    return
  }

  redirectingToLogin = true
  clearStoredAuth()

  const currentFullPath = router.currentRoute.value?.fullPath || '/'
  const shouldKeepRedirect = !isLoginRoutePath(currentFullPath)

  router
    .replace({
      path: '/login',
      query: shouldKeepRedirect ? { redirect: currentFullPath } : {},
    })
    .finally(() => {
      redirectingToLogin = false
    })
}

function containsUnauthKeyword(message) {
  if (!message || typeof message !== 'string') {
    return false
  }

  const lower = message.toLowerCase()

  return (
    lower.includes('未登录') ||
    lower.includes('登录过期') ||
    lower.includes('重新登录') ||
    lower.includes('token') ||
    lower.includes('unauthorized') ||
    lower.includes('expired') ||
    lower.includes('auth')
  )
}

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const token = getStoredToken()

  if (token && !isTokenExpired(token)) {
    config.headers = config.headers || {}
    config.headers.token = token
  } else if (token) {
    clearStoredAuth()
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

      if (containsUnauthKeyword(payload.msg)) {
        redirectToLogin()
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
      redirectToLogin()
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
