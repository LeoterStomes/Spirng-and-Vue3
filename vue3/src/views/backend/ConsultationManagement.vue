<template>
  <div class="consultation-management">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h2 class="page-title">咨询记录管理</h2>
        </div>
        <div class="header-actions">
          <!-- 可以在这里添加操作按钮 -->
        </div>
      </div>
    </div>


    <!-- 筛选和搜索 -->
    <div class="search-area">
      <el-form 
        :model="queryForm" 
        inline 
        label-width="80px"
        @submit.prevent="fetchSessions"
      >
        <el-form-item label="情绪标签">
          <el-select v-model="queryForm.emotionTag" placeholder="选择情绪" clearable>
            <el-option label="全部情绪" value=""></el-option>
            <el-option label="焦虑" value="焦虑"></el-option>
            <el-option label="抑郁" value="抑郁"></el-option>
            <el-option label="压力" value="压力"></el-option>
            <el-option label="愤怒" value="愤怒"></el-option>
            <el-option label="孤独" value="孤独"></el-option>
            <el-option label="迷茫" value="迷茫"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateRangeChange"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="搜索用户或内容"
            @clear="fetchSessions"
            @keyup.enter="fetchSessions"
            clearable
          >
            <template #prefix>
              <i class="fas fa-search"></i>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="fetchSessions">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 会话记录列表 -->
    <el-card class="table-card">
      <el-table 
        :data="sessionList" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="userNickname" label="用户信息" width="150">
          <template #default="scope">
            <div class="user-info">
              <el-avatar :src="scope.row.userAvatar" :size="32">
                <i class="fas fa-user"></i>
              </el-avatar>
              <div class="user-details">
                <div class="user-name">{{ scope.row.userNickname || '未知用户' }}</div>
                <div class="user-id">ID: {{ scope.row.userId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="sessionTitle" label="会话内容" min-width="200">
          <template #default="scope">
            <div class="session-content">
              <div class="session-title">{{ scope.row.sessionTitle || '未命名会话' }}</div>
              <div class="session-preview">{{ scope.row.lastMessageContent || '暂无消息' }}</div>
              <div class="session-meta">
                消息数：{{ scope.row.messageCount || 0 }} | 时长：{{ scope.row.durationMinutes || 0 }}分钟
              </div>
            </div>
          </template>
        </el-table-column>


        <el-table-column prop="messageCount" label="消息数" width="100">
          <template #default="scope">
            <span class="message-count">{{ scope.row.messageCount || 0 }}条</span>
          </template>
        </el-table-column>

        <el-table-column prop="startedAt" label="时间" width="180">
          <template #default="scope">
            <div class="time-info">
              <div>{{ formatDateTime(scope.row.startedAt) }}</div>
              <div class="duration-time">
                时长：{{ scope.row.durationMinutes || 0 }}分钟
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="80" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                text
                @click="viewSessionDetail(scope.row)"
                title="查看详情"
              >
                <i class="fas fa-eye"></i>
                详情
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="queryForm.currentPage"
          :page-size="queryForm.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 会话详情对话框 - 简化版 -->
    <el-dialog 
      v-model="showDetailDialog" 
      title="咨询会话详情" 
      width="70%"
      :close-on-click-modal="false"
    >
      <div v-if="selectedSession" class="session-detail">
        <!-- 会话基本信息 -->
        <div class="detail-header">
          <div class="detail-row">
            <span class="detail-label">用户：</span>
            <span class="detail-value">{{ selectedSession.userNickname || '未知用户' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">开始时间：</span>
            <span class="detail-value">{{ formatDateTime(selectedSession.startedAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">消息数：</span>
            <span class="detail-value">{{ selectedSession.messageCount || 0 }}条</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">时长：</span>
            <span class="detail-value">{{ selectedSession.durationMinutes || 0 }}分钟</span>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages-container">
          <div class="messages-header">
            <h4>对话记录</h4>
          </div>
          <div class="messages-list" v-loading="loadingMessages">
            <div
              v-for="message in sessionMessages"
              :key="message.id"
              class="message-item"
              :class="message.senderType === 1 ? 'user-message' : 'ai-message'"
            >
              <div class="message-header">
                <span class="sender">
                  <i :class="message.senderType === 1 ? 'fas fa-user' : 'fas fa-robot'"></i>
                  {{ message.senderType === 1 ? '用户' : 'AI助手' }}
                </span>
                <span class="time">{{ formatTime(message.createdAt) }}</span>
              </div>
              <div class="message-content">{{ message.content }}</div>
            </div>
            <div v-if="!sessionMessages.length && !loadingMessages" class="no-messages">
              暂无对话记录
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSessionsPage, getSessionDetail, getSessionMessages } from '@/api/consultationSession'

// 响应式数据
const loading = ref(false)
const loadingMessages = ref(false)
const sessionList = ref([])
const total = ref(0)
const dateRange = ref([])
const showDetailDialog = ref(false)
const selectedSession = ref(null)
const sessionMessages = ref([])

// 查询表单
const queryForm = reactive({
  currentPage: 1,
  size: 20,
  emotionTag: '',
  startDate: '',
  endDate: '',
  keyword: ''
})

// 生命周期
onMounted(() => {
  fetchSessions()
})

// 方法
const fetchSessions = async () => {
  loading.value = true
  try {
    await getSessionsPage(queryForm, {
      onSuccess: (data) => {
        sessionList.value = data.records || []
        total.value = data.total || 0
      },
      onError: (error) => {
        ElMessage.error('获取咨询记录失败: ' + error.message)
      }
    })
  } catch (error) {
    console.error('获取咨询记录失败:', error)
    ElMessage.error('获取咨询记录失败')
  } finally {
    loading.value = false
  }
}


const handleDateRangeChange = (dates) => {
  if (dates && dates.length === 2) {
    queryForm.startDate = dates[0]
    queryForm.endDate = dates[1]
  } else {
    queryForm.startDate = ''
    queryForm.endDate = ''
  }
}

const handleReset = () => {
  Object.assign(queryForm, {
    currentPage: 1,
    size: 20,
    emotionTag: '',
    startDate: '',
    endDate: '',
    keyword: ''
  })
  dateRange.value = []
  fetchSessions()
}

const handleSizeChange = (newSize) => {
  queryForm.size = newSize
  fetchSessions()
}

const handleCurrentChange = (newPage) => {
  queryForm.currentPage = newPage
  fetchSessions()
}

const viewSessionDetail = async (session) => {
  selectedSession.value = session
  showDetailDialog.value = true
  
  // 加载会话消息
  loadingMessages.value = true
  try {
    await getSessionMessages(session.id, {
      onSuccess: (messages) => {
        sessionMessages.value = messages || []
      },
      onError: (error) => {
        ElMessage.error('获取会话消息失败: ' + error.message)
      }
    })
  } catch (error) {
    console.error('获取会话消息失败:', error)
    ElMessage.error('获取会话消息失败')
  } finally {
    loadingMessages.value = false
  }
}




const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.consultation-management {
  padding: 2rem;
  background: #fefefe;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.page-header {
  background: #fff;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  transition: all 0.3s ease;
}

.page-header:hover {
  transform: translateY(-2px);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  gap: 1rem;
}

/* 统计卡片样式 */
.stats-grid {
  display: flex;
  justify-content: flex-start;
  gap: 2rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 2rem;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  color: white;
}

.stat-primary { background: #4A90E2; }
.stat-success { background: #52c41a; }
.stat-warning { background: #faad14; }
.stat-info { background: #9254de; }

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin: 0 0 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* 搜索区域 */
.search-area {
  background: #fff;
  padding: 1.5rem 2rem;
  border-radius: 12px;
  margin-bottom: 2rem;
}

.search-area :deep(.el-form) {
  margin: 0;
}

.search-area :deep(.el-form-item) {
  margin-bottom: 1rem;
  margin-right: 2rem;
}

.search-area :deep(.el-form-item:last-child) {
  margin-right: 0;
}

.search-area :deep(.el-form-item__label) {
  color: #4a5568;
  font-weight: 500;
  font-size: 0.875rem;
  margin-bottom: 0;
}

.search-area :deep(.el-input) {
  width: 180px;
}

.search-area :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.search-area :deep(.el-input__wrapper:focus-within) {
  border-color: #667eea;
  background: #fff;
}

.search-area :deep(.el-select) {
  width: 140px;
}

.search-area :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.search-area :deep(.el-select .el-input__wrapper:focus-within) {
  border-color: #667eea;
  background: #fff;
}

.search-area :deep(.el-date-editor) {
  width: 240px;
}

.search-area :deep(.el-date-editor .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.search-area :deep(.el-date-editor .el-input__wrapper:focus-within) {
  border-color: #667eea;
  background: #fff;
}

.search-area :deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
  padding: 8px 16px;
}

.search-area :deep(.el-button--primary) {
  background: #667eea;
  border-color: #667eea;
}

.search-area :deep(.el-button--primary:hover) {
  background: #5a6fd8;
  border-color: #5a6fd8;
}

/* 表格样式 */
.table-card {
  margin-bottom: 2rem;
  border-radius: 16px;
  transition: all 0.3s ease;
}

.table-card:hover {
  transform: translateY(-2px);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.user-id {
  font-size: 12px;
  color: #999;
}

.session-content {
  line-height: 1.5;
}

.session-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.session-preview {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.session-meta {
  font-size: 12px;
  color: #999;
}


.message-count {
  font-weight: 500;
  color: #333;
}

.time-info {
  font-size: 13px;
  line-height: 1.4;
}

.duration-time {
  color: #999;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 会话详情对话框样式 - 简化版 */
.session-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-header {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-weight: 500;
  color: #495057;
  min-width: 80px;
  margin-right: 8px;
}

.detail-value {
  color: #333;
}

.messages-container {
  margin-top: 20px;
}

.messages-header {
  margin-bottom: 16px;
}

.messages-header h4 {
  margin: 0;
  color: #333;
  font-size: 16px;
  font-weight: 500;
}

.messages-list {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.no-messages {
  text-align: center;
  color: #999;
  padding: 40px 20px;
  font-size: 14px;
}

.message-item {
  margin-bottom: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
}

.message-item:last-child {
  margin-bottom: 0;
}

.user-message {
  background: #e8f4fd;
}

.ai-message {
  background: #f0f9f0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.sender {
  font-weight: 500;
  color: #333;
  display: flex;
  align-items: center;
  gap: 4px;
}

.time {
  font-size: 12px;
  color: #999;
}

.message-content {
  color: #333;
  line-height: 1.6;
  white-space: pre-wrap;
  margin-top: 8px;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .consultation-management {
    padding: 10px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-left {
    justify-content: center;
  }
  
  .filter-right {
    justify-content: center;
  }
}
</style>
