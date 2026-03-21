<template>
    <div class="frontend-layout">
      <!-- 顶部导航栏 -->
      <nav class="navbar">
        <div class="navbar-container">
          <div class="navbar-content">
            <!-- 左侧品牌区域 -->
            <div class="brand-section">
              <div class="brand-logo">
                <i class="fas fa-robot brand-icon"></i>
                <h1 class="brand-title">心理健康AI助手</h1>
              </div>
            </div>
            
            <!-- 右侧导航菜单 -->
            <div class="nav-section">
              <div class="nav-links">
                <router-link to="/" class="nav-link">首页</router-link>
                <router-link to="/consultation" class="nav-link" v-if="isLoggedIn">AI咨询</router-link>
                <router-link to="/psychological-test" class="nav-link" v-if="isLoggedIn">心理测试</router-link>
                <router-link to="/emotion-diary" class="nav-link" v-if="isLoggedIn">情绪日记</router-link>
                <router-link to="/favorites" class="nav-link" v-if="isLoggedIn">我的收藏</router-link>
                <router-link to="/knowledge" class="nav-link">知识库</router-link>
              </div>
              
              <!-- 认证区域 -->
              <div class="auth-section">
                <template v-if="isLoggedIn">
                  <router-link to="/profile" class="profile-link">个人中心</router-link>
                  <button @click="handleLogout" class="logout-btn">退出登录</button>
                </template>
                <template v-else>
                  <router-link to="/auth/login" class="login-link">登录</router-link>
                  <router-link to="/auth/register" class="register-btn">注册</router-link>
                </template>
              </div>
            </div>
          </div>
        </div>
      </nav>

      <!-- 主要内容区域 -->
      <main class="main-content">
        <router-view />
      </main>

      <!-- 页脚 -->
      <footer class="footer">
        <div class="footer-container">
          <div class="footer-bottom">
            <p>&copy; 2025 心理健康AI助手. 保留所有权利.</p>
          </div>
        </div>
      </footer>
    </div>
  </template>
  
  <script setup>
  import { computed } from 'vue'
  import { useUserStore } from '@/store/user'
  import { useRouter } from 'vue-router'
  import { ElMessageBox } from 'element-plus'
  
  const userStore = useUserStore()
  const router = useRouter()
  
  const isLoggedIn = computed(() => !!userStore.token)
  
  const handleLogout = () => {
    ElMessageBox.confirm('确定要退出登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await userStore.logout()
        router.push('/auth/login')
      } catch (error) {
        console.error('退出登录失败:', error)
        // 即使后端接口失败，也要清除本地状态并跳转
        userStore.clearUserInfo()
        router.push('/auth/login')
      }
    }).catch(() => {
      // 用户取消退出，不做任何操作
    })
  }
  </script>
  
<style scoped>
/* 导入Font Awesome图标库样式 */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.frontend-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 导航栏样式 */
.navbar {
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 50;
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.navbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 4rem;
}

/* 品牌区域 */
.brand-section {
  display: flex;
  align-items: center;
}

.brand-logo {
  display: flex;
  align-items: center;
}

.brand-icon {
  font-size: 1.875rem;
  color: #4A90E2;
  margin-right: 0.75rem;
}

.brand-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

/* 导航区域 */
.nav-section {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-link {
  color: #4b5563;
  text-decoration: none;
  padding: 0.5rem 0.75rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.nav-link:hover {
  color: #4A90E2;
}

.nav-link.router-link-active {
  color: #4A90E2;
}

/* 认证区域 */
.auth-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.profile-link,
.login-link {
  color: #4b5563;
  text-decoration: none;
  padding: 0.5rem 0.75rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.profile-link:hover,
.login-link:hover {
  color: #4A90E2;
}

.logout-btn {
  background: rgba(74, 144, 226, 0.1);
  color: #4A90E2;
  border: 1px solid rgba(74, 144, 226, 0.2);
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(74, 144, 226, 0.2);
}

.register-btn {
  background: #4A90E2;
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.register-btn:hover {
  background: #3b7bc8;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 0;
}

/* 页脚样式 */
.footer {
  background: #1f2937;
  color: white;
  padding: 1.5rem 0;
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.footer-bottom {
  text-align: center;
}

.footer-bottom p {
  color: #9ca3af;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar-container {
    padding: 0 0.5rem;
  }
  
  .navbar-content {
    flex-direction: column;
    height: auto;
    padding: 1rem 0;
    gap: 1rem;
  }
  
  .nav-section {
    flex-direction: column;
    gap: 1rem;
    width: 100%;
  }
  
  .nav-links {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .auth-section {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .brand-title {
    font-size: 1rem;
  }
  
  .nav-links {
    gap: 1rem;
  }
  
  .nav-link {
    font-size: 0.8rem;
  }
}
</style>