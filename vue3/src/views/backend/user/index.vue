<template>
  <div class="user-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h2 class="page-title">用户管理</h2>
        </div>
        <div class="header-actions">
          <el-button type="primary"  @click="handleAddUser">
            <i class="fas fa-user-plus"></i>添加用户
          </el-button>
          <el-button type="success"    @click="handleExportData">
            <i class="fas fa-download"></i>导出数据
          </el-button>
        </div>
      </div>
    </div>


    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名、邮箱或手机号..."
            :prefix-icon="Search"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        
        <el-form-item>
          <el-select v-model="searchForm.status" placeholder="用户状态" clearable class="search-select">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-select v-model="searchForm.userType" placeholder="用户类型" clearable class="search-select">
            <el-option label="普通用户" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            搜索
          </el-button>
          <el-button @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户列表 -->
    <div class="table-section">
      <div class="table-header">
        <div class="table-header-content">
          <h3 class="table-title">用户列表</h3>
          <div class="table-header-actions">
            <span class="table-total">
              共 {{ total }} 条数据
            </span>
          </div>
        </div>
      </div>
      
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        
        <el-table-column label="用户信息" min-width="200" fixed="left">
          <template #default="scope">
            <div class="user-info">
              <el-avatar
                :size="40"
                :src="scope.row.avatar"
                class="user-avatar"
              >
                <i class="fas fa-user"></i>
              </el-avatar>
              <div class="user-details">
                <div class="user-name">{{ scope.row.displayName }}</div>
                <div class="user-id">ID: {{ scope.row.id }}</div>
                <div class="user-username">@{{ scope.row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="联系方式" min-width="180">
          <template #default="scope">
            <div class="contact-info">
              <div class="contact-item">
                <i class="fas fa-envelope contact-icon"></i>
                <span class="contact-text">{{ scope.row.email }}</span>
              </div>
              <div class="contact-item" v-if="scope.row.phone">
                <i class="fas fa-phone contact-icon"></i>
                <span class="contact-text">{{ scope.row.phone }}</span>
              </div>
              <div class="contact-item" v-else>
                <i class="fas fa-phone contact-icon"></i>
                <span class="contact-text contact-empty">未填写</span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="个人信息" width="140" align="center">
          <template #default="scope">
            <div class="personal-info">
              <div class="info-row">
                <el-tag 
                  :type="scope.row.gender === 1 ? 'primary' : scope.row.gender === 0 ? '' : 'info'"
                  size="small"
                  class="gender-tag"
                >
                  <i :class="scope.row.gender === 1 ? 'fas fa-mars' : scope.row.gender === 0 ? 'fas fa-venus' : 'fas fa-question'"></i>
                  {{ scope.row.gender === 1 ? '男' : scope.row.gender === 0 ? '女' : '未知' }}
                </el-tag>
              </div>
              <div class="info-row">
                <span class="birthday-text">
                  <i class="fas fa-birthday-cake birthday-icon"></i>
                  {{ scope.row.birthday ? formatDate(scope.row.birthday) : '未填写' }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="用户类型" width="100" align="center">
          <template #default="scope">
            <el-tag
              :type="scope.row.userType === 2 ? 'danger' : 'primary'"
              size="small"
              class="type-tag"
            >
              <i :class="scope.row.userType === 2 ? 'fas fa-crown' : 'fas fa-user'" class="type-icon"></i>
              {{ scope.row.userTypeDisplayName }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === 1 ? 'success' : 'danger'"
              size="small"
              class="status-tag"
            >
              <i :class="scope.row.status === 1 ? 'fas fa-check-circle' : 'fas fa-ban'" class="status-icon"></i>
              {{ scope.row.statusDisplayName }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="注册时间" width="140" align="center">
          <template #default="scope">
            <div class="date-info">
              <div class="date-main">
                <i class="fas fa-calendar-plus date-icon"></i>
                {{ formatDate(scope.row.createdAt) }}
              </div>
              <div class="date-sub">{{ formatTime(scope.row.createdAt) }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="最后更新" width="140" align="center">
          <template #default="scope">
            <div class="date-info">
              <div class="date-main">
                <i class="fas fa-edit date-icon"></i>
                {{ formatDate(scope.row.updatedAt) }}
              </div>
              <div class="date-sub">{{ formatTime(scope.row.updatedAt) }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                type="primary"
                size="small"
                @click="handleViewUser(scope.row)"
                title="查看"
              >
                查看
              </el-button>
              <el-button
                type="success"
                size="small"
                @click="handleEditUser(scope.row)"
                title="编辑"
              >
                编辑
              </el-button>
              <el-button
                :type="scope.row.status === 1 ? 'warning' : 'success'"
                size="small"
                @click="handleToggleStatus(scope.row)"
                :title="scope.row.status === 1 ? '禁用' : '启用'"
              >
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDeleteUser(scope.row)"
                title="删除"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户信息"
      width="600px"
      :before-close="handleCancelEdit"
    >
      <el-form :model="editForm" label-width="100px" class="edit-form">
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="性别">
          <el-select v-model="editForm.gender" placeholder="请选择性别" clearable>
            <el-option label="男" :value="1" />
            <el-option label="女" :value="0" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="生日">
          <el-date-picker
            v-model="editForm.birthday"
            type="date"
            placeholder="选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="用户类型">
          <el-select v-model="editForm.userType" placeholder="请选择用户类型">
            <el-option label="普通用户" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="用户状态">
          <el-select v-model="editForm.status" placeholder="请选择用户状态">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelEdit">取消</el-button>
          <el-button type="primary" @click="handleSaveEdit">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加用户对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加用户"
      width="600px"
      :before-close="handleCancelAdd"
    >
      <el-form :model="addForm" label-width="100px" class="add-form">
        <el-form-item label="用户名" required>
          <el-input 
            v-model="addForm.username" 
            placeholder="请输入用户名（3-50个字符，只能包含字母、数字和下划线）" 
            maxlength="50"
          />
        </el-form-item>
        
        <el-form-item label="邮箱" required>
          <el-input 
            v-model="addForm.email" 
            placeholder="请输入邮箱" 
            type="email"
          />
        </el-form-item>
        
        <el-form-item label="密码" required>
          <el-input 
            v-model="addForm.password" 
            placeholder="请输入密码（至少6位）" 
            type="password"
            show-password
            minlength="6"
          />
        </el-form-item>
        
        <el-form-item label="确认密码" required>
          <el-input 
            v-model="addForm.confirmPassword" 
            placeholder="请再次输入密码" 
            type="password"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="昵称">
          <el-input v-model="addForm.nickname" placeholder="请输入昵称（可选，默认使用用户名）" />
        </el-form-item>
        
        <el-form-item label="手机号">
          <el-input v-model="addForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="性别">
          <el-select v-model="addForm.gender" placeholder="请选择性别" clearable>
            <el-option label="男" :value="1" />
            <el-option label="女" :value="0" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="生日">
          <el-date-picker
            v-model="addForm.birthday"
            type="date"
            placeholder="选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="用户类型" required>
          <el-select v-model="addForm.userType" placeholder="请选择用户类型">
            <el-option label="普通用户" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelAdd">取消</el-button>
          <el-button type="primary" @click="handleSaveAdd">添加</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用户详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      :title="selectedUser ? `用户详情 - ${selectedUser.displayName}` : '用户详情'"
      direction="rtl"
      size="500px"
    >
      <div v-if="selectedUser" class="user-detail-content">
        <div class="user-detail-header">
          <el-avatar :size="80" :src="selectedUser.avatar" class="detail-avatar">
            <i class="fas fa-user"></i>
          </el-avatar>
          <h3 class="detail-name">{{ selectedUser.displayName }}</h3>
          <p class="detail-role">{{ selectedUser.userTypeDisplayName }}</p>
        </div>
        
        <div class="user-info-list">
          <div class="info-item">
            <label class="info-label">用户ID</label>
            <p class="info-value">{{ selectedUser.id }}</p>
          </div>
          
          <div class="info-item">
            <label class="info-label">用户名</label>
            <p class="info-value">{{ selectedUser.username }}</p>
          </div>
          
          <div class="info-item">
            <label class="info-label">昵称</label>
            <p class="info-value">{{ selectedUser.nickname || '-' }}</p>
          </div>
          
          <div class="info-item">
            <label class="info-label">邮箱</label>
            <p class="info-value">{{ selectedUser.email }}</p>
          </div>
          
          <div class="info-item">
            <label class="info-label">手机号</label>
            <p class="info-value">{{ selectedUser.phone || '-' }}</p>
          </div>
          
          <div class="info-item">
            <label class="info-label">性别</label>
            <p class="info-value">{{ selectedUser.genderDisplayName || '-' }}</p>
          </div>
          
          <div class="info-item">
            <label class="info-label">生日</label>
            <p class="info-value">{{ selectedUser.birthday || '-' }}</p>
          </div>
          
          <div class="info-item">
            <label class="info-label">注册时间</label>
            <p class="info-value">{{ formatDateTime(selectedUser.createdAt) }}</p>
          </div>
          
          <div class="info-item">
            <label class="info-label">更新时间</label>
            <p class="info-value">{{ formatDateTime(selectedUser.updatedAt) }}</p>
          </div>
          
          <div class="info-item">
            <label class="info-label">状态</label>
            <el-tag :type="selectedUser.status === 1 ? 'success' : 'danger'">
              {{ selectedUser.statusDisplayName }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Plus, 
  Download, 
  View, 
  Edit, 
  Delete 
} from '@element-plus/icons-vue'
import { getUserPage, getUserStatistics, updateUserStatus, deleteUser, updateUser, register } from '@/api/user'
import { formatDate, formatDateTime, formatTime } from '@/utils/dateUtils'
import { exportUserData } from '@/utils/excelUtils'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const detailDrawerVisible = ref(false)
const editDialogVisible = ref(false)
const addDialogVisible = ref(false)
const selectedUser = ref(null)
const selectedUsers = ref([])

// 编辑表单数据
const editForm = reactive({
  id: null,
  email: '',
  nickname: '',
  phone: '',
  gender: null,
  birthday: '',
  userType: null,
  status: null
})

// 添加用户表单数据
const addForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  gender: null,
  birthday: '',
  userType: 1,
  status: 1
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  userType: ''
})

// 统计数据
const statsData = reactive({
  totalUsers: 0,
  totalGrowth: 0,
  activeUsers: 0,
  activeGrowth: 0,
  newUsers: 0,
  newGrowth: 0,
  riskUsers: 0
})

// 获取统计数据
const fetchStatistics = async () => {
  try {
    await getUserStatistics({
      onSuccess: (res) => {
        Object.assign(statsData, res)
      }
    })
  } catch (error) {
    ElMessage.error('获取统计数据失败：' + error.message)
  }
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    // 构造查询参数
    const params = {
      userType: searchForm.userType,
      status: searchForm.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    // 如果有关键词，同时在所有字段中搜索
    if (searchForm.keyword && searchForm.keyword.trim()) {
      const keyword = searchForm.keyword.trim()
      // 同时查询用户名、邮箱、昵称、手机号
      params.username = keyword
      params.email = keyword
      params.nickname = keyword
      params.phone = keyword
    }
    
    await getUserPage(params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    ElMessage.error('获取用户列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    userType: ''
  })
  currentPage.value = 1
  fetchUsers()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchUsers()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUsers()
}

// 选择变更
const handleSelectionChange = (val) => {
  selectedUsers.value = val
}

// 查看用户详情
const handleViewUser = (row) => {
  selectedUser.value = row
  detailDrawerVisible.value = true
}

// 编辑用户
const handleEditUser = (row) => {
  // 填充编辑表单
  Object.assign(editForm, {
    id: row.id,
    email: row.email || '',
    nickname: row.nickname || '',
    phone: row.phone || '',
    gender: row.gender,
    birthday: row.birthday || '',
    userType: row.userType,
    status: row.status
  })
  editDialogVisible.value = true
}

// 保存编辑
const handleSaveEdit = async () => {
  try {
    // 构造更新数据（排除id）
    const { id, ...updateData } = editForm
    
    await updateUser(id, updateData, {
      successMsg: '用户信息更新成功',
      onSuccess: () => {
        editDialogVisible.value = false
        // 刷新列表
        fetchUsers()
        fetchStatistics()
      }
    })
  } catch (error) {
    ElMessage.error('更新失败：' + error.message)
  }
}

// 取消编辑
const handleCancelEdit = () => {
  editDialogVisible.value = false
  // 清空表单
  Object.assign(editForm, {
    id: null,
    email: '',
    nickname: '',
    phone: '',
    gender: null,
    birthday: '',
    userType: null,
    status: null
  })
}

// 切换用户状态
const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${row.displayName}" 吗？`,
      `${action}用户`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用后端接口切换状态
    await updateUserStatus(row.id, { status: newStatus }, {
      successMsg: `${action}成功`,
      onSuccess: () => {
        // 刷新列表
        fetchUsers()
        fetchStatistics()
      }
    })
  } catch {
    // 用户取消操作
  }
}

// 删除用户
const handleDeleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.displayName}" 吗？此操作不可撤销。`,
      '删除用户',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    // 调用后端删除接口
    await deleteUser(row.id, {
      onSuccess: () => {
        // 刷新列表
        fetchUsers()
        fetchStatistics()
      }
    })
  } catch {
    // 用户取消操作
  }
}

// 添加用户
const handleAddUser = () => {
  // 清空表单
  Object.assign(addForm, {
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    phone: '',
    gender: null,
    birthday: '',
    userType: 1,
    status: 1
  })
  addDialogVisible.value = true
}

// 保存添加用户
const handleSaveAdd = async () => {
  try {
    // 基本校验
    if (!addForm.username || !addForm.email || !addForm.password) {
      ElMessage.error('用户名、邮箱和密码不能为空')
      return
    }
    
    // 密码确认校验（根据用户偏好，需要trim处理）
    const password = addForm.password.trim()
    const confirmPassword = addForm.confirmPassword.trim()
    if (password !== confirmPassword) {
      ElMessage.error('两次输入的密码不一致')
      return
    }
    
    // 构造注册数据
    const registerData = {
      username: addForm.username,
      email: addForm.email,
      password: password,
      confirmPassword: confirmPassword,
      nickname: addForm.nickname || addForm.username,
      phone: addForm.phone,
      gender: addForm.gender,
      birthday: addForm.birthday,
      userType: addForm.userType
      // 注意：状态由后端默认设置为正常
    }
    
    await register(registerData, {
      successMsg: '用户添加成功',
      onSuccess: () => {
        addDialogVisible.value = false
        // 刷新列表和统计
        fetchUsers()
        fetchStatistics()
      }
    })
  } catch (error) {
    ElMessage.error('添加失败：' + error.message)
  }
}

// 取消添加用户
const handleCancelAdd = () => {
  addDialogVisible.value = false
  // 清空表单
  Object.assign(addForm, {
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    phone: '',
    gender: null,
    birthday: '',
    userType: 1,
    status: 1
  })
}

// 导出数据
const handleExportData = async () => {
  try {
    // 如果当前有数据，直接导出当前数据
    if (tableData.value && tableData.value.length > 0) {
      const result = exportUserData(tableData.value, '用户数据')
      if (result.success) {
        ElMessage.success(`导出成功！文件名：${result.filename}`)
      } else {
        ElMessage.error(result.message)
      }
    } else {
      // 如果当前没有数据，获取所有数据后导出
      ElMessage.info('正在获取数据，请稍候...')
      
      // 获取所有数据（不分页）
      const params = {
        userType: searchForm.userType,
        status: searchForm.status,
        currentPage: 1,
        size: 10000 // 设置一个较大的数值获取所有数据
      }
      
      // 如果有搜索关键词，同时在所有字段中搜索
      if (searchForm.keyword && searchForm.keyword.trim()) {
        const keyword = searchForm.keyword.trim()
        params.username = keyword
        params.email = keyword
        params.nickname = keyword
        params.phone = keyword
      }
      
      await getUserPage(params, {
        onSuccess: (res) => {
          const allUsers = res.records || []
          if (allUsers.length > 0) {
            const result = exportUserData(allUsers, '用户数据')
            if (result.success) {
              ElMessage.success(`导出成功！共导出 ${allUsers.length} 条记录，文件名：${result.filename}`)
            } else {
              ElMessage.error(result.message)
            }
          } else {
            ElMessage.warning('没有数据可导出')
          }
        },
        onError: (error) => {
          ElMessage.error('获取数据失败：' + error.message)
        }
      })
    }
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// 组件挂载
onMounted(() => {
  fetchUsers()
  fetchStatistics()
})
</script>

<style lang="scss" scoped>
/* 主容器样式 */
.user-management {
  padding: 2rem;
  background: #fefefe;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 页面头部样式 */
.page-header {
  background: #fff;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
  }
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 2rem;
  font-weight: 300;
  color: #2d3748;
  margin: 0;
  letter-spacing: -0.02em;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
  }
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 8px 0;
  font-weight: 400;
  line-height: 1.4;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 8px 0;
  line-height: 1.2;
  
  &.stat-value-blue {
    color: #4A90E2;
  }
  
  &.stat-value-green {
    color: #7ED321;
  }
  
  &.stat-value-orange {
    color: #F5A623;
  }
  
  &.stat-value-red {
    color: #e53e3e;
  }
}

.stat-growth {
  font-size: 14px;
  margin: 0;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  
  &.stat-growth-positive {
    color: #7ED321;
  }
  
  &.stat-growth-warning {
    color: #F5A623;
  }
  
  i {
    font-size: 12px;
  }
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  i {
    font-size: 24px;
  }
  
  &.stat-icon-blue {
    background-color: rgba(74, 144, 226, 0.1);
    color: #4A90E2;
  }
  
  &.stat-icon-green {
    background-color: rgba(126, 211, 33, 0.1);
    color: #7ED321;
  }
  
  &.stat-icon-orange {
    background-color: rgba(245, 166, 35, 0.1);
    color: #F5A623;
  }
  
  &.stat-icon-red {
    background-color: rgba(229, 62, 62, 0.1);
    color: #e53e3e;
  }
}

/* 搜索区域样式 */
.search-section {
  background: #fff;
  border-radius: 12px;
  padding: 1.5rem 2rem;
  margin-bottom: 2rem;
}

.search-form {
  :deep(.el-form-item) {
    margin-bottom: 1rem;
    margin-right: 2rem;
    
    &:last-child {
      margin-right: 0;
    }
  }
  
  :deep(.el-form-item__label) {
    color: #4a5568;
    font-weight: 500;
    font-size: 0.875rem;
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    background: #f8fafc;
  }
  
  :deep(.el-input__wrapper:focus-within) {
    border-color: #667eea;
    background: #fff;
  }
  
  :deep(.el-select .el-input__wrapper) {
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    background: #f8fafc;
  }
  
  :deep(.el-select .el-input__wrapper:focus-within) {
    border-color: #667eea;
    background: #fff;
  }
  
  :deep(.el-button) {
    border-radius: 8px;
    font-weight: 500;
    padding: 8px 16px;
  }
}

.search-input {
  width: 280px;
}

.search-select {
  width: 140px;
}

/* 表格区域样式 */
.table-section {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
  }
}

.table-header {
  background: #f8fafc;
  padding: 2rem;
}

.table-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.table-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  line-height: 1.4;
}

