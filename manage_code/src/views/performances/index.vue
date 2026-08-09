<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Performances</div>
      <h1 class="page-title">非遗演出管理</h1>
      <p class="page-subtitle">维护演出排期、场地、票价与主办信息，让后台具备完整的演出运营展示能力。</p>
    </section>

    <section class="inline-metrics">
      <article class="compact-stat">
        <div class="compact-stat-label">总演出数</div>
        <div class="compact-stat-value">{{ pagination.total }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">进行中</div>
        <div class="compact-stat-value">{{ statusCountMap[1] || 0 }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">待开场 / 已结束</div>
        <div class="compact-stat-value">{{ statusCountMap[0] || 0 }} / {{ statusCountMap[2] || 0 }}</div>
      </article>
    </section>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div class="panel-header-main">
            <h3 class="panel-title">演出排期列表</h3>
            <p class="panel-note">支持新增、编辑、删除和状态切换，可直接用于答辩中的业务流程演示。</p>
          </div>
          <el-button type="primary" @click="openCreate">新增演出</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="演出名称">
            <el-input v-model="searchForm.name" placeholder="搜索演出名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px;">
              <el-option label="未开始" :value="0" />
              <el-option label="进行中" :value="1" />
              <el-option label="已结束" :value="2" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="toolbar-actions">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleSearch">筛选演出</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="performanceList" stripe>
        <el-table-column label="演出信息" min-width="280">
          <template #default="{ row }">
            <div class="avatar-cell">
              <div class="uploader-box" style="width: 76px; height: 56px; border-radius: 14px;">
                <img v-if="row.cover" :src="resolveAssetUrl(row.cover)" alt="cover" class="preview-image" />
                <div v-else class="empty-copy">无封面</div>
              </div>
              <div class="avatar-cell-text">
                <div class="avatar-cell-title">{{ row.name }}</div>
                <div class="avatar-cell-desc">{{ row.organizer || '未填写主办方' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="演出地点" min-width="160" />
        <el-table-column prop="performer" label="演出团队" min-width="120" />
        <el-table-column prop="startTime" label="开始时间" min-width="176" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="票价" width="100">
          <template #default="{ row }">¥{{ row.price ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 2 ? 'success' : 'warning'" @click="toggleStatus(row)">
              {{ row.status === 2 ? '改为进行中' : '标记结束' }}
            </el-button>
            <el-button link type="danger" @click="removePerformance(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-toolbar-foot">
        <div class="table-meta">默认按演出开始时间倒序展示</div>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑演出' : '新增演出'" width="920px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="dialog-grid">
          <el-form-item label="演出名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入演出名称" />
          </el-form-item>
          <el-form-item label="演出地点" prop="location">
            <el-input v-model="form.location" placeholder="请输入演出地点" />
          </el-form-item>

          <el-form-item label="主办单位">
            <el-input v-model="form.organizer" placeholder="请输入主办单位" />
          </el-form-item>
          <el-form-item label="演出团队">
            <el-input v-model="form.performer" placeholder="请输入演出团队" />
          </el-form-item>

          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
          </el-form-item>

          <el-form-item label="票价">
            <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="座位数">
            <el-input-number v-model="form.seats" :min="0" style="width: 100%;" />
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

          <el-form-item label="演出简介" class="span-2">
            <el-input v-model="form.description" type="textarea" :rows="6" placeholder="请输入演出简介" />
          </el-form-item>

          <el-form-item label="演出状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="0">未开始</el-radio>
              <el-radio :label="1">进行中</el-radio>
              <el-radio :label="2">已结束</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存演出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addPerformance, deletePerformance, getPerformanceDetail, getPerformanceList, updatePerformance, updatePerformanceStatus } from '../../api/modules/performance'
import { uploadImage } from '../../api/modules/upload'
import { resolveAssetUrl, toCountMap } from '../../utils/console'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const performanceList = ref([])

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
  cover: '',
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  organizer: '',
  performer: '',
  price: 0,
  seats: 0,
  status: 0
})

const rules = {
  name: [{ required: true, message: '请输入演出名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入演出地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
}

const statusCountMap = computed(() => toCountMap(performanceList.value, 'status'))

const loadData = async () => {
  loading.value = true
  try {
    const result = await getPerformanceList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name,
      status: searchForm.status === '' ? undefined : searchForm.status
    })
    performanceList.value = result.list || []
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

const openEdit = async (row) => {
  isEdit.value = true
  Object.assign(form, await getPerformanceDetail(row.id))
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    name: '',
    cover: '',
    description: '',
    location: '',
    startTime: '',
    endTime: '',
    organizer: '',
    performer: '',
    price: 0,
    seats: 0,
    status: 0
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
      await updatePerformance(form.id, payload)
      ElMessage.success('演出已更新')
    } else {
      delete payload.id
      await addPerformance(payload)
      ElMessage.success('演出已创建')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async (row) => {
  const targetStatus = row.status === 2 ? 1 : 2
  await updatePerformanceStatus(row.id, { status: targetStatus })
  ElMessage.success(targetStatus === 2 ? '演出已标记为结束' : '演出已改为进行中')
  loadData()
}

const removePerformance = async (row) => {
  await ElMessageBox.confirm(`确定删除演出“${row.name}”吗？`, '删除确认', { type: 'warning' })
  await deletePerformance(row.id)
  ElMessage.success('演出已删除')
  loadData()
}

const getStatusType = (status) => ({ 0: 'info', 1: 'success', 2: 'warning' })[status] || 'info'
const getStatusText = (status) => ({ 0: '未开始', 1: '进行中', 2: '已结束' })[status] || '未知'

onMounted(loadData)
</script>
