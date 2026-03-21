<template>
  <div class="detail-page fade-in-up">
    <section v-if="detail" class="detail-card app-card">
      <div class="detail-grid">
        <div class="cover-panel">
          <img v-if="detail.cover" :src="detail.cover" :alt="detail.title" class="cover" />
          <div v-else class="cover placeholder">暂无封面</div>
        </div>

        <div class="info-panel">
          <button class="back-link" type="button" @click="goBack">返回列表</button>
          <h2>{{ detail.title || '未命名商品' }}</h2>

          <div class="status-row">
            <span class="status-badge" :class="statusClass(detail.status)">
              {{ detail.statusDesc || statusLabel(detail.status) }}
            </span>
            <span>商品 ID {{ detail.id }}</span>
            <span>卖家 {{ detail.sellerId }}</span>
          </div>

          <div class="price-row">
            <strong class="price">¥{{ formatMoney(detail.price) }}</strong>
            <span v-if="detail.oldPrice" class="old-price">¥{{ formatMoney(detail.oldPrice) }}</span>
          </div>

          <div class="meta-grid">
            <span>成色 {{ detail.quality ?? '-' }}</span>
            <span>分类 {{ detail.categoryId ?? '-' }}</span>
            <span>面交地点 {{ detail.location || '待补充' }}</span>
          </div>

          <div class="stat-grid">
            <div class="stat-card">
              <span>浏览量</span>
              <strong>{{ detail.viewCount ?? 0 }}</strong>
            </div>
            <div class="stat-card">
              <span>收藏量</span>
              <strong>{{ detail.favoriteCount ?? 0 }}</strong>
            </div>
          </div>

          <div class="actions">
            <button class="app-btn primary" type="button" :disabled="favoriteLoading" @click="toggleFavorite">
              {{ simulatedFavorite ? '取消模拟收藏' : '模拟收藏 +1' }}
            </button>

            <RouterLink v-if="authStore.isSeller" to="/market/seller/goods" class="app-btn secondary">
              去卖家工作台
            </RouterLink>
            <RouterLink v-else-if="!authStore.isLoggedIn" to="/login?mode=user" class="app-btn secondary">
              用户登录
            </RouterLink>
          </div>
        </div>
      </div>

      <section class="description-block">
        <h3>商品描述</h3>
        <p>{{ detail.detail || '卖家暂未填写商品描述。' }}</p>
      </section>
    </section>

    <div v-else-if="!loading" class="empty-state app-card">
      商品不存在或当前状态不可见
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchGoodsDetail, updateGoodsStats } from '@/api/goods'
import { GOODS_STATUS, GOODS_STATUS_LABEL_MAP } from '@/constants/goods'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const favoriteLoading = ref(false)
const detail = ref(null)
const simulatedFavorite = ref(false)

function currentGoodsId() {
  const parsed = Number(route.params.id)
  return Number.isFinite(parsed) ? parsed : null
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

function statusClass(status) {
  if (status === GOODS_STATUS.WAIT_AUDIT || status === GOODS_STATUS.LOCKED) return 'status-pending'
  if (status === GOODS_STATUS.ON_SALE || status === GOODS_STATUS.SOLD) return 'status-approved'
  if (status === GOODS_STATUS.REJECTED) return 'status-rejected'
  return 'status-revoked'
}

async function fetchDetailOnly() {
  const goodsId = currentGoodsId()
  if (!goodsId) {
    detail.value = null
    return
  }

  detail.value = await fetchGoodsDetail(goodsId)
}

async function loadDetail() {
  const goodsId = currentGoodsId()
  if (!goodsId) {
    detail.value = null
    return
  }

  loading.value = true
  simulatedFavorite.value = false

  try {
    await fetchDetailOnly()
    await updateGoodsStats({
      goodsId,
      viewDelta: 1,
    })
    await fetchDetailOnly()
  } catch (error) {
    detail.value = null
    ElMessage.error(error.message || '加载商品详情失败')
  } finally {
    loading.value = false
  }
}

async function toggleFavorite() {
  const goodsId = currentGoodsId()
  if (!goodsId) {
    return
  }

  favoriteLoading.value = true

  try {
    await updateGoodsStats({
      goodsId,
      favoriteDelta: simulatedFavorite.value ? -1 : 1,
    })
    simulatedFavorite.value = !simulatedFavorite.value
    await fetchDetailOnly()
    ElMessage.success(simulatedFavorite.value ? '收藏量已 +1' : '收藏量已 -1')
  } catch (error) {
    ElMessage.error(error.message || '更新收藏量失败')
  } finally {
    favoriteLoading.value = false
  }
}

function goBack() {
  router.push('/market/goods')
}

watch(() => route.params.id, () => {
  loadDetail()
})

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.detail-page {
  display: grid;
}

.detail-card {
  padding: 22px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 420px minmax(0, 1fr);
  gap: 24px;
}

.cover-panel {
  overflow: hidden;
  border-radius: 18px;
  border: 1px solid var(--border);
  background: linear-gradient(180deg, #fff3dc 0%, #ffeed1 100%);
}

.cover {
  width: 100%;
  display: block;
  aspect-ratio: 1 / 1;
  object-fit: cover;
}

.cover.placeholder {
  min-height: 420px;
  display: grid;
  place-items: center;
  color: #9f7c57;
}

.info-panel {
  display: grid;
  align-content: start;
  gap: 14px;
}

.back-link {
  width: fit-content;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  color: #9a5d1b;
  font-weight: 700;
}

.info-panel h2 {
  margin: 0;
  font-size: 32px;
  line-height: 1.2;
  color: #4f3218;
}

.status-row,
.meta-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 12px;
  color: var(--text-secondary);
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.price {
  font-size: 34px;
  color: #ba5a18;
}

.old-price {
  color: #9f8a72;
  text-decoration: line-through;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid #f0d7b8;
  background: #fff9ee;
  display: grid;
  gap: 4px;
}

.stat-card span {
  color: var(--text-secondary);
  font-size: 13px;
}

.stat-card strong {
  font-size: 24px;
  color: #5b3a1d;
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.description-block {
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid var(--border);
}

.description-block h3 {
  margin: 0 0 10px;
  color: #5a391c;
}

.description-block p {
  margin: 0;
  white-space: pre-wrap;
  color: #584532;
}

.empty-state {
  min-height: 320px;
  display: grid;
  place-items: center;
  color: var(--text-secondary);
}

@media (max-width: 980px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .cover.placeholder {
    min-height: 280px;
  }
}
</style>