.table-header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.table-total {
  font-size: 14px;
  color: #6b7280;
  font-weight: 400;
}

/* 表格内容样式 */
.user-info {
  display: flex;
  align-items: center;
}

/* 用户信息列样式优化 */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .user-avatar {
    flex-shrink: 0;
    border: 2px solid #e1e8f0;
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #4A90E2;
      transform: scale(1.05);
    }
  }
  
  .user-details {
    flex: 1;
    min-width: 0;
    
    .user-name {
      font-weight: 500;
      color: #1f2937;
      margin-bottom: 2px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .user-id {
      font-size: 12px;
      color: #9ca3af;
      margin-bottom: 2px;
    }
    
    .user-username {
      font-size: 12px;
      color: #6b7280;
      font-style: italic;
    }
  }
}

/* 联系方式列样式优化 */
.contact-info {
  .contact-item {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 4px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .contact-icon {
      width: 12px;
      font-size: 11px;
      color: #6b7280;
      flex-shrink: 0;
    }
    
    .contact-text {
      font-size: 13px;
      color: #374151;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      
      &.contact-empty {
        color: #9ca3af;
        font-style: italic;
      }
    }
  }
}

/* 个人信息列样式 */
.personal-info {
  .info-row {
    margin-bottom: 6px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .gender-tag {
    .fas {
      margin-right: 4px;
      font-size: 11px;
    }
  }
  
  .birthday-text {
    font-size: 12px;
    color: #6b7280;
    display: flex;
    align-items: center;
    gap: 4px;
    
    .birthday-icon {
      font-size: 10px;
      color: #f59e0b;
    }
  }
}

/* 标签样式优化 */
.type-tag,
.status-tag {
  .type-icon,
  .status-icon {
    margin-right: 4px;
    font-size: 11px;
  }
}

.type-tag {
  .fa-crown {
    color: #fbbf24;
  }
}

/* 日期信息列样式优化 */
.date-info {
  .date-main {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #1f2937;
    line-height: 1.4;
    margin-bottom: 2px;
    
    .date-icon {
      font-size: 11px;
      color: #6b7280;
    }
  }
  
  .date-sub {
    font-size: 12px;
    color: #6b7280;
    line-height: 1.4;
    padding-left: 15px;
  }
}

.user-avatar {
  margin-right: 12px;
  border: 2px solid #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-details {
  flex: 1;
}


.date-info {
  .date-main {
    font-size: 14px;
    color: #1f2937;
    line-height: 1.4;
    margin-bottom: 2px;
  }
  
  .date-sub {
    font-size: 12px;
    color: #6b7280;
    line-height: 1.4;
  }
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

/* 分页样式 */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 24px;
  background: #fafbfc;
  border-top: 1px solid #e5e7eb;
}

/* 用户详情抽屉样式 */
.user-detail-content {
  padding: 16px;
}

.user-detail-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.detail-avatar {
  margin-bottom: 12px;
  border: 3px solid #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.detail-name {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
  line-height: 1.4;
}

.detail-role {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.4;
}

.user-info-list {
  .info-item {
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .info-label {
    display: block;
    font-size: 12px;
    font-weight: 500;
    color: #6b7280;
    margin-bottom: 4px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
  
  .info-value {
    margin: 0;
    font-size: 14px;
    color: #1f2937;
    line-height: 1.4;
  }
}

/* Element Plus 组件自定义样式 */
:deep(.el-button) {
  border-radius: 12px;
  font-weight: 400;
  letter-spacing: 0.02em;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-1px);
  }
  
  &.el-button--primary {
    background: #667eea;
    border-color: #667eea;
    
    &:hover {
      background: #5a6fd8;
      border-color: #5a6fd8;
    }
  }
  
  &.el-button--success {
    background: #38a169;
    border-color: #38a169;
    
    &:hover {
      background: #2f855a;
      border-color: #2f855a;
    }
  }
}

:deep(.el-table) {
  .el-table__header-wrapper {
    background: #f8fafc;
  }
  
  .el-table__body-wrapper {
    .el-table__row:hover {
      background-color: rgba(74, 144, 226, 0.05) !important;
    }
  }
}

:deep(.el-input) {
  .el-input__wrapper {
    border-radius: 12px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-1px);
    }
  }
}

