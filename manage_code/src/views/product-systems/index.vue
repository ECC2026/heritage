<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Product Systems</div>
      <h1 class="page-title">产品体系管理</h1>
      <p class="page-subtitle">独立于传统分类的产品体系维度，承载服务与商品的生态归属，如文创雅物、美食风物、器具器物、手作体验、康养陪伴、民俗演艺。</p>
    </section>

    <section class="inline-metrics">
      <article class="compact-stat">
        <div class="compact-stat-label">体系总数</div>
        <div class="compact-stat-value">{{ pagination.total }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">启用中</div>
        <div class="compact-stat-value">{{ statusCountMap[1] || 0 }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">已停用</div>
        <div class="compact-stat-value">{{ statusCountMap[0] || 0 }}</div>
      </article>
    </section>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div class="panel-header-main">
            <h3 class="panel-title">产品体系列表</h3>
            <p class="panel-note">新增、编辑与启停用，code 全局唯一。</p>
          </div>
          <el-button type="primary" @click="openCreate">新增体系</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="体系名称">
            <el-input v-model="searchForm.name" placeholder="搜索体系名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px;">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="toolbar-actions">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="systemList" stripe>
        <el-table-column prop="name" label="体系名称" min-width="160" />
        <el-table-column prop="code" label="编码" width="180" />
        <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-toolbar-foot">
        <div class="table-meta">体系数据按排序值展示</div>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑体系' : '新增体系'" width="560px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="体系名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入体系名称" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" placeholder="如 cultural_creative，创建后不可修改" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入体系描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存体系</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addProductSystem, getProductSystemList, updateProductSystem, updateProductSystemStatus } from '../../api/modules/productSystem'
import { toCountMap } from '../../utils/console'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const systemList = ref([])

const searchForm = reactive({
  name: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入体系名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入体系编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '仅支持字母、数字、下划线，且以字母开头', trigger: 'blur' }
  ]
}

const statusCountMap = computed(() => toCountMap(systemList.value, 'status'))

const loadData = async () => {
  loading.value = true
  try {
    const result = await getProductSystemList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name,
      status: searchForm.status === '' ? undefined : searchForm.status
    })
    systemList.value = result.list || []
    pagination.total = result.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.status = ''
  handleSearch()
}

const openCreate = () => {
  isEdit.value = false
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description,
    sort: row.sort ?? 0,
    status: row.status
  })
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    name: '',
    code: '',
    description: '',
    sort: 0,
    status: 1
  })
}

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    const payload = { ...form }
    if (isEdit.value) {
      await updateProductSystem(form.id, payload)
      ElMessage.success('体系已更新')
    } else {
      delete payload.id
      await addProductSystem(payload)
      ElMessage.success('体系已创建')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async (row) => {
  const targetStatus = row.status === 1 ? 0 : 1
  await updateProductSystemStatus(row.id, { status: targetStatus })
  ElMessage.success(targetStatus === 1 ? '体系已启用' : '体系已停用')
  loadData()
}

onMounted(loadData)
</script>
