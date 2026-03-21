export const GOODS_STATUS = {
  DRAFT: 0,
  WAIT_AUDIT: 1,
  REJECTED: 2,
  ON_SALE: 3,
  LOCKED: 4,
  SOLD: 5,
  OFF_SHELF: 6,
}

export const GOODS_STATUS_LABEL_MAP = {
  [GOODS_STATUS.DRAFT]: '草稿',
  [GOODS_STATUS.WAIT_AUDIT]: '待审核',
  [GOODS_STATUS.REJECTED]: '已驳回',
  [GOODS_STATUS.ON_SALE]: '在售',
  [GOODS_STATUS.LOCKED]: '锁定',
  [GOODS_STATUS.SOLD]: '已售出',
  [GOODS_STATUS.OFF_SHELF]: '已下架',
}

export const GOODS_STATUS_OPTIONS = Object.entries(GOODS_STATUS_LABEL_MAP).map(([value, label]) => ({
  value: Number(value),
  label,
}))
