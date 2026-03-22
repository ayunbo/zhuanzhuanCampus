<template>
  <div class="order-manage-page app-card fade-in-up">
    <section class="toolbar">
      <div>
        <h3>订单管理</h3>
        <p class="sub-title">管理员查看并管理平台订单，状态变更会同步联动支付状态。</p>
      </div>
    </section>

    <div class="status-tabs">
      <button
        v-for="item in statusTabs"
        :key="item.label"
        class="status-tab"
        :class="{ active: isStatusActive(item.value) }"
        type="button"
        @click="handleStatusTabChange(item.value)"
      >
        {{ item.label }}
      </button>
    </div>

    <form class="filter-grid" @submit.prevent="handleSearch">
      <label>
        <span>订单号</span>
        <input v-model="query.orderNo" class="app-input" type="text" placeholder="请输入订单号" />
      </label>

      <label>
        <span>订单状态</span>
        <select v-model="query.status" class="app-select">
          <option value="">全部状态</option>
          <option :value="0">待支付</option>
          <option :value="1">已支付</option>
          <option :value="2">已完成</option>
          <option :value="3">已取消</option>
          <option :value="4">超时关闭</option>
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
            <th>订单号</th>
            <th>商品标题</th>
            <th>买家</th>
            <th>卖家</th>
            <th>金额</th>
            <th>订单状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && tableData.length === 0">
            <td class="empty-row" colspan="8">暂无订单数据</td>
          </tr>
          <tr v-for="item in tableData" :key="item.id">
            <td>{{ item.orderNo }}</td>
            <td class="title-cell">{{ item.goodsTitle || '-' }}</td>
            <td>{{ item.buyerName || item.buyerId || '-' }}</td>
            <td>{{ item.sellerName || item.sellerId || '-' }}</td>
            <td>￥{{ item.amount }}</td>
            <td>
              <span class="status-tag" :class="statusClass(item.status)">
                {{ formatOrderStatus(item.status) }}
              </span>
            </td>
            <td>{{ item.createTime || '-' }}</td>
            <td class="actions">
              <button class="app-btn primary mini" @click="goDetail(item.id)">详情</button>
              <button class="app-btn secondary mini" @click="openStatusDialog(item)">改状态</button>
              <button class="app-btn danger mini" @click="handleDelete(item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <footer class="pagination-wrap">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 30]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
      />
    </footer>
  </div>

  <el-dialog v-model="showStatusDialog" title="修改订单状态" width="420px" destroy-on-close>
    <div class="dialog-order-no">订单号：{{ statusForm.orderNo }}</div>

    <el-form label-width="88px">
      <el-form-item label="新状态">
        <el-select v-model="statusForm.status" class="dialog-select">
          <el-option :value="0" label="待支付" />
          <el-option :value="1" label="已支付" />
          <el-option :value="2" label="已完成" />
          <el-option :value="3" label="已取消" />
          <el-option :value="4" label="超时关闭" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeStatusDialog">取消</el-button>
        <el-button type="primary" @click="handleUpdateStatus">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteAdminOrder, getAdminOrderPage, updateAdminOrderStatus } from '@/api/admin'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const total = ref(0)
const tableData = ref([])
const showStatusDialog = ref(false)

