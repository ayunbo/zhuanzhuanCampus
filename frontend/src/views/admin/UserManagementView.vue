<template>
  <div class="user-page">
    <section class="page-head app-card">
      <div>
        <p class="head-tag">User Console</p>
        <h2>用户管理</h2>
      </div>

      <div class="head-actions">
        <button class="app-btn primary" @click="openAddDialog">新增用户</button>
        <button class="app-btn secondary" :disabled="loading" @click="loadData">
          {{ loading ? '加载中...' : '刷新列表' }}
        </button>
      </div>
    </section>

    <section class="metric-grid">
      <article class="metric-card app-card">
        <span>当前页用户</span>
        <strong>{{ tableData.length }}</strong>
      </article>
      <article class="metric-card app-card">
        <span>普通用户</span>
        <strong>{{ summary.normalUser }}</strong>
      </article>
      <article class="metric-card app-card">
        <span>校园卖家</span>
        <strong>{{ summary.seller }}</strong>
      </article>
      <article class="metric-card app-card">
        <span>禁用账号</span>
        <strong>{{ summary.disabled }}</strong>
      </article>
    </section>

    <section class="table-panel app-card">
      <form class="filter-grid" @submit.prevent="handleSearch">
        <label>
          <span>学号</span>
          <input v-model="query.studentNo" class="app-input" type="text" placeholder="学号模糊查询" />
        </label>

        <label>
          <span>姓名</span>
          <input v-model="query.name" class="app-input" type="text" placeholder="用户姓名" />
        </label>

        <label>
          <span>角色</span>
          <select v-model="query.role" class="app-select">
            <option value="">全部</option>
            <option :value="1">普通用户</option>
            <option :value="2">校园卖家</option>
          </select>
        </label>

        <div class="action-group">
          <button class="app-btn primary" type="submit">查询</button>
          <button class="app-btn ghost" type="button" @click="resetQuery">重置</button>
        </div>
      </form>

      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>学号</th>
              <th>姓名</th>
              <th>手机号</th>
              <th>角色</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && tableData.length === 0">
              <td class="empty-row" colspan="7">暂无数据</td>
            </tr>
            <tr v-for="row in tableData" :key="String(row.id)">
              <td>{{ row.id }}</td>
              <td>{{ row.studentNo || '-' }}</td>
              <td>{{ row.name || '-' }}</td>
              <td>{{ row.phone || '-' }}</td>
              <td>
                <span class="role-badge" :class="Number(row.role) === 2 ? 'seller' : 'user'">
                  {{ Number(row.role) === 2 ? '校园卖家' : '普通用户' }}
                </span>
              </td>
              <td>
                <span class="status-badge" :class="Number(row.status) === 1 ? 'status-approved' : 'status-rejected'">
                  {{ Number(row.status) === 1 ? '正常' : '禁用' }}
                </span>
              </td>
              <td class="actions">
                <button class="app-btn primary mini" @click="openEditDialog(row)">编辑</button>
                <button class="app-btn secondary mini" @click="handleStatusToggle(row)">
                  {{ Number(row.status) === 1 ? '禁用' : '启用' }}
                </button>
                <button class="app-btn danger mini" @click="handleDelete(row.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </footer>
    </section>
  </div>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="540px" destroy-on-close>
    <el-form :model="form" label-width="84px">
      <el-form-item label="学号">
        <el-input v-model="form.studentNo" :disabled="isEdit" maxlength="32" placeholder="请输入学号" />
      </el-form-item>

      <el-form-item v-if="!isEdit" label="密码">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          maxlength="64"
          placeholder="不填默认 123456"
        />
      </el-form-item>

      <el-form-item label="姓名">
        <el-input v-model="form.name" maxlength="64" placeholder="请输入姓名" />
      </el-form-item>

      <el-form-item label="手机号">
        <el-input v-model="form.phone" maxlength="20" placeholder="请输入手机号" />
      </el-form-item>

      <el-form-item label="角色">
        <el-radio-group v-model="form.role">
          <el-radio-button :label="1">普通用户</el-radio-button>
          <el-radio-button :label="2">校园卖家</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUser, deleteUser, fetchUserById, fetchUserPage, updateUser } from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)

const query = reactive({
  page: 1,
  pageSize: 10,
  studentNo: '',
  name: '',
  role: '',
})

