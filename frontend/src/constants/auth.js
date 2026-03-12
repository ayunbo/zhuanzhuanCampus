export const ADMIN_ROLE = 0
export const NORMAL_USER_ROLE = 1
export const SELLER_ROLE = 2

export const ROLE_LABEL_MAP = {
  [ADMIN_ROLE]: '管理员',
  [NORMAL_USER_ROLE]: '普通用户',
  [SELLER_ROLE]: '卖家',
}

export const AUTH_STORAGE_KEYS = {
  token: 'zhuanzhuan_admin_token',
  user: 'zhuanzhuan_admin_user',
}
