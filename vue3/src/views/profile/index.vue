<template>
  <div class="profile-container">
    <!-- 个人资料头部 -->
    <section class="profile-header">
      <div class="header-content">
        <div class="profile-info">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-avatar :size="96" :src="currentAvatarUrl" @error="() => false">
                <i class="fas fa-user"></i>
              </el-avatar>
              <el-upload
                class="avatar-upload"
                action="#"
                :show-file-list="false"
                :http-request="handleUploadAvatar"
                :before-upload="beforeAvatarUpload"
                :disabled="uploadingAvatar"
              >
                <el-button size="small" circle type="primary" class="upload-btn" :loading="uploadingAvatar">
                  <i v-if="!uploadingAvatar" class="fas fa-camera"></i>
                </el-button>
              </el-upload>
            </div>
            <div class="user-details">
              <h2 class="user-name">{{ userProfile.displayName }}</h2>
              <p class="user-email">{{ userProfile.email }}</p>
              <p class="join-date">加入时间：{{ formatDate(userProfile.createdAt) }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div class="main-content">
      <div class="content-wrapper">
        <!-- 左侧导航菜单 -->
        <div class="sidebar">
          <div class="nav-card">
            <h3 class="nav-title">个人中心</h3>
            <nav class="nav-menu">
              <button 
                v-for="tab in tabs" 
                :key="tab.key"
                :class="['tab-item', activeTab === tab.key ? 'active' : '']"
                @click="activeTab = tab.key"
              >
                <i :class="tab.icon"></i>{{ tab.label }}
              </button>
            </nav>
          </div>
        </div>

        <!-- 主内容区域 -->
        <div class="main-section">

          <!-- 个人信息 -->
          <div v-show="activeTab === 'personal'" class="content-card">
            <h3 class="section-title">
              <i class="fas fa-user"></i>个人信息
            </h3>
            
            <el-form 
              ref="userFormRef" 
              :model="userForm" 
              :rules="userFormRules" 
              label-width="120px"
              class="user-form"
            >
              <div class="form-grid">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="userForm.username" disabled />
                </el-form-item>
                
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
                </el-form-item>
                
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="userForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="userForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="userForm.gender">
                    <el-radio :label="0">未知</el-radio>
                    <el-radio :label="1">男</el-radio>
                    <el-radio :label="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item label="生日" prop="birthday">
                  <el-date-picker
                    v-model="userForm.birthday"
                    type="date"
                    placeholder="选择生日"
                    style="width: 100%"
                  />
                </el-form-item>
              </div>
              
              <el-form-item>
                <el-button type="primary" :loading="updateLoading" @click="updateUserInfo">
                  <i class="fas fa-save mr-2"></i>保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 修改密码 -->
          <div v-show="activeTab === 'password'" class="content-card">
            <h3 class="section-title">
              <i class="fas fa-key"></i>修改密码
            </h3>
            
            <el-form 
              ref="passwordFormRef" 
              :model="passwordForm" 
              :rules="passwordFormRules" 
              label-width="120px"
              class="password-form"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input 
                  v-model="passwordForm.oldPassword" 
                  type="password" 
                  placeholder="请输入原密码"
                  show-password 
                />
              </el-form-item>
              
              <el-form-item label="新密码" prop="newPassword">
                <el-input 
                  v-model="passwordForm.newPassword" 
                  type="password" 
                  placeholder="请输入新密码"
                  show-password 
                />
              </el-form-item>
              
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input 
                  v-model="passwordForm.confirmPassword" 
                  type="password" 
                  placeholder="请再次输入新密码"
                  show-password 
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" :loading="passwordLoading" @click="updatePassword">
                  <i class="fas fa-shield-alt mr-2"></i>修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getCurrentUser, updateUser, updatePassword as updateUserPassword } from '@/api/user'
import { uploadTempBusinessFile, confirmTempFile } from '@/api/FileApi'
import { formatDate } from '@/utils/dateUtils'

const userStore = useUserStore()

// 标签页面
const activeTab = ref('personal')
const updateLoading = ref(false)
const passwordLoading = ref(false)

// 表单引用
const userFormRef = ref()
const passwordFormRef = ref()

