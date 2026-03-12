<template>
  <div class="user-management-simple fade-in-up">
    <!-- Header Section -->
    <div class="page-header">
      <div class="header-info">
        <h1>用户档案</h1>
        <p>系统所有注册用户的管理与资料维护</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openAddDialog">新增用户</el-button>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <el-input
        v-model="query.studentNo"
        placeholder="学号搜索..."
        :prefix-icon="Search"
        style="width: 200px"
        @input="handleSearch"
      />
      <el-input
        v-model="query.name"
        placeholder="姓名搜索..."
        :prefix-icon="Search"
        style="width: 200px"
        @input="handleSearch"
      />
      <el-select v-model="query.role" placeholder="角色" clearable style="width: 140px" @change="handleSearch">
        <el-option label="普通用户" :value="1" />
        <el-option label="校园卖家" :value="2" />
      </el-select>
      <el-button link :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <!-- Main Table Section -->
    <div class="table-container">
      <el-table 
        v-loading="loading" 
        :data="tableData" 
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" color="#8c8c8c" />
        <el-table-column label="学号" width="140">
          <template #default="{ row }">
            <span class="student-no">{{ row.studentNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <span class="simple-badge" :class="row.role === 2 ? 'seller' : 'user'">
              {{ row.role === 2 ? '卖家' : '用户' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="2"
              size="small"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-divider direction="vertical" />
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          small
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑资料' : '新增用户'" width="480px">
      <el-form :model="form" label-position="top">
        <el-form-item label="学号" required>
          <el-input v-model="form.studentNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="初始密码" required v-if="!isEdit">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="所属角色">
          <el-radio-group v-model="form.role" size="small">
            <el-radio-button :label="1">普通用户</el-radio-button>
            <el-radio-button :label="2">校园卖家</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { fetchUserPage, createUser, updateUser, deleteUser, fetchUserById } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)

const query = reactive({ page: 1, pageSize: 10, studentNo: '', name: '', role: null })
const form = reactive({ id: null, studentNo: '', password: '', name: '', phone: '', role: 1, status: 1 })

async function loadData() {
  loading.value = true
  try {
    const res = await fetchUserPage(query)
    tableData.value = Array.isArray(res?.records) ? res.records : []
    total.value = Number(res?.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() { query.page = 1; loadData() }
function resetQuery() { Object.assign(query, { studentNo: '', name: '', role: null }); handleSearch() }

function openAddDialog() {
  isEdit.value = false
  Object.assign(form, { id: null, studentNo: '', password: '', name: '', phone: '', role: 1, status: 1 })
  dialogVisible.value = true
}

async function openEditDialog(user) {
  try {
    isEdit.value = true
    const detail = await fetchUserById(String(user.id))
    Object.assign(form, detail)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '无法获取用户详情')
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEdit.value) await updateUser(form)
    else await createUser(form)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}

async function handleStatusChange(row, val) {
  const previousStatus = val === 1 ? 2 : 1
  try {
    await updateUser({ ...row, status: val })
    ElMessage.success('状态已更新')
  } catch (error) {
    row.status = previousStatus
    ElMessage.error(error.message || '状态更新失败')
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该用户吗？', '提示', { type: 'warning' })
  await deleteUser(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.user-management-simple {
  display: grid;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: linear-gradient(140deg, #fff7e9 0%, #fff1dd 100%);
}

.header-info h1 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 4px 0;
  color: #5b3b1f;
}

.header-info p {
  margin: 0;
  font-size: 14px;
  color: #83684e;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: linear-gradient(180deg, #fffdf9 0%, #fff7eb 100%);
  box-shadow: var(--shadow-soft);
}

.table-container {
  overflow: hidden;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: linear-gradient(180deg, #fffdf9 0%, #fff7eb 100%);
  box-shadow: var(--shadow-soft);
  padding: 8px 10px 16px;
}

.student-no {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 600;
  color: #5b3b1f;
}

.simple-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.simple-badge.seller { color: #ff9100; background: #fff7e6; }
.simple-badge.user { color: #7f6146; background: #f8ecdc; }

.pagination-section {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

:deep(.el-table__row) {
  transition: background-color 0.2s;
}

:deep(.el-table__row:hover) {
  background-color: #fff8ee !important;
}

:deep(.el-switch.is-checked .el-switch__core) {
  border-color: #d8822a;
  background-color: #d8822a;
}

@media (max-width: 768px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .table-container {
    padding: 6px 6px 12px;
  }
}
</style>
