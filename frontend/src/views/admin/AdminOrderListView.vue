<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>订单管理</h2>
        <p class="sub-title">管理员查看并管理平台订单</p>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-item">
          <label>订单号</label>
          <input v-model="query.orderNo" placeholder="请输入订单号" />
        </div>

        <div class="filter-item">
          <label>订单状态</label>
          <select v-model="query.status">
            <option value="">全部状态</option>
            <option :value="0">待支付</option>
            <option :value="1">已支付</option>
            <option :value="2">已完成</option>
            <option :value="3">已取消</option>
            <option :value="4">超时关闭</option>
          </select>
        </div>

        <div class="filter-actions">
          <button class="primary-btn" @click="handleSearch">查询</button>
          <button class="reset-btn" @click="handleReset">重置</button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <div v-if="loading" class="loading-box">加载中...</div>

      <div v-else-if="tableData.length === 0" class="empty-box">
        暂无订单数据
      </div>

      <table v-else class="order-table">
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
        <tr v-for="item in tableData" :key="item.id">
          <td>{{ item.orderNo }}</td>
          <td class="title-cell">{{ item.goodsTitle }}</td>
          <td>{{ item.buyerName || item.buyerId }}</td>
          <td>{{ item.sellerName || item.sellerId }}</td>
          <td>￥{{ item.amount }}</td>
          <td>
              <span class="status-tag" :class="statusClass(item.status)">
                {{ formatOrderStatus(item.status) }}
              </span>
          </td>
          <td>{{ item.createTime }}</td>
          <td>
            <div class="action-group">
              <button class="detail-btn" @click="goDetail(item.id)">详情</button>
              <button class="status-btn" @click="openStatusDialog(item)">改状态</button>
              <button class="delete-btn" @click="handleDelete(item.id)">删除</button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>

      <div class="pager" v-if="total > 0">
        <button
          class="pager-btn"
          :disabled="query.page <= 1"
          @click="prevPage"
        >
          上一页
        </button>
        <span>第 {{ query.page }} 页 / 共 {{ total }} 条</span>
        <button
          class="pager-btn"
          :disabled="query.page * query.pageSize >= total"
          @click="nextPage"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 改状态弹窗 -->
    <div v-if="showStatusDialog" class="dialog-mask">
      <div class="dialog-box">
        <h3>修改订单状态</h3>
        <p class="dialog-order-no">订单号：{{ statusForm.orderNo }}</p>

        <div class="dialog-item">
          <label>新状态</label>
          <select v-model="statusForm.status">
            <option :value="0">待支付</option>
            <option :value="1">已支付</option>
            <option :value="2">已完成</option>
            <option :value="3">已取消</option>
            <option :value="4">超时关闭</option>
          </select>
        </div>

        <div class="dialog-actions">
          <button class="reset-btn" @click="closeStatusDialog">取消</button>
          <button class="primary-btn" @click="handleUpdateStatus">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  getAdminOrderPage,
  updateAdminOrderStatus,
  deleteAdminOrder,
} from '@/api/admin'

const router = useRouter()

const loading = ref(false)
const total = ref(0)
const tableData = ref([])

const showStatusDialog = ref(false)

const query = ref({
  page: 1,
  pageSize: 5,
  status: '',
  orderNo: '',
})

const statusForm = ref({
  id: null,
  orderNo: '',
  status: 0,
})

const formatOrderStatus = (status) => {
  const map = {
    0: '待支付',
    1: '已支付',
    2: '已完成',
    3: '已取消',
    4: '超时关闭',
  }
  return map[status] || '未知状态'
}

const statusClass = (status) => {
  const map = {
    0: 'pending',
    1: 'paid',
    2: 'done',
    3: 'cancel',
    4: 'timeout',
  }
  return map[status] || ''
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: query.value.page,
      pageSize: query.value.pageSize,
    }

    if (query.value.status !== '' && query.value.status !== null) {
      params.status = query.value.status
    }

    if (query.value.orderNo) {
      params.orderNo = query.value.orderNo
    }

    const res = await getAdminOrderPage(params)
    const data = res?.data?.data ?? res?.data ?? res

    tableData.value = data.result || data.records || data.list || []
    total.value = data.total || 0
  } catch (error) {
    console.error('获取订单列表失败：', error)
    alert(
      error?.response?.data?.msg ||
      error?.response?.data?.message ||
      '获取订单列表失败',
    )
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.value.page = 1
  loadData()
}

