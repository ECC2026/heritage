<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Signups</div>
      <h1 class="page-title">活动报名审核</h1>
      <p class="page-subtitle">集中处理用户报名记录、审核结果与名单导出，是答辩展示业务闭环最直观的一页。</p>
    </section>

    <section class="inline-metrics">
      <article class="compact-stat">
        <div class="compact-stat-label">报名总数</div>
        <div class="compact-stat-value">{{ pagination.total }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">待审核</div>
        <div class="compact-stat-value">{{ statusCountMap[0] || 0 }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">已通过 / 已拒绝</div>
        <div class="compact-stat-value">{{ statusCountMap[1] || 0 }} / {{ statusCountMap[2] || 0 }}</div>
      </article>
    </section>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div class="panel-header-main">
            <h3 class="panel-title">报名记录</h3>
            <p class="panel-note">前端已适配后端 CSV 导出和“先过滤后分页”的查询逻辑。</p>
          </div>
        </div>
      </template>

      <ExportAction
        title="报名名单导出"
        description="支持导出当前筛选活动或全部报名记录，方便答辩时展示活动审核成果。"
        :total="pagination.total"
        :loading="exporting"
        :last-export-at="lastExportAt"
        @export="handleExport"
      />

      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="活动名称">
            <el-input v-model="searchForm.activityName" placeholder="搜索活动名称" clearable />
          </el-form-item>
          <el-form-item label="审核状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px;">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
              <el-option label="已取消" :value="3" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="toolbar-actions">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询报名</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="signupList" stripe>
        <el-table-column prop="activityName" label="活动名称" min-width="220" />
        <el-table-column prop="userName" label="报名人" min-width="130" />
        <el-table-column prop="phone" label="联系电话" min-width="140" />
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column label="审核状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报名时间" min-width="176" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
            <el-button v-if="row.status === 0" link type="success" @click="auditSignup(row, 1)">通过</el-button>
            <el-button v-if="row.status === 0" link type="danger" @click="auditSignup(row, 2)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-toolbar-foot">
        <div class="table-meta">报名管理与活动管理页配合使用，演示效果最好</div>
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

    <el-dialog v-model="dialogVisible" title="报名详情" width="720px">
      <template v-if="currentSignup">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="活动名称">{{ currentSignup.activityName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报名人">{{ currentSignup.userName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentSignup.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getStatusType(currentSignup.status)">{{ getStatusText(currentSignup.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="报名时间">{{ currentSignup.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ currentSignup.auditTime || '尚未审核' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentSignup.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ExportAction from '../../components/ExportAction.vue'
import { exportSignups, getSignupDetail, getSignupList, updateSignupStatus } from '../../api/modules/signup'
import { downloadBlobFile, toCountMap } from '../../utils/console'

const loading = ref(false)
const exporting = ref(false)
const lastExportAt = ref('')
const dialogVisible = ref(false)
const currentSignup = ref(null)
const signupList = ref([])

const searchForm = reactive({
  activityName: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const statusCountMap = computed(() => toCountMap(signupList.value, 'status'))

const loadData = async () => {
  loading.value = true
  try {
    const result = await getSignupList({
      page: pagination.page,
      size: pagination.size,
      activityName: searchForm.activityName,
      status: searchForm.status === '' ? undefined : searchForm.status
    })
    signupList.value = result.list || []
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
  searchForm.activityName = ''
  searchForm.status = ''
  handleSearch()
}

const openDetail = async (row) => {
  currentSignup.value = await getSignupDetail(row.id)
  dialogVisible.value = true
}

const auditSignup = async (row, status) => {
  const actionText = status === 1 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确定要${actionText}该报名记录吗？`, '审核确认', { type: 'warning' })
  await updateSignupStatus(row.id, { status })
  ElMessage.success(`已${actionText}`)
  loadData()
}

const handleExport = async (mode) => {
  exporting.value = true
  try {
    const params = mode === 'all'
      ? {}
      : {
          activityName: searchForm.activityName,
          status: searchForm.status === '' ? undefined : searchForm.status
        }
    const blob = await exportSignups(params)
    downloadBlobFile(blob, `signups-${Date.now()}.csv`)
    lastExportAt.value = new Date().toLocaleString('zh-CN', { hour12: false })
    ElMessage.success('报名名单导出成功')
  } finally {
    exporting.value = false
  }
}

const getStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' })[status] || 'info'
const getStatusText = (status) => ({ 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已取消' })[status] || '未知'

onMounted(loadData)
</script>
