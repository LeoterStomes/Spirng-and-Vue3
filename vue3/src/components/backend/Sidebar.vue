<template>
  <div class="sidebar-container" :class="{ 'is-collapsed': isCollapsed }">
    <!-- 品牌Logo区域 -->
    <div class="brand-section">
      <div class="brand-logo">
        <i class="fas fa-robot brand-icon"></i>
        <div class="brand-info" v-show="!isCollapsed">
          <h1 class="brand-title">心理健康AI助手</h1>
          <p class="brand-subtitle">管理后台</p>
        </div>
      </div>
    </div>

    <!-- 管理员信息卡片 -->
    <div class="admin-card" v-show="!isCollapsed">
      <div class="admin-avatar">
        <el-avatar :size="48" :src="adminAvatar">
          {{ adminInfo?.name?.charAt(0)?.toUpperCase() || 'A' }}
        </el-avatar>
      </div>
      <div class="admin-info">
        <h3 class="admin-name">{{ adminInfo?.name || '管理员' }}</h3>
        <p class="admin-role">系统管理员</p>
      </div>
    </div>

    <!-- 导航菜单 -->
    <div class="menu-wrapper">
      <nav class="sidebar-nav">
        <router-link to="/back/dashboard" class="nav-item" :class="{ active: activeMenu === '/back/dashboard' }">
          <i class="fas fa-chart-bar nav-icon"></i>
          <span class="nav-text" v-show="!isCollapsed">数据分析</span>
        </router-link>
        
        <router-link to="/back/user" class="nav-item" :class="{ active: activeMenu === '/back/user' }">
          <i class="fas fa-users nav-icon"></i>
          <span class="nav-text" v-show="!isCollapsed">用户管理</span>
        </router-link>
        
        <router-link to="/back/knowledge" class="nav-item" :class="{ active: activeMenu === '/back/knowledge' }">
          <i class="fas fa-book nav-icon"></i>
          <span class="nav-text" v-show="!isCollapsed">知识文章</span>
        </router-link>
        
        <router-link to="/back/knowledge-category" class="nav-item" :class="{ active: activeMenu === '/back/knowledge-category' }">
          <i class="fas fa-folder nav-icon"></i>
          <span class="nav-text" v-show="!isCollapsed">分类管理</span>
        </router-link>
        
        <router-link to="/back/consultations" class="nav-item" :class="{ active: activeMenu === '/back/consultations' }">
          <i class="fas fa-comments nav-icon"></i>
          <span class="nav-text" v-show="!isCollapsed">咨询记录</span>
        </router-link>
        
        <router-link to="/back/emotion-diary" class="nav-item" :class="{ active: activeMenu === '/back/emotion-diary' }">
          <i class="fas fa-heart nav-icon"></i>
          <span class="nav-text" v-show="!isCollapsed">情绪日记</span>
        </router-link>
        
        <router-link to="/back/ai-analysis-queue" class="nav-item" :class="{ active: activeMenu === '/back/ai-analysis-queue' }">
          <i class="fas fa-tasks nav-icon"></i>
          <span class="nav-text" v-show="!isCollapsed">AI分析队列</span>
        </router-link>
        
        <router-link to="/back/profile" class="nav-item" :class="{ active: activeMenu === '/back/profile' }">
          <i class="fas fa-user-cog nav-icon"></i>
          <span class="nav-text" v-show="!isCollapsed">个人信息</span>
        </router-link>
      </nav>
    </div>

    <!-- 底部退出登录 -->
    <div class="logout-section">
      <button @click="handleLogout" class="logout-btn">
        <i class="fas fa-sign-out-alt logout-icon"></i>
        <span class="logout-text" v-show="!isCollapsed">退出登录</span>
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

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 管理员信息
const adminInfo = computed(() => userStore.userInfo)

// 管理员头像
const adminAvatar = computed(() => {
  // 后端返回的avatar路径已经包含/files/前缀，直接使用即可
  return adminInfo.value?.avatar || ''
})

// 退出登录处理
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
/* 导入Font Awesome图标库样式 */


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
    
    .admin-card {
      display: none;
    }
  }
  
  /* 品牌区域 */
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
    color: #4A90E2;
    margin-right: 0.75rem;
    transition: margin 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .brand-info {
    flex: 1;
  }

  .brand-title {
    font-size: 1.125rem;
    font-weight: 700;
    color: #1f2937;
    margin: 0 0 0.25rem 0;
    line-height: 1.2;
  }

  .brand-subtitle {
    font-size: 0.875rem;
    color: #6b7280;
    margin: 0;
  }

  /* 管理员信息卡片 */
  .admin-card {
    background: #f8fafc;
    border: 1px solid #e5e7eb;
    border-radius: 0.75rem;
    padding: 1rem;
    margin: 0 1.5rem 1.5rem;
    display: flex;
    align-items: center;
    gap: 0.75rem;
    transition: all 0.3s ease;

    &:hover {
      background: #f1f5f9;
    }
  }

  .admin-avatar {
    flex-shrink: 0;
  }

  .admin-info {
    flex: 1;
    min-width: 0;
  }

  .admin-name {
    font-size: 0.875rem;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 0.25rem 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .admin-role {
    font-size: 0.75rem;
    color: #6b7280;
    margin: 0;
  }

  /* 导航菜单区域 */
  .menu-wrapper {
    flex: 1;
    padding: 0 0.5rem;
    overflow-y: auto;
    overflow-x: hidden;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, 0.2);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
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
    position: relative;

    &:hover {
      background: #f3f4f6;
      color: #374151;
      transform: translateX(5px);
    }

    &.active {
      background: #4A90E2;
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

  /* 退出登录区域 */
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

  .logout-text {
    white-space: nowrap;
  }

  /* 折叠状态样式调整 */
  &.is-collapsed {
    .brand-section {
      padding: 1rem;
      text-align: center;
    }

    .brand-icon {
      margin: 0;
    }

    .nav-item {
      justify-content: center;
      padding: 0.75rem;

      &:hover {
        transform: none;
      }
    }

    .nav-icon {
      margin: 0;
    }

    .logout-section {
      padding: 1rem;
    }

    .logout-btn {
      justify-content: center;
      padding: 0.75rem;
    }

    .logout-icon {
      margin: 0;
    }
  }

  /* 响应式设计 */
  @media (max-width: 768px) {
    width: 100%;
    max-width: 264px;
    position: fixed;
    left: 0;
    top: 0;
    z-index: 1000;
    transform: translateX(-100%);
    
    &.is-open {
      transform: translateX(0);
    }
  }
}
</style> 