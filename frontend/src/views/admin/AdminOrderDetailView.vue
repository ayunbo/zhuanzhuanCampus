<template>
  <div class="page" v-if="detail">
    <div class="page-header">
      <h2>订单详情</h2>
      <div class="header-actions">
        <button class="back-btn" @click="goBack">返回列表</button>
        <button class="edit-btn" @click="openEditDialog">编辑交易信息</button>
      </div>
    </div>

    <div class="card goods-card">
      <img :src="detail.goodsCover || defaultCover" class="goods-cover" />
      <div class="goods-info">
        <h3>{{ detail.goodsTitle }}</h3>
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

    <div class="card">
      <h3>交易信息</h3>
      <p>交易地点：{{ detail.meetLocation || '未填写' }}</p>
      <p>交易时间：{{ detail.meetTime || '未填写' }}</p>
      <p>备注：{{ detail.remark || '无' }}</p>
    </div>

    <div class="card">
      <h3>买卖双方信息</h3>
      <p>买家：{{ detail.buyerName || '未记录' }} / {{ detail.buyerPhone || '未记录' }}</p>
      <p>卖家：{{ detail.sellerName || '未记录' }} / {{ detail.sellerPhone || '未记录' }}</p>
    </div>

    <div class="card">
      <h3>订单时间信息</h3>
      <p>创建时间：{{ detail.createTime || '未记录' }}</p>
      <p>支付时间：{{ detail.payTime || '未支付' }}</p>
      <p>完成时间：{{ detail.completeTime || '未完成' }}</p>
      <p>关闭时间：{{ detail.closeTime || '未关闭' }}</p>
      <p>过期时间：{{ detail.expireTime || '未记录' }}</p>
    </div>

    <div class="card">
      <h3>支付信息</h3>
      <p>支付单号：{{ detail.payNo || '未记录' }}</p>
      <p>支付方式：{{ formatPayMethod(detail.payMethod) }}</p>
      <p>支付状态：{{ formatPayStatus(detail.payStatus) }}</p>
    </div>

    <div class="card">
      <h3>支付记录</h3>
      <div v-if="!detail.payRecords || detail.payRecords.length === 0" class="empty-box">
        暂无支付记录
      </div>

      <table v-else class="record-table">
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

    <!-- 编辑交易信息弹窗 -->
    <div v-if="showEditDialog" class="dialog-mask">
      <div class="dialog-box">
        <h3>编辑交易信息</h3>

        <div class="dialog-item">
          <label>交易地点</label>
          <input v-model="editForm.meetLocation" placeholder="请输入交易地点" />
        </div>

        <div class="dialog-item">
          <label>交易时间</label>
          <input v-model="editForm.meetTime" placeholder="格式：2026-03-20 15:00:00" />
        </div>

        <div class="dialog-item">
          <label>备注</label>
          <textarea v-model="editForm.remark" placeholder="请输入备注"></textarea>
        </div>

        <div class="dialog-actions">
          <button class="back-btn" @click="closeEditDialog">取消</button>
          <button class="edit-btn" @click="handleUpdateOrder">保存</button>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="page">
    <div class="empty-box">订单详情加载中...</div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdminOrderDetail, updateAdminOrder } from '@/api/admin'

const route = useRoute()
const router = useRouter()

const detail = ref(null)
const showEditDialog = ref(false)

const editForm = ref({
  id: null,
  meetLocation: '',
  meetTime: '',
  remark: '',
})

const defaultCover = 'https://via.placeholder.com/140x140?text=Goods'

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

const formatPayMethod = (method) => {
  const map = {
    1: '模拟支付',
    2: '微信支付',
    3: '支付宝支付',
    4: '余额支付',
  }
  return map[method] || '未知方式'
}

const formatPayStatus = (status) => {
  const map = {
    0: '待支付',
    1: '成功',
    2: '失败',
    3: '关闭',
  }
  return map[status] || '未知状态'
}

const loadDetail = async () => {
  try {
    const id = Number(route.params.id)
    const res = await getAdminOrderDetail(id)
    detail.value = res?.data?.data ?? res?.data ?? res
  } catch (error) {
    console.error('获取管理员订单详情失败：', error)
    alert(
      error?.response?.data?.msg ||
      error?.response?.data?.message ||
      '获取订单详情失败',
    )
  }
}

const goBack = () => {
  router.push('/admin/order')
}

const openEditDialog = () => {
  editForm.value = {
    id: detail.value.id,
    meetLocation: detail.value.meetLocation || '',
    meetTime: detail.value.meetTime || '',
    remark: detail.value.remark || '',
  }
  showEditDialog.value = true
}

const closeEditDialog = () => {
  showEditDialog.value = false
}

const handleUpdateOrder = async () => {
  try {
    await updateAdminOrder({
      id: editForm.value.id,
      meetLocation: editForm.value.meetLocation,
      meetTime: editForm.value.meetTime,
      remark: editForm.value.remark,
    })
    alert('订单交易信息修改成功')
    closeEditDialog()
    loadDetail()
  } catch (error) {
    console.error('修改订单失败：', error)
    alert(
      error?.response?.data?.msg ||
      error?.response?.data?.message ||
      '修改订单失败',
    )
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.page {
  padding: 24px;
  background: #f6f8fb;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  color: #1f2329;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.back-btn,
.edit-btn {
  height: 38px;
  border: none;
  border-radius: 10px;
  padding: 0 16px;
  cursor: pointer;
}

.back-btn {
  background: #f2f3f5;
  color: #333;
}

.edit-btn {
  background: #409eff;
  color: #fff;
}

.card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 18px;
  box-shadow: 0 8px 24px rgba(15, 35, 95, 0.05);
}

.card h3 {
  margin-top: 0;
  margin-bottom: 14px;
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
}

.goods-info h3 {
  margin: 0 0 12px;
}

.goods-info p,
.card p {
  margin: 8px 0;
  color: #555;
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

.record-table {
  width: 100%;
  border-collapse: collapse;
}

.record-table th,
.record-table td {
  border-bottom: 1px solid #f0f0f0;
  padding: 12px 8px;
  text-align: left;
  font-size: 14px;
}

.empty-box {
  text-align: center;
  color: #8c8c8c;
  padding: 30px 0;
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
  width: 460px;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
}

.dialog-box h3 {
  margin-top: 0;
  margin-bottom: 18px;
}

.dialog-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.dialog-item input,
.dialog-item textarea {
  border: 1px solid #dcdfe6;
  border-radius: 10px;
  padding: 10px 12px;
  box-sizing: border-box;
}

.dialog-item textarea {
  min-height: 90px;
  resize: vertical;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
