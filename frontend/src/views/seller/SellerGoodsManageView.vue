<template>
  <div class="seller-page fade-in-up">
    <section class="page-header app-card">
      <div>
        <p class="eyebrow">Seller</p>
        <h2>卖家商品工作台</h2>
        <p class="intro">
          这里可以直接验证商品草稿、提审、上架、下架、售出等状态流转。草稿允许临时保存，占位字段会在提交审核前再校验。
        </p>
      </div>
      <button class="app-btn secondary" type="button" :disabled="loading" @click="fetchList">
        {{ loading ? '刷新中...' : '刷新列表' }}
      </button>
    </section>

    <section class="workspace">
      <form class="editor app-card" @submit.prevent="handleSaveDraft">
        <div class="editor-head">
          <div>
            <h3>{{ selectedId ? `编辑草稿 #${selectedId}` : '新建草稿' }}</h3>
            <p>提审前至少补齐分类、标题、价格，其他字段建议同步完善。</p>
          </div>

          <button class="app-btn ghost" type="button" @click="resetEditor">清空表单</button>
        </div>

        <div class="form-grid">
          <label>
            <span>分类 ID</span>
            <input v-model="form.categoryId" class="app-input" type="number" min="0" placeholder="草稿可暂填 0" />
          </label>

          <label>
            <span>成色</span>
            <select v-model="form.quality" class="app-select">
              <option value="">默认 5</option>
              <option v-for="quality in [1, 2, 3, 4, 5]" :key="quality" :value="String(quality)">
                {{ quality }}
              </option>
            </select>
          </label>

          <label class="full-span">
            <span>标题</span>
            <input v-model="form.title" class="app-input" type="text" maxlength="100" placeholder="草稿可暂空，提审前必填" />
          </label>

          <label>
            <span>售价</span>
            <input v-model="form.price" class="app-input" type="number" min="0" step="0.01" placeholder="提审前必须大于 0" />
          </label>

          <label>
            <span>原价</span>
            <input v-model="form.oldPrice" class="app-input" type="number" min="0" step="0.01" placeholder="可选" />
          </label>

          <label class="full-span">
            <span>面交地点</span>
            <input v-model="form.location" class="app-input" type="text" maxlength="120" placeholder="例如：东区图书馆门口" />
          </label>

          <label class="full-span">
            <span>封面地址</span>
            <input v-model="form.cover" class="app-input" type="text" placeholder="http(s)://..." />
          </label>

          <label class="full-span">
            <span>商品描述</span>
            <textarea v-model="form.detail" class="app-textarea" rows="5" placeholder="可先记录要点，后续继续补充" />
          </label>
        </div>

        <div class="editor-actions">
          <button class="app-btn primary" type="submit" :disabled="saving">
            {{ saving ? '保存中...' : selectedId ? '保存草稿' : '创建草稿' }}
          </button>
          <button
            class="app-btn secondary"
            type="button"
            :disabled="!selectedId || saving"
            @click="handleSubmitAudit(selectedId)"
          >
            提交审核
          </button>
        </div>
      </form>

      <section class="records app-card">
        <div class="records-toolbar">
          <h3>我的商品</h3>

          <div class="toolbar-actions">
            <select v-model="filters.status" class="app-select" @change="handleFilterChange">
              <option value="">全部状态</option>
              <option v-for="item in GOODS_STATUS_OPTIONS" :key="item.value" :value="String(item.value)">
                {{ item.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>标题</th>
                <th>价格</th>
                <th>成色</th>
                <th>状态</th>
                <th>驳回原因</th>
                <th>统计</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!loading && records.length === 0">
                <td colspan="8" class="empty-row">暂无商品数据</td>
              </tr>

              <tr v-for="record in records" :key="String(record.id)">
                <td>{{ record.id }}</td>
                <td class="title-cell">
                  <strong>{{ record.title || '未命名草稿' }}</strong>
                  <small>{{ record.location || '地点待补充' }}</small>
                </td>
                <td>¥{{ formatMoney(record.price) }}</td>
                <td>{{ record.quality ?? '-' }}</td>
                <td>
                  <span class="status-badge" :class="statusClass(record.status)">
                    {{ record.statusDesc || statusLabel(record.status) }}
                  </span>
                </td>
                <td>{{ record.reason || '-' }}</td>
                <td>浏览 {{ record.viewCount ?? 0 }} / 收藏 {{ record.favoriteCount ?? 0 }}</td>
                <td class="actions">
                  <button class="app-btn ghost mini" type="button" :disabled="!canEdit(record)" @click="fillEditor(record)">
                    编辑
                  </button>
                  <button class="app-btn secondary mini" type="button" :disabled="!canSubmitAudit(record)" @click="handleSubmitAudit(record.id)">
                    提审
                  </button>
                  <button class="app-btn secondary mini" type="button" :disabled="!canOnShelf(record)" @click="handleOnShelf(record.id)">
                    上架
                  </button>
                  <button class="app-btn ghost mini" type="button" :disabled="!canOffShelf(record)" @click="handleOffShelf(record.id)">
                    下架
                  </button>
                  <button class="app-btn danger mini" type="button" :disabled="!canSold(record)" @click="handleSold(record.id)">
                    售出
                  </button>
                  <button class="app-btn danger mini" type="button" @click="handleDelete(record.id)">
                    删除
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <footer class="pagination">
          <div class="left">
            <span>共 {{ pager.total }} 条</span>
            <select v-model.number="pager.pageSize" class="app-select page-size" @change="handlePageSizeChange">
              <option :value="10">10 / 页</option>
              <option :value="20">20 / 页</option>
              <option :value="30">30 / 页</option>
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
      </section>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createSellerGoodsDraft,
  deleteSellerGoods,
  fetchSellerGoodsPage,
  offShelfSellerGoods,
  onShelfSellerGoods,
  soldSellerGoods,
  submitSellerGoodsAudit,
  updateSellerGoodsDraft,
} from '@/api/goods'
import { GOODS_STATUS, GOODS_STATUS_LABEL_MAP, GOODS_STATUS_OPTIONS } from '@/constants/goods'

function createEmptyForm() {
  return {
    categoryId: '',
    title: '',
    detail: '',
    price: '',
    oldPrice: '',
    quality: '',
    location: '',
    cover: '',
  }
}

const loading = ref(false)
const saving = ref(false)
const selectedId = ref(null)
const records = ref([])

const form = reactive(createEmptyForm())
const filters = reactive({
  status: '',
})

const pager = reactive({
  page: 1,
  pageSize: 10,
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

function formatMoney(value) {
  const amount = Number(value)
  if (!Number.isFinite(amount)) {
    return '0.00'
  }

  return amount.toFixed(2)
}

function toNullableNumber(value, integerOnly = false) {
  if (value === '' || value === null || value === undefined) {
    return null
  }

  const parsed = Number(value)
  if (!Number.isFinite(parsed)) {
    return null
  }

  return integerOnly ? Math.trunc(parsed) : parsed
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

function canEdit(record) {
  return [GOODS_STATUS.DRAFT, GOODS_STATUS.REJECTED, GOODS_STATUS.OFF_SHELF].includes(record.status)
}

function canSubmitAudit(record) {
  return [GOODS_STATUS.DRAFT, GOODS_STATUS.REJECTED].includes(record.status)
}

function canOnShelf(record) {
  return record.status === GOODS_STATUS.OFF_SHELF
}

function canOffShelf(record) {
  return record.status === GOODS_STATUS.ON_SALE
}

function canSold(record) {
  return [GOODS_STATUS.ON_SALE, GOODS_STATUS.LOCKED].includes(record.status)
}

function resetEditor() {
  selectedId.value = null
  Object.assign(form, createEmptyForm())
}

function fillEditor(record) {
  selectedId.value = record.id
  form.categoryId = record.categoryId == null ? '' : String(record.categoryId)
  form.title = record.title || ''
  form.detail = record.detail || ''
  form.price = record.price == null ? '' : String(record.price)
  form.oldPrice = record.oldPrice == null ? '' : String(record.oldPrice)
  form.quality = record.quality == null ? '' : String(record.quality)
  form.location = record.location || ''
  form.cover = record.cover || ''
}

function buildPayload() {
  return {
    categoryId: toNullableNumber(form.categoryId, true),
    title: form.title,
    detail: form.detail,
    price: toNullableNumber(form.price),
    oldPrice: toNullableNumber(form.oldPrice),
    quality: toNullableNumber(form.quality, true),
    location: form.location,
    cover: form.cover,
  }
}

function buildQueryParams() {
  const params = {
    page: pager.page,
    pageSize: pager.pageSize,
  }

  const status = toNullableNumber(filters.status, true)
  if (status !== null) {
    params.status = status
  }

  return params
}

async function fetchList() {
  loading.value = true

  try {
    const pageData = await fetchSellerGoodsPage(buildQueryParams())
    records.value = Array.isArray(pageData?.records) ? pageData.records : []
    pager.total = Number(pageData?.total || 0)

    if (selectedId.value) {
      const current = records.value.find((item) => String(item.id) === String(selectedId.value))
      if (current && canEdit(current)) {
        fillEditor(current)
      } else if (!current) {
        resetEditor()
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '加载商品列表失败')
  } finally {
    loading.value = false
  }
}

async function handleSaveDraft() {
  saving.value = true

  try {
    if (selectedId.value) {
      await updateSellerGoodsDraft(selectedId.value, buildPayload())
      ElMessage.success(`草稿 #${selectedId.value} 已保存`)
    } else {
      const goodsId = await createSellerGoodsDraft(buildPayload())
      selectedId.value = goodsId
      ElMessage.success(`草稿 #${goodsId} 创建成功`)
    }

    await fetchList()
  } catch (error) {
    ElMessage.error(error.message || '保存草稿失败')
  } finally {
    saving.value = false
  }
}

async function confirmThenRun(message, action) {
  try {
    await ElMessageBox.confirm(message, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消',
    })
    await action()
  } catch {
    // ignore cancel
  }
}

async function handleSubmitAudit(goodsId) {
  await confirmThenRun(`确认提交商品 #${goodsId} 进入审核吗？`, async () => {
    await submitSellerGoodsAudit(goodsId)
    ElMessage.success(`商品 #${goodsId} 已提交审核`)
    await fetchList()
  })
}

async function handleOnShelf(goodsId) {
  await confirmThenRun(`确认重新上架商品 #${goodsId} 吗？`, async () => {
    await onShelfSellerGoods(goodsId)
    ElMessage.success(`商品 #${goodsId} 已上架`)
    await fetchList()
  })
}

async function handleOffShelf(goodsId) {
  await confirmThenRun(`确认下架商品 #${goodsId} 吗？`, async () => {
    await offShelfSellerGoods(goodsId)
    ElMessage.success(`商品 #${goodsId} 已下架`)
    await fetchList()
  })
}

async function handleSold(goodsId) {
  await confirmThenRun(`确认将商品 #${goodsId} 标记为已售出吗？`, async () => {
    await soldSellerGoods(goodsId)
    ElMessage.success(`商品 #${goodsId} 已售出`)
    await fetchList()
  })
}

async function handleDelete(goodsId) {
  await confirmThenRun(`确认删除商品 #${goodsId} 吗？删除后不可恢复。`, async () => {
    await deleteSellerGoods(goodsId)
    ElMessage.success(`商品 #${goodsId} 已删除`)
    await fetchList()
  })
}

function handleFilterChange() {
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

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.seller-page {
  display: grid;
  gap: 18px;
}

.page-header {
  padding: 22px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
}

.eyebrow {
  margin: 0 0 6px;
  color: #b36a15;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 12px;
}

.page-header h2 {
  margin: 0;
  color: #4f3218;
}

.intro {
  margin: 10px 0 0;
  max-width: 760px;
  color: var(--text-secondary);
}

.workspace {
  display: grid;
  grid-template-columns: 420px minmax(0, 1fr);
  gap: 16px;
}

.editor,
.records {
  padding: 18px;
}

.editor-head,
.records-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.editor-head h3,
.records-toolbar h3 {
  margin: 0;
  color: #5b391d;
}

.editor-head p {
  margin: 6px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.form-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.form-grid label {
  display: grid;
  gap: 8px;
}

.form-grid span {
  font-size: 13px;
  color: var(--text-secondary);
}

.full-span {
  grid-column: 1 / -1;
}

.app-textarea {
  width: 100%;
  min-height: 120px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: #fff;
  padding: 10px;
  outline: none;
  resize: vertical;
}

.app-textarea:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(226, 139, 47, 0.2);
}

.editor-actions {
  margin-top: 14px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.toolbar-actions {
  min-width: 140px;
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

.title-cell {
  min-width: 200px;
}

.title-cell small {
  display: block;
  margin-top: 4px;
  color: var(--text-secondary);
}

.actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.empty-row {
  text-align: center;
  color: var(--text-secondary);
}

.pagination {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
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
  padding: 6px 9px;
  font-size: 12px;
}

@media (max-width: 1120px) {
  .workspace {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .page-header,
  .editor-head,
  .records-toolbar {
    flex-direction: column;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
