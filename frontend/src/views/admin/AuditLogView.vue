<template>
  <div class="audit-log-page">
    <el-card class="audit-log-card" shadow="never">
      <template #header>
        <el-space>
          <el-icon><Document /></el-icon>
          <span>日志列表</span>
          <el-button :icon="RefreshRight" :loading="loading" @click="fetchList">刷新</el-button>
        </el-space>
      </template>

      <div class="audit-log-table-wrap">
        <el-table v-loading="loading" :data="records" border height="100%">
          <el-table-column prop="id" label="日志ID" min-width="90" />
          <el-table-column label="操作管理员" min-width="140">
            <template #default="{ row }">
              {{ row.adminName || `管理员 #${row.adminId}` }}
            </template>
          </el-table-column>
          <el-table-column label="操作类型" min-width="130">
            <template #default="{ row }">
              <el-tag effect="light">
                {{ row.operationTypeDesc || operationTypeLabel(row.operationType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核对象" min-width="320" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.targetSummary || `对象 #${row.targetId}` }}
            </template>
          </el-table-column>
          <el-table-column label="操作动作" min-width="100">
            <template #default="{ row }">
              {{ row.action || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.detail || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作时间" min-width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="详情" fixed="right" min-width="110">
            <template #default="{ row }">
              <el-button link type="primary" :icon="View" @click="openDetail(row.id)">查看详情</el-button>
            </template>
          </el-table-column>

          <template #empty>
            <el-empty description="暂无日志数据" />
          </template>
        </el-table>
      </div>

      <div class="audit-log-pagination">
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
      </div>
    </el-card>
  </div>

  <el-drawer v-model="detailVisible" title="日志详情" size="55%">
    <el-skeleton :loading="detailLoading" animated :rows="10">
      <template #default>
        <el-space direction="vertical" fill>
          <el-card shadow="never">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="日志ID">{{ detailData?.id || '-' }}</el-descriptions-item>
              <el-descriptions-item label="操作管理员">
                {{ detailData?.adminName || (detailData?.adminId ? `管理员 #${detailData.adminId}` : '-') }}
              </el-descriptions-item>
              <el-descriptions-item label="操作类型">
                {{ detailData?.operationTypeDesc || operationTypeLabel(detailData?.operationType) }}
              </el-descriptions-item>
              <el-descriptions-item label="操作动作">{{ detailData?.action || '-' }}</el-descriptions-item>
              <el-descriptions-item label="审核对象" :span="2">
                {{ detailData?.targetSummary || (detailData?.targetId ? `对象 #${detailData.targetId}` : '-') }}
              </el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ detailData?.detail || '-' }}</el-descriptions-item>
              <el-descriptions-item label="操作时间" :span="2">
                {{ formatDateTime(detailData?.createTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card v-if="detailData?.goodsDetail" shadow="never">
            <template #header>商品审核内容</template>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="商品ID">{{ detailData.goodsDetail.id }}</el-descriptions-item>
              <el-descriptions-item label="商品标题">{{ detailData.goodsDetail.title || '-' }}</el-descriptions-item>
              <el-descriptions-item label="发布人">{{ detailData.goodsDetail.sellerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="卖家学号">
                {{ detailData.goodsDetail.sellerStudentNo || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="分类">{{ detailData.goodsDetail.categoryName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                {{ detailData.goodsDetail.statusDesc || goodsStatusLabel(detailData.goodsDetail.status) }}
              </el-descriptions-item>
              <el-descriptions-item label="价格">
                {{ formatPrice(detailData.goodsDetail.price) }}
              </el-descriptions-item>
              <el-descriptions-item label="原价">
                {{ formatPrice(detailData.goodsDetail.oldPrice) }}
              </el-descriptions-item>
              <el-descriptions-item label="面交地点" :span="2">
                {{ detailData.goodsDetail.location || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="商品描述" :span="2">
                {{ detailData.goodsDetail.detail || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="审核备注" :span="2">
                {{ detailData.goodsDetail.reason || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="发布时间">
                {{ formatDateTime(detailData.goodsDetail.publishTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="审核时间">
                {{ formatDateTime(detailData.goodsDetail.auditTime) }}
              </el-descriptions-item>
            </el-descriptions>

            <el-divider content-position="left">商品图片</el-divider>
            <el-space wrap>
              <el-link
                v-for="(image, index) in detailData.goodsDetail.images || []"
                :key="image.url"
                :underline="false"
                type="primary"
                @click="previewImage(index)"
              >
                查看图片 {{ index + 1 }}
              </el-link>
              <el-empty v-if="goodsImageList.length === 0" description="暂无图片" />
            </el-space>
          </el-card>

          <el-card v-if="detailData?.sellerAuthDetail" shadow="never">
            <template #header>卖家认证审核内容</template>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="申请ID">{{ detailData.sellerAuthDetail.id }}</el-descriptions-item>
              <el-descriptions-item label="申请用户">
                {{ detailData.sellerAuthDetail.userName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="真实姓名">
                {{ detailData.sellerAuthDetail.realName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="学号">
                {{ detailData.sellerAuthDetail.studentNo || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="手机号">
                {{ detailData.sellerAuthDetail.phone || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                {{ detailData.sellerAuthDetail.statusDesc || sellerAuthStatusLabel(detailData.sellerAuthDetail.status) }}
              </el-descriptions-item>
              <el-descriptions-item label="审核人">
                {{ detailData.sellerAuthDetail.auditAdminName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="审核时间">
                {{ formatDateTime(detailData.sellerAuthDetail.auditTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="认证材料" :span="2">
                <el-link
                  v-if="isUrl(detailData.sellerAuthDetail.material)"
                  :href="detailData.sellerAuthDetail.material"
                  target="_blank"
                  type="primary"
                >
                  查看材料
                </el-link>
                <span v-else>{{ detailData.sellerAuthDetail.material || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="审核备注" :span="2">
                {{ detailData.sellerAuthDetail.reason || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card v-if="detailData?.reportDetail" shadow="never">
            <template #header>举报处理内容</template>

            <el-descriptions :column="2" border>
              <el-descriptions-item label="举报ID">{{ detailData.reportDetail.id }}</el-descriptions-item>
              <el-descriptions-item label="举报人">
                {{ detailData.reportDetail.reportUserName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="举报对象类型">
                {{ reportTargetTypeLabel(detailData.reportDetail.targetType) }}
              </el-descriptions-item>
              <el-descriptions-item label="举报对象ID">
                {{ detailData.reportDetail.targetId || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="举报对象内容" :span="2">
                {{ detailData.reportDetail.targetName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="举报原因" :span="2">
                {{ detailData.reportDetail.reason || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="处理状态">
                {{ reportStatusLabel(detailData.reportDetail.status) }}
              </el-descriptions-item>
              <el-descriptions-item label="处理管理员">
                {{ detailData.reportDetail.handleAdminName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="处理结果" :span="2">
                {{ detailData.reportDetail.handleResult || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="处理时间">
                {{ formatDateTime(detailData.reportDetail.handleTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="举报时间">
                {{ formatDateTime(detailData.reportDetail.createTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-space>
      </template>
    </el-skeleton>
  </el-drawer>

  <el-image-viewer
    v-if="imageViewerVisible"
    :url-list="goodsImageList"
    :initial-index="imageViewerIndex"
    @close="closeImageViewer"
  />
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ElImageViewer } from 'element-plus'
import { Document, RefreshRight, View } from '@element-plus/icons-vue'
import { fetchAuditLogDetail, fetchAuditLogPage } from '@/api/admin'
import {
  AUDIT_LOG_OPERATION_TYPE,
  AUDIT_LOG_OPERATION_TYPE_LABEL_MAP,
} from '@/constants/auditLog'
import { GOODS_STATUS_LABEL_MAP } from '@/constants/goods'
import { SELLER_AUTH_STATUS_LABEL_MAP } from '@/constants/sellerAuth'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const imageViewerVisible = ref(false)
const imageViewerIndex = ref(0)
const records = ref([])
const detailData = ref(null)

const queryForm = reactive({
  operationType: '',
})

const pager = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const REPORT_STATUS_LABEL_MAP = {
  0: '待处理',
  1: '已处理',
  2: '已忽略',
}

const REPORT_TARGET_TYPE_LABEL_MAP = {
  1: '商品',
  2: '用户',
  3: '消息',
}

const goodsImageList = computed(() =>
  Array.isArray(detailData.value?.goodsDetail?.images)
    ? detailData.value.goodsDetail.images.map((item) => item.url).filter(Boolean)
    : [],
)

function resolveOperationType(value) {
  const raw = Array.isArray(value) ? value[0] : value

  if (raw === undefined || raw === null || raw === '' || raw === 'all') {
    return ''
  }

  const operationType = Number(raw)
  const validTypes = Object.values(AUDIT_LOG_OPERATION_TYPE)

  if (Number.isInteger(operationType) && validTypes.includes(operationType)) {
    return operationType
  }

  return ''
}

function syncOperationTypeFromRoute() {
  queryForm.operationType = resolveOperationType(route.query.operationType)
}

function buildQueryParams() {
  const params = {
    page: pager.page,
    pageSize: pager.pageSize,
  }

  const operationType = resolveOperationType(queryForm.operationType)
  if (operationType !== '') {
    params.operationType = operationType
  }

  return params
}

function operationTypeLabel(operationType) {
  return AUDIT_LOG_OPERATION_TYPE_LABEL_MAP[operationType] || '未知类型'
}

function goodsStatusLabel(status) {
  return GOODS_STATUS_LABEL_MAP[status] || '未知状态'
}

function sellerAuthStatusLabel(status) {
  return SELLER_AUTH_STATUS_LABEL_MAP[status] || '未知状态'
}

function reportStatusLabel(status) {
  return REPORT_STATUS_LABEL_MAP[status] || '未知状态'
}

function reportTargetTypeLabel(targetType) {
  return REPORT_TARGET_TYPE_LABEL_MAP[targetType] || '未知类型'
}

function formatPrice(value) {
  const amount = Number(value)
  if (!Number.isFinite(amount)) {
    return '-'
  }

  return `¥${amount.toFixed(2)}`
}

function isUrl(value) {
  return typeof value === 'string' && /^https?:\/\//i.test(value)
}

function previewImage(index) {
  imageViewerIndex.value = index
  imageViewerVisible.value = true
}

function closeImageViewer() {
  imageViewerVisible.value = false
}

async function fetchList() {
  loading.value = true

  try {
    const pageData = await fetchAuditLogPage(buildQueryParams())
    records.value = Array.isArray(pageData?.records) ? pageData.records : []
    pager.total = Number(pageData?.total || 0)
  } catch (error) {
    ElMessage.error(error.message || '日志列表加载失败')
  } finally {
    loading.value = false
  }
}

async function openDetail(id) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null

  try {
    detailData.value = await fetchAuditLogDetail(id)
  } catch (error) {
    ElMessage.error(error.message || '日志详情加载失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
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
  () => route.query.operationType,
  () => {
    syncOperationTypeFromRoute()
    pager.page = 1
    fetchList()
  },
  { immediate: true },
)
</script>

<style scoped>
.audit-log-page {
  height: 100%;
  min-height: 0;
  display: flex;
}

.audit-log-card {
  flex: 1;
  min-height: 0;
}

.audit-log-table-wrap {
  flex: 1;
  min-height: 0;
}

.audit-log-pagination {
  position: sticky;
  bottom: 0;
  padding-top: 12px;
  margin-top: 12px;
  background: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color-lighter);
  z-index: 1;
}

.audit-log-page :deep(.el-card),
.audit-log-page :deep(.el-message-box),
.audit-log-page :deep(.el-drawer__body .el-card) {
  border-radius: 0 !important;
}

.audit-log-page :deep(.el-card__body) {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.audit-log-page :deep(.el-button) {
  border-radius: var(--el-border-radius-base) !important;
  font-weight: var(--el-font-weight-primary) !important;
  box-shadow: none !important;
}

.audit-log-page :deep(.el-button--primary) {
  box-shadow: none !important;
}

.audit-log-page :deep(.el-table) {
  --el-table-header-bg-color: var(--el-fill-color-light) !important;
  --el-table-border-color: var(--el-border-color-lighter) !important;
  --el-table-row-hover-bg-color: var(--el-fill-color-lighter) !important;
}

.audit-log-page :deep(.el-table th.el-table__cell) {
  color: var(--el-text-color-primary) !important;
  font-weight: var(--el-font-weight-primary) !important;
}

.audit-log-page :deep(.el-pagination.is-background .el-pager li),
.audit-log-page :deep(.el-pagination.is-background .btn-next),
.audit-log-page :deep(.el-pagination.is-background .btn-prev) {
  border-radius: var(--el-border-radius-base) !important;
}
</style>
