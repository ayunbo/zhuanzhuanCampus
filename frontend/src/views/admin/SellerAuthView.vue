<template>
  <el-card shadow="never" style="height: 100%">
    <el-container style="height: 100%">
      <el-header style="height: auto; padding-bottom: 18px">
        <el-form :inline="true" :model="queryForm" @submit.prevent="handleSearch">
          <el-form-item label="姓名">
            <el-input v-model="queryForm.name" clearable placeholder="认证姓名或用户昵称" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="queryForm.phone" clearable placeholder="支持模糊查询" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="学号">
            <el-input v-model="queryForm.studentNo" clearable placeholder="支持模糊查询" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button :loading="loading" @click="fetchList">刷新</el-button>
          </el-form-item>
        </el-form>
      </el-header>

      <el-main style="padding-top: 0; padding-bottom: 0; min-height: 0">
        <el-table v-loading="loading" :data="records" border height="100%">
          <el-table-column prop="id" label="ID" min-width="90" />
          <el-table-column prop="userName" label="用户昵称" min-width="140" />
          <el-table-column prop="realName" label="真实姓名" min-width="120" />
          <el-table-column prop="studentNo" label="学号" min-width="140" />
          <el-table-column prop="phone" label="手机号" min-width="140" />
          <el-table-column label="认证材料" min-width="120">
            <template #default="{ row }">
              <el-button v-if="isUrl(row.material)" link type="primary" @click="openMaterialPreview(row.material)">
                查看资料
              </el-button>
              <span v-else>{{ row.material || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">
                {{ statusLabel(row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="驳回原因" min-width="180" show-overflow-tooltip />
          <el-table-column label="提交时间" min-width="180">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" min-width="150">
            <template #default="{ row }">
              <el-space>
                <el-button
                  size="small"
                  type="primary"
                  :disabled="!canAudit(row) || actionLoadingId === String(row.id)"
                  @click="handleApprove(row)"
                >
                  通过
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  :disabled="!canAudit(row) || actionLoadingId === String(row.id)"
                  @click="handleReject(row)"
                >
                  驳回
                </el-button>
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
          :total="pager.total"
          background
          layout="total, sizes, prev, pager, next"
          @current-change="setPage"
          @size-change="handlePageSizeChange"
        />
      </el-footer>
    </el-container>
  </el-card>

  <el-dialog v-model="previewDialogVisible" title="认证材料预览" width="72%" top="5vh" destroy-on-close>
    <el-image
      v-if="previewType === 'image'"
      :src="previewUrl"
      fit="contain"
      style="display: block; width: 100%"
      @error="handleImagePreviewError"
    />
    <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" style="width: 100%; height: 68vh; border: 0" />
    <el-empty v-else description="当前材料暂不支持预览" />

      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="openMaterialInNewWindow">新窗口打开</el-button>
      </template>
    </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
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
  if (targetPage < 1 || targetPage > pageCount.value) {
    return
  }

  pager.page = targetPage
  fetchList()
}

function statusLabel(record) {
  return record.statusDesc || SELLER_AUTH_STATUS_LABEL_MAP[record.status] || '未知状态'
}

function statusTagType(status) {
  if (status === SELLER_AUTH_STATUS.PENDING) return 'warning'
  if (status === SELLER_AUTH_STATUS.APPROVED) return 'success'
  if (status === SELLER_AUTH_STATUS.REJECTED) return 'danger'
  return 'info'
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

  if (normalized.endsWith('.pdf')) {
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
