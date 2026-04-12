<template>
  <el-card shadow="never" style="height: 100%">
    <el-container style="height: 100%">
      <el-header style="height: auto; padding-bottom: 18px">
        <el-form :inline="true" :model="queryForm" @submit.prevent="handleSearch">
          <el-form-item label="账号">
            <el-input v-model="queryForm.username" clearable placeholder="管理员账号" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="queryForm.name" clearable placeholder="管理员姓名" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="queryForm.phone" clearable placeholder="手机号" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" clearable placeholder="全部状态">
              <el-option v-for="item in ADMIN_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="handleCreate">新增管理员</el-button>
            <el-button :loading="loading" @click="fetchList">刷新</el-button>
          </el-form-item>
        </el-form>
      </el-header>

      <el-main style="padding-top: 0; padding-bottom: 0; min-height: 0">
        <el-table v-loading="loading" :data="records" border height="100%">
          <el-table-column prop="id" label="ID" min-width="90" />
          <el-table-column prop="username" label="账号" min-width="160" />
          <el-table-column prop="name" label="姓名" min-width="120" />
          <el-table-column prop="phone" label="手机号" min-width="150" />
          <el-table-column label="状态" min-width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === ADMIN_STATUS.NORMAL ? 'success' : 'info'">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="180">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="更新时间" min-width="180">
            <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" min-width="160">
            <template #default="{ row }">
              <el-space>
                <el-button size="small" type="primary" :loading="actionLoadingId === row.id" @click="handleEdit(row)">
                  编辑
                </el-button>
                <el-button size="small" type="danger" :loading="actionLoadingId === row.id" @click="handleDelete(row)">
                  删除
                </el-button>
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
          v-model:current-page="pager.page"
          v-model:page-size="pager.pageSize"
          :total="pager.total"
          :page-sizes="[10, 20, 30]"
          background
          layout="total, sizes, prev, pager, next"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </el-footer>
    </el-container>
  </el-card>

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
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submitDialog">
        {{ dialogMode === 'create' ? '确认新增' : '确认保存' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
