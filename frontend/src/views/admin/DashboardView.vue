<template>
  <el-card shadow="never">
    <el-space direction="vertical" fill style="width: 100%">
      <el-form :inline="true">
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadStats">刷新</el-button>
        </el-form-item>
      </el-form>

      <el-descriptions v-if="showPendingPanel" :title="pendingTitle" :column="4" border>
        <el-descriptions-item v-for="item in mergedCards" :key="item.key" :label="item.title">
          {{ formatCount(item.value) }}
        </el-descriptions-item>
      </el-descriptions>

      <el-descriptions v-if="showGoodsPanel" title="商品统计" :column="4" border>
        <el-descriptions-item v-for="item in goodsCards" :key="item.key" :label="item.title">
          {{ formatCount(item.value) }}
        </el-descriptions-item>
      </el-descriptions>

      <el-descriptions v-if="showOrderPanel" title="订单统计" :column="4" border>
        <el-descriptions-item v-for="item in orderCards" :key="item.key" :label="item.title">
          {{ formatCount(item.value) }}
        </el-descriptions-item>
      </el-descriptions>

      <el-card v-if="showQuickPanel" shadow="never">
        <template #header>常用功能</template>
        <el-space wrap>
          <el-button v-for="item in quickActions" :key="item.to" tag="router-link" :to="item.to">
            {{ item.title }}
          </el-button>
        </el-space>
      </el-card>
    </el-space>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchAdminPage, fetchCategoryPage, fetchSellerAuthPage, fetchUserPage } from '@/api/admin'
import { SELLER_AUTH_STATUS } from '@/constants/sellerAuth'

const route = useRoute()
const loading = ref(false)

const authStats = reactive({
  pending: 0,
  approved: 0,
  rejected: 0,
  revoked: 0,
})

const overview = reactive({
  userTotal: 0,
  sellerUserTotal: 0,
  adminTotal: 0,
  categoryTotal: 0,
})

const productStats = reactive({
  online: null,
  toReview: null,
  rejected: null,
  offline: null,
  processing: null,
  autoUp: null,
  autoDown: null,
  afterSale: null,
})

const orderStats = reactive({
  all: null,
  pendingPay: null,
  pendingShip: null,
  shipped: null,
  refunding: null,
  closed: null,
})

const statusConfig = [
  { key: 'pending', status: SELLER_AUTH_STATUS.PENDING },
  { key: 'approved', status: SELLER_AUTH_STATUS.APPROVED },
  { key: 'rejected', status: SELLER_AUTH_STATUS.REJECTED },
  { key: 'revoked', status: SELLER_AUTH_STATUS.REVOKED },
]

const doneAuthCount = computed(() => Number(authStats.approved) + Number(authStats.rejected))

const activePanel = computed(() => {
  const panel = String(route.query.panel || 'overview')
  const panels = ['overview', 'status', 'goods', 'orders']
  return panels.includes(panel) ? panel : 'overview'
})

const pendingTitle = computed(() => (activePanel.value === 'status' ? '状态归纳' : '待处理与今日统计'))
const showPendingPanel = computed(() => activePanel.value === 'overview' || activePanel.value === 'status')
const showGoodsPanel = computed(() => activePanel.value === 'overview' || activePanel.value === 'goods')
const showOrderPanel = computed(() => activePanel.value === 'overview' || activePanel.value === 'orders')
const showQuickPanel = computed(() => activePanel.value === 'overview')

const pendingCards = computed(() => [
  { key: 'p1', title: '待审核申请', value: authStats.pending },
  { key: 'p2', title: '驳回待复核', value: authStats.rejected },
  { key: 'p3', title: '卖家账号', value: overview.sellerUserTotal },
  { key: 'p4', title: '管理员账号', value: overview.adminTotal },
])

const todayCards = computed(() => [
  { key: 't1', title: '平台用户', value: overview.userTotal },
  { key: 't2', title: '认证通过', value: authStats.approved },
  { key: 't3', title: '已处理申请', value: doneAuthCount.value },
  { key: 't4', title: '分类数量', value: overview.categoryTotal },
])

const mergedCards = computed(() => [...pendingCards.value, ...todayCards.value])

const goodsCards = computed(() => [
  { key: 'g1', title: '商品分类数', value: overview.categoryTotal },
  { key: 'g2', title: '销售中', value: productStats.online },
  { key: 'g3', title: '待审核', value: productStats.toReview },
  { key: 'g4', title: '审核驳回', value: productStats.rejected },
  { key: 'g5', title: '已下架', value: productStats.offline },
  { key: 'g6', title: '处理中', value: productStats.processing },
  { key: 'g7', title: '自动上架', value: productStats.autoUp },
  { key: 'g8', title: '自动下架', value: productStats.autoDown },
  { key: 'g9', title: '售后恢复', value: productStats.afterSale },
])

const orderCards = computed(() => [
  { key: 'o1', title: '全部订单', value: orderStats.all },
  { key: 'o2', title: '待买家付款', value: orderStats.pendingPay },
  { key: 'o3', title: '待发货', value: orderStats.pendingShip },
  { key: 'o4', title: '已发货', value: orderStats.shipped },
  { key: 'o5', title: '退款中', value: orderStats.refunding },
  { key: 'o6', title: '交易关闭', value: orderStats.closed },
])

const quickActions = [
  { to: '/product-manage?status=to_review', title: '商品审核' },
  { to: '/order-manage?status=pending_ship', title: '订单处理' },
  { to: '/seller-auth', title: '卖家认证审核' },
  { to: '/category-manage', title: '分类管理' },
]

function formatCount(value) {
  if (value === null || value === undefined) {
    return '--'
  }
  return Number(value || 0).toLocaleString('zh-CN')
}

async function loadStats() {
  loading.value = true

  try {
    const [statusResultList, adminPageData, userPageData, sellerUserPageData, categoryPageData] = await Promise.all([
      Promise.all(
        statusConfig.map((item) =>
          fetchSellerAuthPage({
            page: 1,
            pageSize: 1,
            status: item.status,
          }),
        ),
      ),
      fetchAdminPage({ page: 1, pageSize: 1 }),
      fetchUserPage({ page: 1, pageSize: 1 }),
      fetchUserPage({ page: 1, pageSize: 1, role: 2 }),
      fetchCategoryPage({ page: 1, pageSize: 1 }),
    ])

    statusResultList.forEach((result, index) => {
      const currentKey = statusConfig[index].key
      authStats[currentKey] = Number(result?.total || 0)
    })

    overview.adminTotal = Number(adminPageData?.total || 0)
    overview.userTotal = Number(userPageData?.total || 0)
    overview.sellerUserTotal = Number(sellerUserPageData?.total || 0)
    overview.categoryTotal = Number(categoryPageData?.total || 0)
  } catch (error) {
    ElMessage.error(error.message || '统计数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>
