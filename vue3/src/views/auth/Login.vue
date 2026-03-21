<template>
  <div class="login-container">
    <div class="form-container">
        <!-- 返回首页链接 -->
        <div class="back-home">
          <router-link to="/" class="back-home-link">
            <i class="fas fa-arrow-left"></i>返回首页
          </router-link>
        </div>
        
        <div class="form-header">
          <h2 class="form-title">登录您的账户</h2>
          <p class="form-subtitle">请输入您的登录信息</p>
        </div>

        <form class="login-form" @submit.prevent="handleLogin">
          <!-- 用户名/邮箱输入 -->
          <div class="form-field">
            <label for="username" class="field-label">
              <i class="fas fa-user label-icon"></i>用户名或邮箱
            </label>
            <input 
              id="username" 
              v-model="loginForm.username"
              name="username" 
              type="text" 
              required 
              class="form-input"
              placeholder="请输入用户名或邮箱"
            >
          </div>

          <!-- 密码输入 -->
          <div class="form-field">
            <label for="password" class="field-label">
              <i class="fas fa-lock label-icon"></i>密码
            </label>
            <div class="password-wrapper">
              <input 
                id="password" 
                v-model="loginForm.password"
                name="password" 
                :type="showPassword ? 'text' : 'password'"
                required 
                class="form-input password-input"
                placeholder="请输入密码"
              >
              <button 
                type="button" 
                class="password-toggle"
                @click="togglePasswordVisibility"
              >
                <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'" class="toggle-icon"></i>
              </button>
            </div>
          </div>

          <!-- 记住我和忘记密码 -->
          <div class="form-options">
            <div class="remember-section">
              <input 
                id="remember-me" 
                v-model="rememberMe"
                name="remember-me" 
                type="checkbox" 
                class="checkbox-input"
              >
              <label for="remember-me" class="checkbox-label">记住我</label>
            </div>
          </div>

          <!-- 登录按钮 -->
          <div class="button-section">
            <button 
              type="submit" 
              :disabled="loading"
              class="login-button"
            >
              <span class="button-icon">
                <i v-if="!loading" class="fas fa-sign-in-alt"></i>
                <i v-else class="fas fa-spinner fa-spin"></i>
              </span>
              {{ loading ? '登录中...' : '登录账户' }}
            </button>
          </div>

          <!-- 注册链接 -->
          <div class="register-section">
            <p class="register-text">
              还没有账户？
              <router-link to="/auth/register" class="register-link">
                立即注册
              </router-link>
            </p>
          </div>
        </form>

        <!-- 安全提示 -->
        <div class="security-notice">
          <div class="notice-content">
            <div class="notice-icon">
              <i class="fas fa-shield-alt"></i>
            </div>
            <div class="notice-text">
              <h3 class="notice-title">安全提示</h3>
              <div class="notice-description">
                <p>为了保护您的隐私和数据安全，请不要在公共设备上保存登录信息。</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { login } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const rememberMe = ref(false)
const showPassword = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

// 组件挂载时恢复记住我状态和用户名
onMounted(() => {
  // 恢复记住我状态
  const savedRememberMe = localStorage.getItem('rememberMe')
  if (savedRememberMe === 'true') {
    rememberMe.value = true
    // 恢复用户名
    const savedUsername = localStorage.getItem('rememberedUsername')
    if (savedUsername) {
      loginForm.username = savedUsername
    }
  }
})

// 监听记住我状态变化，实时处理存储
watch(rememberMe, (newValue) => {
  if (!newValue) {
    // 用户取消勾选记住我，立即清除相关存储
    localStorage.removeItem('rememberMe')
    localStorage.removeItem('rememberedUsername')
  }
})

// 切换密码显示/隐藏
const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

// 表单验证
const validateForm = () => {
  if (!loginForm.username) {
    ElMessage.error('请输入用户名或邮箱')
    return false
  }
  if (!loginForm.password) {
    ElMessage.error('请输入密码')
    return false
  }
  return true
}
// 处理记住我功能
const handleRememberMe = () => {
  if (rememberMe.value) {
    // 用户勾选了记住我，保存用户名和记住我状态
    localStorage.setItem('rememberMe', 'true')
    localStorage.setItem('rememberedUsername', loginForm.username.trim())
  } else {
    // 用户没有勾选记住我，清除相关存储
    localStorage.removeItem('rememberMe')
    localStorage.removeItem('rememberedUsername')
  }
}

