import { AUTH_STORAGE_KEYS } from '@/constants/auth'

function safeParseUser(rawUser) {
  if (!rawUser) {
    return null
  }

  try {
    const parsed = JSON.parse(rawUser)
    if (parsed && typeof parsed === 'object') {
      return parsed
    }
  } catch {
    return null
  }

  return null
}

export function getStoredToken() {
  return localStorage.getItem(AUTH_STORAGE_KEYS.token) || ''
}

export function getStoredUser() {
  return safeParseUser(localStorage.getItem(AUTH_STORAGE_KEYS.user))
}

export function setStoredAuth(loginInfo) {
  if (!loginInfo?.token) {
    clearStoredAuth()
    return
  }

  const normalizedUser = {
    id: loginInfo.id ?? null,
    username: loginInfo.username ?? '',
    studentNo: loginInfo.studentNo ?? '',
    name: loginInfo.name ?? '',
    role: loginInfo.role ?? null,
  }

  localStorage.setItem(AUTH_STORAGE_KEYS.token, loginInfo.token)
  localStorage.setItem(AUTH_STORAGE_KEYS.user, JSON.stringify(normalizedUser))
}

export function clearStoredAuth() {
  localStorage.removeItem(AUTH_STORAGE_KEYS.token)
  localStorage.removeItem(AUTH_STORAGE_KEYS.user)
}
