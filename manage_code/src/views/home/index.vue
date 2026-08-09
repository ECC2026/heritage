<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Dashboard</div>
      <h1 class="page-title">非遗文化平台运营总览</h1>
      <p class="page-subtitle">
        这里汇总平台的用户、内容、活动与交易数据，适合作为答辩展示时的后台首页。
      </p>
    </section>

    <section class="metric-grid">
      <article v-for="item in metrics" :key="item.label" class="metric-card">
        <div class="soft-tag">{{ item.tag }}</div>
        <div class="metric-value">{{ item.value }}</div>
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-footnote">{{ item.note }}</div>
      </article>
    </section>

    <section class="chart-grid">
      <article class="chart-card">
        <h3 class="chart-title">业务模块占比</h3>
        <p class="chart-note">用轻量 SVG 展示平台用户、内容、商品、订单在整体后台中的构成关系。</p>

        <div class="donut-wrap">
          <svg viewBox="0 0 220 220" width="220" height="220" aria-hidden="true">
            <circle cx="110" cy="110" r="68" fill="none" stroke="rgba(92,65,52,0.08)" stroke-width="20" />
            <circle
              v-for="segment in donutSegments"
              :key="segment.label"
              cx="110"
              cy="110"
              r="68"
              fill="none"
              :stroke="segment.color"
              stroke-width="20"
              stroke-linecap="round"
              :stroke-dasharray="`${segment.length} ${segment.gap}`"
              :stroke-dashoffset="segment.offset"
              transform="rotate(-90 110 110)"
            />
            <text x="110" y="100" text-anchor="middle" style="font-size: 14px; fill: #756259;">核心数据</text>
            <text x="110" y="128" text-anchor="middle" style="font-size: 30px; font-weight: 800; fill: #34251f;">
              {{ donutTotal }}
            </text>
          </svg>

          <div class="donut-legend">
            <div v-for="item in donutSource" :key="item.label" class="legend-item">
              <div class="legend-label">
                <span class="legend-dot" :style="{ background: item.color }"></span>
                <span>{{ item.label }}</span>
              </div>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </div>
      </article>

      <article class="chart-card">
        <h3 class="chart-title">活动热度条形图</h3>
        <p class="chart-note">用报名人数 / 上限的比例快速定位最适合答辩演示的活动案例。</p>

        <div class="bar-chart">
          <div v-for="item in activityHeatmap" :key="item.name" class="bar-row">
            <div>{{ item.name }}</div>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: `${item.percent}%` }"></div>
            </div>
            <div class="bar-value">{{ item.signupCount }}/{{ item.limitCount }}</div>
          </div>
        </div>

        <div class="status-strip">
          <div class="status-pill warning">
            待审核
            <strong>{{ activityStatusCount.pending }}</strong>
          </div>
          <div class="status-pill success">
            进行中
            <strong>{{ activityStatusCount.active }}</strong>
          </div>
          <div class="status-pill info">
            已结束
            <strong>{{ activityStatusCount.finished }}</strong>
          </div>
        </div>
      </article>
    </section>

    <el-row :gutter="18">
      <el-col :xs="24" :lg="14">
        <el-card class="panel-card">
          <template #header>
            <div class="panel-header">
              <div class="panel-header-main">
                <h3 class="panel-title">近期活动状态</h3>
                <p class="panel-note">展示最近录入的活动，用于快速查看报名热度和审核进度。</p>
              </div>
            </div>
          </template>

          <el-table :data="recentActivities" stripe>
            <el-table-column prop="name" label="活动名称" min-width="180" />
            <el-table-column prop="organizerName" label="组织者" min-width="120" />
            <el-table-column prop="signupCount" label="报名人数" width="100" />
            <el-table-column prop="limitCount" label="人数上限" width="100" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="statusTypeMap[row.status] || 'info'">
                  {{ row.statusText || '未知' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card class="panel-card">
          <template #header>
            <div class="panel-header">
              <div class="panel-header-main">
                <h3 class="panel-title">管理建议</h3>
                <p class="panel-note">结合当前后端能力和平台场景，给出更适合演示展示的操作提示。</p>
              </div>
            </div>
          </template>

          <div style="display: grid; gap: 14px;">
            <div class="metric-card" style="padding: 18px;">
              <div class="soft-tag">内容区</div>
              <div style="margin-top: 12px; font-weight: 700;">先维护资讯与轮播图</div>
              <div class="metric-footnote">首页展示最直观，答辩时能立刻体现平台定位。</div>
            </div>
            <div class="metric-card" style="padding: 18px;">
              <div class="soft-tag">活动区</div>
              <div style="margin-top: 12px; font-weight: 700;">活动和报名是核心闭环</div>
              <div class="metric-footnote">前台报名、后台审核、导出名单，这条线最适合演示业务完整性。</div>
            </div>
            <div class="metric-card" style="padding: 18px;">
              <div class="soft-tag">商城区</div>
              <div style="margin-top: 12px; font-weight: 700;">商品和订单体现商业化能力</div>
              <div class="metric-footnote">适合突出“文化传播 + 文创转化”的平台价值。</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { get } from '../../utils/request'
import { getActivityList } from '../../api/modules/activity'

const stats = ref({
  userCount: 0,
  newsCount: 0,
  productCount: 0,
  orderCount: 0,
  activityCount: 0
})
const recentActivities = ref([])

const statusTypeMap = {
  0: 'warning',
  1: 'success',
  2: 'info'
}

const donutSource = computed(() => [
  { label: '用户', value: stats.value.userCount || 0, color: '#b14d2d' },
  { label: '资讯', value: stats.value.newsCount || 0, color: '#d9a441' },
  { label: '商品', value: stats.value.productCount || 0, color: '#3f8f68' },
  { label: '订单', value: stats.value.orderCount || 0, color: '#7d675d' }
])

const donutTotal = computed(() => donutSource.value.reduce((sum, item) => sum + item.value, 0))
const donutCircumference = 2 * Math.PI * 68
const donutSegments = computed(() => {
  if (!donutTotal.value) return []
  let offset = 0
  return donutSource.value.map((item) => {
    const ratio = item.value / donutTotal.value
    const length = ratio * donutCircumference
    const segment = {
      ...item,
      length,
      gap: donutCircumference - length,
      offset: -offset
    }
    offset += length
    return segment
  })
})

const activityHeatmap = computed(() =>
  recentActivities.value.slice(0, 5).map((item) => {
    const limitCount = Number(item.limitCount) || 0
    const signupCount = Number(item.signupCount) || 0
    const percent = limitCount > 0 ? Math.min(100, Math.round((signupCount / limitCount) * 100)) : 0
    return {
      name: item.name,
      signupCount,
      limitCount,
      percent
    }
  })
)

const activityStatusCount = computed(() => ({
  pending: recentActivities.value.filter((item) => item.status === 0).length,
  active: recentActivities.value.filter((item) => item.status === 1).length,
  finished: recentActivities.value.filter((item) => item.status === 2).length
}))

const metrics = computed(() => [
  { label: '平台用户数', value: stats.value.userCount, tag: 'Users', note: '反映平台基础用户规模' },
  { label: '资讯内容数', value: stats.value.newsCount, tag: 'Content', note: '体现平台内容运营能力' },
  { label: '文创商品数', value: stats.value.productCount, tag: 'Commerce', note: '支撑商城展示与转化' },
  { label: '活动 / 订单总量', value: `${stats.value.activityCount} / ${stats.value.orderCount}`, tag: 'Flow', note: '活动闭环与交易闭环并行' }
])

const loadStats = async () => {
  stats.value = await get('/stats')
}

const loadActivities = async () => {
  const res = await getActivityList({ page: 1, size: 8 })
  recentActivities.value = res.list || []
}

onMounted(() => {
  loadStats()
  loadActivities()
})
</script>
