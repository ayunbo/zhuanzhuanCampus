<template>
  <div class="admin-goods-page">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryForm" size="small" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="queryForm.keyword"
            clearable
            placeholder="标题 / 描述 / 分类 / 卖家"
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="卖家 ID">
          <el-input-number
            v-model="queryForm.sellerId"
            controls-position="right"
            :min="1"
            :precision="0"
            placeholder="卖家 ID"
            style="width: 130px"
          />
        </el-form-item>

        <el-form-item label="分类 ID">
          <el-input-number
            v-model="queryForm.categoryId"
            controls-position="right"
            :min="1"
            :precision="0"
            placeholder="分类 ID"
            style="width: 130px"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="全部状态" style="width: 130px">
            <el-option v-for="item in GOODS_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="String(item.value)" />
          </el-select>
        </el-form-item>

        <el-form-item label="最低价">
          <el-input-number
            v-model="queryForm.minPrice"
            controls-position="right"
            :min="0"
            :precision="2"
            :step="10"
            placeholder="最低价"
            style="width: 130px"
          />
        </el-form-item>

        <el-form-item label="最高价">
          <el-input-number
            v-model="queryForm.maxPrice"
            controls-position="right"
            :min="0"
            :precision="2"
            :step="10"
            placeholder="最高价"
            style="width: 130px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button :loading="loading" @click="fetchList">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-card-body">
        <div class="table-box">
          <el-table :data="records" v-loading="loading" border stripe height="100%">
            <el-table-column prop="id" label="ID" min-width="90" />

            <el-table-column label="卖家" min-width="170">
              <template #default="{ row }">
                <div class="cell-stack">
                  <strong>{{ row.sellerName || `卖家 #${row.sellerId}` }}</strong>
                  <small>学号：{{ row.sellerStudentNo || '-' }}</small>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="分类" min-width="140">
              <template #default="{ row }">
                {{ row.categoryName || `分类 #${row.categoryId}` }}
              </template>
            </el-table-column>

            <el-table-column label="标题" min-width="220">
              <template #default="{ row }">
                <div class="cell-stack">
                  <strong>{{ row.title || '-' }}</strong>
                  <small>{{ row.location || '地点未填写' }}</small>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="价格" min-width="110">
              <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
            </el-table-column>

            <el-table-column label="状态" min-width="110">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" effect="light">
                  {{ row.statusDesc || statusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="reason" label="驳回原因" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.reason || '-' }}
              </template>
            </el-table-column>

            <el-table-column label="发布时间" min-width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.publishTime) }}
              </template>
            </el-table-column>

            <el-table-column label="更新时间" min-width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.updateTime) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" fixed="right" min-width="210">
              <template #default="{ row }">
                <el-space wrap>
                  <el-button size="small" @click="openDetail(row.id)">详情</el-button>
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
              <el-empty description="暂无商品数据" />
            </template>
          </el-table>
        </div>

        <footer class="pagination-wrap">
          <el-pagination
            v-model:current-page="pager.page"
            v-model:page-size="pager.pageSize"
            :background="true"
            layout="total, sizes, prev, pager, next"
            :page-sizes="[10, 20, 30]"
            :pager-count="5"
            :total="pager.total"
            @current-change="handleCurrentChange"
            @size-change="handleSizeChange"
          />
        </footer>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="商品详情" width="760px" destroy-on-close class="goods-detail-dialog">
      <div v-if="detailLoading" class="detail-empty">详情加载中...</div>

      <div v-else-if="detailData" class="detail-panel">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品 ID">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="卖家">{{ detailData.sellerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="卖家学号">{{ detailData.sellerStudentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ detailData.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ formatMoney(detailData.price) }}</el-descriptions-item>
          <el-descriptions-item label="原价">
            {{ detailData.oldPrice == null ? '-' : `¥${formatMoney(detailData.oldPrice)}` }}
          </el-descriptions-item>
          <el-descriptions-item label="成色">{{ detailData.quality ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detailData.status)" effect="light">
              {{ detailData.statusDesc || statusLabel(detailData.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="浏览量">{{ detailData.viewCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="收藏量">{{ detailData.favoriteCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDateTime(detailData.publishTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ formatDateTime(detailData.auditTime) }}</el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ detailData.title || '-' }}</el-descriptions-item>
          <el-descriptions-item label="面交地点" :span="2">{{ detailData.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商品描述" :span="2">{{ detailData.detail || '暂无描述' }}</el-descriptions-item>
          <el-descriptions-item label="驳回原因" :span="2">{{ detailData.reason || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="Array.isArray(detailData.images) && detailData.images.length > 0" class="detail-images">
          <el-image
            v-for="image in detailData.images"
            :key="`${image.url}-${image.sort}`"
            :src="image.url"
            :preview-src-list="detailData.images.map((item) => item.url)"
            fit="cover"
          />
        </div>
      </div>

      <div v-else class="detail-empty">暂无详情数据</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { auditAdminGoods, fetchAdminGoodsDetail, fetchAdminGoodsPage } from '@/api/admin'
import { GOODS_STATUS, GOODS_STATUS_LABEL_MAP, GOODS_STATUS_OPTIONS } from '@/constants/goods'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const actionLoadingId = ref(null)
const records = ref([])
const detailData = ref(null)

const queryForm = reactive({
  keyword: '',
  sellerId: undefined,
  categoryId: undefined,
  status: '',
  minPrice: undefined,
  maxPrice: undefined,
})

const pager = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const routeStatusMap = {
  all: '',
  to_review: String(GOODS_STATUS.WAIT_AUDIT),
  online: String(GOODS_STATUS.ON_SALE),
  offline: String(GOODS_STATUS.OFF_SHELF),
  rejected: String(GOODS_STATUS.REJECTED),
}

function formatMoney(value) {
  const amount = Number(value)
  if (!Number.isFinite(amount)) {
    return '0.00'
  }

  return amount.toFixed(2)
}

function statusLabel(status) {
  return GOODS_STATUS_LABEL_MAP[status] || '未知状态'
}

function statusTagType(status) {
  if (status === GOODS_STATUS.WAIT_AUDIT || status === GOODS_STATUS.LOCKED) return 'warning'
  if (status === GOODS_STATUS.ON_SALE || status === GOODS_STATUS.SOLD) return 'success'
  if (status === GOODS_STATUS.REJECTED) return 'danger'
  return 'info'
}

function canAudit(record) {
  return record.status === GOODS_STATUS.WAIT_AUDIT
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

  if (queryForm.keyword) {
    params.keyword = queryForm.keyword
  }

  if (typeof queryForm.sellerId === 'number') {
    params.sellerId = queryForm.sellerId
  }

  if (typeof queryForm.categoryId === 'number') {
    params.categoryId = queryForm.categoryId
  }

  if (queryForm.status !== '' && queryForm.status !== null) {
    params.status = Number(queryForm.status)
  }

  if (typeof queryForm.minPrice === 'number') {
    params.minPrice = queryForm.minPrice
  }

  if (typeof queryForm.maxPrice === 'number') {
    params.maxPrice = queryForm.maxPrice
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

async function openDetail(goodsId) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null

  try {
    detailData.value = await fetchAdminGoodsDetail(goodsId)
  } catch (error) {
    ElMessage.error(error.message || '加载详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

function handleSearch() {
  pager.page = 1
  fetchList()
}

function handleReset() {
  queryForm.keyword = ''
  queryForm.sellerId = undefined
  queryForm.categoryId = undefined
  queryForm.minPrice = undefined
  queryForm.maxPrice = undefined
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
    if (detailVisible.value && detailData.value?.id === goodsId) {
      detailData.value = await fetchAdminGoodsDetail(goodsId)
    }
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    actionLoadingId.value = null
  }
}

async function handleApprove(record) {
  if (!canAudit(record)) {
    return
  }

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
  if (!canAudit(record)) {
    return
  }

  try {
    const { value } = await ElMessageBox.prompt(`请输入驳回商品 #${record.id} 的原因`, '驳回商品', {
      inputPlaceholder: '驳回原因',
      inputPattern: /\S+/,
      inputErrorMessage: '驳回原因不能为空',
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
    })
    await submitAudit(record.id, GOODS_STATUS.REJECTED, value)
  } catch {
    // ignore cancel
  }
}

onMounted(() => {
  syncRouteFilters()
  fetchList()
})

watch(
  () => route.query.status,
  () => {
    syncRouteFilters()
    pager.page = 1
    fetchList()
  },
)
</script>

<style scoped>
.admin-goods-page {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.admin-goods-page :deep(.el-card),
.admin-goods-page :deep(.el-card__body),
.admin-goods-page :deep(.el-input__wrapper),
.admin-goods-page :deep(.el-select__wrapper),
.admin-goods-page :deep(.el-input-number),
.admin-goods-page :deep(.el-input-number__decrease),
.admin-goods-page :deep(.el-input-number__increase),
.admin-goods-page :deep(.el-button),
.admin-goods-page :deep(.el-table),
.admin-goods-page :deep(.el-table__inner-wrapper),
.admin-goods-page :deep(.el-table__cell),
.admin-goods-page :deep(.el-tag),
.admin-goods-page :deep(.el-pagination button),
.admin-goods-page :deep(.el-pager li),
.admin-goods-page :deep(.el-image),
.admin-goods-page :deep(.el-image__inner) {
  border-radius: 0 !important;
}

:deep(.goods-detail-dialog .el-dialog),
:deep(.goods-detail-dialog .el-dialog__header),
:deep(.goods-detail-dialog .el-dialog__body),
:deep(.goods-detail-dialog .el-descriptions__table),
:deep(.goods-detail-dialog .el-tag) {
  border-radius: 0 !important;
}

.filter-card :deep(.el-card__body) {
  padding-bottom: 8px;
}

.table-card {
  flex: 1;
  min-height: 0;
}

.table-card :deep(.el-card__body) {
  height: 100%;
  padding: 0;
}

.table-card-body {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.table-box {
  flex: 1;
  min-height: 0;
}

.pagination-wrap {
  flex: 0 0 auto;
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  display: flex;
  justify-content: flex-end;
  background: var(--el-bg-color);
}

.cell-stack {
  display: grid;
  gap: 4px;
}

.cell-stack small {
  color: var(--el-text-color-secondary);
}

.detail-panel {
  display: grid;
  gap: 16px;
}

.detail-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.detail-images :deep(.el-image) {
  width: 100%;
  height: 120px;
  overflow: hidden;
}

.detail-empty {
  padding: 24px 0;
  text-align: center;
  color: var(--el-text-color-secondary);
}

@media (max-width: 760px) {
  .pagination-wrap {
    justify-content: center;
    overflow-x: auto;
  }
}
</style>
