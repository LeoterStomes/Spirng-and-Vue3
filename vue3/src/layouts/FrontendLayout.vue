<template>
  <div class="frontend-layout">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="navbar-container">
        <div class="navbar-content">
          <!-- 左侧品牌区域 -->
          <div class="brand-section">
            <div class="brand-logo">
              <h1 class="brand-title">
                小暖知你
              </h1>
            </div>
          </div>
            
          <!-- 右侧导航菜单 -->
          <div class="nav-section">
            <div class="nav-links">
              <router-link
                to="/"
                class="nav-link"
              >
                首页
              </router-link>
              <router-link
                v-if="isLoggedIn"
                to="/consultation"
                class="nav-link"
              >
                AI咨询
              </router-link>
              <router-link
                v-if="isLoggedIn"
                to="/psychological-test"
                class="nav-link"
              >
                心理测试
              </router-link>
              <router-link
                v-if="isLoggedIn"
                to="/emotion-diary"
                class="nav-link"
              >
                情绪日记
              </router-link>
              <router-link
                v-if="isLoggedIn"
                to="/favorites"
                class="nav-link"
              >
                我的收藏
              </router-link>
              <router-link
                to="/knowledge"
                class="nav-link"
              >
                知识库
              </router-link>
            </div>
              
            <!-- 认证区域 -->
            <div class="auth-section">
              <template v-if="isLoggedIn">
                <router-link
                  to="/profile"
                  class="profile-avatar-link"
                  title="个人中心"
                >
                  <img
                    v-if="avatarUrl"
                    :src="avatarUrl"
                    alt="用户头像"
                    class="profile-avatar"
                  >
                  <span
                    v-else
                    class="profile-avatar avatar-fallback"
                  >
                    {{ avatarInitial }}
                  </span>
                </router-link>
              </template>
              <template v-else>
                <router-link
                  to="/auth/login"
                  class="login-link"
                >
                  登录
                </router-link>
                <router-link
                  to="/auth/register"
                  class="register-btn"
                >
                  注册
                </router-link>
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
  
  const userStore = useUserStore()
  
  const isLoggedIn = computed(() => !!userStore.token)
  const avatarUrl = computed(() => userStore.userInfo?.avatar || '')
  const avatarInitial = computed(() => {
    const name = userStore.userInfo?.nickname || userStore.userInfo?.username || 'U'
    return String(name).charAt(0).toUpperCase()
  })
  </script>
  
<style scoped>
/* 导入Font Awesome图标库样式 */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.frontend-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  position: relative;
}

.frontend-layout::before,
.frontend-layout::after {
  content: '';
  position: fixed;
  width: 320px;
  height: 320px;
  border-radius: 999px;
  pointer-events: none;
  z-index: 0;
  filter: blur(30px);
}

.frontend-layout::before {
  top: 60px;
  left: -120px;
  background: rgba(91, 123, 255, 0.2);
}

.frontend-layout::after {
  right: -120px;
  bottom: 40px;
  background: rgba(34, 197, 94, 0.16);
}

/* 导航栏样式 */
.navbar {
  background: rgba(255, 255, 255, 0.78);
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  backdrop-filter: blur(12px);
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.08);
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

.brand-title {
  font-size: 1.18rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  letter-spacing: 0.01em;
  background: linear-gradient(135deg, #5b7bff 0%, #4f46e5 45%, #22c55e 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
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
  color: #475569;
  text-decoration: none;
  padding: 0.45rem 0.7rem;
  border-radius: 999px;
  font-size: 0.9rem;
  font-weight: 600;
  transition: all 0.25s ease;
}

.nav-link:hover {
  color: #4f46e5;
  background: rgba(91, 123, 255, 0.12);
  transform: translateY(-1px);
}

.nav-link.router-link-active {
  color: #4f46e5;
  background: rgba(91, 123, 255, 0.16);
  box-shadow: 0 6px 16px rgba(91, 123, 255, 0.2);
}

/* 认证区域 */
.auth-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.login-link {
  color: #4b5563;
  text-decoration: none;
  padding: 0.5rem 0.75rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.login-link:hover {
  color: #4f46e5;
}

.profile-avatar-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
}

.profile-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #dbeafe;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.avatar-fallback {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #5b7bff 0%, #4f46e5 100%);
  color: #fff;
  font-size: 0.85rem;
  font-weight: 700;
}

.profile-avatar-link:hover .profile-avatar {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(79, 70, 229, 0.3);
}

.register-btn {
  background: linear-gradient(135deg, #5b7bff 0%, #4f46e5 100%);
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.register-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(79, 70, 229, 0.3);
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 0;
  position: relative;
  z-index: 1;
}

/* 页脚样式 */
.footer {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  color: white;
  padding: 1.5rem 0;
  margin-top: auto;
  position: relative;
  z-index: 1;
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