const handleReset = () => {
  query.value = {
    page: 1,
    pageSize: 5,
    status: '',
    orderNo: '',
  }
  loadData()
}

const prevPage = () => {
  if (query.value.page > 1) {
    query.value.page--
    loadData()
  }
}

const nextPage = () => {
  if (query.value.page * query.value.pageSize < total.value) {
    query.value.page++
    loadData()
  }
}

const goDetail = (id) => {
  router.push(`/admin/order/detail/${id}`)
}

const openStatusDialog = (item) => {
  statusForm.value = {
    id: item.id,
    orderNo: item.orderNo,
    status: item.status,
  }
  showStatusDialog.value = true
}

const closeStatusDialog = () => {
  showStatusDialog.value = false
}

const handleUpdateStatus = async () => {
  try {
    await updateAdminOrderStatus({
      id: statusForm.value.id,
      status: statusForm.value.status,
    })
    alert('订单状态修改成功')
    closeStatusDialog()
    loadData()
  } catch (error) {
    console.error('修改订单状态失败：', error)
    alert(
      error?.response?.data?.msg ||
      error?.response?.data?.message ||
      '修改订单状态失败',
    )
  }
}

const handleDelete = async (id) => {
  const ok = window.confirm('确定要删除该订单吗？删除后不可恢复！')
  if (!ok) return

  try {
    await deleteAdminOrder(id)
    alert('删除成功')
    loadData()
  } catch (error) {
    console.error('删除订单失败：', error)
    alert(
      error?.response?.data?.msg ||
      error?.response?.data?.message ||
      '删除订单失败',
    )
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page {
  padding: 24px;
  background: #f6f8fb;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  color: #1f2329;
}

.sub-title {
  margin-top: 8px;
  color: #8c8c8c;
  font-size: 14px;
}

.filter-card,
.table-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 24px rgba(15, 35, 95, 0.05);
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  gap: 16px;
  align-items: end;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-item label {
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

.filter-item input,
.filter-item select {
  width: 220px;
  height: 38px;
  border: 1px solid #dcdfe6;
  border-radius: 10px;
  padding: 0 12px;
  box-sizing: border-box;
}

.filter-actions,
.action-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.primary-btn,
.reset-btn,
.detail-btn,
.status-btn,
.delete-btn,
.pager-btn {
  height: 38px;
  border: none;
  border-radius: 10px;
  padding: 0 18px;
  cursor: pointer;
}

.primary-btn,
.detail-btn,
.status-btn,
.pager-btn {
  background: #409eff;
  color: #fff;
}

.reset-btn {
  background: #f2f3f5;
  color: #333;
}

.delete-btn {
  background: #ff7875;
  color: #fff;
}

.loading-box,
.empty-box {
  text-align: center;
  padding: 40px 0;
  color: #8c8c8c;
}

.order-table {
  width: 100%;
  border-collapse: collapse;
}

.order-table th,
.order-table td {
  border-bottom: 1px solid #f0f0f0;
  padding: 14px 10px;
  text-align: left;
  font-size: 14px;
}

.title-cell {
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.pending {
  background: #fff7e6;
  color: #d48806;
}

.paid {
  background: #e6f7ff;
  color: #1677ff;
}

.done {
  background: #f6ffed;
  color: #389e0d;
}

.cancel {
  background: #fff1f0;
  color: #cf1322;
}

.timeout {
  background: #f5f5f5;
  color: #666;
}

.pager {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 16px;
  align-items: center;
}

.pager-btn:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.38);
  display: flex;
  justify-content: center;
  align-items: center;
}

.dialog-box {
  width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
}

.dialog-box h3 {
  margin-top: 0;
  margin-bottom: 12px;
}

.dialog-order-no {
  color: #666;
  margin-bottom: 18px;
}

.dialog-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 18px;
}

.dialog-item select {
  height: 38px;
  border: 1px solid #dcdfe6;
  border-radius: 10px;
  padding: 0 12px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
