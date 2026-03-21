import { defineStore } from 'pinia'
import { ROLE_LABEL_MAP } from '@/constants/auth'
import { clearStoredAuth, getStoredToken, getStoredUser, hasValidStoredToken, setStoredAuth } from '@/utils/auth'

function createEmptyUser() {
  return {
    id: null,
    username: '',
    studentNo: '',
    name: '',
    role: null,
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => {
    const validToken = hasValidStoredToken()
    const storedUser = validToken ? getStoredUser() : null

    return {
      token: validToken ? getStoredToken() : '',
      user: {
        ...createEmptyUser(),
        ...storedUser,
      },
    }
  },
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    isAdmin: (state) => state.user.role === 0,
    isSeller: (state) => state.user.role === 2,
    roleLabel: (state) => ROLE_LABEL_MAP[state.user.role] || '未知角色',
  },
  actions: {
    setLoginInfo(loginInfo) {
      this.token = loginInfo?.token || ''
      this.user = {
        id: loginInfo?.id ?? null,
        username: loginInfo?.username ?? '',
        studentNo: loginInfo?.studentNo ?? '',
        name: loginInfo?.name ?? '',
        role: loginInfo?.role ?? null,
      }

      setStoredAuth(loginInfo)
    },
    logout() {
      this.token = ''
      this.user = createEmptyUser()
      clearStoredAuth()
    },
  },
})