:deep(.el-select) {
  .el-select__wrapper {
    border-radius: 12px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-1px);
    }
  }
}

/* 编辑表单样式 */
.edit-form,
.add-form {
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .el-input,
  .el-select,
  .el-date-picker {
    width: 100%;
  }
}

.dialog-footer {
  text-align: right;
  
  .el-button {
    margin-left: 8px;
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .user-management {
    padding: 16px;
  }
  
  .page-header {
    padding: 16px;
    margin-bottom: 24px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    margin-bottom: 24px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .search-section {
    padding: 16px;
    margin-bottom: 24px;
  }
  
  .search-form {
    :deep(.el-form-item) {
      display: block;
      width: 100%;
      margin-bottom: 16px;
      margin-right: 0;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .el-input,
      .el-select {
        width: 100% !important;
      }
    }
  }
  
  .table-section {
    :deep(.el-table) {
      font-size: 12px;
    }
  }
  
  .table-header {
    padding: 16px;
  }
  
  .pagination-section {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 24px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    
    i {
      font-size: 20px;
    }
  }
  
  .header-actions {
    flex-direction: column;
    width: 100%;
    
    .el-button {
      width: 100%;
      justify-content: center;
    }
  }
  
  .action-buttons {
    flex-wrap: wrap;
    gap: 4px;
    
    .el-button {
      padding: 4px 6px;
      font-size: 12px;
    }
  }
}
</style>