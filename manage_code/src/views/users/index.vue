<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Users</div>
      <h1 class="page-title">平台用户管理</h1>
      <p class="page-subtitle">
        集中查看用户账号状态、联系方式和活跃情况，适合在答辩时展示平台用户运营能力。
      </p>
    </section>

    <section class="inline-metrics">
      <article class="compact-stat">
        <div class="compact-stat-label">当前检索结果</div>
        <div class="compact-stat-value">{{ pagination.total }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">正常账号</div>
        <div class="compact-stat-value">{{ activeCount }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">禁用账号</div>
        <div class="compact-stat-value">{{ disabledCount }}</div>
      </article>
    </section>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div class="panel-header-main">
            <h3 class="panel-title">用户列表</h3>
            <p class="panel-note">支持按用户名和手机号快速检索，并直接进行状态管理。</p>
          </div>
          <div class="soft-tag">共 {{ pagination.total }} 条用户记录</div>
        </div>
      </template>

      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="搜索用户名" clearable />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="searchForm.phone" placeholder="搜索手机号" clearable />
          </el-form-item>
        </el-form>

        <div class="toolbar-actions">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleSearch">立即查询</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="userList" stripe>
        <el-table-column label="用户信息" min-width="240">
          <template #default="{ row }">
            <div class="avatar-cell">
              <el-avatar :size="42" :src="row.avatar">
                {{ (row.nickname || row.username || '用').slice(0, 1) }}
              </el-avatar>
              <div class="avatar-cell-text">
                <div class="avatar-cell-title">{{ row.nickname || row.username || '未命名用户' }}</div>
                <div class="avatar-cell-desc">@{{ row.username || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="账号状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="176" />
        <el-table-column prop="lastLoginTime" label="最近登录" min-width="176" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-toolbar-foot">
        <div class="table-meta">默认按注册时间倒序排列</div>
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

    <el-drawer v-model="drawerVisible" size="460px" title="用户详情">
      <template v-if="currentUser">
        <div class="soft-block" style="display: flex; align-items: center; gap: 14px; margin-bottom: 16px;">
          <el-avatar :size="56" :src="currentUser.avatar">
            {{ (currentUser.nickname || currentUser.username || '用').slice(0, 1) }}
          </el-avatar>
          <div>
            <div style="font-size: 18px; font-weight: 700;">{{ currentUser.nickname || currentUser.username }}</div>
            <div class="empty-copy">@{{ currentUser.username || '-' }}</div>
          </div>
        </div>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="手机号">{{ currentUser.phone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ genderText(currentUser.gender) }}</el-descriptions-item>
          <el-descriptions-item label="生日">{{ currentUser.birthday || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
              {{ currentUser.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentUser.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="最近登录">{{ currentUser.lastLoginTime || '暂无记录' }}</el-descriptions-item>
          <el-descriptions-item label="登录 IP">{{ currentUser.lastLoginIp || '暂无记录' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserDetail, getUserList, updateUserStatus } from '../../api/modules/user'

const loading = ref(false)
const drawerVisible = ref(false)
const currentUser = ref(null)
const userList = ref([])

const searchForm = reactive({
  username: '',
  phone: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const activeCount = computed(() => userList.value.filter((item) => item.status === 1).length)
const disabledCount = computed(() => userList.value.filter((item) => item.status !== 1).length)

const loadData = async () => {
  loading.value = true
  try {
    const result = await getUserList({
      page: pagination.page,
      size: pagination.size,
      username: searchForm.username,
      phone: searchForm.phone
    })
    userList.value = result.list || []
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
  searchForm.username = ''
  searchForm.phone = ''
  handleSearch()
}

const openDetail = async (row) => {
  const detail = await getUserDetail(row.id)
  currentUser.value = detail
  drawerVisible.value = true
}

const toggleStatus = async (row) => {
  const targetStatus = row.status === 1 ? 0 : 1
  const actionText = targetStatus === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确定要${actionText}用户“${row.username}”吗？`, '状态确认', {
    type: 'warning'
  })
  await updateUserStatus(row.id, { status: targetStatus })
  ElMessage.success(`${actionText}成功`)
  if (currentUser.value?.id === row.id) {
    currentUser.value.status = targetStatus
  }
  loadData()
}

const genderText = (value) => {
  if (value === 1) return '男'
  if (value === 2) return '女'
  return '未填写'
}

onMounted(loadData)
</script>