// 登录处理
const handleLogin = async () => {
  if (!validateForm()) return
  
  loading.value = true
  
  try {
    // 清理登录数据，去除两端空格
    const cleanedForm = {
      username: loginForm.username.trim(),
      password: loginForm.password.trim()
    }
    
    // 使用API接口进行登录
    await login(cleanedForm, {
      successMsg: "登录成功",
      showDefaultMsg: true,
      onSuccess: async (data) => {
        // 处理记住我功能
        handleRememberMe()
        
        // 设置用户信息到store
        userStore.setUserInfo(data)

        // 根据返回的角色决定跳转路径
        if (data.userInfo.userType === 2) { // 2代表管理员
          await router.isReady()
          router.push(route.query.redirect || '/back/dashboard')
        } else {
          // 普通用户登录，直接跳转到前台
          const redirect = route.query.redirect || '/'
          router.push(redirect)
        }
      },
      onError: (error) => {
        console.error('登录失败:', error)
      }
    })
  } catch (error) {
    console.error('登录异常:', error)
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 基本字体和样式 */

/* 主容器 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100%;
}

.form-container {
  margin: 0 auto;
  width: 100%;
  max-width: 24rem;
}

/* 返回首页链接 */
.back-home {
  margin-bottom: 2rem;
}

.back-home-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #6b7280;
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition: color 0.3s ease;
}

.back-home-link:hover {
  color: #4A90E2;
}

.back-home-link i {
  font-size: 0.75rem;
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
}

.form-title {
  font-size: 1.875rem;
  font-weight: bold;
  color: #111827;
  margin-bottom: 0.5rem;
  margin-top: 0;
}

.form-subtitle {
  color: #6b7280;
  margin: 0;
}

/* 表单样式 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-field {
  display: flex;
  flex-direction: column;
}

.field-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.label-icon {
  margin-right: 0.5rem;
  color: #4A90E2;
}

.form-input {
  appearance: none;
  position: relative;
  display: block;
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  color: #111827;
  background-color: white;
  transition: all 0.4s ease-in-out;
  box-sizing: border-box;
}

.form-input::placeholder {
  color: #6b7280;
}

.form-input:focus {
  outline: none;
  border-color: #4A90E2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1), 0 10px 25px rgba(74, 144, 226, 0.2);
  transform: translateY(-2px);
}

.password-wrapper {
  position: relative;
}

.password-input {
  padding-right: 2.5rem;
}

.password-toggle {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  padding-right: 0.75rem;
  display: flex;
  align-items: center;
  background: none;
  border: none;
  cursor: pointer;
}

.toggle-icon {
  color: #9ca3af;
  transition: color 0.3s ease;
}

.toggle-icon:hover {
  color: #6b7280;
}

/* 表单选项行 */
.form-options {
  display: flex;
  align-items: center;
}

.remember-section {
  display: flex;
  align-items: center;
}

.checkbox-input {
  height: 1rem;
  width: 1rem;
  color: #4A90E2;
  border: 1px solid #d1d5db;
  border-radius: 0.25rem;
}

.checkbox-input:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.5);
}

.checkbox-label {
  margin-left: 0.5rem;
  display: block;
  font-size: 0.875rem;
  color: #374151;
}


/* 登录按钮 */
.button-section {
  margin-top: 1rem;
}

.login-button {
  position: relative;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0.75rem 1rem;
  border: none;
  font-size: 0.875rem;
  font-weight: 500;
  border-radius: 0.5rem;
  color: white;
  background-color: #4A90E2;
  box-shadow: 0 0 30px rgba(74, 144, 226, 0.3);
  transition: all 0.3s ease;
  cursor: pointer;
}

.login-button:hover:not(:disabled) {
  background-color: #357abd;
  box-shadow: 0 0 40px rgba(74, 144, 226, 0.4);
  transform: translateY(-2px);
}

.login-button:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.5), 0 0 30px rgba(74, 144, 226, 0.3);
}

.login-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 0 30px rgba(74, 144, 226, 0.3);
}

.button-icon {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  padding-left: 0.75rem;
  color: #bfdbfe;
}

/* 注册链接 */
.register-section {
  text-align: center;
  margin-top: 1.5rem;
}

.register-text {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.register-link {
  font-weight: 500;
  color: #4A90E2;
  text-decoration: none;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #357abd;
}

/* 安全提示 */
.security-notice {
  margin-top: 2rem;
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 0.5rem;
  padding: 1rem;
}

.notice-content {
  display: flex;
}

.notice-icon {
  flex-shrink: 0;
}

.notice-icon i {
  color: #4A90E2;
}

.notice-text {
  margin-left: 0.75rem;
}

.notice-title {
  font-size: 0.875rem;
  font-weight: 500;
  color: #1e40af;
  margin: 0 0 0.25rem 0;
}

.notice-description {
  font-size: 0.875rem;
  color: #1d4ed8;
  margin-top: 0.25rem;
}

.notice-description p {
  margin: 0;
}


/* 链接悬停效果 */
a {
  transition: color 0.3s ease;
}

/* 按钮悬停效果 */
button {
  transition: all 0.3s ease;
}

button:hover {
  transform: translateY(-1px);
}

/* 微动效效果 - 符合呼吸节奏规范 */
.form-input,
.login-button,
button {
  transition-duration: 0.4s;
  transition-timing-function: ease-in-out;
}
</style>

