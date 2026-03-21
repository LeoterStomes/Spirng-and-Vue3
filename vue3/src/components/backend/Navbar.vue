<template>
  <div class="navbar">
    <!-- 左侧区域 -->
    <div class="navbar-left">
      <!-- 侧边栏切换按钮 -->
      <button class="sidebar-toggle" @click="toggleSidebar">
        <i :class="appStore.sidebarCollapsed ? 'fas fa-expand' : 'fas fa-compress'"></i>
      </button>
      
      <!-- 页面标题区域 -->
      <div class="page-header">
        <h2 class="page-title">{{ pageTitle }}</h2>
      
      </div>
    </div>
    
    <!-- 右侧工具区域 -->
    <div class="navbar-right">
      <!-- 全屏切换按钮 -->
      <div class="toolbar-item" @click="toggleFullScreen">
        <i :class="isFullscreen ? 'fas fa-compress' : 'fas fa-expand'" class="toolbar-icon"></i>
      </div>
      
      <!-- 时间显示 -->
      <div class="time-display">
        <div class="current-time">{{ currentTime }}</div>
      </div>
      
      <!-- 用户信息下拉菜单 -->
      <el-dropdown trigger="click" @command="handleDropdownCommand">
        <div class="user-dropdown">
          <el-avatar :size="32" :src="avatarUrl" class="user-avatar">
            {{ userInfo?.name?.charAt(0)?.toUpperCase() || userInfo?.username?.charAt(0)?.toUpperCase() || 'A' }}
          </el-avatar>
          <span class="user-name">{{ userInfo?.name || userInfo?.username || '管理员' }}</span>
          <i class="fas fa-chevron-down dropdown-arrow"></i>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <i class="fas fa-user dropdown-icon"></i>
              个人资料
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <i class="fas fa-sign-out-alt dropdown-icon"></i>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

// 响应式数据
const isFullscreen = ref(false)
const currentTime = ref('')

// 计算属性
const userInfo = computed(() => userStore.userInfo)

const avatarUrl = computed(() => {
  // 后端返回的avatar路径已经包含/files/前缀，直接使用即可
  return userInfo.value?.avatar || ''
})

// 页面标题和副标题
const pageTitle = computed(() => {
  const titleMap = {
    '/back/dashboard': '管理仪表盘',
    '/back/user': '用户管理',
    '/back/knowledge': '知识文章',
    '/back/consultations': '咨询记录',
    '/back/emotion-diary': '情绪日记',
    '/back/crisis': '危机预警',
    '/back/analytics': '数据分析',
    '/back/settings': '系统设置'
  }
  return titleMap[route.path] || route.meta?.title || '管理后台'
})



// 方法定义
const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
      isFullscreen.value = false
    }
  }
}


// 下拉菜单命令处理
const handleDropdownCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/back/profile')
      break
    case 'logout':
      handleLogout()
      break
  }
}

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

// 更新当前时间
const updateTime = () => {
  const now = new Date()
  const options = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }
  currentTime.value = now.toLocaleString('zh-CN', options)
}

// 监听全屏状态变化
const fullscreenChangeHandler = () => {
  isFullscreen.value = !!document.fullscreenElement
}

// 生命周期
onMounted(() => {
  updateTime()
  // 每秒更新时间
  const timeInterval = setInterval(updateTime, 1000)
  
  // 添加全屏状态监听
  document.addEventListener('fullscreenchange', fullscreenChangeHandler)
  
  // 组件卸载时清理
  onUnmounted(() => {
    clearInterval(timeInterval)
    document.removeEventListener('fullscreenchange', fullscreenChangeHandler)
  })
})
</script>

<style lang="scss" scoped>
/* 导入Font Awesome图标库样式 */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.navbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  border-bottom: 1px solid #e5e7eb;
  z-index: 10;
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;

  /* 左侧区域 */
  .navbar-left {
    display: flex;
    align-items: center;
    gap: 1rem;
    flex: 1;
  }

  .sidebar-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 2rem;
    height: 2rem;
    background: transparent;
    border: none;
    border-radius: 0.25rem;
    color: #6b7280;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      background: #f3f4f6;
      color: #374151;
    }

    i {
      font-size: 1rem;
    }
  }

  .page-header {
    flex: 1;
  }

  .page-title {
    font-size: 1.5rem;
    font-weight: 700;
    color: #1f2937;
    margin: 0 0 0.25rem 0;
    line-height: 1.2;
  }



  /* 右侧工具区域 */
  .navbar-right {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .toolbar-item {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 2rem;
    height: 2rem;
    border-radius: 0.25rem;
    color: #6b7280;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      background: #f3f4f6;
      color: #374151;
    }
  }

  .toolbar-icon {
    font-size: 1.125rem;
  }

  .notification-badge {
    position: absolute;
    top: -0.25rem;
    right: -0.25rem;
    min-width: 1rem;
    height: 1rem;
    background: #ef4444;
    color: white;
    font-size: 0.6875rem;
    font-weight: 600;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 0.125rem;
    line-height: 1;
  }

  /* 时间显示 */
  .time-display {
    padding: 0 0.75rem;
    margin-right: 0.5rem;
  }

  .current-time {
    font-size: 0.875rem;
    color: #6b7280;
    font-family: 'Courier New', monospace;
    white-space: nowrap;
  }

  /* 用户下拉菜单 */
  .user-dropdown {
    display: flex;
    align-items: center;
    padding: 0.25rem 0.5rem;
    border-radius: 0.375rem;
    cursor: pointer;
    transition: all 0.3s ease;
    gap: 0.5rem;
    min-width: 0;

    &:hover {
      background: #f3f4f6;
    }
  }

  .user-avatar {
    flex-shrink: 0;
  }

  .user-name {
    font-size: 0.875rem;
    color: #374151;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 120px;
  }

  .dropdown-arrow {
    color: #9ca3af;
    font-size: 0.75rem;
    flex-shrink: 0;
    transition: transform 0.3s ease;
  }

  /* Element Plus 下拉菜单样式覆盖 */
  :deep(.el-dropdown-menu) {
    border-radius: 0.5rem;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
    border: 1px solid #e5e7eb;
    padding: 0.5rem 0;
  }

  :deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1rem;
    font-size: 0.875rem;
    color: #374151;
    transition: all 0.2s ease;
    
    &:hover {
      background: #f3f4f6;
      color: #1f2937;
    }

    &.is-divided {
      border-top: 1px solid #e5e7eb;
      margin-top: 0.25rem;
      padding-top: 1rem;
    }
  }

  .dropdown-icon {
    width: 1rem;
    text-align: center;
    color: #6b7280;
    flex-shrink: 0;
  }

  /* 响应式设计 */
  @media (max-width: 768px) {
    padding: 0 1rem;

    .page-title {
      font-size: 1.25rem;
    }

    .page-subtitle {
      display: none;
    }

    .user-name {
      display: none;
    }

    .time-display {
      display: none;
    }

    .navbar-right {
      gap: 0.25rem;
    }
  }

  @media (max-width: 480px) {
    .page-title {
      font-size: 1.125rem;
    }

    .toolbar-item:not(:last-child) {
      display: none;
    }
  }
}
</style> 