<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">News</div>
      <h1 class="page-title">非遗资讯内容中心</h1>
      <p class="page-subtitle">
        用统一内容后台维护资讯封面、分类和正文，适合展示平台在文化传播上的运营能力。
      </p>
    </section>

    <section class="inline-metrics">
      <article class="compact-stat">
        <div class="compact-stat-label">资讯总数</div>
        <div class="compact-stat-value">{{ pagination.total }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">已发布</div>
        <div class="compact-stat-value">{{ publishedCount }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">草稿 / 下架</div>
        <div class="compact-stat-value">{{ draftCount }} / {{ archivedCount }}</div>
      </article>
    </section>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div class="panel-header-main">
            <h3 class="panel-title">资讯列表</h3>
            <p class="panel-note">支持资讯新增、编辑、状态切换和删除，内容可直接用于前台展示。</p>
          </div>
          <el-button type="primary" @click="openCreate">发布新资讯</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="搜索资讯标题" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px;">
              <el-option label="草稿" :value="0" />
              <el-option label="已发布" :value="1" />
              <el-option label="已下架" :value="2" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="toolbar-actions">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询资讯</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="newsList" stripe>
        <el-table-column label="内容标题" min-width="280">
          <template #default="{ row }">
            <div class="avatar-cell">
              <div class="uploader-box" style="width: 76px; height: 56px; border-radius: 14px;">
                <img v-if="row.cover" :src="resolveAssetUrl(row.cover)" alt="cover" class="preview-image" />
                <div v-else class="empty-copy">无封面</div>
              </div>
              <div class="avatar-cell-text">
                <div class="avatar-cell-title">{{ row.title }}</div>
                <div class="avatar-cell-desc">{{ row.summary || getSummary(row.content) || '暂无摘要' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" min-width="120" />
        <el-table-column prop="author" label="作者" min-width="120" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览量" width="96" />
        <el-table-column prop="createTime" label="创建时间" min-width="176" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="statusActionType(row.status)" @click="toggleStatus(row)">
              {{ statusActionText(row.status) }}
            </el-button>
            <el-button link type="danger" @click="removeNews(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-toolbar-foot">
        <div class="table-meta">资讯摘要已自动兼容后端 `summary` 和正文截断</div>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑资讯' : '新增资讯'" width="860px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="dialog-grid">
          <el-form-item label="资讯标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入资讯标题" />
          </el-form-item>
          <el-form-item label="内容分类" prop="category">
            <el-select v-model="form.category" placeholder="请选择分类">
              <el-option label="非遗动态" value="非遗动态" />
              <el-option label="传承人风采" value="传承人风采" />
              <el-option label="活动回顾" value="活动回顾" />
              <el-option label="政策解读" value="政策解读" />
            </el-select>
          </el-form-item>

          <el-form-item label="作者" prop="author">
            <el-input v-model="form.author" placeholder="请输入作者名称" />
          </el-form-item>
          <el-form-item label="来源">
            <el-input v-model="form.source" placeholder="请输入来源，如：平台原创" />
          </el-form-item>

          <el-form-item label="封面图" class="span-2">
            <div style="display: flex; gap: 16px; flex-wrap: wrap;">
              <el-upload :show-file-list="false" action="#" :before-upload="handleCoverUpload">
                <div class="uploader-box">
                  <img v-if="form.cover" :src="resolveAssetUrl(form.cover)" alt="cover" class="preview-image" />
                  <div v-else class="empty-copy">点击上传</div>
                </div>
              </el-upload>
              <el-input v-model="form.cover" placeholder="或直接粘贴图片链接" />
            </div>
          </el-form-item>

          <el-form-item label="资讯正文" prop="content" class="span-2">
            <el-input v-model="form.content" type="textarea" :rows="10" placeholder="支持直接粘贴富文本或纯文本内容" />
          </el-form-item>

          <el-form-item label="发布状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="0">草稿</el-radio>
              <el-radio :label="1">已发布</el-radio>
              <el-radio :label="2">已下架</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="置顶推荐">
            <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存资讯</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addNews, deleteNews, getNewsDetail, getNewsList, updateNews, updateNewsStatus } from '../../api/modules/news'
import { uploadImage } from '../../api/modules/upload'
import { resolveAssetUrl, stripHtml } from '../../utils/console'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const newsList = ref([])

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
  category: '',
  cover: '',
  content: '',
  author: '',
  source: '',
  status: 1,
  isTop: 0
})

const rules = {
  title: [{ required: true, message: '请输入资讯标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
}

const publishedCount = computed(() => newsList.value.filter((item) => item.status === 1).length)
const draftCount = computed(() => newsList.value.filter((item) => item.status === 0).length)
const archivedCount = computed(() => newsList.value.filter((item) => item.status === 2).length)

const loadData = async () => {
  loading.value = true
  try {
    const result = await getNewsList({
      page: pagination.page,
      size: pagination.size,
      title: searchForm.title,
      status: searchForm.status === '' ? undefined : searchForm.status
    })
    newsList.value = result.list || []
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
  const detail = await getNewsDetail(row.id)
  Object.assign(form, {
    id: detail.id,
    title: detail.title || '',
    category: detail.category || '',
    cover: detail.cover || '',
    content: detail.content || '',
    author: detail.author || '',
    source: detail.source || '',
    status: detail.status ?? 1,
    isTop: detail.isTop ?? 0
  })
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    title: '',
    category: '',
    cover: '',
    content: '',
    author: '',
    source: '',
    status: 1,
    isTop: 0
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
      await updateNews(form.id, payload)
      ElMessage.success('资讯已更新')
    } else {
      delete payload.id
      await addNews(payload)
      ElMessage.success('资讯已创建')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async (row) => {
  const targetStatus = row.status === 1 ? 2 : 1
  await updateNewsStatus(row.id, { status: targetStatus })
  ElMessage.success(targetStatus === 1 ? '资讯已发布' : '资讯已下架')
  loadData()
}

const removeNews = async (row) => {
  await ElMessageBox.confirm(`确定删除资讯“${row.title}”吗？`, '删除确认', {
    type: 'warning'
  })
  await deleteNews(row.id)
  ElMessage.success('资讯已删除')
  loadData()
}

const getStatusType = (status) => ({ 0: 'info', 1: 'success', 2: 'warning' })[status] || 'info'
const getStatusText = (status) => ({ 0: '草稿', 1: '已发布', 2: '已下架' })[status] || '未知'
const statusActionText = (status) => (status === 1 ? '下架' : '发布')
const statusActionType = (status) => (status === 1 ? 'warning' : 'success')
const getSummary = (content) => {
  const text = stripHtml(content)
  return text.length > 42 ? `${text.slice(0, 42)}...` : text
}

onMounted(loadData)
</script>
