<template>
  <div class="admin-page">
    <section class="app-card table-panel">
      <form class="filter-grid" @submit.prevent="handleSearch">
        <label>
          <span>账号</span>
          <input v-model="queryForm.username" class="app-input" type="text" placeholder="管理员账号" />
        </label>

        <label>
          <span>姓名</span>
          <input v-model="queryForm.name" class="app-input" type="text" placeholder="管理员姓名" />
        </label>

        <label>
          <span>手机号</span>
          <input v-model="queryForm.phone" class="app-input" type="text" placeholder="手机号" />
        </label>

        <label>
          <span>状态</span>
          <select v-model="queryForm.status" class="app-select">
            <option value="">全部</option>
            <option v-for="item in ADMIN_STATUS_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </label>

        <div class="action-group">
          <button class="app-btn primary" type="submit">查询</button>
          <button class="app-btn ghost" type="button" @click="handleReset">重置</button>
          <button class="app-btn primary" type="button" @click="handleCreate">新增管理员</button>
          <button class="app-btn secondary" type="button" :disabled="loading" @click="fetchList">
            {{ loading ? '加载中...' : '刷新列表' }}
          </button>
        </div>
      </form>

      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>账号</th>
              <th>姓名</th>
              <th>手机号</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && records.length === 0">
              <td class="empty-row" colspan="8">
                <div class="table-empty">
                  <el-icon><Box /></el-icon>
                  <span>暂无数据</span>
                </div>
              </td>
            </tr>

            <tr v-for="record in records" :key="record.id">
              <td>{{ record.id }}</td>
              <td>{{ record.username || '-' }}</td>
              <td>{{ record.name || '-' }}</td>
              <td>{{ record.phone || '-' }}</td>
              <td>
                <span class="status-badge" :class="statusClass(record.status)">
                  {{ statusLabel(record.status) }}
                </span>
              </td>
              <td>{{ formatDateTime(record.createTime) }}</td>
              <td>{{ formatDateTime(record.updateTime) }}</td>
              <td class="actions">
                <button class="app-btn primary mini" :disabled="actionLoadingId === record.id" @click="handleEdit(record)">
                  编辑
                </button>
                <button class="app-btn danger mini" :disabled="actionLoadingId === record.id" @click="handleDelete(record)">
                  删除
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="pagination-wrap">
        <el-pagination
          v-model:current-page="pager.page"
          v-model:page-size="pager.pageSize"
          :total="pager.total"
          :page-sizes="[10, 20, 30]"
          layout="total, sizes, prev, pager, next"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </footer>
    </section>
  </div>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="540px" destroy-on-close>
    <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="88px" status-icon>
      <el-form-item label="账号" prop="username">
        <el-input v-model="dialogForm.username" maxlength="64" placeholder="请输入管理员账号" />
      </el-form-item>

      <el-form-item label="姓名" prop="name">
        <el-input v-model="dialogForm.name" maxlength="64" placeholder="请输入管理员姓名" />
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="dialogForm.phone" maxlength="20" placeholder="请输入手机号（可选）" />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-select v-model="dialogForm.status" placeholder="请选择状态">
          <el-option
            v-for="item in ADMIN_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
          v-model="dialogForm.password"
          type="password"
          show-password
          maxlength="64"
          :placeholder="dialogMode === 'create' ? '不填则默认 123456' : '不填则不修改密码'"
        />
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
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box } from '@element-plus/icons-vue'
import {
  createAdmin,
  deleteAdmin,
  fetchAdminById,
  fetchAdminPage,
  updateAdmin,
} from '@/api/admin'
import { ADMIN_STATUS, ADMIN_STATUS_LABEL_MAP, ADMIN_STATUS_OPTIONS } from '@/constants/admin'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const actionLoadingId = ref(null)
const records = ref([])

const queryForm = reactive({
  username: '',
  name: '',
  phone: '',
  status: '',
})

const pager = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitLoading = ref(false)
const dialogFormRef = ref()

const dialogForm = reactive({
  id: null,
  username: '',
  name: '',
  phone: '',
  status: ADMIN_STATUS.NORMAL,
  password: '',
})