const statusTabs = [
  { value: '', label: '全部订单' },
  { value: 0, label: '待支付' },
  { value: 1, label: '已支付' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已取消' },
  { value: 4, label: '超时关闭' },
]

const query = ref({
  page: 1,
  pageSize: 10,
  status: '',
  orderNo: '',
})

const statusForm = ref({
  id: null,
  orderNo: '',
  status: 0,
})

const routeStatusMap = {
  all: '',
  pending_pay: 0,
  paid: 1,
  completed: 2,
  canceled: 3,
  timeout_closed: 4,
}

function formatOrderStatus(status) {
  const map = {
    0: '待支付',
    1: '已支付',
    2: '已完成',
    3: '已取消',
    4: '超时关闭',
  }
  return map[status] || '未知状态'
}

function statusClass(status) {
  const map = {
    0: 'pending',
    1: 'paid',
    2: 'done',
    3: 'cancel',
    4: 'timeout',
  }
  return map[status] || ''
}

function syncRouteFilters() {
  const routeStatus = typeof route.query.status === 'string' ? route.query.status : 'all'
  query.value.status = Object.prototype.hasOwnProperty.call(routeStatusMap, routeStatus)
    ? routeStatusMap[routeStatus]
    : ''
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: query.value.page,
      pageSize: query.value.pageSize,
    }

    if (query.value.status !== '' && query.value.status !== null && query.value.status !== undefined) {
      params.status = Number(query.value.status)
    }

    if (query.value.orderNo) {
      params.orderNo = query.value.orderNo
    }

    const data = await getAdminOrderPage(params)
    tableData.value = data?.result || data?.records || data?.list || []
    total.value = data?.total || 0
  } catch (error) {
    ElMessage.error(error.message || '获取订单列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.value.page = 1
  loadData()
}

function isStatusActive(status) {
  return String(query.value.status) === String(status)
}

function handleStatusTabChange(status) {
  query.value.status = status
  query.value.page = 1
  loadData()
}

function handleReset() {
  query.value = {
    page: 1,
    pageSize: 10,
    status: '',
    orderNo: '',
  }
  loadData()
}

function goDetail(id) {
  router.push(`/order-manage/detail/${id}`)
}

function openStatusDialog(item) {
  statusForm.value = {
    id: item.id,
    orderNo: item.orderNo,
    status: item.status,
  }
  showStatusDialog.value = true
}

function closeStatusDialog() {
  showStatusDialog.value = false
}

async function handleUpdateStatus() {
  try {
    await updateAdminOrderStatus({
      id: statusForm.value.id,
      status: statusForm.value.status,
    })
    ElMessage.success('订单状态修改成功')
    closeStatusDialog()
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '修改订单状态失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？删除后不可恢复。', '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  try {
    await deleteAdminOrder(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '删除订单失败')
  }
}

onMounted(() => {
  syncRouteFilters()
  loadData()
})

watch(
  () => route.query.status,
  () => {
    syncRouteFilters()
    query.value.page = 1
    loadData()
  },
)
</script>

<style scoped>
.order-manage-page {
  padding: 18px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: start;
  gap: 12px;
}

.toolbar h3 {
  margin: 0;
  font-size: 18px;
  color: #5c3b1f;
}

.sub-title {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.filter-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  align-items: end;
}

.status-tabs {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.status-tab {
  border: 1px solid #ead8bc;
  background: #fffaf1;
  color: #7b5d38;
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.status-tab:hover {
  border-color: #d4b07d;
  background: #fff3df;
}

.status-tab.active {
  border-color: #c98d36;
  background: linear-gradient(135deg, #ffe5b7 0%, #ffd18a 100%);
  color: #6a4208;
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
  min-width: 980px;
  border-collapse: collapse;
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

.title-cell {
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.actions {
  display: flex;
  gap: 6px;
}

.app-btn.mini {
  padding: 6px 9px;
  font-size: 12px;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.pending {
  color: #9a6700;
  background: #fff1d6;
}

.paid {
  color: #0c63e7;
  background: #e2eeff;
}

.done {
  color: #147d4c;
  background: #dcf5e7;
}

.cancel {
  color: #c43d32;
  background: #ffe0dd;
}

.timeout {
  color: #6b7280;
  background: #eceff3;
}

.pagination-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.dialog-order-no {
  margin-bottom: 14px;
  color: var(--text-secondary);
}

.dialog-select {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1180px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .order-manage-page {
    padding: 14px;
  }

  .filter-grid {
    grid-template-columns: 1fr;
  }

  .action-group,
  .actions {
    flex-wrap: wrap;
  }
}
</style>
