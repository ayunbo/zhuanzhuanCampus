<template>
  <el-card shadow="never" style="height: 100%">
    <el-container style="height: 100%">
      <el-header style="height: auto; padding-bottom: 18px">
        <el-form :inline="true" :model="filters">
          <el-form-item label="关键字">
            <el-input v-model="filters.keyword" clearable placeholder="搜索订单号 / 商品标题" />
          </el-form-item>
          <el-form-item label="买家">
            <el-input v-model="filters.buyer" clearable placeholder="买家昵称" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status">
              <el-option value="all" label="全部状态" />
              <el-option v-for="item in statusOptions" :key="item.value" :value="item.value" :label="item.label" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </el-header>

      <el-main style="padding-top: 0; padding-bottom: 0; min-height: 0">
        <el-table :data="pagedOrders" border height="100%">
          <el-table-column prop="id" label="订单号" min-width="150" />
          <el-table-column prop="goodsTitle" label="商品" min-width="180" />
          <el-table-column prop="buyerName" label="买家" min-width="120" />
          <el-table-column label="金额" min-width="110">
            <template #default="{ row }">{{ typeof row.amount === 'number' ? `¥${row.amount}` : '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" min-width="120">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" min-width="180" />
          <el-table-column label="操作" fixed="right" min-width="180">
            <template #default>
              <el-space>
                <el-button size="small">查看</el-button>
                <el-button size="small">审核</el-button>
                <el-button size="small" type="danger">关闭</el-button>
              </el-space>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty />
          </template>
        </el-table>
      </el-main>

      <el-footer style="height: auto; padding-top: 16px; padding-bottom: 0">
        <el-pagination
          v-model:current-page="pager.page"
          v-model:page-size="pager.pageSize"
          :page-sizes="[10, 20, 30]"
          :total="filteredOrders.length"
          background
          layout="total, sizes, prev, pager, next"
          @current-change="setPage"
          @size-change="handlePageSizeChange"
        />
      </el-footer>
    </el-container>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const orderList = ref([])
const pager = reactive({
  page: 1,
  pageSize: 10,
})

const statusOptions = [
  { value: 'pending_pay', label: '待买家付款' },
  { value: 'pending_ship', label: '待发货' },
  { value: 'shipped', label: '已发货' },
  { value: 'refunding', label: '退款中' },
  { value: 'closed', label: '交易关闭' },
]

const filters = reactive({
  keyword: '',
  buyer: '',
  status: 'all',
})

const activeStatus = computed(() => {
  const current = String(route.query.status || 'all')
  const valid = ['all', ...statusOptions.map((item) => item.value)]
  return valid.includes(current) ? current : 'all'
})

const filteredOrders = computed(() =>
  orderList.value.filter((item) => {
    const routeMatch = activeStatus.value === 'all' ? true : item.status === activeStatus.value
    const filterMatch = filters.status === 'all' ? true : item.status === filters.status

    const keyword = filters.keyword.trim().toLowerCase()
    const keywordMatch = keyword
      ? String(item.id || '').toLowerCase().includes(keyword) ||
        String(item.goodsTitle || '').toLowerCase().includes(keyword)
      : true

    const buyer = filters.buyer.trim().toLowerCase()
    const buyerMatch = buyer ? String(item.buyerName || '').toLowerCase().includes(buyer) : true

    return routeMatch && filterMatch && keywordMatch && buyerMatch
  }),
)

const pageCount = computed(() => {
  const count = Math.ceil(filteredOrders.value.length / pager.pageSize)
  return count > 0 ? count : 1
})

const visiblePages = computed(() => {
  const max = pageCount.value
  const current = pager.page
  const pages = []

  let start = Math.max(1, current - 2)
  let end = Math.min(max, start + 4)

  if (end - start < 4) {
    start = Math.max(1, end - 4)
  }

  for (let page = start; page <= end; page += 1) {
    pages.push(page)
  }

  return pages
})

const pagedOrders = computed(() => {
  const start = (pager.page - 1) * pager.pageSize
  return filteredOrders.value.slice(start, start + pager.pageSize)
})

function statusLabel(status) {
  const map = {
    all: '全部状态',
    pending_pay: '待买家付款',
    pending_ship: '待发货',
    shipped: '已发货',
    refunding: '退款中',
    closed: '交易关闭',
  }
  return map[status] || '未知状态'
}

function statusTagType(status) {
  if (status === 'shipped') return 'success'
  if (status === 'pending_pay' || status === 'pending_ship') return 'warning'
  if (status === 'closed') return 'danger'
  return 'info'
}

function resetFilters() {
  filters.keyword = ''
  filters.buyer = ''
  filters.status = 'all'
  pager.page = 1
}

function handlePageSizeChange() {
  pager.page = 1
}

function setPage(targetPage) {
  if (targetPage < 1 || targetPage > pageCount.value || targetPage === pager.page) {
    return
  }

  pager.page = targetPage
}

function initOrders() {
  // TODO: 接入订单管理接口后替换为真实数据加载
  orderList.value = []
}

onMounted(() => {
  initOrders()
})

watch(
  () => [activeStatus.value, filters.keyword, filters.buyer, filters.status],
  () => {
    pager.page = 1
  },
)

watch(
  () => pageCount.value,
  (value) => {
    if (pager.page > value) {
      pager.page = value
    }
  },
)
</script>