const dialogRules = {
  username: [
    {
      required: true,
      message: '请输入管理员账号',
      trigger: 'blur',
    },
  ],
  name: [
    {
      required: true,
      message: '请输入管理员姓名',
      trigger: 'blur',
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

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增管理员' : '编辑管理员'))

function normalize(value) {
  return typeof value === 'string' ? value.trim() : ''
}

function resetDialogForm() {
  dialogForm.id = null
  dialogForm.username = ''
  dialogForm.name = ''
  dialogForm.phone = ''
  dialogForm.status = ADMIN_STATUS.NORMAL
  dialogForm.password = ''
}

function buildQueryParams() {
  const params = {
    page: pager.page,
    pageSize: pager.pageSize,
  }

  const username = normalize(queryForm.username)
  const name = normalize(queryForm.name)
  const phone = normalize(queryForm.phone)

  if (username) {
    params.username = username
  }

  if (name) {
    params.name = name
  }

  if (phone) {
    params.phone = phone
  }

  if (queryForm.status !== '' && queryForm.status !== null) {
    params.status = Number(queryForm.status)
  }

  return params
}

async function fetchList() {
  loading.value = true

  try {
    const pageData = await fetchAdminPage(buildQueryParams())
    records.value = Array.isArray(pageData?.records) ? pageData.records : []
    pager.total = Number(pageData?.total || 0)
  } catch (error) {
    ElMessage.error(error.message || '管理员列表加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pager.page = 1
  fetchList()
}

function handleReset() {
  queryForm.username = ''
  queryForm.name = ''
  queryForm.phone = ''
  queryForm.status = ''
  pager.page = 1
  fetchList()
}

function handleCurrentChange(page) {
  pager.page = page
  fetchList()
}

function handleSizeChange(pageSize) {
  pager.page = 1
  pager.pageSize = pageSize
  fetchList()
}

function statusLabel(status) {
  return ADMIN_STATUS_LABEL_MAP[status] || '未知状态'
}

function statusClass(status) {
  if (status === ADMIN_STATUS.NORMAL) {
    return 'status-normal'
  }

  return 'status-disabled'
}

function handleCreate() {
  dialogMode.value = 'create'
  resetDialogForm()
  dialogVisible.value = true
  nextTick(() => {
    dialogFormRef.value?.clearValidate()
  })
}

async function handleEdit(record) {
  actionLoadingId.value = record.id

  try {
    const detail = await fetchAdminById(String(record.id))
    dialogMode.value = 'edit'

    dialogForm.id = detail?.id ?? record.id
    dialogForm.username = detail?.username || ''
    dialogForm.name = detail?.name || ''
    dialogForm.phone = detail?.phone || ''
    dialogForm.status = detail?.status ?? ADMIN_STATUS.NORMAL
    dialogForm.password = ''

    dialogVisible.value = true
    nextTick(() => {
      dialogFormRef.value?.clearValidate()
    })
  } catch (error) {
    ElMessage.error(error.message || '管理员详情加载失败')
  } finally {
    actionLoadingId.value = null
  }
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

  const username = normalize(dialogForm.username)
  const name = normalize(dialogForm.name)
  const phone = normalize(dialogForm.phone)
  const password = normalize(dialogForm.password)

  submitLoading.value = true

  try {
    if (dialogMode.value === 'create') {
      const payload = {
        username,
        name,
        status: Number(dialogForm.status),
      }

      if (phone) {
        payload.phone = phone
      }

      if (password) {
        payload.password = password
      }

      await createAdmin(payload)
      ElMessage.success('新增管理员成功')
      dialogVisible.value = false
      pager.page = 1
      await fetchList()
      return
    }

    const payload = {
      id: dialogForm.id,
      username,
      name,
      status: Number(dialogForm.status),
    }

    if (phone) {
      payload.phone = phone
    }

    if (password) {
      payload.password = password
    }

    await updateAdmin(payload)
    ElMessage.success('管理员信息更新成功')
    dialogVisible.value = false
    await fetchList()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(record) {
  try {
    await ElMessageBox.confirm(
      `确认删除管理员「${record.name || record.username || record.id}」吗？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
  } catch {
    return
  }

  actionLoadingId.value = record.id

  try {
    await deleteAdmin(String(record.id))
    ElMessage.success('删除成功')

    if (records.value.length === 1 && pager.page > 1) {
      pager.page -= 1
    }

    await fetchList()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  } finally {
    actionLoadingId.value = null
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.table-panel {
  padding: 14px;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.filter-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: end;
}

.filter-grid label {
  display: grid;
  gap: 6px;
  flex: 1 1 200px;
  min-width: 180px;
}

.filter-grid span {
  color: #4a648c;
  font-size: 13px;
  font-weight: 600;
}

.action-group {
  display: flex;
  gap: 8px;
  margin-left: auto;
  justify-content: flex-end;
}

.table-wrap {
  margin-top: 12px;
  border: 1px solid var(--border);
  border-radius: 14px;
  overflow: auto;
  background: #ffffff;
  flex: 1 1 auto;
  height: 0;
  min-height: 0;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 960px;
}

.data-table th,
.data-table td {
  padding: 11px 10px;
  border-bottom: 1px solid var(--border);
  text-align: left;
  vertical-align: middle;
}

.data-table th {
  color: #35557f;
  background: #f3f8ff;
  font-weight: 700;
  position: sticky;
  top: 0;
}

.data-table tbody tr:hover {
  background: #f9fcff;
}

.empty-row {
  padding: 0 !important;
  text-align: center;
}

.actions {
  display: flex;
  gap: 6px;
  justify-content: flex-end;
  white-space: nowrap;
}

.data-table th:last-child,
.data-table td:last-child {
  text-align: right;
}

.app-btn.mini {
  padding: 6px 9px;
  font-size: 12px;
}

.status-normal {
  color: #13795b;
  background: #ddf8ee;
}

.status-disabled {
  color: #b0444d;
  background: #ffe8eb;
}

.pagination-wrap {
  margin-top: 0;
  padding-top: 12px;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0) 0%, #f9fcff 100%);
  flex: 0 0 auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 860px) {
  .action-group {
    margin-left: 0;
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}

@media (max-width: 620px) {
  .table-panel {
    padding: 10px;
  }
}
</style>
