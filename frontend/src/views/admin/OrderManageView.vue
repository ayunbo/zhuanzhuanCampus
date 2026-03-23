<template>
  <div class="order-page">
    <section class="app-card order-panel">
      <form class="filter-row" @submit.prevent>
        <input v-model="filters.keyword" class="app-input" type="text" placeholder="搜索订单号 / 商品标题" />
        <input v-model="filters.buyer" class="app-input" type="text" placeholder="买家昵称" />
        <select v-model="filters.status" class="app-select">
          <option value="all">全部状态</option>
          <option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
        </select>
        <button class="app-btn primary" type="button">查询</button>
        <button class="app-btn ghost" type="button" @click="resetFilters">重置</button>
      </form>

      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>订单号</th>
              <th>商品</th>
              <th>买家</th>
              <th>金额</th>
              <th>状态</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="pagedOrders.length === 0">
              <td colspan="7" class="empty-row">
                <div class="table-empty">
                  <el-icon><Box /></el-icon>
                  <span>暂无数据</span>
                </div>
              </td>
            </tr>
            <tr v-for="item in pagedOrders" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.goodsTitle || '-' }}</td>
              <td>{{ item.buyerName || '-' }}</td>
              <td>{{ typeof item.amount === 'number' ? `¥${item.amount}` : '-' }}</td>
              <td>
                <span class="status-badge" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span>
              </td>
              <td>{{ item.updateTime || '-' }}</td>
              <td>
                <div class="action-row">
                  <button class="app-btn mini ghost" type="button">查看</button>
                  <button class="app-btn mini secondary" type="button">审核</button>
                  <button class="app-btn mini danger" type="button">关闭</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="pagination">
        <div class="left">
          <span>共 {{ filteredOrders.length }} 条</span>
          <select v-model.number="pager.pageSize" class="app-select page-size" @change="handlePageSizeChange">
            <option :value="10">10 / 页</option>
            <option :value="20">20 / 页</option>
            <option :value="30">30 / 页</option>
          </select>
        </div>

        <div class="pages">
          <button class="app-btn ghost mini" :disabled="pager.page <= 1" @click="setPage(pager.page - 1)">上一页</button>
          <button
            v-for="page in visiblePages"
            :key="page"
            class="app-btn mini"
            :class="page === pager.page ? 'primary' : 'ghost'"
            @click="setPage(page)"
          >
            {{ page }}
          </button>
          <button class="app-btn ghost mini" :disabled="pager.page >= pageCount" @click="setPage(pager.page + 1)">
            下一页
          </button>
        </div>
      </footer>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Box } from '@element-plus/icons-vue'

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

function statusClass(status) {
  if (status === 'shipped') return 'status-approved'
  if (status === 'pending_pay' || status === 'pending_ship') return 'status-pending'
  if (status === 'closed') return 'status-rejected'
  return 'status-revoked'
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

<style scoped>
.order-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.order-panel {
  padding: 14px;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.filter-row {
  display: grid;
  grid-template-columns: 1.2fr 1fr 180px auto auto;
  gap: 10px;
}

.table-wrap {
  margin-top: 12px;
  border: 1px solid var(--border);
  border-radius: 14px;
  overflow: auto;
  background: #fff;
  flex: 1 1 auto;
  height: 0;
  min-height: 0;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 980px;
}

.data-table th,
.data-table td {
  padding: 11px 10px;
  border-bottom: 1px solid var(--border);
  text-align: left;
}

.data-table th {
  background: #f3f8ff;
  color: #35557f;
}

.empty-row {
  padding: 0 !important;
  text-align: center;
}

.action-row {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.app-btn.mini {
  padding: 6px 9px;
  font-size: 12px;
}

.pagination {
  margin-top: 0;
  padding-top: 12px;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0) 0%, #f9fcff 100%);
  flex: 0 0 auto;
}

.pagination .left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-size {
  width: 110px;
}

.pages {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

@media (max-width: 1220px) {
  .filter-row {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
