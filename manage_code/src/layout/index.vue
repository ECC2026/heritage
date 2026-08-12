<template>
  <div class="shell">
    <aside :class="['sidebar', { collapsed }]">
      <div class="brand">
        <div class="brand-mark">遗</div>
        <div v-if="!collapsed" class="brand-copy">
          <strong>非遗文化后台</strong>
          <span>Heritage Console</span>
        </div>
      </div>

      <div class="nav-groups">
        <div v-for="group in menuGroups" :key="group.label" class="nav-group">
          <p v-if="!collapsed" class="nav-group-label">{{ group.label }}</p>
          <el-menu
            :default-active="activeMenu"
            :collapse="collapsed"
            :collapse-transition="false"
            class="heritage-menu"
            router
          >
            <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>{{ item.label }}</template>
            </el-menu-item>
          </el-menu>
        </div>
      </div>
    </aside>

    <div class="workspace">
      <header class="topbar">
        <div class="topbar-left">
          <button class="collapse-toggle" type="button" @click="collapsed = !collapsed">
            <el-icon><component :is="collapsed ? Expand : Fold" /></el-icon>
          </button>
          <div>
            <p class="topbar-caption">非遗文化互动平台管理系统</p>
            <h2 class="topbar-title">{{ route.meta?.title || '总览看板' }}</h2>
          </div>
        </div>

        <div class="topbar-right">
          <div class="topbar-chip">
            <span>服务地址</span>
            <strong>{{ apiBase }}</strong>
          </div>
          <el-dropdown @command="handleCommand">
            <div class="profile">
              <el-avatar :size="40">{{ avatarLetter }}</el-avatar>
              <div class="profile-copy">
                <strong>{{ userStore.userInfo?.realName || userStore.userInfo?.username || '管理员' }}</strong>
                <span>内容与运营后台</span>
              </div>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <div class="content-frame">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import {
  ArrowDown,
  Expand,
  Fold,
  Calendar,
  Collection,
  Goods,
  HomeFilled,
  Picture,
  Setting,
  Tickets,
  User,
  UserFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(false)
const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const menuGroups = [
  {
    label: '总览',
    items: [{ path: '/home', label: '总览看板', icon: HomeFilled }]
  },
  {
    label: '文化内容',
    items: [
      { path: '/news', label: '非遗资讯', icon: Collection },
      { path: '/performances', label: '非遗演出', icon: Tickets },
      { path: '/banners', label: '轮播图管理', icon: Picture },
      { path: '/product-systems', label: '产品体系', icon: Collection },
      { path: '/services', label: '服务管理', icon: Tickets },
      { path: '/cooperations', label: '合作申请', icon: Collection }
    ]
  },
  {
    label: '用户与互动',
    items: [
      { path: '/users', label: '用户管理', icon: User },
      { path: '/inheritants', label: '传承人管理', icon: UserFilled },
      { path: '/activities', label: '活动管理', icon: Calendar },
      { path: '/signups', label: '报名审核', icon: Calendar }
    ]
  },
  {
    label: '交易运营',
    items: [
      { path: '/products', label: '文创商品', icon: Goods },
      { path: '/orders', label: '订单管理', icon: Goods },
      { path: '/settings', label: '系统设置', icon: Setting }
    ]
  }
]

const activeMenu = computed(() => route.path)
const avatarLetter = computed(() => {
  const raw = userStore.userInfo?.realName || userStore.userInfo?.username || '管'
  return raw.slice(0, 1)
})

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
    return
  }
  if (command === 'settings') {
    router.push('/settings')
  }
}
</script>

<style scoped>
.shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: auto 1fr;
}

.sidebar {
  width: 292px;
  padding: 18px 16px;
  background:
    linear-gradient(180deg, rgba(52, 37, 31, 0.96), rgba(72, 50, 41, 0.96)),
    linear-gradient(180deg, rgba(217, 164, 65, 0.1), transparent);
  color: #f8f1e8;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  transition: width 0.25s ease;
}

.sidebar.collapsed {
  width: 94px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  margin-bottom: 14px;
}

.brand-mark {
  width: 46px;
  height: 46px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(217, 164, 65, 0.94), rgba(177, 77, 45, 0.88));
  color: #fff8ef;
  font-family: var(--font-display);
  font-size: 24px;
  box-shadow: 0 14px 24px rgba(0, 0, 0, 0.18);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.brand-copy strong {
  font-size: 16px;
}

.brand-copy span {
  color: rgba(248, 241, 232, 0.62);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.nav-groups {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.nav-group {
  padding: 8px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.04);
}

.nav-group-label {
  margin: 6px 10px 12px;
  color: rgba(248, 241, 232, 0.48);
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

:deep(.heritage-menu) {
  border-right: none;
  background: transparent;
}

:deep(.heritage-menu .el-menu-item) {
  margin-bottom: 6px;
  border-radius: 14px;
  color: rgba(248, 241, 232, 0.82);
}

:deep(.heritage-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fffaf3;
}

:deep(.heritage-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(217, 164, 65, 0.2), rgba(177, 77, 45, 0.28));
  color: #fff;
}

.workspace {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 26px;
}

.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.topbar-right {
  justify-content: flex-end;
}

.collapse-toggle {
  width: 46px;
  height: 46px;
  border: none;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
  color: var(--primary-deep);
  box-shadow: var(--shadow);
  cursor: pointer;
}

.topbar-caption {
  margin: 0 0 4px;
  color: var(--text-soft);
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.topbar-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
}

.topbar-chip {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.7);
  color: var(--text-soft);
  font-size: 12px;
}

.topbar-chip strong {
  color: var(--text);
  font-size: 13px;
}

.profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: var(--shadow);
  cursor: pointer;
}

.profile-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-copy strong {
  font-size: 14px;
}

.profile-copy span {
  color: var(--text-soft);
  font-size: 12px;
}

.content {
  padding: 0 26px 26px;
}

.content-frame {
  min-height: calc(100vh - 110px);
}

@media (max-width: 1180px) {
  .shell {
    grid-template-columns: 1fr;
  }

  .sidebar,
  .sidebar.collapsed {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .topbar,
  .topbar-left,
  .topbar-right {
    flex-direction: column;
    align-items: stretch;
  }

  .content,
  .topbar {
    padding-left: 18px;
    padding-right: 18px;
  }
}
</style>
