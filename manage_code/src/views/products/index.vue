<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Products</div>
      <h1 class="page-title">文创商品管理</h1>
      <p class="page-subtitle">统一维护商品信息、库存和上架状态，强化“文化传播 + 文创转化”的业务闭环。</p>
    </section>

    <section class="inline-metrics">
      <article class="compact-stat">
        <div class="compact-stat-label">商品总数</div>
        <div class="compact-stat-value">{{ pagination.total }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">上架中</div>
        <div class="compact-stat-value">{{ statusCountMap[1] || 0 }}</div>
      </article>
      <article class="compact-stat">
        <div class="compact-stat-label">总库存</div>
        <div class="compact-stat-value">{{ stockTotal }}</div>
      </article>
    </section>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <div class="panel-header-main">
            <h3 class="panel-title">商品列表</h3>
            <p class="panel-note">支持新增、编辑、上下架和删除，字段已经对齐后端 `category` / `categoryId` 逻辑。</p>
          </div>
          <el-button type="primary" @click="openCreate">新增商品</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="商品名称">
            <el-input v-model="searchForm.name" placeholder="搜索商品名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px;">
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="0" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="toolbar-actions">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询商品</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="productList" stripe>
        <el-table-column label="商品信息" min-width="280">
          <template #default="{ row }">
            <div class="avatar-cell">
              <div class="uploader-box" style="width: 76px; height: 56px; border-radius: 14px;">
                <img v-if="row.cover" :src="resolveAssetUrl(row.cover)" alt="cover" class="preview-image" />
                <div v-else class="empty-copy">无封面</div>
              </div>
              <div class="avatar-cell-text">
                <div class="avatar-cell-title">{{ row.name }}</div>
                <div class="avatar-cell-desc">{{ row.category || '未分类' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="售价" width="120">
          <template #default="{ row }">¥{{ row.price ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="原价" width="120">
          <template #default="{ row }">¥{{ row.originalPrice ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="96" />
        <el-table-column prop="sales" label="销量" width="96" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isRecommend === 1 ? 'warning' : 'info'">
              {{ row.isRecommend === 1 ? '推荐' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="removeProduct(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-toolbar-foot">
        <div class="table-meta">商品数据按创建时间倒序展示</div>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="920px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="dialog-grid">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入商品名称" />
          </el-form-item>
          <el-form-item label="商品分类" prop="category">
            <el-select v-model="form.category" placeholder="请选择分类">
              <el-option label="非遗手工艺品" value="非遗手工艺品" />
              <el-option label="非遗文创" value="非遗文创" />
              <el-option label="非遗服饰" value="非遗服饰" />
              <el-option label="非遗食品" value="非遗食品" />
            </el-select>
          </el-form-item>

          <el-form-item label="售价" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="原价">
            <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%;" />
          </el-form-item>

          <el-form-item label="库存" prop="stock">
            <el-input-number v-model="form.stock" :min="0" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="销量">
            <el-input-number v-model="form.sales" :min="0" style="width: 100%;" />
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

          <el-form-item label="商品描述" class="span-2">
            <el-input v-model="form.description" type="textarea" :rows="6" placeholder="请输入商品描述" />
          </el-form-item>

          <el-form-item label="上架状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">上架</el-radio>
              <el-radio :label="0">下架</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="首页推荐">
            <el-switch v-model="form.isRecommend" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存商品</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addProduct, deleteProduct, getProductDetail, getProductList, updateProduct, updateProductStatus } from '../../api/modules/product'
import { uploadImage } from '../../api/modules/upload'
import { resolveAssetUrl, toCountMap } from '../../utils/console'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const productList = ref([])

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
  category: '',
  price: 0,
  originalPrice: 0,
  stock: 0,
  sales: 0,
  cover: '',
  description: '',
  status: 1,
  isRecommend: 0
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'change' }]
}

const statusCountMap = computed(() => toCountMap(productList.value, 'status'))
const stockTotal = computed(() => productList.value.reduce((sum, item) => sum + (Number(item.stock) || 0), 0))

const loadData = async () => {
  loading.value = true
  try {
    const result = await getProductList({
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name,
      status: searchForm.status === '' ? undefined : searchForm.status
    })
    productList.value = result.list || []
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
  Object.assign(form, await getProductDetail(row.id))
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    name: '',
    category: '',
    price: 0,
    originalPrice: 0,
    stock: 0,
    sales: 0,
    cover: '',
    description: '',
    status: 1,
    isRecommend: 0
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
      await updateProduct(form.id, payload)
      ElMessage.success('商品已更新')
    } else {
      delete payload.id
      await addProduct(payload)
      ElMessage.success('商品已创建')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async (row) => {
  const targetStatus = row.status === 1 ? 0 : 1
  await updateProductStatus(row.id, { status: targetStatus })
  ElMessage.success(targetStatus === 1 ? '商品已上架' : '商品已下架')
  loadData()
}

const removeProduct = async (row) => {
  await ElMessageBox.confirm(`确定删除商品“${row.name}”吗？`, '删除确认', { type: 'warning' })
  await deleteProduct(row.id)
  ElMessage.success('商品已删除')
  loadData()
}

onMounted(loadData)
</script>
