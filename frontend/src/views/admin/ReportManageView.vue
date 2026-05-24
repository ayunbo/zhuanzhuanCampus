<template>
  <el-card shadow="never" style="height: 100%">
    <el-container style="height: 100%">
      <el-header style="height: auto; padding-bottom: 18px">
        <el-form :inline="true" :model="queryForm" @submit.prevent="handleSearch">
          <el-form-item label="处理状态">
            <el-select v-model="queryForm.status" clearable placeholder="全部状态" style="width: 140px">
              <el-option
                v-for="item in REPORT_STATUS_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="String(item.value)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="举报对象">
            <el-select v-model="queryForm.targetType" clearable placeholder="全部对象" style="width: 140px">
              <el-option
                v-for="item in REPORT_TARGET_TYPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="String(item.value)"
              />
            </el-select>
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
          <el-table-column prop="id" label="举报ID" min-width="90" />
          <el-table-column label="举报人" min-width="150">
            <template #default="{ row }">
              {{ row.reportUserName || `用户 #${row.reportUserId}` }}
            </template>
          </el-table-column>
          <el-table-column label="对象类型" min-width="110">
            <template #default="{ row }">
              <el-tag effect="light">{{ targetTypeLabel(row.targetType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetId" label="对象ID" min-width="100" />
          <el-table-column label="举报原因" min-width="260" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.reason || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="处理状态" min-width="110">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" effect="light">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="举报时间" min-width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" min-width="260">
            <template #default="{ row }">
              <el-space wrap>
                <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
                <el-button
                  link
                  type="success"
                  :disabled="!canHandle(row) || actionLoadingId === String(row.id)"
                  @click="openHandleDialog(row)"
                >
                  处理
                </el-button>
                <el-button
                  link
                  type="warning"
                  :disabled="!canHandle(row) || actionLoadingId === String(row.id)"
                  @click="handleIgnore(row)"
                >
                  忽略
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
          :total="pager.total"
          :page-sizes="[10, 20, 30, 50]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </el-footer>
    </el-container>
  </el-card>

  <el-drawer v-model="detailVisible" title="举报详情" size="50%">
    <el-skeleton :loading="detailLoading" animated :rows="8">
      <template #default>
        <el-empty v-if="!detailData" />
        <el-space v-else direction="vertical" fill style="width: 100%">
          <el-card shadow="never">
            <template #header>举报信息</template>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="举报ID">{{ detailData.id }}</el-descriptions-item>
              <el-descriptions-item label="处理状态">
                <el-tag :type="statusTagType(detailData.status)" effect="light">
                  {{ statusLabel(detailData.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="举报对象类型">
                {{ targetTypeLabel(detailData.targetType) }}
              </el-descriptions-item>
              <el-descriptions-item label="举报对象ID">{{ detailData.targetId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="举报原因" :span="2">
                {{ detailData.reason || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="举报时间" :span="2">
                {{ formatDateTime(detailData.createTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card shadow="never">
            <template #header>举报人信息</template>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="用户ID">{{ detailData.reportUserId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="昵称">{{ detailData.reportUserName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="学号">{{ detailData.reportUserStudentNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ detailData.reportUserPhone || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card v-if="detailData.targetType === REPORT_TARGET_TYPE.GOODS" shadow="never">
            <template #header>被举报商品信息</template>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="商品ID">{{ detailData.targetId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="商品标题">
                {{ detailData.goodsTitle || detailData.targetName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="分类">{{ detailData.goodsCategoryName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                {{ detailData.goodsStatusDesc || goodsStatusLabel(detailData.goodsStatus) }}
              </el-descriptions-item>
              <el-descriptions-item label="价格">{{ formatPrice(detailData.goodsPrice) }}</el-descriptions-item>
              <el-descriptions-item label="面交地点">{{ detailData.goodsLocation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="商品描述" :span="2">
                {{ detailData.goodsDetail || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="封面地址" :span="2">
                {{ detailData.goodsCover || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card v-if="detailData.targetType === REPORT_TARGET_TYPE.GOODS" shadow="never">
            <template #header>商品发布人信息</template>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="卖家ID">{{ detailData.goodsSellerId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="昵称">{{ detailData.goodsSellerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="学号">{{ detailData.goodsSellerStudentNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ detailData.goodsSellerPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="校区">{{ detailData.goodsSellerCampus || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card v-if="detailData.targetType === REPORT_TARGET_TYPE.USER" shadow="never">
            <template #header>被举报用户/商家信息</template>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="用户ID">{{ detailData.targetId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="昵称">{{ detailData.targetName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="学号">{{ detailData.targetUserStudentNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ detailData.targetUserPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="角色">{{ userRoleLabel(detailData.targetUserRole) }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ userStatusLabel(detailData.targetUserStatus) }}</el-descriptions-item>
              <el-descriptions-item label="校区">{{ detailData.targetUserCampus || '-' }}</el-descriptions-item>
              <el-descriptions-item label="简介" :span="2">
                {{ detailData.targetUserIntro || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card v-if="detailData.targetType === REPORT_TARGET_TYPE.MESSAGE" shadow="never">
            <template #header>被举报消息信息</template>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="消息ID">{{ detailData.targetId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="消息内容" :span="2">
                {{ detailData.targetName || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card shadow="never">
            <template #header>处理信息</template>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="处理管理员">
                {{ detailData.handleAdminName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="处理时间">
                {{ formatDateTime(detailData.handleTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="处理结果" :span="2">
                {{ detailData.handleResult || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-space>
      </template>
    </el-skeleton>
  </el-drawer>

  <el-dialog v-model="handleDialogVisible" title="处理举报" width="520px" destroy-on-close>
    <el-form :model="handleForm" label-width="96px">
      <el-form-item label="举报ID">
        <el-input :model-value="String(handleTarget?.id || '')" disabled />
      </el-form-item>
      <el-form-item label="处理方式">
        <el-select v-model="handleForm.handleAction" placeholder="请选择处理方式" style="width: 100%">
          <el-option
            v-for="item in handleActionOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="处理结果">
        <el-input
          v-model="handleForm.handleResult"
          type="textarea"
          :rows="4"
          maxlength="255"
          show-word-limit
          placeholder="请输入处理说明"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button :disabled="handleSubmitting" @click="handleDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="handleSubmitting" @click="submitHandle">确认处理</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchReportDetail,
  fetchReportPage,
  handleReport,
  ignoreReport,
} from '@/api/admin'
import { formatDateTime } from '@/utils/format'

const route = useRoute()

const REPORT_STATUS = {
  PENDING: 0,
  HANDLED: 1,
  IGNORED: 2,
}

const REPORT_TARGET_TYPE = {
  GOODS: 1,
  USER: 2,
  MESSAGE: 3,
}

const REPORT_HANDLE_ACTION = {
  MARK_HANDLED: 1,
  OFF_SHELF_GOODS: 2,
  BAN_USER: 3,
}

const REPORT_STATUS_OPTIONS = [
  { label: '待处理', value: REPORT_STATUS.PENDING },
  { label: '已处理', value: REPORT_STATUS.HANDLED },
  { label: '已忽略', value: REPORT_STATUS.IGNORED },
]

const REPORT_TARGET_TYPE_OPTIONS = [
  { label: '商品', value: REPORT_TARGET_TYPE.GOODS },
  { label: '用户/商家', value: REPORT_TARGET_TYPE.USER },
  { label: '消息', value: REPORT_TARGET_TYPE.MESSAGE },
]

const REPORT_STATUS_LABEL_MAP = {
  [REPORT_STATUS.PENDING]: '待处理',
  [REPORT_STATUS.HANDLED]: '已处理',
  [REPORT_STATUS.IGNORED]: '已忽略',
}

const REPORT_TARGET_TYPE_LABEL_MAP = {
  [REPORT_TARGET_TYPE.GOODS]: '商品',
  [REPORT_TARGET_TYPE.USER]: '用户/商家',
  [REPORT_TARGET_TYPE.MESSAGE]: '消息',
}

const routeStatusMap = {
  all: '',
  pending: String(REPORT_STATUS.PENDING),
  handled: String(REPORT_STATUS.HANDLED),
  ignored: String(REPORT_STATUS.IGNORED),
}

const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const handleDialogVisible = ref(false)
const handleSubmitting = ref(false)
const actionLoadingId = ref(null)
const records = ref([])
const detailData = ref(null)
const handleTarget = ref(null)

const queryForm = reactive({
  status: '',
  targetType: '',
})

const pager = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const handleForm = reactive({
  handleAction: REPORT_HANDLE_ACTION.MARK_HANDLED,
  handleResult: '',
})

const handleActionOptions = computed(() => {
  const options = [{ label: '仅标记为已处理', value: REPORT_HANDLE_ACTION.MARK_HANDLED }]
  const targetType = Number(handleTarget.value?.targetType)

  if (targetType === REPORT_TARGET_TYPE.GOODS) {
    options.push({ label: '下架被举报商品', value: REPORT_HANDLE_ACTION.OFF_SHELF_GOODS })
  }

  if (targetType === REPORT_TARGET_TYPE.USER) {
    options.push({ label: '封禁被举报用户/商家', value: REPORT_HANDLE_ACTION.BAN_USER })
  }

  return options
})

function statusLabel(status) {
  return REPORT_STATUS_LABEL_MAP[status] || '未知状态'
}

function targetTypeLabel(targetType) {
  return REPORT_TARGET_TYPE_LABEL_MAP[targetType] || '未知对象'
}

function userRoleLabel(role) {
  if (role === 1) return '普通用户'
  if (role === 2) return '卖家'
  return '未知角色'
}

function userStatusLabel(status) {
  if (status === 1) return '正常'
  if (status === 2) return '封禁'
  return '未知状态'
}

function goodsStatusLabel(status) {
  const statusMap = {
    0: '草稿',
    1: '待审核',
    2: '已驳回',
    3: '在售',
    4: '锁定',
    5: '已售出',
    6: '已下架',
  }

  return statusMap[status] || '未知状态'
}

function formatPrice(value) {
  const amount = Number(value)
  if (!Number.isFinite(amount)) {
    return '-'
  }

  return `¥${amount.toFixed(2)}`
}

function statusTagType(status) {
  if (status === REPORT_STATUS.PENDING) return 'warning'
  if (status === REPORT_STATUS.HANDLED) return 'success'
  if (status === REPORT_STATUS.IGNORED) return 'info'
  return ''
}

function canHandle(record) {
  return Number(record?.status) === REPORT_STATUS.PENDING
}

function syncRouteFilters() {
  const routeStatus = typeof route.query.status === 'string' ? route.query.status : 'all'
  queryForm.status = Object.prototype.hasOwnProperty.call(routeStatusMap, routeStatus)
    ? routeStatusMap[routeStatus]
    : ''
}

function buildQueryParams() {
  const params = {
    page: pager.page,
    pageSize: pager.pageSize,
  }

  if (queryForm.status !== '' && queryForm.status !== null) {
    params.status = Number(queryForm.status)
  }

  if (queryForm.targetType !== '' && queryForm.targetType !== null) {
    params.targetType = Number(queryForm.targetType)
  }

  return params
}

async function fetchList() {
  loading.value = true

  try {
    const pageData = await fetchReportPage(buildQueryParams())
    records.value = Array.isArray(pageData?.records) ? pageData.records : []
    pager.total = Number(pageData?.total || 0)
  } catch (error) {
    ElMessage.error(error.message || '举报列表加载失败')
  } finally {
    loading.value = false
  }
}

async function openDetail(id) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null

  try {
    detailData.value = await fetchReportDetail(id)
  } catch (error) {
    ElMessage.error(error.message || '举报详情加载失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

function openHandleDialog(record) {
  if (!canHandle(record)) {
    return
  }

  handleTarget.value = record
  handleForm.handleAction = REPORT_HANDLE_ACTION.MARK_HANDLED
  handleForm.handleResult = ''
  handleDialogVisible.value = true
}

async function submitHandle() {
  if (!handleTarget.value?.id) {
    return
  }

  if (!handleForm.handleResult.trim()) {
    ElMessage.warning('请输入处理结果')
    return
  }

  handleSubmitting.value = true
  actionLoadingId.value = String(handleTarget.value.id)

  try {
    await handleReport(handleTarget.value.id, {
      handleAction: handleForm.handleAction,
      handleResult: handleForm.handleResult.trim(),
    })
    ElMessage.success('举报已处理')
    handleDialogVisible.value = false
    await fetchList()
    if (detailVisible.value && detailData.value?.id === handleTarget.value.id) {
      detailData.value = await fetchReportDetail(handleTarget.value.id)
    }
  } catch (error) {
    ElMessage.error(error.message || '处理举报失败')
  } finally {
    handleSubmitting.value = false
    actionLoadingId.value = null
  }
}

async function handleIgnore(record) {
  if (!canHandle(record)) {
    return
  }

  try {
    await ElMessageBox.confirm(`确认忽略举报 #${record.id} 吗？`, '忽略确认', {
      type: 'warning',
      confirmButtonText: '确认忽略',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  actionLoadingId.value = String(record.id)
  try {
    await ignoreReport(record.id)
    ElMessage.success('举报已忽略')
    await fetchList()
    if (detailVisible.value && detailData.value?.id === record.id) {
      detailData.value = await fetchReportDetail(record.id)
    }
  } catch (error) {
    ElMessage.error(error.message || '忽略举报失败')
  } finally {
    actionLoadingId.value = null
  }
}

function handleSearch() {
  pager.page = 1
  fetchList()
}

function handleReset() {
  queryForm.targetType = ''
  syncRouteFilters()
  pager.page = 1
  fetchList()
}

function handleCurrentChange(page) {
  pager.page = page
  fetchList()
}

function handleSizeChange(pageSize) {
  pager.page = 1
  pager.pageSize = pageSize
  fetchList()
}

watch(
  () => route.query.status,
  () => {
    syncRouteFilters()
    pager.page = 1
    fetchList()
  },
  { immediate: true },
)
</script>
