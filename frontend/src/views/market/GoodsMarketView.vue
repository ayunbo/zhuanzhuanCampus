<template>
  <div class="market-page fade-in-up">
    <section class="hero app-card">
      <div>
        <p class="eyebrow">Market</p>
        <h2>商品广场</h2>
        <p class="intro">
          这里验证展示侧查询与详情页能力。列表只读取冗余统计字段，详情页可以直接查看浏览量和收藏量变化。
        </p>
      </div>

      <div class="hero-actions">
        <RouterLink v-if="authStore.isSeller" to="/market/seller/goods" class="app-btn primary">
          进入卖家工作台
        </RouterLink>
        <RouterLink v-else-if="authStore.isAdmin" to="/goods-manage" class="app-btn primary">
          去商品审核页
        </RouterLink>
        <RouterLink v-else to="/login?mode=user" class="app-btn primary">
          用户登录
        </RouterLink>
      </div>
    </section>

    <form class="filter-grid app-card" @submit.prevent="handleSearch">
      <label>
        <span>标题</span>
        <input v-model="queryForm.title" class="app-input" type="text" placeholder="支持模糊查询" />
      </label>

      <label>
        <span>卖家 ID</span>
        <input v-model="queryForm.sellerId" class="app-input" type="number" min="1" placeholder="精确匹配" />
      </label>

      <label>
        <span>分类 ID</span>
        <input v-model="queryForm.categoryId" class="app-input" type="number" min="1" placeholder="精确匹配" />
      </label>

      <div class="action-group">
        <button class="app-btn primary" type="submit">查询</button>
        <button class="app-btn ghost" type="button" @click="handleReset">重置</button>
      </div>
    </form>

    <section class="goods-grid">
      <article
        v-for="record in records"
        :key="String(record.id)"
        class="goods-card app-card"
      >
        <div class="cover-wrap" @click="goToDetail(record.id)">
          <img v-if="record.cover" :src="record.cover" :alt="record.title" class="cover" />
          <div v-else class="cover placeholder">暂无封面</div>
        </div>

        <div class="card-body">
          <h3 class="title" @click="goToDetail(record.id)">{{ record.title || '未命名商品' }}</h3>
          <div class="price-row">
            <strong class="price">¥{{ formatMoney(record.price) }}</strong>
            <span v-if="record.oldPrice" class="old-price">¥{{ formatMoney(record.oldPrice) }}</span>
          </div>

          <div class="meta-grid">
            <span>成色 {{ record.quality ?? '-' }}</span>
            <span>卖家 {{ record.sellerId ?? '-' }}</span>
            <span>分类 {{ record.categoryId ?? '-' }}</span>
            <span>{{ record.location || '地点待补充' }}</span>
          </div>

          <div class="stat-row">
            <span>浏览 {{ record.viewCount ?? 0 }}</span>
            <span>收藏 {{ record.favoriteCount ?? 0 }}</span>
          </div>

          <button class="app-btn secondary" type="button" @click="goToDetail(record.id)">查看详情</button>
        </div>
      </article>

      <div v-if="!loading && records.length === 0" class="empty-card app-card">
        暂无商品数据
      </div>
    </section>

    <footer class="pagination app-card">
      <div class="left">
        <span>共 {{ pager.total }} 条</span>
        <select v-model.number="pager.pageSize" class="app-select page-size" @change="handlePageSizeChange">
          <option :value="8">8 / 页</option>
          <option :value="12">12 / 页</option>
          <option :value="16">16 / 页</option>
        </select>
      </div>

      <div class="pages">
        <button class="app-btn ghost mini" :disabled="pager.page <= 1" @click="setPage(pager.page - 1)">上一页</button>
        <button
          v-for="page in visiblePages"
          :key="page"
          class="app-btn mini"
          :class="page === pager.page ? 'primary' : 'ghost'"
          @click="setPage(page)"
        >
          {{ page }}
        </button>
        <button class="app-btn ghost mini" :disabled="pager.page >= pageCount" @click="setPage(pager.page + 1)">
          下一页
        </button>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchGoodsPage } from '@/api/goods'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const records = ref([])

