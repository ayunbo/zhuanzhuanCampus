<template>
  <div class="seller-auth-page app-card fade-in-up">
    <section class="toolbar">
      <h3>卖家认证审核</h3>
      <button class="app-btn secondary" :disabled="loading" @click="fetchList">
        {{ loading ? '加载中...' : '刷新列表' }}
      </button>
    </section>

    <form class="filter-grid" @submit.prevent="handleSearch">
      <label>
        <span>姓名</span>
        <input v-model="queryForm.name" class="app-input" type="text" placeholder="认证姓名或用户昵称" />
      </label>

      <label>
        <span>手机号</span>
        <input v-model="queryForm.phone" class="app-input" type="text" placeholder="支持模糊查询" />
      </label>

      <label>
        <span>学号</span>
        <input v-model="queryForm.studentNo" class="app-input" type="text" placeholder="支持模糊查询" />
      </label>

      <label>
        <span>状态</span>
        <select v-model="queryForm.status" class="app-select">
          <option value="">全部</option>
          <option v-for="item in SELLER_AUTH_STATUS_OPTIONS" :key="item.value" :value="item.value">
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
            <th>用户昵称</th>
            <th>真实姓名</th>
            <th>学号</th>
            <th>手机号</th>
            <th>认证材料</th>
            <th>状态</th>
            <th>驳回原因</th>
            <th>提交时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && records.length === 0">
            <td class="empty-row" colspan="10">暂无数据</td>
          </tr>

          <tr v-for="record in records" :key="record.id">
            <td>{{ record.id }}</td>
            <td>{{ record.userName || '-' }}</td>
            <td>{{ record.realName || '-' }}</td>
            <td>{{ record.studentNo || '-' }}</td>
            <td>{{ record.phone || '-' }}</td>
            <td>
              <a
                v-if="isUrl(record.material)"
                class="material-link"
                :href="record.material"
                target="_blank"
                rel="noopener noreferrer"
              >
                查看材料
              </a>
              <span v-else>{{ record.material || '-' }}</span>
            </td>
            <td>
              <span class="status-badge" :class="statusClass(record.status)">
                {{ statusLabel(record) }}
              </span>
            </td>
            <td>{{ record.reason || '-' }}</td>
            <td>{{ formatDateTime(record.createTime) }}</td>
            <td class="actions">
              <button
                class="app-btn primary mini"
                :disabled="!canAudit(record) || actionLoadingId === record.id"
                @click="handleApprove(record)"
              >
                通过
              </button>
              <button
                class="app-btn danger mini"
                :disabled="!canAudit(record) || actionLoadingId === record.id"
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
import { auditSellerAuth, fetchSellerAuthPage } from '@/api/admin'
import {
  SELLER_AUTH_STATUS,
  SELLER_AUTH_STATUS_LABEL_MAP,
  SELLER_AUTH_STATUS_OPTIONS,
} from '@/constants/sellerAuth'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const actionLoadingId = ref(null)
const records = ref([])

const queryForm = reactive({
  name: '',
  phone: '',
  studentNo: '',
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

function buildQueryParams() {
  const params = {
    page: pager.page,
    pageSize: pager.pageSize,
  }

  const name = queryForm.name.trim()
  const phone = queryForm.phone.trim()
  const studentNo = queryForm.studentNo.trim()

  if (name) {
    params.name = name
  }

  if (phone) {
    params.phone = phone
  }

  if (studentNo) {
    params.studentNo = studentNo
  }

  if (queryForm.status !== '' && queryForm.status !== null) {
    params.status = Number(queryForm.status)
  }

  return params
}

async function fetchList() {
  loading.value = true

  try {
    const pageData = await fetchSellerAuthPage(buildQueryParams())
    records.value = Array.isArray(pageData?.records) ? pageData.records : []
    pager.total = Number(pageData?.total || 0)

    if (pager.page > pageCount.value) {
      pager.page = pageCount.value
      await fetchList()
    }
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
  queryForm.name = ''
  queryForm.phone = ''
  queryForm.studentNo = ''
  queryForm.status = ''
  pager.page = 1
  fetchList()
}

function handlePageSizeChange() {
  pager.page = 1
  fetchList()
}

function setPage(targetPage) {
  if (targetPage < 1 || targetPage > pageCount.value || targetPage === pager.page) {
    return
  }

  pager.page = targetPage
  fetchList()
}

function statusLabel(record) {
  return record.statusDesc || SELLER_AUTH_STATUS_LABEL_MAP[record.status] || '未知状态'
}

function statusClass(status) {
  if (status === SELLER_AUTH_STATUS.PENDING) {
    return 'status-pending'
  }

  if (status === SELLER_AUTH_STATUS.APPROVED) {
    return 'status-approved'
  }

  if (status === SELLER_AUTH_STATUS.REJECTED) {
    return 'status-rejected'
  }

  return 'status-revoked'
}

function canAudit(record) {
  return record.status === SELLER_AUTH_STATUS.PENDING
}

function isUrl(content) {
  return typeof content === 'string' && /^https?:\/\//i.test(content)
}

async function submitAudit(authId, status, reason = '') {
  actionLoadingId.value = authId

  try {
    await auditSellerAuth({
      authId,
      status,
      reason,
    })

    if (status === SELLER_AUTH_STATUS.APPROVED) {
      ElMessage.success(`申请 #${authId} 已通过`)
    } else {
      ElMessage.success(`申请 #${authId} 已驳回`)
    }

    await fetchList()
  } catch (error) {
    ElMessage.error(error.message || '审核操作失败')
  } finally {
    actionLoadingId.value = null
  }
}

async function handleApprove(record) {
  if (!canAudit(record)) {
    return
  }

  try {
    await ElMessageBox.confirm(`确认通过申请 #${record.id} 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消',
    })
    await submitAudit(record.id, SELLER_AUTH_STATUS.APPROVED)
  } catch {
    // 用户取消操作，不提示。
  }
}

async function handleReject(record) {
  if (!canAudit(record)) {
    return
  }

  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', `驳回申请 #${record.id}`, {
      inputPlaceholder: '驳回原因（必填）',
      inputPattern: /\S+/,
      inputErrorMessage: '驳回原因不能为空',
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
    })

    await submitAudit(record.id, SELLER_AUTH_STATUS.REJECTED, value.trim())
  } catch {
    // 用户取消操作，不提示。
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.seller-auth-page {
  padding: 18px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
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
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1080px;
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

.material-link {
  color: #175fbe;
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
  .filter-grid {
    grid-template-columns: 1fr;
  }

  .action-group {
    flex-wrap: wrap;
  }
}
</style>
