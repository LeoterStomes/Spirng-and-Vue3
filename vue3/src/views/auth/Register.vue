<template>
  <div class="register-container">
    <div class="form-container">
        <!-- 返回首页链接 -->
        <div class="back-home">
          <router-link to="/" class="back-home-link">
            <i class="fas fa-arrow-left"></i>返回首页
          </router-link>
        </div>
        
        <div class="form-header">
          <h2 class="form-title">创建您的账户</h2>
          <p class="form-subtitle">请填写注册信息</p>
        </div>

        <form class="register-form" @submit.prevent="handleRegister">
          <!-- 用户名输入 -->
          <div class="form-field">
            <label for="username" class="field-label">
              <i class="fas fa-user label-icon"></i>用户名
            </label>
            <input 
              id="username" 
              v-model="registerForm.username"
              name="username" 
              type="text" 
              required 
              class="form-input"
              placeholder="请输入用户名"
            >
          </div>

          <!-- 邮箱输入 -->
          <div class="form-field">
            <label for="email" class="field-label">
              <i class="fas fa-envelope label-icon"></i>邮箱
            </label>
            <input 
              id="email" 
              v-model="registerForm.email"
              name="email" 
              type="email" 
              required 
              class="form-input"
              placeholder="请输入邮箱"
            >
          </div>

          <!-- 昵称输入 -->
          <div class="form-field">
            <label for="nickname" class="field-label">
              <i class="fas fa-user-tag label-icon"></i>昵称
            </label>
            <input 
              id="nickname" 
              v-model="registerForm.nickname"
              name="nickname" 
              type="text" 
              class="form-input"
              placeholder="请输入昵称（可选）"
            >
          </div>
          <!-- 手机号输入 -->
          <div class="form-field">
            <label for="phone" class="field-label">
              <i class="fas fa-phone label-icon"></i>手机号
            </label>
            <input 
              id="phone" 
              v-model="registerForm.phone"
              name="phone" 
              type="tel" 
              class="form-input"
              placeholder="请输入手机号（可选）"
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
                v-model="registerForm.password"
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

          <!-- 确认密码输入 -->
          <div class="form-field">
            <label for="confirmPassword" class="field-label">
              <i class="fas fa-lock label-icon"></i>确认密码
            </label>
            <div class="password-wrapper">
              <input 
                id="confirmPassword" 
                v-model="registerForm.confirmPassword"
                name="confirmPassword" 
                :type="showConfirmPassword ? 'text' : 'password'"
                required 
                class="form-input password-input"
                placeholder="请再次输入密码"
              >
              <button 
                type="button" 
                class="password-toggle"
                @click="toggleConfirmPasswordVisibility"
              >
                <i :class="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'" class="toggle-icon"></i>
              </button>
            </div>
          </div>

          <!-- 用户协议 -->
          <div class="agreement-section">
            <input 
              id="agree-terms" 
              v-model="agreeTerms"
              name="agree-terms" 
              type="checkbox" 
              class="checkbox-input"
            >
            <label for="agree-terms" class="checkbox-label">
              我已阅读并同意 
              <a href="#" class="agreement-link">用户协议</a> 
              和 
              <a href="#" class="agreement-link">隐私政策</a>
            </label>
          </div>

          <!-- 注册按钮 -->
          <div class="button-section">
            <button 
              type="submit" 
              :disabled="loading || !agreeTerms"
              class="register-button"
            >
              <span class="button-icon">
                <i v-if="!loading" class="fas fa-user-plus"></i>
                <i v-else class="fas fa-spinner fa-spin"></i>
              </span>
              {{ loading ? '注册中...' : '创建账户' }}
            </button>
          </div>

          <!-- 登录链接 -->
          <div class="login-section">
            <p class="login-text">
              已有账户？
              <router-link to="/auth/login" class="login-link">
                立即登录
              </router-link>
            </p>
          </div>
        </form>

        <!-- 安全承诺 -->
        <div class="security-notice">
          <div class="notice-content">
            <div class="notice-icon">
              <i class="fas fa-shield-alt"></i>
            </div>
            <div class="notice-text">
              <h3 class="notice-title">安全承诺</h3>
              <div class="notice-description">
                <p>我们采用行业标准的加密技术保护您的个人信息和隐私数据安全。</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const agreeTerms = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  nickname: '', // 修改name为nickname以匹配后端DTO
  gender: 0, // 添加性别字段
  userType: 1 // 添加用户类型字段，默认为普通用户
})

