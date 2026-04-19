<template>
  <el-card shadow="never" style="height: 100%">
    <el-container style="height: 100%">
      <el-header style="height: auto; padding-bottom: 18px">
        <el-form :inline="true" :model="query" @submit.prevent="handleSearch">
          <el-form-item label="学号">
            <el-input v-model="query.studentNo" clearable placeholder="学号模糊查询" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="query.name" clearable placeholder="用户姓名" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="query.role" clearable placeholder="全部角色">
              <el-option :value="1" label="普通用户" />
              <el-option :value="2" label="校园卖家" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
            <el-button type="primary" @click="openAddDialog">新增用户</el-button>
            <el-button :loading="loading" @click="loadData">刷新</el-button>
          </el-form-item>
        </el-form>
      </el-header>

      <el-main style="padding-top: 0; padding-bottom: 0; min-height: 0">
        <el-table v-loading="loading" :data="tableData" border height="100%">
          <el-table-column prop="id" label="ID" min-width="90" />
          <el-table-column prop="studentNo" label="学号" min-width="140" />
          <el-table-column prop="name" label="姓名" min-width="120" />
          <el-table-column prop="phone" label="手机号" min-width="150" />
          <el-table-column label="角色" min-width="120">
            <template #default="{ row }">
              <el-tag :type="Number(row.role) === 2 ? 'warning' : 'info'">
                {{ Number(row.role) === 2 ? '校园卖家' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="Number(row.status) === 1 ? 'success' : 'danger'">
                {{ Number(row.status) === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" min-width="220">
            <template #default="{ row }">
              <el-space wrap>
                <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
                <el-button size="small" @click="handleStatusToggle(row)">
                  {{ Number(row.status) === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
              </el-space>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty />
          </template>
        </el-table>
      </el-main>

      <el-footer style="height: auto; padding-top: 16px; padding-bottom: 0">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 30]"
          :total="total"
          background
          layout="total, sizes, prev, pager, next"
          @current-change="loadData"
          @size-change="loadData"
        />
      </el-footer>
    </el-container>
  </el-card>

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
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
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
