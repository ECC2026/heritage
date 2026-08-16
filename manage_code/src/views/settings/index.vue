<template>
  <div class="page-shell">
    <section class="page-banner">
      <div class="page-eyebrow">Settings</div>
      <h1 class="page-title">系统设置与答辩配置</h1>
      <p class="page-subtitle">这里不只放基础设置，也帮你把答辩时常用的项目说明、推荐非遗项目和管理员信息整合到一起。</p>
    </section>

    <el-row :gutter="18">
      <el-col :xs="24" :lg="16">
        <el-card class="panel-card">
          <template #header>
            <div class="panel-header">
              <div class="panel-header-main">
                <h3 class="panel-title">平台基础信息</h3>
                <p class="panel-note">当前页面使用本地持久化保存设置，方便你在答辩前快速调整文案和视觉内容。</p>
              </div>
            </div>
          </template>

          <el-form label-position="top">
            <div class="dialog-grid">
              <el-form-item label="系统名称">
                <el-input v-model="basicForm.systemName" placeholder="请输入系统名称" />
              </el-form-item>
              <el-form-item label="版权信息">
                <el-input v-model="basicForm.copyright" placeholder="请输入版权信息" />
              </el-form-item>

              <el-form-item label="系统标识" class="span-2">
                <div style="display: flex; gap: 16px; flex-wrap: wrap;">
                  <el-upload :show-file-list="false" action="#" :before-upload="handleLogoUpload">
                    <div class="uploader-box">
                      <img v-if="basicForm.logo" :src="basicForm.logo" alt="logo" class="preview-image" />
                      <div v-else class="empty-copy">点击上传</div>
                    </div>
                  </el-upload>
                  <el-input v-model="basicForm.logo" placeholder="或直接粘贴 Logo 链接" />
                </div>
              </el-form-item>

              <el-form-item label="系统描述" class="span-2">
                <el-input v-model="basicForm.description" type="textarea" :rows="5" placeholder="请输入系统描述" />
              </el-form-item>

              <el-form-item label="答辩亮点" class="span-2">
                <el-input v-model="basicForm.highlight" type="textarea" :rows="4" placeholder="例如：内容展示、活动报名、商城订单、社区互动三端联动" />
              </el-form-item>
            </div>

            <div style="display: flex; justify-content: flex-end; margin-top: 12px;">
              <el-button type="primary" @click="saveBasicSettings">保存基础信息</el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card class="panel-card">
          <template #header>
            <div class="panel-header">
              <div class="panel-header-main">
                <h3 class="panel-title">管理员信息</h3>
                <p class="panel-note">读取当前登录管理员资料，便于答辩时快速确认后台账号。</p>
              </div>
            </div>
          </template>

          <div class="soft-block" style="display: flex; gap: 14px; align-items: center;">
            <el-avatar :size="56" :src="adminInfo.avatar">
              {{ (adminInfo.realName || adminInfo.username || '管').slice(0, 1) }}
            </el-avatar>
            <div>
              <div style="font-size: 18px; font-weight: 700;">{{ adminInfo.realName || '管理员' }}</div>
              <div class="empty-copy">@{{ adminInfo.username || 'admin' }}</div>
            </div>
          </div>

          <el-descriptions :column="1" border style="margin-top: 18px;">
            <el-descriptions-item label="系统名称">{{ basicForm.systemName }}</el-descriptions-item>
            <el-descriptions-item label="当前环境">{{ apiBase }}</el-descriptions-item>
            <el-descriptions-item label="推荐答辩操作">先演示首页，再演示活动报名和订单导出</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="18">
      <el-col :xs="24" :lg="14">
        <el-card class="panel-card">
          <template #header>
            <div class="panel-header">
              <div class="panel-header-main">
                <h3 class="panel-title">推荐非遗项目</h3>
                <p class="panel-note">这部分适合放答辩时重点介绍的典型案例。</p>
              </div>
              <el-button type="primary" plain @click="openHeritageEditor()">添加推荐项</el-button>
            </div>
          </template>

          <el-table :data="heritageList" stripe>
            <el-table-column prop="name" label="项目名称" min-width="160" />
            <el-table-column prop="category" label="分类" min-width="120" />
            <el-table-column prop="sort" label="排序" width="90" />
            <el-table-column prop="highlight" label="展示亮点" min-width="220" show-overflow-tooltip />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row, $index }">
                <el-button link type="primary" @click="openHeritageEditor(row, $index)">编辑</el-button>
                <el-button link type="danger" @click="removeHeritage($index)">删除</el-button>
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
                <h3 class="panel-title">账号安全提示</h3>
                <p class="panel-note">这里做轻量的前端表单校验，便于展示“系统设置”模块的完整性。</p>
              </div>
            </div>
          </template>

          <el-form label-position="top">
            <el-form-item label="当前账号">
              <el-input :model-value="adminInfo.username || 'admin'" disabled />
            </el-form-item>
            <el-form-item label="原密码">
              <el-input v-model="securityForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="securityForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="securityForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>

            <div style="display: flex; justify-content: flex-end;">
              <el-button type="primary" @click="updatePasswordDemo">提交修改</el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="heritageDialogVisible" :title="heritageEditIndex === null ? '新增推荐项目' : '编辑推荐项目'" width="720px">
      <el-form label-position="top">
        <div class="dialog-grid">
          <el-form-item label="项目名称">
            <el-input v-model="heritageForm.name" placeholder="请输入项目名称" />
          </el-form-item>
          <el-form-item label="项目分类">
            <el-input v-model="heritageForm.category" placeholder="请输入项目分类" />
          </el-form-item>
          <el-form-item label="展示排序">
            <el-input-number v-model="heritageForm.sort" :min="1" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="答辩亮点" class="span-2">
            <el-input v-model="heritageForm.highlight" type="textarea" :rows="4" placeholder="请输入项目亮点，例如工艺特色、传播价值、活动结合点" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="heritageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveHeritage">保存项目</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminInfo } from '../../api/modules/login'
