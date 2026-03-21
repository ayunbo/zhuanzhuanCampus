<template>
  <div class="admin-goods-page app-card fade-in-up">
    <section class="toolbar">
      <h3>商品审核管理</h3>
      <button class="app-btn secondary" :disabled="loading" @click="fetchList">
        {{ loading ? '加载中...' : '刷新列表' }}
      </button>
    </section>

    <form class="filter-grid" @submit.prevent="handleSearch">
      <label>
        <span>标题</span>
        <input v-model="queryForm.title" class="app-input" type="text" placeholder="按标题模糊查询" />
      </label>

      <label>
        <span>卖家ID</span>
        <input v-model="queryForm.sellerId" class="app-input" type="number" min="1" placeholder="精确匹配" />
      </label>

      <label>
        <span>状态</span>
        <select v-model="queryForm.status" class="app-select">
          <option value="">全部</option>
          <option v-for="item in GOODS_STATUS_OPTIONS" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </label>

      <div class="action-group">
        <button class="app-btn primary" type="submit">查询</button>
        <button class="app-btn ghost" type="button" @click="handleReset">重置</button>
      </div>
    </form>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>卖家ID</th>
            <th>标题</th>
            <th>价格</th>
            <th>成色</th>
            <th>面交地点</th>
            <th>状态</th>
            <th>驳回原因</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && records.length === 0">
            <td class="empty-row" colspan="10">暂无数据</td>
          </tr>

          <tr v-for="record in records" :key="String(record.id)">
            <td>{{ record.id }}</td>
            <td>{{ record.sellerId }}</td>
            <td class="title-cell">{{ record.title || '-' }}</td>
            <td>￥{{ record.price ?? '-' }}</td>
            <td>{{ record.quality ?? '-' }}</td>
            <td>{{ record.location || '-' }}</td>
            <td>
              <span class="status-badge" :class="statusClass(record.status)">
                {{ statusLabel(record.status) }}
              </span>
            </td>
            <td>{{ record.reason || '-' }}</td>
            <td>{{ formatDateTime(record.updateTime) }}</td>
            <td class="actions">
              <button
                class="app-btn primary mini"
                :disabled="!canAudit(record) || actionLoadingId === String(record.id)"
                @click="handleApprove(record)"
              >
                通过
              </button>
              <button
                class="app-btn danger mini"
                :disabled="!canAudit(record) || actionLoadingId === String(record.id)"
                @click="handleReject(record)"
              >
                驳回
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <footer class="pagination">
      <div class="left">
        <span>共 {{ pager.total }} 条</span>
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditAdminGoods, fetchAdminGoodsPage } from '@/api/admin'
import { formatDateTime } from '@/utils/format'

const GOODS_STATUS = {
  DRAFT: 0,
  WAIT_AUDIT: 1,
  REJECTED: 2,
  ON_SALE: 3,
  LOCKED: 4,
  SOLD: 5,
  OFF_SHELF: 6,
}

const GOODS_STATUS_LABEL_MAP = {
  [GOODS_STATUS.DRAFT]: '草稿',
  [GOODS_STATUS.WAIT_AUDIT]: '待审核',
  [GOODS_STATUS.REJECTED]: '已驳回',
  [GOODS_STATUS.ON_SALE]: '在售',
  [GOODS_STATUS.LOCKED]: '锁定',
  [GOODS_STATUS.SOLD]: '已售出',
  [GOODS_STATUS.OFF_SHELF]: '已下架',
}

const GOODS_STATUS_OPTIONS = Object.entries(GOODS_STATUS_LABEL_MAP).map(([value, label]) => ({
  value: Number(value),
  label,
}))

const loading = ref(false)
const actionLoadingId = ref(null)
const records = ref([])

const queryForm = reactive({
  title: '',
  sellerId: '',
  status: '',
})

