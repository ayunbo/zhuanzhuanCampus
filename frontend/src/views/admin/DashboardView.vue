<template>
  <div class="dashboard-page">
    <section class="headline app-card">
      <div>
        <p class="caption">Seller Auth Metrics</p>
        <h2>审核总览</h2>
      </div>
      <button class="app-btn primary" :disabled="loading" @click="loadStats">
        {{ loading ? '刷新中...' : '刷新数据' }}
      </button>
    </section>

    <section class="stats-grid">
      <article v-for="item in statCards" :key="item.key" class="stat-card" :class="`status-${item.key}`">
        <h3>{{ item.title }}</h3>
        <strong>{{ item.count }}</strong>
        <span>{{ item.tip }}</span>
      </article>
    </section>

    <section class="panel-grid">
      <article class="overview-card app-card">
        <header>
          <h3>平台总体数据</h3>
          <span>实时汇总</span>
        </header>

        <div class="overview-grid">
          <div v-for="item in overviewCards" :key="item.key" class="overview-item">
            <p>{{ item.title }}</p>
            <strong>{{ formatCount(item.count) }}</strong>
            <span>{{ item.tip }}</span>
          </div>
        </div>
      </article>

      <article class="shortcut-card app-card">
        <h3>快捷入口</h3>
        <div class="shortcut-grid">
          <RouterLink class="shortcut-item" to="/seller-auth">卖家认证审核</RouterLink>
          <RouterLink class="shortcut-item" to="/user-manage">用户管理</RouterLink>
          <RouterLink class="shortcut-item" to="/admin-manage">管理员管理</RouterLink>
          <RouterLink class="shortcut-item" to="/category-manage">分类管理</RouterLink>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminPage, fetchCategoryPage, fetchSellerAuthPage, fetchUserPage } from '@/api/admin'
import { SELLER_AUTH_STATUS } from '@/constants/sellerAuth'

const loading = ref(false)

const stats = reactive({
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

const statusConfig = [
  {
    key: 'pending',
    title: '待审核',
    tip: '待管理员处理',
    status: SELLER_AUTH_STATUS.PENDING,
  },
  {
    key: 'approved',
    title: '已通过',
    tip: '通过认证申请',
    status: SELLER_AUTH_STATUS.APPROVED,
  },
  {
    key: 'rejected',
    title: '已驳回',
    tip: '审核未通过',
    status: SELLER_AUTH_STATUS.REJECTED,
  },
  {
    key: 'revoked',
    title: '已撤回',
    tip: '申请人撤销',
    status: SELLER_AUTH_STATUS.REVOKED,
  },
]

const statCards = computed(() =>
  statusConfig.map((item) => ({
    ...item,
    count: stats[item.key],
  })),
)

const totalCount = computed(() => Object.values(stats).reduce((sum, count) => sum + Number(count || 0), 0))
const doneAuthCount = computed(() => Number(stats.approved) + Number(stats.rejected))
const overviewCards = computed(() => [
  {
    key: 'users',
    title: '用户总量',
    count: overview.userTotal,
    tip: '平台注册用户',
  },
  {
    key: 'sellers',
    title: '卖家用户',
    count: overview.sellerUserTotal,
    tip: '已成为卖家的用户',
  },
  {
    key: 'admins',
    title: '管理员账号',
    count: overview.adminTotal,
    tip: '后台管理账号数',
  },
  {
    key: 'categories',
    title: '分类总量',
    count: overview.categoryTotal,
    tip: '平台分类节点总数',
  },
  {
    key: 'auth_total',
    title: '认证申请总量',
    count: totalCount.value,
    tip: '全部卖家认证申请',
  },
  {
    key: 'auth_done',
    title: '已处理申请',
    count: doneAuthCount.value,
    tip: '已完成审核处理',
  },
])

function formatCount(value) {
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
      fetchAdminPage({
        page: 1,
        pageSize: 1,
      }),
      fetchUserPage({
        page: 1,
        pageSize: 1,
      }),
      fetchUserPage({
        page: 1,
        pageSize: 1,
        role: 2,
      }),
      fetchCategoryPage({
        page: 1,
        pageSize: 1,
      }),
    ])

    statusResultList.forEach((result, index) => {
      const currentKey = statusConfig[index].key
      stats[currentKey] = Number(result?.total || 0)
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

<style scoped>
.dashboard-page {
  display: grid;
  gap: 14px;
}

.headline {
  padding: 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.caption {
  margin: 0;
  font-family: 'Lexend', sans-serif;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 11px;
  color: var(--text-light);
}

.headline h2 {
  margin: 2px 0 0;
  font-size: 28px;
  color: #24456f;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  border-radius: 16px;
  border: 1px solid var(--border);
  padding: 14px;
  background: #ffffff;
  display: grid;
  gap: 8px;
}

.stat-card h3 {
  margin: 0;
  font-size: 14px;
  color: #4f658b;
}

.stat-card strong {
  font-family: 'Lexend', sans-serif;
  font-size: 34px;
  line-height: 1;
  color: #1f467a;
}

.stat-card span {
  color: var(--text-secondary);
  font-size: 12px;
}

.stat-pending {
  background: linear-gradient(150deg, #ffffff 0%, #fff6ea 100%);
}

.stat-approved {
  background: linear-gradient(150deg, #ffffff 0%, #ebfff7 100%);
}

.stat-rejected {
  background: linear-gradient(150deg, #ffffff 0%, #ffeff1 100%);
}

.stat-revoked {
  background: linear-gradient(150deg, #ffffff 0%, #edf4ff 100%);
}

.panel-grid {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 12px;
}

.overview-card,
.shortcut-card {
  padding: 16px;
}

.overview-card header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.overview-card h3,
.shortcut-card h3 {
  margin: 0;
  color: #2b4f80;
}

.overview-card header span {
  font-family: 'Lexend', sans-serif;
  font-size: 13px;
  color: var(--text-secondary);
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.overview-item {
  border: 1px solid #d8e8fb;
  border-radius: 12px;
  background: #f5faff;
  padding: 10px;
  display: grid;
  gap: 4px;
}

.overview-item p {
  margin: 0;
  color: #5f7498;
  font-size: 12px;
}

.overview-item strong {
  font-family: 'Lexend', sans-serif;
  color: #1f467a;
  font-size: 22px;
  line-height: 1;
}

.overview-item span {
  color: var(--text-secondary);
  font-size: 12px;
}

.shortcut-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.shortcut-item {
  min-height: 78px;
  border-radius: 14px;
  border: 1px solid #d6e7fa;
  background: #f4f9ff;
  display: grid;
  place-items: center;
  text-align: center;
  padding: 10px;
  color: #2c5b95;
  font-weight: 600;
  transition: transform 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
}

.shortcut-item:hover {
  transform: translateY(-2px);
  border-color: #bdd8ff;
  background: #e9f4ff;
}

@media (max-width: 1080px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .panel-grid {
    grid-template-columns: 1fr;
  }

  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .headline {
    flex-direction: column;
    align-items: flex-start;
  }

  .headline h2 {
    font-size: 24px;
  }

  .stats-grid,
  .shortcut-grid,
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
