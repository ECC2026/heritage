<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Services</div>
      <h1 class="page-title">服务管理</h1>
      <p class="page-subtitle">维护可预约的服务资源（康养陪伴、民俗演艺等），挂载产品体系并管理预约场次与容量。</p>
    </section>

    <section class="inline-metrics">
      <article class="compact-stat">
        <div class="compact-stat-label">服务总数</div>
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
            <h3 class="panel-title">服务列表</h3>
            <p class="panel-note">支持新增、编辑、启停用，并为服务维护可预约场次。</p>
          </div>
          <el-button type="primary" @click="openCreate">新增服务</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="服务名称">
            <el-input v-model="searchForm.name" placeholder="搜索服务名称" clearable />
          </el-form-item>
          <el-form-item label="产品体系">
            <el-select v-model="searchForm.productSystemId" placeholder="全部体系" clearable style="width: 180px;">
              <el-option v-for="item in systems" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
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

      <el-table v-loading="loading" :data="serviceList" stripe>
        <el-table-column label="服务信息" min-width="280">
          <template #default="{ row }">
            <div class="avatar-cell">
              <div class="uploader-box" style="width: 76px; height: 56px; border-radius: 14px;">
                <img v-if="row.cover" :src="resolveAssetUrl(row.cover)" alt="cover" class="preview-image" />
                <div v-else class="empty-copy">无封面</div>
              </div>
              <div class="avatar-cell-text">
                <div class="avatar-cell-title">{{ row.name }}</div>
                <div class="avatar-cell-desc">{{ row.productSystem || '未归属体系' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="providerName" label="服务方" width="130" show-overflow-tooltip />
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            ¥{{ row.price ?? 0 }}<span v-if="row.unit">/{{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" @click="openSchedules(row)">场次</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-toolbar-foot">
        <div class="table-meta">服务数据按创建时间倒序展示</div>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑服务' : '新增服务'" width="760px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="dialog-grid">
          <el-form-item label="服务名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入服务名称" />
          </el-form-item>
          <el-form-item label="产品体系" prop="productSystemId">
            <el-select v-model="form.productSystemId" placeholder="请选择产品体系" style="width: 100%;">
              <el-option v-for="item in systems" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="服务方">
            <el-input v-model="form.providerName" placeholder="请输入服务方名称" />
          </el-form-item>
          <el-form-item label="地点">
            <el-input v-model="form.location" placeholder="请输入服务地点" />
          </el-form-item>

          <el-form-item label="价格" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="计价单位">
            <el-input v-model="form.unit" placeholder="如 次 / 小时" />
          </el-form-item>

          <el-form-item label="封面图" class="span-2">
            <div style="display: flex; gap: 16px; flex-wrap: wrap;">
              <el-upload :show-file-list="false" action="#" :before-upload="handleCoverUpload">
                <div class="uploader-box">
                  <img v-if="form.cover" :src="resolveAssetUrl(form.cover)" alt="cover" class="preview-image" />
                  <div v-else class="empty-copy">点击上传</div>
                </div>
              </el-upload>
              <el-input v-model="form.cover" placeholder="或直接粘贴封面链接" />
            </div>
          </el-form-item>

          <el-form-item label="服务简介" class="span-2">
            <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="一句话简介，用于列表展示" />
          </el-form-item>

          <el-form-item label="服务详情" class="span-2">
            <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请输入服务详情" />
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
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存服务</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="scheduleVisible" :title="`场次管理：${currentService?.name || ''}`" width="720px">
      <el-table v-loading="scheduleLoading" :data="schedules" stripe>
        <el-table-column label="开始时间" width="150">
          <template #default="{ row }">{{ row.startTime }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="150">
          <template #default="{ row }">{{ row.endTime }}</template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量" width="90" />
        <el-table-column prop="bookedCount" label="已约" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '开放' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-divider>新增场次</el-divider>
      <el-form ref="scheduleFormRef" :model="scheduleForm" :rules="scheduleRules" label-position="top">
        <div class="dialog-grid">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="scheduleForm.startTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择开始时间"
              style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="scheduleForm.endTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择结束时间"
              style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="容量" prop="capacity">
            <el-input-number v-model="scheduleForm.capacity" :min="1" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="scheduleForm.status">
              <el-radio :label="1">开放</el-radio>
              <el-radio :label="0">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-form>
      <div class="dialog-footer-right">
        <el-button @click="scheduleVisible = false">关闭</el-button>
        <el-button type="primary" :loading="scheduleSubmitting" @click="submitSchedule">新增场次</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addService, addServiceSchedule, getProductSystemList, getServiceList, getServiceScheduleList, updateService, updateServiceStatus } from '../../api'
import { uploadImage } from '../../api/modules/upload'
import { resolveAssetUrl, toCountMap } from '../../utils/console'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const serviceList = ref([])
const systems = ref([])

const searchForm = reactive({
  name: '',
  productSystemId: '',
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
  productSystemId: null,
  cover: '',
  summary: '',
  description: '',
  providerName: '',
  location: '',
  price: 0,
  unit: '',
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  productSystemId: [{ required: true, message: '请选择产品体系', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }]
}

const scheduleVisible = ref(false)
const scheduleLoading = ref(false)
const scheduleSubmitting = ref(false)
const scheduleFormRef = ref()
const currentService = ref(null)
const schedules = ref([])

const scheduleForm = reactive({
  startTime: '',
  endTime: '',
  capacity: 10,
  status: 1
})

const scheduleRules = {
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'change' }]
}

const statusCountMap = computed(() => toCountMap(serviceList.value, 'status'))

const loadSystems = async () => {
  try {
    const result = await getProductSystemList({ page: 1, size: 200 })
    systems.value = result.list || []
  } catch (error) {
    systems.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const result = await getServiceList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name,
      productSystemId: searchForm.productSystemId === '' ? undefined : searchForm.productSystemId,
      status: searchForm.status === '' ? undefined : searchForm.status
    })
    serviceList.value = result.list || []
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
  searchForm.productSystemId = ''
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
    productSystemId: row.productSystemId,
    cover: row.cover,
    summary: row.summary,
    description: row.description,
    providerName: row.providerName,
    location: row.location,
    price: row.price ?? 0,
    unit: row.unit,
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
    productSystemId: null,
    cover: '',
    summary: '',
    description: '',
    providerName: '',
    location: '',
    price: 0,
    unit: '',
    sort: 0,
    status: 1
  })
}

