<template>
  <div class="category-page">
    <section class="page-head app-card">
      <div>
        <p class="head-tag">Category Console</p>
        <h2>分类管理</h2>
      </div>

      <div class="head-actions">
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增分类</el-button>
        <el-button :icon="RefreshRight" :loading="loading" @click="refreshAll">刷新</el-button>
      </div>
    </section>

    <el-row :gutter="12">
      <el-col :xs="24" :lg="6" :xl="5">
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

          <el-scrollbar height="460px" class="tree-scroll">
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

      <el-col :xs="24" :lg="18" :xl="19">
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

          <div class="table-area">
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
              <el-table-column label="更新时间" min-width="170">
                <template #default="{ row }">
                  {{ formatDateTime(row.updateTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" fixed="right" min-width="290">
                <template #default="{ row }">
                  <div class="action-row">
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
                  </div>
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
          <el-tree-select
            v-model="dialogForm.parentId"
            :data="dialogParentTreeData"
            :props="dialogTreeProps"
            node-key="id"
            check-strictly
            filterable
            default-expand-all
            :render-after-expand="false"
            placeholder="请选择父分类"
            style="width: 100%"
          />
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
const dialogTreeProps = {
  children: 'children',
  label: 'label',
  disabled: 'disabled',
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

const queryParentOptions = computed(() =>
  flatTreeList.value.map((item) => ({
    id: item.id,
    label: `${'　'.repeat(Math.max(item.level - 1, 0))}${item.name}`,
  })),
)

const dialogParentTreeData = computed(() => {
  const currentId = String(dialogForm.id || '')
  const blockedIds = new Set()

  if (currentId) {
    markBlockedIds(treeData.value, currentId, blockedIds)
  }

  const buildTree = (nodes) =>
    (Array.isArray(nodes) ? nodes : []).map((node) => {
      const id = String(node.id)
      const level = Number(node.level || 1)

      return {
        id,
        label: node.name,
        disabled: blockedIds.has(id) || level >= 3,
        children: buildTree(node.children),
      }
    })

  return [
    {
      id: '0',
      label: '根分类',
      disabled: false,
      children: buildTree(treeData.value),
    },
  ]
})

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

function appendChildrenIds(nodes, blockedIds) {
  if (!Array.isArray(nodes)) {
    return
  }

  for (const node of nodes) {
    const id = String(node.id)
    blockedIds.add(id)
    appendChildrenIds(node.children, blockedIds)
  }
}

function markBlockedIds(nodes, targetId, blockedIds) {
  if (!Array.isArray(nodes)) {
    return false
  }

  for (const node of nodes) {
    const id = String(node.id)
    if (id === targetId) {
      blockedIds.add(id)
      appendChildrenIds(node.children, blockedIds)
      return true
    }

    if (markBlockedIds(node.children, targetId, blockedIds)) {
      return true
    }
  }

  return false
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
  gap: 12px;
}

.page-head {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.head-tag {
  margin: 0;
  font-family: 'Lexend', sans-serif;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 11px;
  color: var(--text-light);
}

.page-head h2 {
  margin: 2px 0 0;
  font-size: 26px;
  color: #24446f;
}

.head-actions {
  display: flex;
  gap: 8px;
}

.side-card,
.main-card {
  border-radius: 16px;
  border: 1px solid var(--border);
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: var(--shadow-card);
  height: 100%;
}

.side-card :deep(.el-card__body),
.main-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  min-height: 640px;
}

.card-header h3 {
  margin: 0;
  color: #2e5286;
  font-size: 17px;
}

.card-header p {
  margin: 4px 0 0;
  color: var(--text-secondary);
  font-size: 12px;
}

.tree-tools {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.tree-scroll {
  margin-top: 8px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #fbfdff;
  padding: 8px;
  flex: 1;
  min-height: 0;
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
  border: 1px solid var(--border);
  background: #fbfdff;
}

.query-bar :deep(.el-form-item:last-child) {
  margin-left: auto;
  margin-right: 0;
}

.category-table {
  margin-top: 0;
}

.table-area {
  margin-top: 12px;
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
}

.action-row {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 6px;
  white-space: nowrap;
}

.pagination-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .page-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .query-bar {
    padding: 10px 10px 0;
  }

  .query-bar :deep(.el-form-item) {
    margin-right: 0;
  }

  .side-card :deep(.el-card__body),
  .main-card :deep(.el-card__body) {
    min-height: auto;
  }

  .action-row {
    flex-wrap: wrap;
    white-space: normal;
  }
}
</style>
