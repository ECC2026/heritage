<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Banners</div>
      <h1 class="page-title">首页轮播运营</h1>
      <p class="page-subtitle">维护首页焦点图、排序与跳转链接，让后台首页展示内容更有“答辩第一屏”效果。</p>
    </section>

    <section class="inline-metrics">
      <article class="compact-stat">
        <div class="compact-stat-label">轮播总数</div>
        <div class="compact-stat-value">{{ pagination.total }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">启用中</div>
        <div class="compact-stat-value">{{ statusCountMap[1] || 0 }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">禁用中</div>
        <div class="compact-stat-value">{{ statusCountMap[0] || 0 }}</div>
      </article>
    </section>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div class="panel-header-main">
            <h3 class="panel-title">轮播图列表</h3>
            <p class="panel-note">支持封面管理、启停控制与排序配置，适合快速维护前台首页视觉内容。</p>
          </div>
          <el-button type="primary" @click="openCreate">新增轮播图</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="搜索轮播标题" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px;">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="toolbar-actions">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询轮播</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="bannerList" stripe>
        <el-table-column label="轮播图" min-width="320">
          <template #default="{ row }">
            <div class="avatar-cell">
              <div class="uploader-box" style="width: 108px; height: 62px; border-radius: 14px;">
                <img v-if="row.image" :src="resolveAssetUrl(row.image)" alt="banner" class="preview-image" />
                <div v-else class="empty-copy">无图片</div>
              </div>
              <div class="avatar-cell-text">
                <div class="avatar-cell-title">{{ row.title }}</div>
                <div class="avatar-cell-desc">{{ row.link || '无跳转链接' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column prop="linkType" label="跳转类型" min-width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="176" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="removeBanner(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-toolbar-foot">
        <div class="table-meta">默认按 `sort` 倒序，方便维护首页优先级</div>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑轮播图' : '新增轮播图'" width="760px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="dialog-grid">
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入轮播标题" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sort" :min="0" style="width: 100%;" />
          </el-form-item>

          <el-form-item label="跳转类型">
            <el-select v-model="form.linkType" placeholder="请选择跳转类型">
              <el-option label="无跳转" value="none" />
              <el-option label="站内页面" value="page" />
              <el-option label="外部链接" value="url" />
            </el-select>
          </el-form-item>
          <el-form-item label="跳转链接">
            <el-input v-model="form.link" placeholder="请输入跳转链接或页面路径" />
          </el-form-item>

          <el-form-item label="轮播图片" prop="image" class="span-2">
            <div style="display: flex; gap: 16px; flex-wrap: wrap;">
              <el-upload :show-file-list="false" action="#" :before-upload="handleImageUpload">
                <div class="uploader-box" style="width: 240px; height: 132px;">
                  <img v-if="form.image" :src="resolveAssetUrl(form.image)" alt="banner" class="preview-image" />
                  <div v-else class="empty-copy">点击上传轮播图</div>
                </div>
              </el-upload>
              <el-input v-model="form.image" placeholder="或直接粘贴图片链接" />
            </div>
          </el-form-item>

          <el-form-item label="启用状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存轮播图</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addBanner, deleteBanner, getBannerDetail, getBannerList, updateBanner, updateBannerStatus } from '../../api/modules/banner'
import { uploadImage } from '../../api/modules/upload'
import { resolveAssetUrl, toCountMap } from '../../utils/console'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const bannerList = ref([])

const searchForm = reactive({
  title: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  title: '',
  image: '',
  link: '',
  linkType: 'none',
  sort: 0,
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入轮播标题', trigger: 'blur' }],
  image: [{ required: true, message: '请上传轮播图', trigger: 'change' }]
}

const statusCountMap = computed(() => toCountMap(bannerList.value, 'status'))

const loadData = async () => {
  loading.value = true
  try {
    const result = await getBannerList({
      page: pagination.page,
      size: pagination.size,
      title: searchForm.title,
      status: searchForm.status === '' ? undefined : searchForm.status
    })
    bannerList.value = result.list || []
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
  searchForm.title = ''
  searchForm.status = ''
  handleSearch()
}

const openCreate = () => {
  isEdit.value = false
  dialogVisible.value = true
}

const openEdit = async (row) => {
  isEdit.value = true
  Object.assign(form, await getBannerDetail(row.id))
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    title: '',
    image: '',
    link: '',
    linkType: 'none',
    sort: 0,
    status: 1
  })
}

const handleImageUpload = async (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (file.size / 1024 / 1024 >= 5) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  form.image = await uploadImage(file)
  ElMessage.success('图片上传成功')
  return false
}

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    const payload = { ...form }
    if (isEdit.value) {
      await updateBanner(form.id, payload)
      ElMessage.success('轮播图已更新')
    } else {
      delete payload.id
      await addBanner(payload)
      ElMessage.success('轮播图已创建')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async (row) => {
  const targetStatus = row.status === 1 ? 0 : 1
  await updateBannerStatus(row.id, { status: targetStatus })
  ElMessage.success(targetStatus === 1 ? '轮播图已启用' : '轮播图已禁用')
  loadData()
}

const removeBanner = async (row) => {
  await ElMessageBox.confirm(`确定删除轮播图“${row.title}”吗？`, '删除确认', { type: 'warning' })
  await deleteBanner(row.id)
  ElMessage.success('轮播图已删除')
  loadData()
}

onMounted(loadData)
</script>
