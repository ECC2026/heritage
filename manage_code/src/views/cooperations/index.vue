<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Cooperation</div>
      <h1 class="page-title">合作申请管理</h1>
      <p class="page-subtitle">处理B端合作申请：文旅合作、企业定制、非遗活动落地、平台入驻，跟踪沟通进度并填写备注。</p>
    </section>

    <section class="inline-metrics">
      <article class="compact-stat">
        <div class="compact-stat-label">申请总数</div>
        <div class="compact-stat-value">{{ pagination.total }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">待处理</div>
        <div class="compact-stat-value">{{ statusCountMap[0] || 0 }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">已联系</div>
        <div class="compact-stat-value">{{ statusCountMap[1] || 0 }}</div>
      </article>
    </section>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div class="panel-header-main">
            <h3 class="panel-title">合作申请列表</h3>
            <p class="panel-note">申请由B端公开页提交，状态统一在后台维护。</p>
          </div>
        </div>
      </template>

      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="企业名称">
            <el-input v-model="searchForm.companyName" placeholder="搜索企业/机构名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px;">
              <el-option label="待处理" :value="0" />
              <el-option label="已联系" :value="1" />
              <el-option label="已完成" :value="2" />
              <el-option label="已关闭" :value="3" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="toolbar-actions">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="applicationList" stripe>
        <el-table-column prop="companyName" label="企业/机构" min-width="180" show-overflow-tooltip />
        <el-table-column prop="cooperationTypeText" label="合作类型" width="140" />
        <el-table-column prop="contactName" label="联系人" width="110" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="requirement" label="合作需求" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="warning" @click="openStatus(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-toolbar-foot">
        <div class="table-meta">申请数据按提交时间倒序展示</div>
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

    <el-dialog v-model="detailVisible" title="申请详情" width="640px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="企业/机构">{{ detail.companyName }}</el-descriptions-item>
        <el-descriptions-item label="合作类型">{{ detail.cooperationTypeText }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detail.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="合作需求">{{ detail.requirement || '—' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">{{ detail.statusText }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="openStatus(detail)">处理申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusVisible" title="处理合作申请" width="560px">
      <el-form :model="statusForm" label-position="top">
        <el-form-item label="企业/机构">
          <el-input :model-value="statusForm.companyName" disabled />
        </el-form-item>
        <el-form-item label="合作类型">
          <el-input :model-value="statusForm.cooperationTypeText" disabled />
        </el-form-item>
        <el-form-item label="处理状态" prop="status">
          <el-radio-group v-model="statusForm.status">
            <el-radio :label="0">待处理</el-radio>
            <el-radio :label="1">已联系</el-radio>
            <el-radio :label="2">已完成</el-radio>
            <el-radio :label="3">已关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="statusForm.remark" type="textarea" :rows="4" placeholder="填写沟通进展、反馈内容等备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusSubmitting" @click="submitStatus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCooperationApplicationDetail, getCooperationApplicationList, updateCooperationApplicationStatus } from '../../api/modules/cooperation'
import { toCountMap } from '../../utils/console'

const loading = ref(false)
const applicationList = ref([])
const detailVisible = ref(false)
const statusVisible = ref(false)
const statusSubmitting = ref(false)
const detail = ref({})

const searchForm = reactive({
  companyName: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const statusForm = reactive({
  id: null,
  companyName: '',
  cooperationTypeText: '',
  status: 0,
  remark: ''
})

const statusCountMap = computed(() => toCountMap(applicationList.value, 'status'))

const statusTagType = (status) => {
  const map = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'info'
  }
  return map[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const result = await getCooperationApplicationList({
      page: pagination.page,
      size: pagination.size,
      companyName: searchForm.companyName,
      status: searchForm.status === '' ? undefined : searchForm.status
    })
    applicationList.value = result.list || []
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
  searchForm.companyName = ''
  searchForm.status = ''
  handleSearch()
}

const openDetail = async (row) => {
  detail.value = await getCooperationApplicationDetail(row.id)
  detailVisible.value = true
}

const openStatus = (row) => {
  statusForm.id = row.id
  statusForm.companyName = row.companyName
  statusForm.cooperationTypeText = row.cooperationTypeText
  statusForm.status = row.status ?? 0
  statusForm.remark = row.remark || ''
  statusVisible.value = true
  detailVisible.value = false
}

const submitStatus = async () => {
  statusSubmitting.value = true
  try {
    await updateCooperationApplicationStatus(statusForm.id, {
      status: statusForm.status,
      remark: statusForm.remark
    })
    ElMessage.success('申请状态已更新')
    statusVisible.value = false
    loadData()
  } finally {
    statusSubmitting.value = false
  }
}

onMounted(loadData)
</script>
