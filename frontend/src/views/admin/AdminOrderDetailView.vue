<template>
  <div v-if="detail" class="order-detail-page fade-in-up">
    <section class="page-card hero-card">
      <div class="toolbar">
        <div>
          <h3>订单详情</h3>
          <p class="sub-title">订单、支付和交易信息统一在后台主布局内查看。</p>
        </div>
        <div class="header-actions">
          <button class="app-btn ghost" @click="goBack">返回列表</button>
          <button class="app-btn primary" @click="openEditDialog">编辑交易信息</button>
        </div>
      </div>

      <div class="goods-card">
        <img :src="detail.goodsCover || defaultCover" class="goods-cover" />
        <div class="goods-info">
          <h4>{{ detail.goodsTitle }}</h4>
          <p>订单号：{{ detail.orderNo }}</p>
          <p>订单金额：￥{{ detail.amount }}</p>
          <p>
            订单状态：
            <span class="status-tag" :class="statusClass(detail.status)">
              {{ formatOrderStatus(detail.status) }}
            </span>
          </p>
        </div>
      </div>
    </section>

    <section class="detail-grid">
      <article class="page-card">
        <h4>交易信息</h4>
        <p>交易地点：{{ detail.meetLocation || '未填写' }}</p>
        <p>交易时间：{{ detail.meetTime || '未填写' }}</p>
        <p>备注：{{ detail.remark || '无' }}</p>
      </article>

      <article class="page-card">
        <h4>买卖双方信息</h4>
        <p>买家：{{ detail.buyerName || '未记录' }} / {{ detail.buyerPhone || '未记录' }}</p>
        <p>卖家：{{ detail.sellerName || '未记录' }} / {{ detail.sellerPhone || '未记录' }}</p>
      </article>

      <article class="page-card">
        <h4>订单时间信息</h4>
        <p>创建时间：{{ detail.createTime || '未记录' }}</p>
        <p>支付时间：{{ detail.payTime || '未支付' }}</p>
        <p>完成时间：{{ detail.completeTime || '未完成' }}</p>
        <p>关闭时间：{{ detail.closeTime || '未关闭' }}</p>
        <p>过期时间：{{ detail.expireTime || '未记录' }}</p>
      </article>

      <article class="page-card">
        <h4>支付信息</h4>
        <p>支付单号：{{ detail.payNo || '未记录' }}</p>
        <p>支付方式：{{ formatPayMethod(detail.payMethod) }}</p>
        <p>支付状态：{{ formatPayStatus(detail.payStatus) }}</p>
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
              <td>{{ item.recordNo }}</td>
              <td>{{ item.content }}</td>
              <td>{{ formatPayStatus(item.status) }}</td>
              <td>{{ item.channelResponse }}</td>
              <td>{{ item.createTime }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>

  <div v-else class="page-card loading-card">订单详情加载中...</div>

  <el-dialog v-model="showEditDialog" title="编辑交易信息" width="460px" destroy-on-close>
    <el-form label-width="88px">
      <el-form-item label="交易地点">
        <el-input v-model="editForm.meetLocation" placeholder="请输入交易地点" />
      </el-form-item>

      <el-form-item label="交易时间">
        <el-input v-model="editForm.meetTime" placeholder="格式：2026-03-20 15:00:00" />
      </el-form-item>

      <el-form-item label="备注">
        <el-input v-model="editForm.remark" type="textarea" :rows="4" placeholder="请输入备注" />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeEditDialog">取消</el-button>
        <el-button type="primary" @click="handleUpdateOrder">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAdminOrderDetail, updateAdminOrder } from '@/api/admin'

const route = useRoute()
const router = useRouter()

const detail = ref(null)
const showEditDialog = ref(false)
const defaultCover = 'https://via.placeholder.com/140x140?text=Goods'

const editForm = ref({
  id: null,
  meetLocation: '',
  meetTime: '',
  remark: '',
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

function formatPayMethod(method) {
  const map = {
    1: '模拟支付',
    2: '微信支付',
    3: '支付宝支付',
    4: '余额支付',
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

async function loadDetail() {
  try {
    const id = Number(route.params.id)
    const data = await getAdminOrderDetail(id)
    detail.value = data
  } catch (error) {
    ElMessage.error(error.message || '获取订单详情失败')
  }
}

function goBack() {
  router.push('/order-manage')
}

function openEditDialog() {
  editForm.value = {
    id: detail.value.id,
    meetLocation: detail.value.meetLocation || '',
    meetTime: detail.value.meetTime || '',
    remark: detail.value.remark || '',
  }
  showEditDialog.value = true
}

function closeEditDialog() {
  showEditDialog.value = false
}

async function handleUpdateOrder() {
  try {
    await updateAdminOrder({
      id: editForm.value.id,
      meetLocation: editForm.value.meetLocation,
      meetTime: editForm.value.meetTime,
      remark: editForm.value.remark,
    })
    ElMessage.success('订单交易信息修改成功')
    closeEditDialog()
    loadDetail()
  } catch (error) {
    ElMessage.error(error.message || '修改订单失败')
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

.sub-title {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.goods-card {
  margin-top: 18px;
  display: flex;
  gap: 20px;
  align-items: center;
}

.goods-cover {
  width: 140px;
  height: 140px;
  object-fit: cover;
  border-radius: 12px;
}

.goods-info h4 {
  margin-bottom: 12px;
}

.goods-info p,
.page-card p {
  margin: 8px 0;
  color: #555;
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

.empty-box,
.loading-card {
  text-align: center;
  color: var(--text-secondary);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
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