const handleCoverUpload = async (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (file.size / 1024 / 1024 >= 5) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  form.cover = await uploadImage(file)
  ElMessage.success('封面上传成功')
  return false
}

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    const payload = { ...form }
    if (isEdit.value) {
      await updateService(form.id, payload)
      ElMessage.success('服务已更新')
    } else {
      delete payload.id
      await addService(payload)
      ElMessage.success('服务已创建')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async (row) => {
  const targetStatus = row.status === 1 ? 0 : 1
  await updateServiceStatus(row.id, { status: targetStatus })
  ElMessage.success(targetStatus === 1 ? '服务已启用' : '服务已停用')
  loadData()
}

const openSchedules = async (row) => {
  currentService.value = row
  scheduleVisible.value = true
  loadSchedules(row.id)
}

const loadSchedules = async (serviceId) => {
  scheduleLoading.value = true
  try {
    schedules.value = await getServiceScheduleList(serviceId)
  } catch (error) {
    schedules.value = []
  } finally {
    scheduleLoading.value = false
  }
}

const submitSchedule = async () => {
  if (!scheduleFormRef.value) return
  try {
    await scheduleFormRef.value.validate()
    scheduleSubmitting.value = true
    await addServiceSchedule(currentService.value.id, { ...scheduleForm })
    ElMessage.success('场次已新增')
    scheduleForm.startTime = ''
    scheduleForm.endTime = ''
    scheduleForm.capacity = 10
    scheduleForm.status = 1
    loadSchedules(currentService.value.id)
  } finally {
    scheduleSubmitting.value = false
  }
}

onMounted(() => {
  loadSystems()
  loadData()
})
</script>