const form = reactive({
  id: null,
  studentNo: '',
  password: '',
  name: '',
  phone: '',
  role: 1,
  status: 1,
})

const summary = computed(() => {
  const normalUser = tableData.value.filter((item) => Number(item.role) === 1).length
  const seller = tableData.value.filter((item) => Number(item.role) === 2).length
  const disabled = tableData.value.filter((item) => Number(item.status) !== 1).length

  return {
    normalUser,
    seller,
    disabled,
  }
})

function normalize(value) {
  return typeof value === 'string' ? value.trim() : ''
}

function buildQueryParams() {
  const params = {
    page: query.page,
    pageSize: query.pageSize,
  }

  const studentNo = normalize(query.studentNo)
  const name = normalize(query.name)

  if (studentNo) {
    params.studentNo = studentNo
  }

  if (name) {
    params.name = name
  }

  if (query.role !== null && query.role !== '') {
    params.role = Number(query.role)
  }

  return params
}

async function loadData() {
  loading.value = true

  try {
    const result = await fetchUserPage(buildQueryParams())
    tableData.value = Array.isArray(result?.records) ? result.records : []
    total.value = Number(result?.total || 0)
  } catch (error) {
    ElMessage.error(error.message || '用户列表加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadData()
}

function resetQuery() {
  query.studentNo = ''
  query.name = ''
  query.role = ''
  query.page = 1
  loadData()
}

function resetForm() {
  form.id = null
  form.studentNo = ''
  form.password = ''
  form.name = ''
  form.phone = ''
  form.role = 1
  form.status = 1
}

function openAddDialog() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(user) {
  isEdit.value = true

  try {
    const detail = await fetchUserById(String(user.id))
    form.id = detail?.id ?? user.id
    form.studentNo = detail?.studentNo || ''
    form.password = ''
    form.name = detail?.name || ''
    form.phone = detail?.phone || ''
    form.role = Number(detail?.role ?? 1)
    form.status = Number(detail?.status ?? 1)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '无法获取用户详情')
  }
}

async function handleSubmit() {
  const studentNo = normalize(form.studentNo)
  const password = normalize(form.password)
  const name = normalize(form.name)
  const phone = normalize(form.phone)

  if (!studentNo) {
    ElMessage.warning('学号不能为空')
    return
  }

  submitting.value = true

  try {
    if (isEdit.value) {
      const payload = {
        id: form.id,
        studentNo,
        name,
        role: Number(form.role),
        status: Number(form.status),
      }

      if (phone) {
        payload.phone = phone
      }

      if (password) {
        payload.password = password
      }

      await updateUser(payload)
    } else {
      const payload = {
        studentNo,
        role: Number(form.role),
      }

      if (name) {
        payload.name = name
      }

      if (phone) {
        payload.phone = phone
      }

      if (password) {
        payload.password = password
      }

      await createUser(payload)
    }

    ElMessage.success('操作成功')
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

async function handleStatusToggle(row) {
  const currentStatus = Number(row.status)
  const newStatus = currentStatus === 1 ? 2 : 1

  try {
    await updateUser({ ...row, status: newStatus })
    row.status = newStatus
    ElMessage.success('账号状态已更新')
  } catch (error) {
    row.status = currentStatus
    ElMessage.error(error.message || '状态更新失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('删除后不可恢复，确认删除该用户吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  try {
    await deleteUser(String(id))
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-page {
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

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.metric-card {
  padding: 14px;
  display: grid;
  gap: 4px;
}

.metric-card span {
  color: var(--text-secondary);
  font-size: 12px;
}

.metric-card strong {
  font-family: 'Lexend', sans-serif;
  color: #1f467a;
  font-size: 26px;
  line-height: 1;
}

.table-panel {
  padding: 14px;
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
  flex: 1 1 210px;
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
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 920px;
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
  text-align: center;
  color: var(--text-secondary);
}

.role-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.role-badge.seller {
  color: #0f7f6f;
  background: #dcf8f4;
}

.role-badge.user {
  color: #33609d;
  background: #eaf3ff;
}

.actions {
  display: flex;
  gap: 6px;
}

.app-btn.mini {
  padding: 6px 9px;
  font-size: 12px;
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

@media (max-width: 1180px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .page-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }

  .action-group {
    margin-left: 0;
    width: 100%;
    justify-content: flex-start;
  }

  .action-group {
    flex-wrap: wrap;
  }
}
</style>
