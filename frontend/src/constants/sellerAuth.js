export const SELLER_AUTH_STATUS = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2,
  REVOKED: 3,
}

export const SELLER_AUTH_STATUS_LABEL_MAP = {
  [SELLER_AUTH_STATUS.PENDING]: '待审核',
  [SELLER_AUTH_STATUS.APPROVED]: '已通过',
  [SELLER_AUTH_STATUS.REJECTED]: '已驳回',
  [SELLER_AUTH_STATUS.REVOKED]: '已撤回',
}

export const SELLER_AUTH_STATUS_OPTIONS = [
  {
    label: '待审核',
    value: SELLER_AUTH_STATUS.PENDING,
  },
  {
    label: '已通过',
    value: SELLER_AUTH_STATUS.APPROVED,
  },
  {
    label: '已驳回',
    value: SELLER_AUTH_STATUS.REJECTED,
  },
  {
    label: '已撤回',
    value: SELLER_AUTH_STATUS.REVOKED,
  },
]
