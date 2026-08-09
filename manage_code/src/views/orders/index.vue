<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Orders</div>
      <h1 class="page-title">订单履约中心</h1>
      <p class="page-subtitle">后台可查看订单状态、收货信息和商品明细，并支持一键导出 CSV 用于答辩材料展示。</p>
    </section>

    <section class="inline-metrics">
      <article class="compact-stat">
        <div class="compact-stat-label">订单总数</div>
        <div class="compact-stat-value">{{ pagination.total }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">待发货</div>
        <div class="compact-stat-value">{{ statusCountMap[1] || 0 }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">已发货 / 已完成</div>
        <div class="compact-stat-value">{{ statusCountMap[2] || 0 }} / {{ statusCountMap[3] || 0 }}</div>
      </article>
    </section>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div class="panel-header-main">
            <h3 class="panel-title">订单列表</h3>
            <p class="panel-note">支持筛选、查看详情、发货和导出，已经联通第二轮后端导出接口。</p>
          </div>
        </div>
      </template>

      <ExportAction
        title="订单数据导出"
        description="可导出当前筛选结果，也可一键导出全部订单，适合答辩时展示履约数据。"
        :total="pagination.total"
        :loading="exporting"
        :last-export-at="lastExportAt"
        @export="handleExport"
      />

      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="订单号">
            <el-input v-model="searchForm.orderNo" placeholder="搜索订单号" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px;">
              <el-option label="待支付" :value="0" />
              <el-option label="已支付" :value="1" />
              <el-option label="已发货" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="toolbar-actions">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询订单</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="orderList" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="220" />
        <el-table-column prop="userName" label="下单用户" min-width="140" />
        <el-table-column prop="productName" label="商品信息" min-width="220" show-overflow-tooltip />
        <el-table-column label="订单金额" width="110">
          <template #default="{ row }">¥{{ row.totalPrice ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiverName" label="收货人" min-width="120" />
        <el-table-column prop="createTime" label="下单时间" min-width="176" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
            <el-button v-if="row.status === 1" link type="success" @click="shipOrder(row)">发货</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-toolbar-foot">
        <div class="table-meta">导出接口返回 `blob`，前端已适配下载</div>
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

    <el-dialog v-model="dialogVisible" title="订单详情" width="900px">
      <template v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单用户">{{ currentOrder.userName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone || currentOrder.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.receiverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ currentOrder.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ currentOrder.payTime || '未支付' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ currentOrder.shipTime || '未发货' }}</el-descriptions-item>
          <el-descriptions-item label="订单备注">{{ currentOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 18px;">
          <div style="margin-bottom: 10px; font-weight: 700;">商品明细</div>
          <el-table :data="currentOrder.items || []" border>
            <el-table-column prop="productName" label="商品名称" min-width="180" />
            <el-table-column label="单价" width="120">
              <template #default="{ row }">¥{{ row.price ?? 0 }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="90" />
            <el-table-column label="小计" width="120">
              <template #default="{ row }">¥{{ row.subtotal ?? 0 }}</template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ExportAction from '../../components/ExportAction.vue'
import { exportOrders, getOrderDetail, getOrderList, updateOrderStatus } from '../../api/modules/order'
import { downloadBlobFile, toCountMap } from '../../utils/console'

const loading = ref(false)
const exporting = ref(false)
const lastExportAt = ref('')
const dialogVisible = ref(false)
const currentOrder = ref(null)
const orderList = ref([])

const searchForm = reactive({
  orderNo: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const statusCountMap = computed(() => toCountMap(orderList.value, 'status'))

const loadData = async () => {
  loading.value = true
  try {
    const result = await getOrderList({
      page: pagination.page,
      size: pagination.size,
      orderNo: searchForm.orderNo,
      status: searchForm.status === '' ? undefined : searchForm.status
    })
    orderList.value = result.list || []
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
  searchForm.orderNo = ''
  searchForm.status = ''
  handleSearch()
}

const openDetail = async (row) => {
  currentOrder.value = await getOrderDetail(row.id)
  dialogVisible.value = true
}

const shipOrder = async (row) => {
  await updateOrderStatus(row.id, { status: 2 })
  ElMessage.success('订单已发货')
  if (currentOrder.value?.id === row.id) {
    currentOrder.value.status = 2
  }
  loadData()
}

const handleExport = async (mode) => {
  exporting.value = true
  try {
    const params = mode === 'all'
      ? {}
      : {
          orderNo: searchForm.orderNo,
          status: searchForm.status === '' ? undefined : searchForm.status
        }
    const blob = await exportOrders(params)
    downloadBlobFile(blob, `orders-${Date.now()}.csv`)
    lastExportAt.value = new Date().toLocaleString('zh-CN', { hour12: false })
    ElMessage.success('订单导出成功')
  } finally {
    exporting.value = false
  }
}

const getStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'primary', 3: 'info', 4: 'danger' })[status] || 'info'
const getStatusText = (status) => ({ 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' })[status] || '未知'

onMounted(loadData)
</script>
