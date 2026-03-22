<template>
  <div class="product-page">
    <section class="product-main app-card">
      <form class="filter-row" @submit.prevent>
        <input v-model="filters.keyword" class="app-input" type="text" placeholder="搜索商品标题 / ID" />
        <select v-model="filters.category" class="app-select">
          <option value="all">全部分类</option>
        </select>
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
              <th>ID</th>
              <th>照片</th>
              <th>商品标题</th>
              <th>分类</th>
              <th>价格</th>
              <th>状态</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="pagedProducts.length === 0">
              <td colspan="8" class="empty-row">
                <div class="table-empty">
                  <el-icon><Box /></el-icon>
                  <span>暂无数据</span>
                </div>
              </td>
            </tr>
            <tr v-for="item in pagedProducts" :key="item.id">
              <td>{{ item.id }}</td>
              <td>
                <div class="thumb">
                  <img v-if="item.coverUrl" :src="item.coverUrl" alt="商品照片" />
                  <span v-else>暂无</span>
                </div>
              </td>
              <td>{{ item.title || '-' }}</td>
              <td>{{ item.category || '-' }}</td>
              <td>{{ typeof item.price === 'number' ? `¥${item.price}` : '-' }}</td>
              <td>
                <span class="status-badge" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span>
              </td>
              <td>{{ item.updateTime || '-' }}</td>
              <td>
                <div class="action-row">
                  <button class="app-btn mini ghost" type="button">查看</button>
                  <button class="app-btn mini secondary" type="button">下架</button>
                  <button class="app-btn mini danger" type="button">删除</button>
                  <button class="app-btn mini primary" type="button">审核</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="pagination">
        <div class="left">
          <span>共 {{ filteredProducts.length }} 条</span>
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

const statusOptions = [
  { value: 'to_review', label: '待审核' },
  { value: 'online', label: '销售中' },
  { value: 'offline', label: '已下架' },
  { value: 'rejected', label: '审核驳回' },
]

const filters = reactive({
  keyword: '',
  category: 'all',
  status: 'all',
})

const productList = ref([])
const pager = reactive({
  page: 1,
  pageSize: 10,
})

const activeStatus = computed(() => {
  const current = String(route.query.status || 'all')
  const valid = ['all', ...statusOptions.map((item) => item.value)]
  return valid.includes(current) ? current : 'all'
})

const filteredProducts = computed(() =>
  productList.value.filter((item) => {
    const routeMatch = activeStatus.value === 'all' ? true : item.status === activeStatus.value
    const filterMatch = filters.status === 'all' ? true : item.status === filters.status

    const keyword = filters.keyword.trim().toLowerCase()
    const keywordMatch = keyword
      ? String(item.id || '').toLowerCase().includes(keyword) ||
        String(item.title || '').toLowerCase().includes(keyword)
      : true

    const categoryMatch = filters.category === 'all' ? true : item.category === filters.category

    return routeMatch && filterMatch && keywordMatch && categoryMatch
  }),
)

const pageCount = computed(() => {
  const count = Math.ceil(filteredProducts.value.length / pager.pageSize)
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

const pagedProducts = computed(() => {
  const start = (pager.page - 1) * pager.pageSize
  return filteredProducts.value.slice(start, start + pager.pageSize)
})

function statusLabel(status) {
  const map = {
    all: '全部状态',
    to_review: '待审核',
    online: '销售中',
    offline: '已下架',
    rejected: '审核驳回',
  }
  return map[status] || '未知'
}

function statusClass(status) {
  if (status === 'online') return 'status-approved'
  if (status === 'to_review') return 'status-pending'
  if (status === 'offline') return 'status-revoked'
  return 'status-rejected'
}

function resetFilters() {
  filters.keyword = ''
  filters.category = 'all'
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

function initProducts() {
  // TODO: 接入商品管理接口后替换为真实数据加载
  productList.value = []
}

onMounted(() => {
  initProducts()
})

watch(
  () => [activeStatus.value, filters.keyword, filters.category, filters.status],
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
.product-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.product-main {
  padding: 14px;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.filter-row {
  display: grid;
  grid-template-columns: 1.2fr 180px 180px auto auto;
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
  min-width: 1120px;
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

.thumb {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  border: 1px dashed #c8d7ea;
  background: #f6fafe;
  display: grid;
  place-items: center;
  overflow: hidden;
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb span {
  font-size: 12px;
  color: #7d90ad;
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
