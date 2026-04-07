<template>
  <div
    class="sidebar-container"
    :class="{ 'is-collapsed': isCollapsed }"
  >
    <div class="brand-section">
      <div class="brand-logo">
        <i class="fas fa-user-md brand-icon" />
        <div
          v-show="!isCollapsed"
          class="brand-info"
        >
          <h1 class="brand-title">
            心理健康AI助手
          </h1>
          <p class="brand-subtitle">
            医生工作台
          </p>
        </div>
      </div>
    </div>

    <div
      v-show="!isCollapsed"
      class="admin-card"
    >
      <div class="admin-avatar">
        <el-avatar
          :size="48"
          :src="doctorAvatar"
        >
          {{ doctorInfo?.nickname?.charAt(0) || doctorInfo?.username?.charAt(0)?.toUpperCase() || 'D' }}
        </el-avatar>
      </div>
      <div class="admin-info">
        <h3 class="admin-name">
          {{ doctorInfo?.nickname || doctorInfo?.username || '医生' }}
        </h3>
        <p class="admin-role">
          <el-tag
            size="small"
            type="success"
            effect="plain"
          >
            医生
          </el-tag>
        </p>
      </div>
    </div>

    <div class="menu-wrapper">
      <nav class="sidebar-nav">
        <router-link
          to="/doctor/dashboard"
          class="nav-item"
          :class="{ active: activeMenu === '/doctor/dashboard' }"
        >
          <i class="fas fa-tachometer-alt nav-icon" />
          <span
            v-show="!isCollapsed"
            class="nav-text"
          >工作台总览</span>
        </router-link>

        <router-link
          to="/doctor/patients"
          class="nav-item"
          :class="{ active: activeMenu === '/doctor/patients' }"
        >
          <i class="fas fa-hospital-user nav-icon" />
          <span
            v-show="!isCollapsed"
            class="nav-text"
          >我的患者</span>
        </router-link>

        <router-link
          to="/doctor/profile"
          class="nav-item"
          :class="{ active: activeMenu === '/doctor/profile' }"
        >
          <i class="fas fa-user-cog nav-icon" />
          <span
            v-show="!isCollapsed"
            class="nav-text"
          >个人信息</span>
        </router-link>
      </nav>
    </div>

    <div class="logout-section">
      <button
        class="logout-btn"
        @click="handleLogout"
      >
        <i class="fas fa-sign-out-alt logout-icon" />
        <span
          v-show="!isCollapsed"
          class="logout-text"
        >退出登录</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) return meta.activeMenu
  return path
})

const doctorInfo = computed(() => userStore.userInfo)
const doctorAvatar = computed(() => doctorInfo.value?.avatar || '')

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await userStore.logout()
    router.push('/auth/login')
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  min-height: 100vh;
  background: white;
  display: flex;
  flex-direction: column;
  width: 264px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;

  &.is-collapsed {
    width: 64px;
    .admin-card { display: none; }
  }

  .brand-section {
    padding: 1.5rem;
    border-bottom: 1px solid #e5e7eb;
  }
  .brand-logo {
    display: flex;
    align-items: center;
  }
  .brand-icon {
    font-size: 1.875rem;
    color: #10b981;
    margin-right: 0.75rem;
    transition: margin 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }
  .brand-info { flex: 1; }
  .brand-title {
    font-size: 1.125rem;
    font-weight: 700;
    color: #1f2937;
    margin: 0 0 0.25rem 0;
    line-height: 1.2;
  }
  .brand-subtitle {
    font-size: 0.875rem;
    color: #10b981;
    margin: 0;
    font-weight: 500;
  }

  .admin-card {
    background: #f0fdf4;
    border: 1px solid #bbf7d0;
    border-radius: 0.75rem;
    padding: 1rem;
    margin: 1rem 1.5rem 1.5rem;
    display: flex;
    align-items: center;
    gap: 0.75rem;
    transition: all 0.3s ease;
    &:hover { background: #dcfce7; }
  }
  .admin-avatar { flex-shrink: 0; }
  .admin-info { flex: 1; min-width: 0; }
  .admin-name {
    font-size: 0.875rem;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 0.25rem 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .admin-role { margin: 0; }

  .menu-wrapper {
    flex: 1;
    padding: 0 0.5rem;
    overflow-y: auto;
    overflow-x: hidden;
    &::-webkit-scrollbar { width: 6px; }
    &::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.2); border-radius: 3px; }
    &::-webkit-scrollbar-track { background: transparent; }
  }
  .sidebar-nav {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  .nav-item {
    display: flex;
    align-items: center;
    padding: 0.75rem 1rem;
    border-radius: 0.5rem;
    color: #6b7280;
    text-decoration: none;
    transition: all 0.3s ease;
    &:hover {
      background: #f0fdf4;
      color: #374151;
      transform: translateX(5px);
    }
    &.active {
      background: #10b981;
      color: white;
    }
  }
  .nav-icon {
    width: 1.25rem;
    margin-right: 0.75rem;
    text-align: center;
    font-size: 1rem;
    flex-shrink: 0;
  }
  .nav-text {
    font-size: 0.875rem;
    font-weight: 500;
    white-space: nowrap;
    flex: 1;
  }

  .logout-section {
    padding: 1.5rem;
    border-top: 1px solid #e5e7eb;
    margin-top: auto;
  }
  .logout-btn {
    width: 100%;
    display: flex;
    align-items: center;
    padding: 0.75rem 1rem;
    background: #f8fafc;
    border: 1px solid #e5e7eb;
    border-radius: 0.5rem;
    color: #6b7280;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 0.875rem;
    font-weight: 500;
    &:hover {
      background: #ef4444;
      color: white;
      border-color: #ef4444;
      transform: translateY(-2px);
    }
  }
  .logout-icon {
    width: 1.25rem;
    margin-right: 0.75rem;
    text-align: center;
    flex-shrink: 0;
  }
  .logout-text { white-space: nowrap; }

  &.is-collapsed {
    .brand-section { padding: 1rem; text-align: center; }
    .brand-icon { margin: 0; }
    .nav-item { justify-content: center; padding: 0.75rem; &:hover { transform: none; } }
    .nav-icon { margin: 0; }
    .logout-section { padding: 1rem; }
    .logout-btn { justify-content: center; padding: 0.75rem; }
    .logout-icon { margin: 0; }
  }
}
</style>
