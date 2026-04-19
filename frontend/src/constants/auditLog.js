export const AUDIT_LOG_OPERATION_TYPE = {
  GOODS_AUDIT: 1,
  SELLER_AUTH_AUDIT: 2,
  REPORT_HANDLE: 3,
}

export const AUDIT_LOG_OPERATION_TYPE_OPTIONS = [
  {
    label: '商品审核',
    value: AUDIT_LOG_OPERATION_TYPE.GOODS_AUDIT,
  },
  {
    label: '卖家认证审核',
    value: AUDIT_LOG_OPERATION_TYPE.SELLER_AUTH_AUDIT,
  },
  {
    label: '举报处理',
    value: AUDIT_LOG_OPERATION_TYPE.REPORT_HANDLE,
  },
]

export const AUDIT_LOG_OPERATION_TYPE_LABEL_MAP = AUDIT_LOG_OPERATION_TYPE_OPTIONS.reduce(
  (result, item) => {
    result[item.value] = item.label
    return result
  },
  {},
)
