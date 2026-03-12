<template>
  <div class="dashboard-page fade-in-up">
    <section class="stats app-card">
      <div class="stats-head">
        <h3>卖家认证审核统计</h3>
        <button class="app-btn secondary" :disabled="loading" @click="loadStats">
          {{ loading ? '刷新中...' : '刷新统计' }}
        </button>
      </div>

      <div class="stats-grid">
        <article v-for="item in statCards" :key="item.key" class="stat-card">
          <h4>{{ item.title }}</h4>
          <strong>{{ item.count }}</strong>
          <p>{{ item.tip }}</p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchSellerAuthPage } from '@/api/admin'
import { SELLER_AUTH_STATUS } from '@/constants/sellerAuth'

const loading = ref(false)

const stats = reactive({
  pending: 0,
  approved: 0,
  rejected: 0,
  revoked: 0,
})

const statusConfig = [
  {
    key: 'pending',
    title: '待审核',
    tip: '需要管理员处理',
    status: SELLER_AUTH_STATUS.PENDING,
  },
  {
    key: 'approved',
    title: '已通过',
    tip: '已升级为卖家',
    status: SELLER_AUTH_STATUS.APPROVED,
  },
  {
    key: 'rejected',
    title: '已驳回',
    tip: '审核未通过申请',
    status: SELLER_AUTH_STATUS.REJECTED,
  },
  {
    key: 'revoked',
    title: '已撤回',
    tip: '申请方主动撤销',
    status: SELLER_AUTH_STATUS.REVOKED,
  },
]

const statCards = computed(() =>
  statusConfig.map((item) => ({
    ...item,
    count: stats[item.key],
  })),
)

async function loadStats() {
  loading.value = true

  try {
    const resultList = await Promise.all(
      statusConfig.map((item) =>
        fetchSellerAuthPage({
          page: 1,
          pageSize: 1,
          status: item.status,
        }),
      ),
    )

    resultList.forEach((result, index) => {
      const currentKey = statusConfig[index].key
      stats[currentKey] = Number(result?.total || 0)
    })
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
  gap: 16px;
}

.stats {
  padding: 18px 18px 20px;
}

.stats-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.stats-head h3 {
  margin: 0;
  font-size: 18px;
  color: #5c3b1f;
}

.stats-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  border-radius: 14px;
  padding: 14px 14px 12px;
  border: 1px solid #f0d5b2;
  background: linear-gradient(140deg, #fffbf4 0%, #ffe9ca 100%);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.6);
}

.stat-card h4 {
  margin: 0;
  color: #7a5a33;
}

.stat-card strong {
  margin-top: 10px;
  display: block;
  font-size: 34px;
  line-height: 1;
  color: #8e4b03;
}

.stat-card p {
  margin-top: 8px;
  color: #775d43;
  font-size: 13px;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .stats-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
