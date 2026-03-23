<template>
  <div class="seller-page">
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

        <div class="action-group">
          <button class="app-btn primary" type="submit">查询</button>
          <button class="app-btn ghost" type="button" @click="handleReset">重置</button>
          <button class="app-btn secondary" type="button" :disabled="loading" @click="fetchList">
            {{ loading ? '加载中...' : '刷新列表' }}
          </button>
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
              <td class="empty-row" colspan="10">
                <div class="table-empty">
                  <el-icon><Box /></el-icon>
                  <span>暂无数据</span>
                </div>
              </td>
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
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box } from '@element-plus/icons-vue'
import { auditSellerAuth, fetchSellerAuthPage } from '@/api/admin'
import { SELLER_AUTH_STATUS, SELLER_AUTH_STATUS_LABEL_MAP } from '@/constants/sellerAuth'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
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

const routeStatusMap = {
  pending: SELLER_AUTH_STATUS.PENDING,
  approved: SELLER_AUTH_STATUS.APPROVED,
  rejected: SELLER_AUTH_STATUS.REJECTED,
  revoked: SELLER_AUTH_STATUS.REVOKED,
}

function resolveStatusFromRoute(routeStatus) {
  const raw = Array.isArray(routeStatus) ? routeStatus[0] : routeStatus

  if (raw === undefined || raw === null || raw === '' || raw === 'all') {
    return ''
  }

  if (Object.prototype.hasOwnProperty.call(routeStatusMap, raw)) {
    return routeStatusMap[raw]
  }

  const numberStatus = Number(raw)
  const validStatus = Object.values(SELLER_AUTH_STATUS)
  if (Number.isInteger(numberStatus) && validStatus.includes(numberStatus)) {
    return numberStatus
  }

  return ''
}

function syncStatusFromRoute() {
  queryForm.status = resolveStatusFromRoute(route.query.status)
}

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
  syncStatusFromRoute()
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

watch(
  () => route.query.status,
  () => {
    syncStatusFromRoute()
    pager.page = 1
    fetchList()
  },
  { immediate: true },
)
</script>

<style scoped>
.seller-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.table-panel {
  padding: 14px;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
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
  padding: 0 !important;
  text-align: center;
}

.actions {
  display: flex;
  gap: 6px;
  justify-content: flex-end;
  white-space: nowrap;
}

.data-table th:last-child,
.data-table td:last-child {
  text-align: right;
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

@media (max-width: 760px) {
  .action-group {
    margin-left: 0;
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>
