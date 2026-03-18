import { AUTH_STORAGE_KEYS } from '@/constants/auth'

const JWT_PARTS_LENGTH = 3

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

function decodeBase64Url(value) {
  if (!value || typeof value !== 'string') {
    return null
  }

  const normalized = value.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized + '='.repeat((4 - (normalized.length % 4 || 4)) % 4)

  try {
    return decodeURIComponent(
      atob(padded)
        .split('')
        .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join(''),
    )
  } catch {
    return null
  }
}

function parseJwtPayload(token) {
  if (!token || typeof token !== 'string') {
    return null
  }

  const segments = token.split('.')
  if (segments.length !== JWT_PARTS_LENGTH) {
    return null
  }

  const payloadRaw = decodeBase64Url(segments[1])
  if (!payloadRaw) {
    return null
  }

  try {
    const payload = JSON.parse(payloadRaw)
    return payload && typeof payload === 'object' ? payload : null
  } catch {
    return null
  }
}

export function isTokenExpired(token = getStoredToken()) {
  if (!token) {
    return true
  }

  const payload = parseJwtPayload(token)
  if (!payload || typeof payload.exp !== 'number') {
    return true
  }

  return Date.now() >= payload.exp * 1000
}

export function hasValidStoredToken() {
  const token = getStoredToken()
  if (!token) {
    return false
  }

  if (isTokenExpired(token)) {
    clearStoredAuth()
    return false
  }

  return true
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