import { readImageAsDataUrl } from '../../utils/console'

const SETTINGS_KEY = 'heritage-console-settings'
const HERITAGE_KEY = 'heritage-console-topics'
const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const basicForm = reactive({
  systemName: '非遗文化互动平台',
  logo: '',
  copyright: '© 2026 西南石油大学',
  description: '基于 Spring Boot、Vue 与 uni-app 的非遗文化互动平台，覆盖内容展示、活动报名、文创商城与后台管理。',
  highlight: '三端协同、活动闭环、订单导出、非遗文化数字化展示'
})

const securityForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const adminInfo = reactive({
  username: '',
  realName: '',
  avatar: ''
})

const heritageList = ref([])
const heritageDialogVisible = ref(false)
const heritageEditIndex = ref(null)
const heritageForm = reactive({
  name: '',
  category: '',
  sort: 1,
  highlight: ''
})

const loadLocalSettings = () => {
  const savedSettings = localStorage.getItem(SETTINGS_KEY)
  if (savedSettings) {
    Object.assign(basicForm, JSON.parse(savedSettings))
  }

  const savedHeritage = localStorage.getItem(HERITAGE_KEY)
  if (savedHeritage) {
    heritageList.value = JSON.parse(savedHeritage)
    return
  }

  heritageList.value = [
    { name: '蜀绣', category: '传统美术', sort: 1, highlight: '适合展示非遗工艺细节与文创延展能力' },
    { name: '川剧变脸', category: '传统戏剧', sort: 2, highlight: '适合结合演出模块展示文化活动场景' },
    { name: '竹编技艺', category: '传统技艺', sort: 3, highlight: '适合展示商品转化与工艺传承结合' }
  ]
}

const loadAdminInfo = async () => {
  try {
    Object.assign(adminInfo, await getAdminInfo())
  } catch {
    adminInfo.username = 'admin'
    adminInfo.realName = '管理员'
  }
}

const handleLogoUpload = async (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (file.size / 1024 / 1024 >= 5) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  basicForm.logo = await readImageAsDataUrl(file)
  return false
}

const saveBasicSettings = () => {
  localStorage.setItem(SETTINGS_KEY, JSON.stringify({ ...basicForm }))
  ElMessage.success('基础设置已保存')
}

const resetHeritageForm = () => {
  heritageEditIndex.value = null
  Object.assign(heritageForm, {
    name: '',
    category: '',
    sort: heritageList.value.length + 1,
    highlight: ''
  })
}

const openHeritageEditor = (row = null, index = null) => {
  if (!row) {
    resetHeritageForm()
  } else {
    heritageEditIndex.value = index
    Object.assign(heritageForm, { ...row })
  }
  heritageDialogVisible.value = true
}

const saveHeritage = () => {
  if (!heritageForm.name || !heritageForm.category) {
    ElMessage.warning('请先填写项目名称和分类')
    return
  }

  const payload = { ...heritageForm }
  if (heritageEditIndex.value === null) {
    heritageList.value.push(payload)
  } else {
    heritageList.value.splice(heritageEditIndex.value, 1, payload)
  }
  heritageList.value = [...heritageList.value].sort((a, b) => a.sort - b.sort)
  localStorage.setItem(HERITAGE_KEY, JSON.stringify(heritageList.value))
  heritageDialogVisible.value = false
  ElMessage.success('推荐项目已保存')
}

const removeHeritage = (index) => {
  heritageList.value.splice(index, 1)
  localStorage.setItem(HERITAGE_KEY, JSON.stringify(heritageList.value))
  ElMessage.success('推荐项目已删除')
}

const updatePasswordDemo = () => {
  if (!securityForm.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!securityForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (securityForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度至少 6 位')
    return
  }
  if (securityForm.newPassword !== securityForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  securityForm.oldPassword = ''
  securityForm.newPassword = ''
  securityForm.confirmPassword = ''
  ElMessage.success('前端演示用密码修改校验已通过')
}

onMounted(() => {
  loadLocalSettings()
  loadAdminInfo()
})
</script>
