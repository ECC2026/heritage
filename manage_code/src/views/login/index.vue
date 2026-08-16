<template>
  <div class="login-wallpaper">
    <section class="login-brand">
      <div>
        <div class="login-badge">Cultural Heritage Console</div>
        <h1>非遗文化互动平台后台</h1>
        <p>
          面向非遗资讯、传承人、活动、商城与用户运营的一体化管理系统。
          我们把日常管理收进统一视图，让后台既能演示，也能真正跑业务。
        </p>
      </div>

      <div class="login-highlights">
        <div class="login-highlight">
          <strong>内容运营</strong>
          <p>资讯、轮播、演出统一维护</p>
        </div>
        <div class="login-highlight">
          <strong>活动闭环</strong>
          <p>活动创建、报名审核、数据导出</p>
        </div>
        <div class="login-highlight">
          <strong>交易管理</strong>
          <p>商品上架、订单状态、库存追踪</p>
        </div>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <div class="page-eyebrow">管理员登录</div>
        <h2 class="page-title" style="margin-top: 18px;">欢迎回来</h2>
        <p class="page-subtitle">使用管理员账号进入后台，开始管理非遗文化内容与运营数据。</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          style="margin-top: 24px;"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" size="large" placeholder="请输入管理员用户名" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入登录密码" />
          </el-form-item>

          <div style="display: flex; gap: 12px; margin-top: 12px;">
            <el-button size="large" style="flex: 1;" @click="fillDemo">填入示例账号</el-button>
            <el-button type="primary" size="large" style="flex: 1;" :loading="loading" @click="handleLogin">
              进入后台
            </el-button>
          </div>
        </el-form>

        <div style="margin-top: 18px; padding: 14px 16px; border-radius: 18px; background: rgba(177, 77, 45, 0.05); color: var(--text-soft); font-size: 13px; line-height: 1.7;">
          默认示例账号通常为 `admin / 123456`。如果你已经导入最新 SQL 示例数据，可以直接登录验证。
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '../../api/modules/login'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const fillDemo = () => {
  form.username = 'admin'
  form.password = '123456'
}

const handleLogin = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await adminLogin(form)
    console.log('登录成功，返回数据：', res)
    userStore.setToken(res.token)
    userStore.setUserInfo(res.admin)
    ElMessage.success('登录成功')
    router.push('/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-highlight strong {
  display: block;
  margin-bottom: 8px;
  font-size: 15px;
}

.login-highlight p {
  margin: 0;
  color: rgba(248, 239, 226, 0.74);
  font-size: 13px;
  line-height: 1.6;
}
</style>
