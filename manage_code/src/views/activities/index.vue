<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Activities</div>
      <h1 class="page-title">活动运营管理</h1>
      <p class="page-subtitle">统一管理活动创建、审核和人数限制，是前台报名与后台审核闭环的核心页面。</p>
    </section>

    <section class="inline-metrics">
      <article class="compact-stat">
        <div class="compact-stat-label">活动总数</div>
        <div class="compact-stat-value">{{ pagination.total }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">待审核</div>
        <div class="compact-stat-value">{{ statusCountMap[0] || 0 }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">进行中 / 已结束</div>
        <div class="compact-stat-value">{{ statusCountMap[1] || 0 }} / {{ statusCountMap[2] || 0 }}</div>
      </article>
    </section>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div class="panel-header-main">
            <h3 class="panel-title">活动列表</h3>
            <p class="panel-note">字段已经对齐后端 `organizerName`、`limitCount`、`signupCount` 和审核接口。</p>
          </div>
          <el-button type="primary" @click="openCreate">新增活动</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="活动名称">
            <el-input v-model="searchForm.name" placeholder="搜索活动名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px;">
              <el-option label="待审核" :value="0" />
              <el-option label="进行中" :value="1" />
              <el-option label="已结束" :value="2" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="toolbar-actions">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询活动</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="activityList" stripe>
        <el-table-column label="活动信息" min-width="300">
          <template #default="{ row }">
            <div class="avatar-cell">
              <div class="uploader-box" style="width: 76px; height: 56px; border-radius: 14px;">
                <img v-if="row.cover" :src="resolveAssetUrl(row.cover)" alt="cover" class="preview-image" />
                <div v-else class="empty-copy">无封面</div>
              </div>
              <div class="avatar-cell-text">
                <div class="avatar-cell-title">{{ row.name }}</div>
                <div class="avatar-cell-desc">{{ row.location || '未填写地点' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="organizerName" label="组织者" min-width="130" />
        <el-table-column prop="startTime" label="开始时间" min-width="176" />
        <el-table-column label="报名 / 限额" width="120">
          <template #default="{ row }">{{ row.signupCount || 0 }} / {{ row.limitCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" link type="success" @click="approveActivity(row)">通过审核</el-button>
            <el-button v-else-if="row.status === 1" link type="warning" @click="finishActivity(row)">结束</el-button>
            <el-button link type="danger" @click="removeActivity(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-toolbar-foot">
        <div class="table-meta">适合作为“活动创建 -> 报名 -> 审核 -> 导出”的后台入口</div>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动' : '新增活动'" width="920px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="dialog-grid">
          <el-form-item label="活动名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入活动名称" />
          </el-form-item>
          <el-form-item label="组织者" prop="organizer">
            <el-input v-model="form.organizer" placeholder="请输入组织者名称" />
          </el-form-item>

          <el-form-item label="活动地点">
            <el-input v-model="form.location" placeholder="请输入活动地点" />
          </el-form-item>
          <el-form-item label="人数上限">
            <el-input-number v-model="form.limitCount" :min="1" style="width: 100%;" />
          </el-form-item>

          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
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

          <el-form-item label="活动描述" class="span-2">
            <el-input v-model="form.description" type="textarea" :rows="6" placeholder="请输入活动描述" />
          </el-form-item>

          <el-form-item label="活动状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="0">待审核</el-radio>
              <el-radio :label="1">进行中</el-radio>
              <el-radio :label="2">已结束</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存活动</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addActivity, auditActivity, deleteActivity, getActivityDetail, getActivityList, updateActivity, updateActivityStatus } from '../../api/modules/activity'
import { uploadImage } from '../../api/modules/upload'
import { resolveAssetUrl, toCountMap } from '../../utils/console'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const activityList = ref([])

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
  organizer: '',
  location: '',
  startTime: '',
  endTime: '',
  limitCount: 30,
  cover: '',
  description: '',
  status: 0
})

const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  organizer: [{ required: true, message: '请输入组织者', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
}

const statusCountMap = computed(() => toCountMap(activityList.value, 'status'))

const loadData = async () => {
  loading.value = true
  try {
    const result = await getActivityList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name,
      status: searchForm.status === '' ? undefined : searchForm.status
    })
    activityList.value = result.list || []
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
  const detail = await getActivityDetail(row.id)
  Object.assign(form, {
    ...detail,
    organizer: detail.organizerName || detail.organizer || ''
  })
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    name: '',
    organizer: '',
    location: '',
    startTime: '',
    endTime: '',
    limitCount: 30,
    cover: '',
    description: '',
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
    const payload = {
      ...form,
      organizerName: form.organizer
    }
    if (isEdit.value) {
      await updateActivity(form.id, payload)
      ElMessage.success('活动已更新')
    } else {
      delete payload.id
      await addActivity(payload)
      ElMessage.success('活动已创建')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const approveActivity = async (row) => {
  await auditActivity(row.id, { status: 1 })
  ElMessage.success('活动已审核通过')
  loadData()
}

const finishActivity = async (row) => {
  await updateActivityStatus(row.id, { status: 2 })
  ElMessage.success('活动已标记为结束')
  loadData()
}

const removeActivity = async (row) => {
  await ElMessageBox.confirm(`确定删除活动“${row.name}”吗？`, '删除确认', { type: 'warning' })
  await deleteActivity(row.id)
  ElMessage.success('活动已删除')
  loadData()
}

const getStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'info' })[status] || 'info'
const getStatusText = (status) => ({ 0: '待审核', 1: '进行中', 2: '已结束' })[status] || '未知'

onMounted(loadData)
</script>
