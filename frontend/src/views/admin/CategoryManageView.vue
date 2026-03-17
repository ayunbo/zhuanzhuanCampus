<template>
  <div class="category-page fade-in-up">
    <section class="hero-card">
      <div class="hero-text">
        <p class="eyebrow">Catalog Console</p>
        <h2>分类管理</h2>
        <p>维护分类树、排序和启用状态，前台发布与筛选都依赖这里的数据。</p>
      </div>

      <div class="hero-stats">
        <article class="stat-box">
          <span>总分类</span>
          <strong>{{ stats.total }}</strong>
        </article>
        <article class="stat-box">
          <span>启用</span>
          <strong>{{ stats.enabled }}</strong>
        </article>
        <article class="stat-box">
          <span>禁用</span>
          <strong>{{ stats.disabled }}</strong>
        </article>
        <article class="stat-box">
          <span>一级分类</span>
          <strong>{{ stats.level1 }}</strong>
        </article>
      </div>

      <div class="hero-actions">
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增分类</el-button>
        <el-button :icon="RefreshRight" :loading="loading" @click="refreshAll">刷新</el-button>
      </div>
    </section>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="7" :xl="6">
        <el-card class="side-card">
          <template #header>
            <div class="card-header">
              <div>
                <h3>分类树</h3>
                <p>{{ currentTreeLabel }}</p>
              </div>
            </div>
          </template>

          <el-input v-model="treeKeyword" clearable placeholder="筛选分类名称">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <div class="tree-tools">
            <el-button size="small" text @click="viewAllFromTree">全部</el-button>
            <el-button size="small" text @click="expandTree">展开</el-button>
            <el-button size="small" text @click="collapseTree">收起</el-button>
          </div>

          <el-scrollbar height="520px" class="tree-scroll">
            <el-tree
              ref="treeRef"
              node-key="id"
              :data="treeData"
              :props="treeProps"
              :default-expanded-keys="expandedKeys"
              :expand-on-click-node="false"
              :highlight-current="true"
              :filter-node-method="filterTreeNode"
              @node-click="handleTreeNodeClick"
            >
              <template #default="{ data }">
                <div class="tree-node">
                  <span class="tree-node-name">{{ data.name }}</span>
                  <el-tag size="small" :type="Number(data.status) === 1 ? 'success' : 'info'">
                    {{ Number(data.status) === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </div>
              </template>
            </el-tree>
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="17" :xl="18">
        <el-card class="main-card">
          <template #header>
            <div class="card-header">
              <div>
                <h3>分类列表</h3>
                <p>{{ currentFilterLabel }}</p>
              </div>
            </div>
          </template>

          <el-form class="query-bar" :inline="true" :model="query">
            <el-form-item label="分类名称">
              <el-input
                v-model="query.name"
                clearable
                placeholder="按名称模糊查询"
                style="width: 200px"
                @keyup.enter="handleSearch"
              />
            </el-form-item>

            <el-form-item label="状态">
              <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px">
                <el-option label="启用" value="1" />
                <el-option label="禁用" value="0" />
              </el-select>
            </el-form-item>

            <el-form-item label="父分类">
              <el-select v-model="query.parentId" clearable filterable placeholder="全部父分类" style="width: 230px">
                <el-option label="根分类" value="0" />
                <el-option
                  v-for="item in queryParentOptions"
                  :key="String(item.id)"
                  :label="item.label"
                  :value="String(item.id)"
                />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table
            v-loading="loading"
            :data="tableData"
            border
            stripe
            row-key="id"
            class="category-table"
            empty-text="暂无数据"
          >
            <el-table-column prop="id" label="ID" min-width="168" />
            <el-table-column prop="name" label="分类名称" min-width="170" show-overflow-tooltip />
            <el-table-column label="父分类" min-width="170" show-overflow-tooltip>
              <template #default="{ row }">
                {{ parentName(row.parentId) }}
              </template>
            </el-table-column>
            <el-table-column label="层级" width="90">
              <template #default="{ row }">
                <el-tag size="small" type="warning">{{ levelText(row.level) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="排序" width="90" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'" size="small">
                  {{ Number(row.status) === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" min-width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="更新时间" min-width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.updateTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" min-width="280">
              <template #default="{ row }">
                <el-space wrap>
                  <el-button size="small" type="primary" :loading="isActionLoading(row)" @click="openEditDialog(row)">
                    编辑
                  </el-button>
                  <el-button size="small" :loading="isActionLoading(row)" @click="handleSort(row)">排序</el-button>
                  <el-button size="small" :loading="isActionLoading(row)" @click="handleStatus(row)">
                    {{ Number(row.status) === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button size="small" type="danger" :loading="isActionLoading(row)" @click="handleDelete(row)">
                    删除
                  </el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="query.page"
              v-model:page-size="query.pageSize"
              :total="total"
              :page-sizes="[10, 20, 30]"
              layout="total, sizes, prev, pager, next"
              @current-change="loadPage"
              @size-change="handleSizeChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="88px" status-icon>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="dialogForm.name" maxlength="50" placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="父分类" prop="parentId">
          <el-select v-model="dialogForm.parentId" filterable placeholder="请选择父分类">
            <el-option label="根分类" value="0" />
            <el-option
              v-for="item in dialogParentOptions"
              :key="String(item.id)"
              :label="item.label"
              :value="String(item.id)"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="dialogForm.sort" :min="0" :max="999999" />
        </el-form-item>

        <el-form-item v-if="dialogMode === 'create'" label="状态" prop="status">
          <el-select v-model="dialogForm.status" placeholder="请选择状态">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitDialog">
            {{ dialogMode === 'create' ? '确认新增' : '确认保存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshRight, Search } from '@element-plus/icons-vue'
import {
  createCategory,
  deleteCategory,
  fetchCategoryPage,
  fetchCategoryTree,
  updateCategory,
  updateCategorySort,
  updateCategoryStatus,
} from '@/api/admin'
import { formatDateTime } from '@/utils/format'

const treeRef = ref()
const treeKeyword = ref('')
const treeProps = {
  children: 'children',
  label: 'name',
}
const expandedKeys = ref([])

const loading = ref(false)
const actionLoadingId = ref('')
const tableData = ref([])
const total = ref(0)

const treeData = ref([])
const parentLabelMap = ref({})

const query = reactive({
  name: '',
  parentId: '',
  status: '',
  page: 1,
  pageSize: 10,
})

const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitLoading = ref(false)
const dialogFormRef = ref()

const dialogForm = reactive({
  id: '',
  name: '',
  parentId: '0',
  sort: 0,
  status: '1',
})

const dialogRules = {
  name: [
    {
      required: true,
      message: '请输入分类名称',
      trigger: 'blur',
    },
  ],
  parentId: [
    {
      required: true,
      message: '请选择父分类',
      trigger: 'change',
    },
  ],
  sort: [
    {
      required: true,
      message: '请输入排序值',
      trigger: 'change',
    },
  ],
  status: [
    {
      required: true,
      message: '请选择状态',
      trigger: 'change',
    },
  ],
}

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增分类' : '编辑分类'))

const flatTreeList = computed(() => {
  const result = []

  const walk = (nodes, path = []) => {
    if (!Array.isArray(nodes)) {
      return
    }

    for (const node of nodes) {
      const level = Number(node.level || path.length + 1)
      const currentPath = [...path, node.name]
      result.push({
        id: node.id,
        parentId: node.parentId,
        name: node.name,
        level,
        status: Number(node.status),
        pathLabel: currentPath.join(' / '),
      })

      if (Array.isArray(node.children) && node.children.length > 0) {
        walk(node.children, currentPath)
      }
    }
  }

  walk(treeData.value)
  return result
})

const stats = computed(() => {
  const list = flatTreeList.value
  return {
    total: list.length,
    enabled: list.filter((item) => item.status === 1).length,
    disabled: list.filter((item) => item.status !== 1).length,
    level1: list.filter((item) => item.level === 1).length,
  }
})

const queryParentOptions = computed(() =>
  flatTreeList.value.map((item) => ({
    id: item.id,
    label: `${'　'.repeat(Math.max(item.level - 1, 0))}${item.name}`,
  })),
)

const dialogParentOptions = computed(() =>
  flatTreeList.value
    .filter((item) => item.level < 3 && String(item.id) !== String(dialogForm.id))
    .map((item) => ({
      id: item.id,
      label: `${'　'.repeat(Math.max(item.level - 1, 0))}${item.pathLabel}`,
    })),
)

const currentTreeLabel = computed(() => {
  if (query.parentId === '') {
    return '当前：全部分类'
  }
  if (query.parentId === '0') {
    return '当前：根分类'
  }
  return `当前：${parentLabelMap.value[String(query.parentId)] || query.parentId}`
})

const currentFilterLabel = computed(() => {
  if (query.parentId === '') {
    return '列表范围：全部父分类'
  }
  if (query.parentId === '0') {
    return '列表范围：根分类'
  }
  return `列表范围：${parentLabelMap.value[String(query.parentId)] || query.parentId} 的子分类`
})

function normalize(value) {
  return typeof value === 'string' ? value.trim() : ''
}

function isActionLoading(row) {
  return actionLoadingId.value === String(row.id)
}

function levelText(level) {
  const lv = Number(level)
  if (lv === 1) return '一级'
  if (lv === 2) return '二级'
  if (lv === 3) return '三级'
  return `${lv || '-'}`
}

function parentName(parentId) {
  if (String(parentId) === '0') {
    return '根分类'
  }
  return parentLabelMap.value[String(parentId)] || `ID: ${parentId}`
}

function filterTreeNode(value, data) {
  if (!value) {
    return true
  }
  return String(data.name || '').includes(value)
}

function buildQueryParams() {
  const params = {
    page: query.page,
    pageSize: query.pageSize,
  }

  const name = normalize(query.name)
  if (name) {
    params.name = name
  }

  if (query.parentId !== '') {
    params.parentId = query.parentId
  }

  if (query.status !== '') {
    params.status = Number(query.status)
  }

  return params
}

function resetDialogForm() {
  dialogForm.id = ''
  dialogForm.name = ''
  dialogForm.parentId = '0'
  dialogForm.sort = 0
  dialogForm.status = '1'
}

async function loadTree() {
  try {
    const data = await fetchCategoryTree()
    treeData.value = Array.isArray(data) ? data : []
    expandedKeys.value = flatTreeList.value.filter((item) => item.level <= 2).map((item) => item.id)

    const map = {}
    for (const item of flatTreeList.value) {
      map[String(item.id)] = item.name
    }
    parentLabelMap.value = map
  } catch (error) {
    ElMessage.error(error.message || '分类树加载失败')
  }
}

async function loadPage() {
  loading.value = true
  try {
    const pageData = await fetchCategoryPage(buildQueryParams())
    tableData.value = Array.isArray(pageData?.records) ? pageData.records : []
    total.value = Number(pageData?.total || 0)
  } catch (error) {
    ElMessage.error(error.message || '分类列表加载失败')
  } finally {
    loading.value = false
  }
}

async function refreshAll() {
  await Promise.all([loadTree(), loadPage()])
}

function handleSearch() {
  query.page = 1
  loadPage()
}

function handleReset() {
  query.name = ''
  query.parentId = ''
  query.status = ''
  query.page = 1
  treeRef.value?.setCurrentKey(null)
  loadPage()
}

function handleSizeChange(size) {
  query.page = 1
  query.pageSize = size
  loadPage()
}

function handleTreeNodeClick(node) {
  query.parentId = String(node.id)
  query.page = 1
  loadPage()
}

function viewAllFromTree() {
  query.parentId = ''
  query.page = 1
  treeRef.value?.setCurrentKey(null)
  loadPage()
}

function expandTree() {
  expandedKeys.value = flatTreeList.value.map((item) => item.id)
}

function collapseTree() {
  expandedKeys.value = []
}

function openCreateDialog() {
  dialogMode.value = 'create'
  resetDialogForm()
  dialogVisible.value = true
  nextTick(() => dialogFormRef.value?.clearValidate())
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  dialogForm.id = String(row.id)
  dialogForm.name = row.name || ''
  dialogForm.parentId = String(row.parentId ?? '0')
  dialogForm.sort = Number(row.sort ?? 0)
  dialogForm.status = String(row.status ?? '1')
  dialogVisible.value = true
  nextTick(() => dialogFormRef.value?.clearValidate())
}

async function submitDialog() {
  if (!dialogFormRef.value) {
    return
  }

  try {
    await dialogFormRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  const name = normalize(dialogForm.name)

  try {
    if (dialogMode.value === 'create') {
      await createCategory({
        name,
        parentId: dialogForm.parentId,
        sort: Number(dialogForm.sort || 0),
        status: Number(dialogForm.status),
      })
      ElMessage.success('分类新增成功')
    } else {
      await updateCategory({
        id: dialogForm.id,
        name,
        parentId: dialogForm.parentId,
        sort: Number(dialogForm.sort || 0),
      })
      ElMessage.success('分类更新成功')
    }

    dialogVisible.value = false
    await refreshAll()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleStatus(row) {
  const targetStatus = Number(row.status) === 1 ? 0 : 1
  const actionText = targetStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确认${actionText}分类「${row.name || row.id}」吗？`, '状态确认', {
      type: 'warning',
      confirmButtonText: `确认${actionText}`,
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  actionLoadingId.value = String(row.id)
  try {
    await updateCategoryStatus({
      id: row.id,
      status: targetStatus,
    })
    ElMessage.success(`分类已${actionText}`)
    await refreshAll()
  } catch (error) {
    ElMessage.error(error.message || `${actionText}失败`)
  } finally {
    actionLoadingId.value = ''
  }
}

async function handleSort(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的排序值（数字越小越靠前）', '修改排序', {
      inputValue: String(row.sort ?? 0),
      inputPlaceholder: '例如：0、10、99',
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputValidator: (input) => {
        if (!/^\d+$/.test(String(input).trim())) {
          return '排序值必须是非负整数'
        }
        return true
      },
    })

    actionLoadingId.value = String(row.id)
    await updateCategorySort({
      id: row.id,
      sort: Number(String(value).trim()),
    })
    ElMessage.success('排序更新成功')
    await refreshAll()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error(error.message || '排序更新失败')
  } finally {
    actionLoadingId.value = ''
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除分类「${row.name || row.id}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  actionLoadingId.value = String(row.id)
  try {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')

    if (tableData.value.length === 1 && query.page > 1) {
      query.page -= 1
    }

    await refreshAll()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  } finally {
    actionLoadingId.value = ''
  }
}

watch(treeKeyword, (value) => {
  treeRef.value?.filter(value)
})

watch(
  () => query.parentId,
  (value) => {
    if (value === '' || value === '0') {
      treeRef.value?.setCurrentKey(null)
      return
    }
    treeRef.value?.setCurrentKey(value)
  },
)

onMounted(() => {
  refreshAll()
})
</script>

<style scoped>
.category-page {
  display: grid;
  gap: 16px;
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(280px, 1.3fr) minmax(260px, 1fr) auto;
  gap: 16px;
  align-items: center;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid #efcf9c;
  background:
    radial-gradient(520px 240px at 95% -30%, rgba(235, 161, 80, 0.26) 0%, rgba(235, 161, 80, 0) 70%),
    linear-gradient(135deg, #fff4df 0%, #fffbf3 60%, #ffffff 100%);
  box-shadow: 0 18px 30px rgba(180, 118, 45, 0.16);
}

.hero-text .eyebrow {
  margin: 0;
  color: #c56d14;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 12px;
  font-weight: 700;
}

.hero-text h2 {
  margin: 4px 0 6px;
  font-size: 28px;
  line-height: 1.2;
  color: #4f3015;
}

.hero-text p {
  margin: 0;
  color: #7e5a35;
  font-size: 13px;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.stat-box {
  border: 1px solid #f1d8b6;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.86);
  padding: 10px 12px;
  display: grid;
  gap: 2px;
}

.stat-box span {
  color: #8d6c48;
  font-size: 12px;
}

.stat-box strong {
  color: #6c3b10;
  font-size: 24px;
  line-height: 1;
}

.hero-actions {
  display: grid;
  gap: 10px;
}

.side-card,
.main-card {
  border-radius: 16px;
  border: 1px solid #ecd7bb;
  background: linear-gradient(180deg, #fffefb 0%, #fff8ed 100%);
  box-shadow: 0 14px 26px rgba(181, 121, 48, 0.12);
}

.card-header h3 {
  margin: 0;
  font-size: 17px;
  color: #5d3818;
}

.card-header p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #82603f;
}

.tree-tools {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.tree-scroll {
  margin-top: 8px;
  border: 1px solid #efdcc0;
  border-radius: 12px;
  background: #fffdf8;
  padding: 8px;
}

.tree-node {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  padding-right: 6px;
}

.tree-node-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.query-bar {
  padding: 12px 12px 2px;
  border-radius: 12px;
  border: 1px solid #efd8ba;
  background: #fffdf8;
}

.category-table {
  margin-top: 14px;
}

.pagination-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1400px) {
  .hero-card {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-text h2 {
    font-size: 24px;
  }

  .hero-stats {
    grid-template-columns: 1fr 1fr;
  }

  .hero-actions {
    grid-template-columns: 1fr;
  }

  .query-bar {
    padding: 10px 10px 0;
  }

  .query-bar :deep(.el-form-item) {
    margin-right: 0;
  }
}
</style>