// 标签配置
const tabs = [
  { key: 'personal', label: '个人信息', icon: 'fas fa-user' },
  { key: 'password', label: '修改密码', icon: 'fas fa-key' }
]

// 用户资料
const userProfile = reactive({
  id: '',
  username: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  birthday: '',
  avatar: '',
  displayName: '',
  userType: 1,
  status: 1,
  createdAt: ''
})

// 用户表单
const userForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  birthday: '',
  avatar: '' // 添加头像字段
})

// 头像上传相关状态
const tempFileId = ref(null) // 临时文件ID
const uploadingAvatar = ref(false) // 上传状态
const tempAvatarUrl = ref(null) // 临时头像预览URL

// 当前显示的头像URL（优先显示临时预览）
const currentAvatarUrl = computed(() => {
  return tempAvatarUrl.value || userProfile.avatar
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const userFormRules = reactive({
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
})

const passwordFormRules = reactive({
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 头像上传处理（策略A：两阶段提交上传）
const handleUploadAvatar = async ({ file, onSuccess, onError }) => {
  try {
    uploadingAvatar.value = true
    
    // 创建本地预览URL
    const previewUrl = URL.createObjectURL(file)
    
    // 第一阶段：上传到临时业务存储区
    await uploadTempBusinessFile(file, {
      businessType: 'USER_AVATAR',
      businessField: 'avatar'
    }, {
      onSuccess: (res) => {
        console.log('临时业务文件上传成功:', res)
        tempFileId.value = res.id
        tempAvatarUrl.value = previewUrl // 设置临时预览
        ElMessage.success('头像预上传成功，请点击保存修改完成头像更新')
        onSuccess(res)
      },
      onError: (err) => {
        console.error('临时业务文件上传失败:', err)
        // 清理预览URL
        URL.revokeObjectURL(previewUrl)
        ElMessage.error('头像上传失败: ' + (err.message || '未知错误'))
        onError(err)
      }
    })
  } catch (error) {
    console.error('头像上传异常:', error)
    ElMessage.error('头像上传失败')
    onError(error)
  } finally {
    uploadingAvatar.value = false
  }
}

// 取消头像上传
const cancelAvatarUpload = () => {
  // 清理临时预览URL
  if (tempAvatarUrl.value) {
    URL.revokeObjectURL(tempAvatarUrl.value)
    tempAvatarUrl.value = null
  }
  
  // 重置临时文件ID
  tempFileId.value = null
  
  ElMessage.info('已取消头像上传')
}

// 更新用户信息
const updateUserInfo = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    updateLoading.value = true
    
    // 调用更新用户信息API
    console.log('更新用户信息，发送数据:', userForm)
    await updateUser(userForm, {
      successMsg: '用户信息更新成功',
      onSuccess: async (res) => {
        console.log('用户信息更新成功，返回数据:', res)
        // 更新本地用户信息
        Object.assign(userProfile, res)
        userStore.setUserInfo(res)
        
        // 第二阶段：如果有临时头像文件，确认并关联，然后再次更新用户信息
        if (tempFileId.value) {
          console.log('开始确认临时头像文件:', tempFileId.value)
          try {
            await confirmTempFile(tempFileId.value, {
              businessType: 'USER_AVATAR',
              businessId: res.id,
              businessField: 'avatar'
            }, {
              onSuccess: async (fileRes) => {
                console.log('头像确认成功，文件信息:', fileRes)
                // 清理临时预览URL
                if (tempAvatarUrl.value) {
                  URL.revokeObjectURL(tempAvatarUrl.value)
                  tempAvatarUrl.value = null
                }
                
                // 更新头像路径
                const newAvatarPath = fileRes.filePath
                userProfile.avatar = newAvatarPath
                userForm.avatar = newAvatarPath
                tempFileId.value = null
                
                // 重要：再次调用后端更新用户信息，包含新的头像路径
                console.log('开始更新用户头像字段到后端:', newAvatarPath)
                await updateUser({ avatar: newAvatarPath }, {
                  showDefaultMsg: false, // 不显示默认消息，避免重复提示
                  onSuccess: (updateRes) => {
                    console.log('用户头像字段更新成功:', updateRes)
                    // 更新本地数据
                    Object.assign(userProfile, updateRes)
                    userStore.setUserInfo(updateRes)
                    ElMessage.success('头像更新成功')
                  },
                  onError: (updateErr) => {
                    console.error('用户头像字段更新失败:', updateErr)
                    ElMessage.error('头像保存失败: ' + (updateErr.message || '未知错误'))
                  }
                })
              },
              onError: (err) => {
                console.error('头像确认失败:', err)
                ElMessage.error('头像确认失败: ' + (err.message || '未知错误'))
              }
            })
          } catch (error) {
            console.error('头像确认异常:', error)
            ElMessage.error('头像确认失败')
          }
        }
      }
    })
  } catch (error) {
    console.error('更新用户信息失败:', error)
  } finally {
    updateLoading.value = false
  }
}

// 修改密码
const updatePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true
    
    // 调用修改密码API
    await updateUserPassword(passwordForm, {
      successMsg: '密码修改成功',
      onSuccess: () => {
        // 重置表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
        passwordFormRef.value.resetFields()
      }
    })
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

// 组件挂载
onMounted(async () => {
  try {
    // 获取当前用户信息
    await getCurrentUser(null, {
      onSuccess: (res) => {
        Object.assign(userProfile, res)
        Object.assign(userForm, {
          username: res.username,
          nickname: res.nickname || '',
          email: res.email || '',
          phone: res.phone || '',
          gender: res.gender || 0,
          birthday: res.birthday || '',
          avatar: res.avatar || '' // 添加头像字段初始化
        })
        
        // 设置显示名称
        userProfile.displayName = res.nickname || res.username
      }
    })
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
})
</script>

<style scoped>
/* 主容器 */
.profile-container {
  min-height: 100vh;
  background-color: #f9fafb;
}

/* 头部样式 */
.profile-header {
  background: #4A90E2;
  color: white;
  padding: 3rem 0;
}

.header-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 1rem;
}

.profile-info {
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.avatar-wrapper {
  position: relative;
}

.avatar-upload {
  position: absolute;
  bottom: -0.5rem;
  right: -0.5rem;
}

.upload-btn {
  width: 2rem !important;
  height: 2rem !important;
  font-size: 0.75rem;
}

.user-details {
  text-align: left;
}

.user-name {
  font-size: 1.875rem;
  font-weight: bold;
  margin: 0 0 0.5rem 0;
}

.user-email {
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
}

.join-date {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.875rem;
  margin: 0;
}

/* 主内容区域 */
.main-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 3fr;
  gap: 2rem;
}

/* 侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
}

.nav-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  position: sticky;
  top: 6rem;
}

.nav-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 1rem 0;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.tab-item {
  width: 100%;
  text-align: left;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  font-weight: 500;
  transition: all 0.3s ease;
  background: none;
  border: none;
  cursor: pointer;
  color: #374151;
  display: flex;
  align-items: center;
}

.tab-item i {
  margin-right: 0.75rem;
  width: 1rem;
}

.tab-item:hover {
  background-color: #f3f4f6;
}

.tab-item.active {
  background: #4A90E2;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.3);
}

/* 主内容区 */
.main-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.content-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  padding: 2rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 1.5rem 0;
  display: flex;
  align-items: center;
}

.section-title i {
  margin-right: 0.75rem;
  color: #4A90E2;
}

/* 表单样式 */
.user-form {
  max-width: 48rem;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.password-form {
  max-width: 32rem;
}


/* Element Plus 样式覆盖 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
}

:deep(.el-input__wrapper) {
  border-radius: 0.5rem;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #4A90E2;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #4A90E2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
}

:deep(.el-button--primary) {
  background: #4A90E2;
  border: none;
  border-radius: 0.5rem;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  background: #357abd;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.3);
}

:deep(.el-avatar) {
  font-size: 2rem;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .nav-card {
    position: static;
  }
  
  .nav-menu {
    flex-direction: row;
    overflow-x: auto;
  }
  
  .tab-item {
    white-space: nowrap;
    min-width: fit-content;
  }
}

@media (max-width: 768px) {
  .profile-header {
    padding: 2rem 0;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .user-details {
    text-align: center;
  }
  
  .user-name {
    font-size: 1.5rem;
  }
  
  .main-content {
    padding: 1rem;
  }
  
  .content-card {
    padding: 1.5rem;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
}

</style> 