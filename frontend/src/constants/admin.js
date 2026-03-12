export const ADMIN_STATUS = {
  NORMAL: 1,
  DISABLED: 2,
}

export const ADMIN_STATUS_LABEL_MAP = {
  [ADMIN_STATUS.NORMAL]: '正常',
  [ADMIN_STATUS.DISABLED]: '禁用',
}

export const ADMIN_STATUS_OPTIONS = [
  {
    label: '正常',
    value: ADMIN_STATUS.NORMAL,
  },
  {
    label: '禁用',
    value: ADMIN_STATUS.DISABLED,
  },
]