// 切换密码显示/隐藏
const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

const toggleConfirmPasswordVisibility = () => {
  showConfirmPassword.value = !showConfirmPassword.value
}

// 表单验证
const validateForm = () => {
  if (!registerForm.username) {
    ElMessage.error('请输入用户名')
    return false
  }
  if (registerForm.username.length < 3 || registerForm.username.length > 50) {
    ElMessage.error('用户名长度必须在3到50个字符之间')
    return false
  }
  if (!registerForm.email) {
    ElMessage.error('请输入邮箱')
    return false
  }
  const emailRegex = /^[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!emailRegex.test(registerForm.email)) {
    ElMessage.error('邮箱格式不正确')
    return false
  }
  if (registerForm.phone && !/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    ElMessage.error('手机号格式不正确')
    return false
  }
  if (!registerForm.password) {
    ElMessage.error('请输入密码')
    return false
  }
  if (registerForm.password.length < 6 || registerForm.password.length > 100) {
    ElMessage.error('密码长度必须在6到100个字符之间')
    return false
  }
  if (!registerForm.confirmPassword) {
    ElMessage.error('请确认密码')
    return false
  }
  
  // 去除密码两端的空格后再比较
  const password = registerForm.password.trim()
  const confirmPassword = registerForm.confirmPassword.trim()
  
  if (password !== confirmPassword) {
    console.log('密码比较失败:', {
      password: password,
      confirmPassword: confirmPassword,
      passwordLength: password.length,
      confirmPasswordLength: confirmPassword.length
    })
    ElMessage.error('两次输入密码不一致')
    return false
  }
  
  if (!agreeTerms.value) {
    ElMessage.error('请同意用户协议和隐私政策')
    return false
  }
  return true
}

// 注册处理
const handleRegister = async () => {
  if (!validateForm()) return
  
  loading.value = true
  
  try {
    // 清理密码数据，去除两端空格
    const cleanedForm = {
      ...registerForm,
      password: registerForm.password.trim(),
      confirmPassword: registerForm.confirmPassword.trim()
    }
    
    // 除去不需要的字段，发送包含confirmPassword的数据到后端进行二次验证
    const registerData = {
      username: cleanedForm.username,
      email: cleanedForm.email,
      password: cleanedForm.password,
      confirmPassword: cleanedForm.confirmPassword,
      nickname: cleanedForm.nickname,
      phone: cleanedForm.phone,
      gender: cleanedForm.gender,
      userType: cleanedForm.userType
    }
    
    await register(registerData, {
      successMsg: "注册成功",
      showDefaultMsg: true,
      onSuccess: () => {
        ElMessage.success('注册成功，请登录')
        router.push('/auth/login')
      },
      onError: (error) => {
        console.error('注册失败:', error)
      }
    })
  } catch (error) {
    console.error('注册异常:', error)
    ElMessage.error('注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 基本字体和样式 */

/* 主容器 */
.register-container {
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
.register-form {
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

/* 协议部分 */
.agreement-section {
  display: flex;
  align-items: flex-start;
}

.checkbox-input {
  height: 1rem;
  width: 1rem;
  color: #4A90E2;
  border: 1px solid #d1d5db;
  border-radius: 0.25rem;
  margin-top: 0.25rem;
  flex-shrink: 0;
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
  line-height: 1.4;
}

.agreement-link {
  color: #4A90E2;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.3s ease;
}

.agreement-link:hover {
  color: #357abd;
}

/* 注册按钮 */
.button-section {
  margin-top: 1rem;
}

.register-button {
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

.register-button:hover:not(:disabled) {
  background-color: #357abd;
  box-shadow: 0 0 40px rgba(74, 144, 226, 0.4);
  transform: translateY(-2px);
}

.register-button:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.5), 0 0 30px rgba(74, 144, 226, 0.3);
}

.register-button:disabled {
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

/* 登录链接 */
.login-section {
  text-align: center;
  margin-top: 1.5rem;
}

.login-text {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.login-link {
  font-weight: 500;
  color: #4A90E2;
  text-decoration: none;
  transition: color 0.3s ease;
}

.login-link:hover {
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
.register-button,
button {
  transition-duration: 0.4s;
  transition-timing-function: ease-in-out;
}
</style>
