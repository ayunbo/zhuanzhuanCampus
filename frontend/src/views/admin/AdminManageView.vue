<template>
  <div class="admin-manage-container fade-in-up">
    <!-- Search -->
    <div class="admin-card search-area">
      <el-form :inline="true" :model="queryForm" class="compact-form">
        <el-form-item label="账号">
          <el-input v-model="queryForm.username" placeholder="管理员账号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryForm.name" placeholder="管理员姓名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="状态" clearable style="width: 120px">
            <el-option v-for="item in ADMIN_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Data Table -->
    <div class="admin-card table-area">
      <div class="table-header">
        <div class="title-wrap">
          <span class="dot"></span>
          <h3>管理员团队管理</h3>
        </div>
        <el-button type="primary" :icon="Plus" @click="handleCreate">新增管理员</el-button>
      </div>

      <el-table v-loading="loading" :data="records" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="账号" width="150" />
        <el-table-column prop="name" label="姓名" width="150" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column label="当前状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ ADMIN_STATUS_LABEL_MAP[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="加入时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-footer">
        <el-pagination
          v-model:current-page="pager.page"
          v-model:page-size="pager.pageSize"
          :total="pager.total"
          :page-sizes="[10, 20, 30]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </div>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="dialogForm.username" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="dialogForm.name" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="dialogForm.phone" />
        </el-form-item>
        <el-form-item label="设置密码" prop="password">
          <el-input v-model="dialogForm.password" type="password" show-password :placeholder="dialogMode === 'create' ? '默认 123456' : '留空则不修改'" />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-radio-group v-model="dialogForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitDialog">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createAdmin, deleteAdmin, fetchAdminById, fetchAdminPage, updateAdmin } from '@/api/admin'
import { ADMIN_STATUS_LABEL_MAP, ADMIN_STATUS_OPTIONS } from '@/constants/admin'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const records = ref([])
const queryForm = reactive({ username: '', name: '', status: '' })
const pager = reactive({ page: 1, pageSize: 10, total: 0 })

const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitLoading = ref(false)
const dialogFormRef = ref()
const dialogForm = reactive({ id: null, username: '', name: '', phone: '', status: 1, password: '' })

const dialogRules = {
  username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
}

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增管理员' : '编辑管理员资料'))

async function fetchList() {
  loading.value = true
  try {
    const res = await fetchAdminPage({ ...queryForm, page: pager.page, pageSize: pager.pageSize })
    records.value = res.records || []
    pager.total = res.total || 0
  } catch (err) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { pager.page = 1; fetchList() }
function handleReset() { Object.assign(queryForm, { username: '', name: '', status: '' }); handleSearch() }

function handleCreate() {
  dialogMode.value = 'create'
  Object.assign(dialogForm, { id: null, username: '', name: '', phone: '', status: 1, password: '' })
  dialogVisible.value = true
}

async function handleEdit(record) {
  try {
    const detail = await fetchAdminById(record.id)
    dialogMode.value = 'edit'
    Object.assign(dialogForm, detail, { password: '' })
    dialogVisible.value = true
  } catch { ElMessage.error('无法获取详情') }
}

async function submitDialog() {
  await dialogFormRef.value.validate()
  submitLoading.value = true
  try {
    if (dialogMode.value === 'create') {
      await createAdmin(dialogForm)
      ElMessage.success('已新增')
    } else {
      await updateAdmin(dialogForm)
      ElMessage.success('已保存')
    }
    dialogVisible.value = false
    fetchList()
  } catch (err) { ElMessage.error(err.message) } finally { submitLoading.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认移除管理员 ${row.name}？`, '警示', { type: 'error', roundButton: true })
    await deleteAdmin(row.id)
    ElMessage.success('已移除')
    fetchList()
  } catch {}
}

onMounted(() => fetchList())
</script>

<style scoped>
.admin-manage-container {
  display: grid;
  gap: 16px;
}

.admin-card {
  padding: 16px;
}

.search-area {
  overflow-x: auto;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
  flex-wrap: wrap;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-wrap .dot {
  width: 4px;
  height: 18px;
  background: var(--admin-primary);
  border-radius: 2px;
}

.title-wrap h3 {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  color: #5d3b1f;
}

.pagination-footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.compact-form :deep(.el-form-item) {
  margin-bottom: 8px;
  margin-right: 10px;
}

:deep(.el-tag.el-tag--success) {
  border-color: #add9b8;
  background: #eaf8ef;
}

:deep(.el-tag.el-tag--danger) {
  border-color: #edc4bf;
  background: #fef1ef;
}

:deep(.el-table__row:hover) {
  background-color: #fff9ef !important;
}

@media (max-width: 768px) {
  .admin-card {
    padding: 12px;
  }
}
</style>