const queryForm = reactive({
  title: '',
  sellerId: '',
  categoryId: '',
})

const pager = reactive({
  page: 1,
  pageSize: 12,
  total: 0,
})

const pageCount = computed(() => {
  const count = Math.ceil(pager.total / pager.pageSize)
  return count > 0 ? count : 1
})

const visiblePages = computed(() => {
  const max = pageCount.value
  const current = pager.page
  const pages = []

  let start = Math.max(1, current - 2)
  let end = Math.min(max, start + 4)

  if (end - start < 4) {
    start = Math.max(1, end - 4)
  }

  for (let page = start; page <= end; page += 1) {
    pages.push(page)
  }

  return pages
})

function toNullableNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return null
  }

  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

function formatMoney(value) {
  const amount = Number(value)
  if (!Number.isFinite(amount)) {
    return '0.00'
  }

  return amount.toFixed(2)
}

function buildQueryParams() {
  const params = {
    page: pager.page,
    pageSize: pager.pageSize,
  }

  const title = queryForm.title.trim()
  if (title) {
    params.title = title
  }

  const sellerId = toNullableNumber(queryForm.sellerId)
  if (sellerId) {
    params.sellerId = sellerId
  }

  const categoryId = toNullableNumber(queryForm.categoryId)
  if (categoryId) {
    params.categoryId = categoryId
  }

  return params
}

async function fetchList() {
  loading.value = true

  try {
    const pageData = await fetchGoodsPage(buildQueryParams())
    records.value = Array.isArray(pageData?.records) ? pageData.records : []
    pager.total = Number(pageData?.total || 0)
  } catch (error) {
    ElMessage.error(error.message || '查询商品失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pager.page = 1
  fetchList()
}

function handleReset() {
  queryForm.title = ''
  queryForm.sellerId = ''
  queryForm.categoryId = ''
  pager.page = 1
  fetchList()
}

function handlePageSizeChange() {
  pager.page = 1
  fetchList()
}

function setPage(targetPage) {
  if (targetPage < 1 || targetPage > pageCount.value || targetPage === pager.page) {
    return
  }

  pager.page = targetPage
  fetchList()
}

function goToDetail(id) {
  router.push(`/market/goods/${id}`)
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.market-page {
  display: grid;
  gap: 18px;
}

.hero {
  padding: 24px;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
}

.eyebrow {
  margin: 0 0 6px;
  color: #b36a15;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 12px;
}

.hero h2 {
  margin: 0;
  font-size: 28px;
  color: #4f3218;
}

.intro {
  max-width: 660px;
  margin: 10px 0 0;
  color: var(--text-secondary);
}

.hero-actions {
  display: flex;
  align-items: center;
}

.filter-grid {
  padding: 18px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  align-items: end;
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

.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.goods-card {
  overflow: hidden;
}

.cover-wrap {
  cursor: pointer;
  background: linear-gradient(180deg, #fff3dc 0%, #ffeed1 100%);
}

.cover {
  display: block;
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
}

.cover.placeholder {
  display: grid;
  aspect-ratio: 1 / 1;
  place-items: center;
  color: #9f7c57;
}

.card-body {
  padding: 14px;
  display: grid;
  gap: 10px;
}

.title {
  margin: 0;
  font-size: 16px;
  color: #523518;
  cursor: pointer;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price {
  font-size: 22px;
  color: #ba5a18;
}

.old-price {
  color: #9f8a72;
  text-decoration: line-through;
}

.meta-grid,
.stat-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  color: var(--text-secondary);
  font-size: 13px;
}

.empty-card {
  min-height: 180px;
  display: grid;
  place-items: center;
  grid-column: 1 / -1;
  color: var(--text-secondary);
}

.pagination {
  padding: 14px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.pagination .left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pages {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.page-size {
  width: 110px;
}

.app-btn.mini {
  padding: 7px 10px;
  font-size: 12px;
}

@media (max-width: 1180px) {
  .goods-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .hero {
    padding: 18px;
    flex-direction: column;
    align-items: flex-start;
  }

  .goods-grid,
  .filter-grid {
    grid-template-columns: 1fr;
  }

  .action-group {
    flex-wrap: wrap;
  }
}
</style>
