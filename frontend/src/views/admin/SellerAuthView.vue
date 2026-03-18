<template>
  <div class="seller-page">
    <section class="page-head app-card">
      <div>
        <p class="head-tag">Seller Verification</p>
        <h2>卖家认证审核</h2>
      </div>
      <button class="app-btn secondary" :disabled="loading" @click="fetchList">
        {{ loading ? '加载中...' : '刷新列表' }}
      </button>
    </section>

    <section class="metric-grid">
      <article class="metric-card app-card">
        <span>当前页申请</span>
        <strong>{{ records.length }}</strong>
      </article>
      <article class="metric-card app-card">
        <span>待审核</span>
        <strong>{{ summary.pending }}</strong>
      </article>
      <article class="metric-card app-card">
        <span>已通过</span>
        <strong>{{ summary.approved }}</strong>
      </article>
      <article class="metric-card app-card">
        <span>已驳回</span>
        <strong>{{ summary.rejected }}</strong>
      </article>
    </section>

    <section class="table-panel app-card">
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

            <tr v-for="record in records" :key="String(record.id)">
              <td>{{ record.id }}</td>
              <td>{{ record.userName || '-' }}</td>
              <td>{{ record.realName || '-' }}</td>
              <td>{{ record.studentNo || '-' }}</td>
              <td>{{ record.phone || '-' }}</td>
              <td>
                <button
                  v-if="isUrl(record.material)"
                  class="material-link"
                  type="button"
                  @click="openMaterialPreview(record.material)"
                >
                  查看资料
                </button>
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
    </section>

    <el-dialog v-model="previewDialogVisible" title="认证材料预览" width="72%" top="5vh" destroy-on-close>
      <div class="preview-wrap">
        <img
          v-if="previewType === 'image'"
          :src="previewUrl"
          alt="认证材料"
          class="preview-image"
          @error="handleImagePreviewError"
        />

        <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" class="preview-frame" />

        <div v-else class="preview-tip">当前材料类型暂不支持内嵌预览，请点击“新窗口打开”查看。</div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="previewDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="openMaterialInNewWindow">新窗口打开</el-button>
        </div>
      </template>
    </el-dialog>
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
const previewDialogVisible = ref(false)
const previewUrl = ref('')
const previewType = ref('other')

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

const summary = computed(() => ({
  pending: records.value.filter((item) => Number(item.status) === SELLER_AUTH_STATUS.PENDING).length,
  approved: records.value.filter((item) => Number(item.status) === SELLER_AUTH_STATUS.APPROVED).length,
  rejected: records.value.filter((item) => Number(item.status) === SELLER_AUTH_STATUS.REJECTED).length,
}))

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

function detectPreviewType(url) {
  const normalized = String(url).split('?')[0].toLowerCase()

  if (/\.(png|jpe?g|gif|webp|bmp|svg)$/.test(normalized)) {
    return 'image'
  }

  if (/\.pdf$/.test(normalized)) {
    return 'pdf'
  }

  return 'other'
}

function openMaterialPreview(material) {
  if (!isUrl(material)) {
    ElMessage.warning('材料链接无效')
    return
  }

  previewUrl.value = material
  previewType.value = detectPreviewType(material)
  previewDialogVisible.value = true
}

function openMaterialInNewWindow() {
  if (!previewUrl.value) {
    return
  }

  window.open(previewUrl.value, '_blank', 'noopener,noreferrer')
}

function handleImagePreviewError() {
  ElMessage.warning('图片预览失败，请尝试新窗口打开')
}

async function submitAudit(authId, status, reason = '') {
  actionLoadingId.value = String(authId)

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
    await submitAudit(String(record.id), SELLER_AUTH_STATUS.APPROVED)
  } catch {
    // 用户取消
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

    await submitAudit(String(record.id), SELLER_AUTH_STATUS.REJECTED, value.trim())
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.seller-page {
  display: grid;
  gap: 12px;
}

.page-head {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.head-tag {
  margin: 0;
  font-family: 'Lexend', sans-serif;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 11px;
  color: var(--text-light);
}

.page-head h2 {
  margin: 2px 0 0;
  font-size: 26px;
  color: #24446f;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.metric-card {
  padding: 14px;
  display: grid;
  gap: 4px;
}

.metric-card span {
  color: var(--text-secondary);
  font-size: 12px;
}

.metric-card strong {
  font-family: 'Lexend', sans-serif;
  color: #1f467a;
  font-size: 26px;
  line-height: 1;
}

.table-panel {
  padding: 14px;
}

.filter-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: end;
}

.filter-grid label {
  display: grid;
  gap: 6px;
  flex: 1 1 210px;
  min-width: 180px;
}

.filter-grid span {
  color: #4a648c;
  font-size: 13px;
  font-weight: 600;
}

.action-group {
  display: flex;
  gap: 8px;
  margin-left: auto;
  justify-content: flex-end;
}

.table-wrap {
  margin-top: 12px;
  border: 1px solid var(--border);
  border-radius: 14px;
  overflow: auto;
  background: #ffffff;
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
  vertical-align: middle;
}

.data-table th {
  color: #35557f;
  background: #f3f8ff;
  font-weight: 700;
  position: sticky;
  top: 0;
}

.data-table tbody tr:hover {
  background: #f9fcff;
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
  border: 0;
  background: transparent;
  color: #1f6fd8;
  cursor: pointer;
  padding: 0;
  font-weight: 700;
  text-decoration: underline;
}

.pagination {
  margin-top: 12px;
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

.preview-wrap {
  min-height: 360px;
  max-height: 72vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: auto;
  background: #f6fbff;
  border-radius: 12px;
  border: 1px solid var(--border);
}

.preview-image {
  display: block;
  max-width: 100%;
  max-height: 68vh;
  object-fit: contain;
}

.preview-frame {
  width: 100%;
  height: 68vh;
  border: 0;
}

.preview-tip {
  color: #56719b;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1180px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .page-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }

  .action-group {
    margin-left: 0;
    width: 100%;
    justify-content: flex-start;
  }

  .action-group {
    flex-wrap: wrap;
  }
}
</style>
