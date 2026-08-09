<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Inheritors</div>
      <h1 class="page-title">传承人资质审核</h1>
      <p class="page-subtitle">
        这里用于集中审核非遗传承人申请信息，突出平台对文化主体的扶持与规范管理。
      </p>
    </section>

    <section class="inline-metrics">
      <article class="compact-stat">
        <div class="compact-stat-label">待审核</div>
        <div class="compact-stat-value">{{ auditCountMap[0] || 0 }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">已通过</div>
        <div class="compact-stat-value">{{ auditCountMap[1] || 0 }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">已拒绝</div>
        <div class="compact-stat-value">{{ auditCountMap[2] || 0 }}</div>
      </article>
    </section>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div class="panel-header-main">
            <h3 class="panel-title">传承人申请列表</h3>
            <p class="panel-note">按姓名与审核状态筛选，快速完成资质审核与证书核验。</p>
          </div>
          <div class="soft-tag">审核队列</div>
        </div>
      </template>

      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="姓名">
            <el-input v-model="searchForm.name" placeholder="搜索申请人姓名" clearable />
          </el-form-item>
          <el-form-item label="审核状态">
            <el-select v-model="searchForm.auditStatus" placeholder="全部状态" clearable style="width: 140px;">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="toolbar-actions">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询申请</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="inheritantList" stripe>
        <el-table-column label="申请人" min-width="220">
          <template #default="{ row }">
            <div class="avatar-cell">
              <el-avatar :size="42">{{ (row.name || '承').slice(0, 1) }}</el-avatar>
              <div class="avatar-cell-text">
                <div class="avatar-cell-title">{{ row.name || '未命名申请人' }}</div>
                <div class="avatar-cell-desc">{{ row.phone || '未填写手机号' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="skillType" label="技艺类型" min-width="140" />
        <el-table-column prop="skillDesc" label="技艺简介" min-width="240" show-overflow-tooltip />
        <el-table-column label="证书材料" width="110">
          <template #default="{ row }">
            <el-tag :type="row.certificate ? 'success' : 'info'">
              {{ row.certificate ? '已上传' : '缺少材料' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.auditStatus)">
              {{ getStatusText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" min-width="176" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
            <el-button v-if="row.auditStatus === 0" link type="success" @click="submitAudit(row, 1)">通过</el-button>
            <el-button v-if="row.auditStatus === 0" link type="danger" @click="submitAudit(row, 2)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-toolbar-foot">
        <div class="table-meta">已自动对接后端 `auditStatus` 字段</div>
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

    <el-drawer v-model="drawerVisible" size="520px" title="传承人详情">
      <template v-if="currentInheritant">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="姓名">{{ currentInheritant.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentInheritant.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentInheritant.idCard || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="技艺类型">{{ currentInheritant.skillType || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getStatusType(currentInheritant.auditStatus)">
              {{ getStatusText(currentInheritant.auditStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentInheritant.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="从业经历">{{ currentInheritant.experience || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="技艺简介">{{ currentInheritant.skillDesc || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="作品展示">{{ currentInheritant['作品展示'] || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="审核备注">{{ currentInheritant.auditRemark || '暂无' }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 18px;">
          <div style="margin-bottom: 8px; font-weight: 700;">资质证书</div>
          <div class="uploader-box" style="width: 100%; height: 220px;">
            <el-image
              v-if="currentInheritant.certificate"
              :src="currentInheritant.certificate"
              :preview-src-list="[currentInheritant.certificate]"
              fit="contain"
              class="preview-image"
            />
            <div v-else class="empty-copy">暂无证书图片</div>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditInheritor, getInheritantDetail, getInheritantList } from '../../api/modules/user'
import { toCountMap } from '../../utils/console'

const loading = ref(false)
const drawerVisible = ref(false)
const currentInheritant = ref(null)
const inheritantList = ref([])

const searchForm = reactive({
  name: '',
  auditStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const auditCountMap = computed(() => toCountMap(inheritantList.value, 'auditStatus'))

const loadData = async () => {
  loading.value = true
  try {
    const result = await getInheritantList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name,
      auditStatus: searchForm.auditStatus === '' ? undefined : searchForm.auditStatus
    })
    inheritantList.value = result.list || []
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
  searchForm.auditStatus = ''
  handleSearch()
}

const openDetail = async (row) => {
  currentInheritant.value = await getInheritantDetail(row.id)
  drawerVisible.value = true
}

const submitAudit = async (row, auditStatus) => {
  const actionText = auditStatus === 1 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确定要${actionText}“${row.name}”的认证申请吗？`, '审核确认', {
    type: 'warning'
  })
  await auditInheritor(row.id, { auditStatus })
  ElMessage.success(`已${actionText}`)
  if (currentInheritant.value?.id === row.id) {
    currentInheritant.value.auditStatus = auditStatus
  }
  loadData()
}

const getStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' })[status] || 'info'
const getStatusText = (status) => ({ 0: '待审核', 1: '已通过', 2: '已拒绝' })[status] || '未知'

onMounted(loadData)
</script>