const pager = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const pageCount = computed(() => {
  const count = Math.ceil(pager.total / pager.pageSize)
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

function statusLabel(status) {
  return GOODS_STATUS_LABEL_MAP[status] || '未知状态'
}

function statusClass(status) {
  if (status === GOODS_STATUS.WAIT_AUDIT || status === GOODS_STATUS.LOCKED) return 'status-pending'
  if (status === GOODS_STATUS.ON_SALE || status === GOODS_STATUS.SOLD) return 'status-approved'
  if (status === GOODS_STATUS.REJECTED) return 'status-rejected'
  return 'status-revoked'
}

function canAudit(record) {
  return record.status === GOODS_STATUS.WAIT_AUDIT
}

function buildQueryParams() {
  const params = {
    page: pager.page,
    pageSize: pager.pageSize,
  }

  const title = queryForm.title.trim()
  if (title) params.title = title

  if (queryForm.sellerId) params.sellerId = Number(queryForm.sellerId)

  if (queryForm.status !== '' && queryForm.status !== null) {
    params.status = Number(queryForm.status)
  }

  return params
}

async function fetchList() {
  loading.value = true
  try {
    const pageData = await fetchAdminGoodsPage(buildQueryParams())
    records.value = Array.isArray(pageData?.records) ? pageData.records : []
    pager.total = Number(pageData?.total || 0)
  } catch (error) {
    ElMessage.error(error.message || '查询失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pager.page = 1
  fetchList()
}

function handleReset() {
  queryForm.title = ''
  queryForm.sellerId = ''
  queryForm.status = ''
  pager.page = 1
  fetchList()
}

function handlePageSizeChange() {
  pager.page = 1
  fetchList()
}

function setPage(targetPage) {
  if (targetPage < 1 || targetPage > pageCount.value || targetPage === pager.page) return
  pager.page = targetPage
  fetchList()
}

async function submitAudit(goodsId, status, reason = '') {
  actionLoadingId.value = String(goodsId)
  try {
    await auditAdminGoods({
      goodsId,
      status,
      reason,
    })
    ElMessage.success(status === GOODS_STATUS.ON_SALE ? '审核通过' : '已驳回')
    await fetchList()
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    actionLoadingId.value = null
  }
}

async function handleApprove(record) {
  if (!canAudit(record)) return
  try {
    await ElMessageBox.confirm(`确认通过商品 #${record.id} 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消',
    })
    await submitAudit(record.id, GOODS_STATUS.ON_SALE)
  } catch {
    // ignore cancel
  }
}

async function handleReject(record) {
  if (!canAudit(record)) return
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', `驳回商品 #${record.id}`, {
      inputPlaceholder: '驳回原因（必填）',
      inputPattern: /\S+/,
      inputErrorMessage: '驳回原因不能为空',
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
    })
    await submitAudit(record.id, GOODS_STATUS.REJECTED, value.trim())
  } catch {
    // ignore cancel
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.admin-goods-page {
  padding: 18px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.toolbar h3 {
  margin: 0;
  font-size: 18px;
  color: #5c3b1f;
}

.filter-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  align-items: end;
}

.filter-grid label {
  display: grid;
  gap: 8px;
}

.filter-grid span {
  font-size: 13px;
  color: var(--text-secondary);
}

.action-group {
  display: flex;
  gap: 8px;
}

.table-wrap {
  margin-top: 14px;
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: #fffdf8;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1220px;
}

.data-table th,
.data-table td {
  padding: 10px;
  border-bottom: 1px solid var(--border);
  text-align: left;
  vertical-align: top;
}

.data-table th {
  color: #6d4f2d;
  background: #fff5e4;
  position: sticky;
  top: 0;
}

.title-cell {
  max-width: 260px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-row {
  text-align: center;
  color: var(--text-secondary);
}

.actions {
  display: flex;
  gap: 6px;
}

.app-btn.mini {
  padding: 6px 9px;
  font-size: 12px;
}

.pagination {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
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

@media (max-width: 1180px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .admin-goods-page {
    padding: 14px;
  }

  .filter-grid {
    grid-template-columns: 1fr;
  }

  .action-group {
    flex-wrap: wrap;
  }
}
</style>
