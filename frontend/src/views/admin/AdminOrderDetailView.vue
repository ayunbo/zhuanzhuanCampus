<template>
  <div class="order-detail-page fade-in-up">
    <div v-if="loading" class="page-card loading-card">订单详情加载中...</div>

    <template v-else-if="detail">
      <section class="page-card hero-card">
        <div class="toolbar">
          <div>
            <h3>订单详情</h3>
            <p class="sub-title">查看订单快照、支付信息和管理员可操作状态。</p>
          </div>
          <div class="header-actions">
            <button class="app-btn ghost" type="button" @click="goBack">返回列表</button>
            <button class="app-btn primary" type="button" @click="openEditDialog">编辑交易信息</button>
          </div>
        </div>

        <div class="hero-layout">
          <div class="goods-card">
            <img :src="detail.goodsCover || defaultCover" class="goods-cover" alt="goods-cover" />
            <div class="goods-info">
              <h4>{{ detail.goodsTitle || '未命名商品' }}</h4>
              <p>订单号：{{ detail.orderNo }}</p>
              <p>订单金额：￥{{ formatAmount(detail.amount) }}</p>
              <p>
                订单状态：
                <span class="status-tag" :class="statusClass(detail.status)">
                  {{ formatOrderStatus(detail.status) }}
                </span>
              </p>
              <p>
                支付状态：
                <span class="status-tag" :class="payStatusClass(detail.payStatus)">
                  {{ formatPayStatus(detail.payStatus) }}
                </span>
              </p>
            </div>
          </div>

          <div class="status-panel">
            <h4>状态管理</h4>
            <p class="sub-line">修改后会同步联动订单时间、支付状态和商品状态。</p>
            <el-select v-model="statusForm.status" class="dialog-select">
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <button class="app-btn primary status-btn" type="button" :disabled="statusSubmitting" @click="handleUpdateStatus">
              {{ statusSubmitting ? '提交中...' : '更新订单状态' }}
            </button>
          </div>
        </div>
      </section>

      <section class="detail-grid">
        <article class="page-card">
          <h4>交易信息</h4>
          <p>交易地点：{{ detail.meetLocation || '未填写' }}</p>
          <p>交易时间：{{ formatDateTime(detail.meetTime) }}</p>
          <p>备注：{{ detail.remark || '无' }}</p>
        </article>

        <article class="page-card">
          <h4>买卖双方信息</h4>
          <p>买家：{{ detail.buyerName || '未记录' }} / {{ detail.buyerPhone || '未记录' }}</p>
          <p>卖家：{{ detail.sellerName || '未记录' }} / {{ detail.sellerPhone || '未记录' }}</p>
          <p>买家 ID：{{ detail.buyerId || '-' }}</p>
          <p>卖家 ID：{{ detail.sellerId || '-' }}</p>
        </article>

        <article class="page-card">
          <h4>订单时间信息</h4>
          <p>创建时间：{{ formatDateTime(detail.createTime) }}</p>
          <p>支付时间：{{ formatDateTime(detail.payTime) }}</p>
          <p>完成时间：{{ formatDateTime(detail.completeTime) }}</p>
          <p>关闭时间：{{ formatDateTime(detail.closeTime) }}</p>
          <p>过期时间：{{ formatDateTime(detail.expireTime) }}</p>
        </article>

        <article class="page-card">
          <h4>支付信息</h4>
          <p>支付单号：{{ detail.payNo || '未记录' }}</p>
          <p>支付方式：{{ formatPayMethod(detail.payMethod) }}</p>
          <p>支付状态：{{ formatPayStatus(detail.payStatus) }}</p>
          <p>支付记录时间：{{ formatDateTime(detail.payRecordTime) }}</p>
        </article>
      </section>

      <section class="page-card">
        <h4>支付记录</h4>
        <div v-if="!detail.payRecords || detail.payRecords.length === 0" class="empty-box">暂无支付记录</div>

        <div v-else class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>记录号</th>
                <th>内容</th>
                <th>状态</th>
                <th>渠道响应</th>
                <th>时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in detail.payRecords" :key="item.id">
                <td>{{ item.recordNo || '-' }}</td>
                <td>{{ item.content || '-' }}</td>
                <td>
                  <span class="status-tag" :class="payStatusClass(item.status)">
                    {{ formatPayStatus(item.status) }}
                  </span>
                </td>
                <td class="response-cell">{{ item.channelResponse || '-' }}</td>
                <td>{{ formatDateTime(item.createTime) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>

    <div v-else class="page-card loading-card">未找到订单详情</div>
  </div>

  <el-dialog v-model="showEditDialog" title="编辑交易信息" width="480px" destroy-on-close>
    <el-form label-width="88px">
      <el-form-item label="交易地点">
        <el-input v-model="editForm.meetLocation" placeholder="请输入交易地点" />
      </el-form-item>

      <el-form-item label="交易时间">
        <el-date-picker
          v-model="editForm.meetTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="请选择交易时间"
          class="dialog-select"
        />
      </el-form-item>

      <el-form-item label="备注">
        <el-input v-model="editForm.remark" type="textarea" :rows="4" placeholder="请输入备注" />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeEditDialog">取消</el-button>
        <el-button type="primary" :disabled="saving" @click="handleUpdateOrder">
          {{ saving ? '保存中...' : '保存' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/format'
import { getAdminOrderDetail, updateAdminOrder, updateAdminOrderStatus } from '@/api/admin'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const statusSubmitting = ref(false)
const detail = ref(null)
const showEditDialog = ref(false)
const defaultCover = 'https://via.placeholder.com/140x140?text=Goods'

const statusOptions = [
  { value: 0, label: '待支付' },
  { value: 1, label: '已支付' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已取消' },
  { value: 4, label: '超时关闭' },
]

const editForm = ref({
  id: null,
  meetLocation: '',
  meetTime: '',
  remark: '',
})

const statusForm = ref({
  id: null,
  status: 0,
})

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

function payStatusClass(status) {
  const map = {
    0: 'pending',
    1: 'done',
    2: 'cancel',
    3: 'timeout',
  }
  return map[status] || ''
}

function formatPayMethod(method) {
  const map = {
    1: '模拟支付',
    2: '虚拟钱包',
  }
  return map[method] || '未知方式'
}

function formatPayStatus(status) {
  const map = {
    0: '待支付',
    1: '成功',
    2: '失败',
    3: '关闭',
  }
  return map[status] || '未知状态'
}

function formatAmount(amount) {
  const value = Number(amount)
  return Number.isFinite(value) ? value.toFixed(2) : '0.00'
}

function syncForms() {
  if (!detail.value) {
    return
  }

  editForm.value = {
    id: detail.value.id,
    meetLocation: detail.value.meetLocation || '',
    meetTime: detail.value.meetTime ? formatDateTime(detail.value.meetTime) : '',
    remark: detail.value.remark || '',
  }

  statusForm.value = {
    id: detail.value.id,
    status: Number(detail.value.status ?? 0),
  }
}

async function loadDetail() {
  const id = Number(route.params.id)
  if (!Number.isFinite(id) || id <= 0) {
    ElMessage.error('订单编号无效')
    router.replace('/order-manage')
    return
  }

  loading.value = true
  try {
    detail.value = await getAdminOrderDetail(id)
    syncForms()
  } catch (error) {
    ElMessage.error(error.message || '获取订单详情失败')
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push({
    path: '/order-manage',
    query: route.query,
  })
}

function openEditDialog() {
  syncForms()
  showEditDialog.value = true
}

function closeEditDialog() {
  showEditDialog.value = false
}

async function handleUpdateOrder() {
  saving.value = true
  try {
    await updateAdminOrder({
      id: editForm.value.id,
      meetLocation: editForm.value.meetLocation?.trim() || null,
      meetTime: editForm.value.meetTime || null,
      remark: editForm.value.remark?.trim() || null,
    })
    ElMessage.success('订单交易信息修改成功')
    closeEditDialog()
    await loadDetail()
  } catch (error) {
    ElMessage.error(error.message || '修改订单失败')
  } finally {
    saving.value = false
  }
}

async function handleUpdateStatus() {
  if (!detail.value) {
    return
  }

  statusSubmitting.value = true
  try {
    await updateAdminOrderStatus({
      id: detail.value.id,
      status: statusForm.value.status,
    })
    ElMessage.success('订单状态修改成功')
    await loadDetail()
  } catch (error) {
    ElMessage.error(error.message || '修改订单状态失败')
  } finally {
    statusSubmitting.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.order-detail-page {
  display: grid;
  gap: 16px;
}

.page-card {
  padding: 18px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: #fffdf8;
  box-shadow: var(--shadow-soft);
}

.hero-card {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: start;
  gap: 12px;
}

.toolbar h3,
.page-card h4 {
  margin: 0;
  color: #5c3b1f;
}

.sub-title,
.sub-line {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.hero-layout {
  margin-top: 18px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 16px;
}

.goods-card {
  display: flex;
  gap: 20px;
  align-items: center;
}

.goods-cover {
  width: 140px;
  height: 140px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: #fff;
}

.goods-info h4 {
  margin-bottom: 12px;
}

.goods-info p,
.page-card p {
  margin: 8px 0;
  color: #555;
}

.status-panel {
  border: 1px dashed #e2c89f;
  border-radius: 14px;
  padding: 16px;
  background: #fff8ed;
}

.status-btn {
  width: 100%;
  margin-top: 14px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
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

.table-wrap {
  margin-top: 14px;
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid var(--border);
}

.data-table {
  width: 100%;
  min-width: 860px;
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
}

.response-cell {
  min-width: 220px;
  white-space: pre-wrap;
  word-break: break-word;
}

.empty-box,
.loading-card {
  text-align: center;
  color: var(--text-secondary);
}

.dialog-select {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 960px) {
  .hero-layout,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .goods-card,
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    flex-wrap: wrap;
  }
}
</style>
